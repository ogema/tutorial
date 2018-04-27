package com.example.eval.presence;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.tools.timeseries.iterator.api.SampledValueDataPoint;

import de.iwes.timeseries.eval.api.EvaluationInput;
import de.iwes.timeseries.eval.api.EvaluationInstance.EvaluationListener;
import de.iwes.timeseries.eval.api.EvaluationProvider;
import de.iwes.timeseries.eval.api.ResultType;
import de.iwes.timeseries.eval.api.SingleEvaluationResult;
import de.iwes.timeseries.eval.api.TimeSeriesData;
import de.iwes.timeseries.eval.api.configuration.ConfigurationInstance;
import de.iwes.timeseries.eval.base.provider.utils.SingleValueResultImpl;
import de.iwes.timeseries.eval.base.provider.utils.TimeSeriesResultImpl;
import de.iwes.timeseries.eval.garo.api.base.GaRoDataType;
import de.iwes.timeseries.eval.garo.multibase.generic.GenericGaRoEvaluationCore;
import de.iwes.timeseries.eval.garo.multibase.generic.GenericGaRoResultType;
import de.iwes.timeseries.eval.garo.multibase.generic.GenericGaRoSingleEvalProvider;
import de.iwes.timeseries.eval.online.utils.BaseOnlineEstimator;
import de.iwes.timeseries.eval.online.utils.BaseOnlineEstimator.AverageMode;
import de.iwes.timeseries.eval.online.utils.InputSeriesAggregator;
import de.iwes.timeseries.eval.online.utils.InputSeriesAggregator.AggregationMode;
import de.iwes.timeseries.eval.online.utils.InputSeriesAggregator.ValueDuration;
import de.iwes.timeseries.eval.online.utils.TimeSeriesOnlineBuilder;

/**
 * Example GaRo EvaluationProvider
 */
@Service(EvaluationProvider.class)
@Component
public class ExamplePresenceEvalProvider extends GenericGaRoSingleEvalProvider {
	
	/** Adapt these values to your provider*/
    public final static String ID = "presence_cleaner_eval_provider";
    public final static String LABEL = "Presence signal cleaner evaluation provider";
    public final static String DESCRIPTION = "Provides cleaned up presence signal and more";
    
    public ExamplePresenceEvalProvider() {
        super(ID, LABEL, DESCRIPTION);
    }

	@Override
	/** Provide your data types here*/
	public GaRoDataType[] getGaRoInputTypes() {
		return new GaRoDataType[] {
	        	GaRoDataType.MotionDetection,
	        	GaRoDataType.TemperatureMeasurementRoomSensor
		};
	}
	/** It is recommended to define the indices of your input here.*/
	public static final int MOTION_IDX = 0; 
	public static final int TEMP_IDX = 1; 
        
 	public class EvalCore extends GenericGaRoEvaluationCore {
    	final long totalTime;
    	
    	/** Application specific state variables, see also documentation of the util classes used*/
    	public final InputSeriesAggregator motion;
    	public final InputSeriesAggregator temperature;
    	public final BaseOnlineEstimator avTemperatureWhenPresence = new BaseOnlineEstimator(false, AverageMode.AVERAGE_ONLY);
    	public final TimeSeriesOnlineBuilder tsBuilder;

    	//Before we get the first motion signal we assume no presence
    	private boolean isPresent = false;
    	private float lastTemperature = Float.NaN;
    	private long presenceStarted;
    	private long memorizePotentialAbsence;
    	private static final long MINIMUM_PRESENCE_DURATION = 600000;
    	
    	public EvalCore(List<EvaluationInput> input, List<ResultType> requestedResults,
    			Collection<ConfigurationInstance> configurations, EvaluationListener listener, long time,
    			int size, int[] nrInput, int[] idxSumOfPrevious, long[] startEnd) {
    		//example how to calculate total time assuming offline evaluation
    		totalTime = startEnd[1] - startEnd[0];
    		/**The InputSeriesAggregator aggregates the input from all motion sensors in the room.
    		 *	AggregationMode.MAX makes sure that the aggregated value will be one if any motion
    		 *  sensor in the room has signal one.
    		*/
    		motion = new InputSeriesAggregator(nrInput, idxSumOfPrevious,
    				MOTION_IDX, startEnd[1], null, AggregationMode.MAX);
    		/**If there are several temperature sensors we always use the average of the current values*/
       		temperature = new InputSeriesAggregator(nrInput, idxSumOfPrevious,
    				TEMP_IDX, startEnd[1], null, AggregationMode.AVERAGING);
    		tsBuilder = new TimeSeriesOnlineBuilder();
    	}
    	
    	/** In processValue the core data processing takes place. This method is called for each input
    	 * value of any input time series.*/
    	@Override
    	protected void processValue(int idxOfRequestedInput, int idxOfEvaluationInput,
    			int totalInputIdx, long timeStamp,
    			SampledValue sv, SampledValueDataPoint dataPoint, long duration) {
    		if(memorizePotentialAbsence > 0 && memorizePotentialAbsence < timeStamp) {
    			//In the mean time the datapoint is in the past, so we have to indicate absence
   				tsBuilder.addValue(new SampledValue(new FloatValue(0), timeStamp, Quality.GOOD));
   				isPresent = false;
    		}
    		
    		switch(idxOfRequestedInput) {
    		case MOTION_IDX:// temperature sensor value
    			final ValueDuration val = motion.getCurrentValueDuration(idxOfEvaluationInput, sv, dataPoint, true);
   				boolean newPresence = (val.value > 0.5f);
   				if(isPresent && (!newPresence)) {
   					if(timeStamp - presenceStarted < MINIMUM_PRESENCE_DURATION) {
   						memorizePotentialAbsence = presenceStarted + MINIMUM_PRESENCE_DURATION;
   						break; //we do not put this data point in the cleaned time series for now
   					}
   				} else if(memorizePotentialAbsence > 0 && newPresence) {
   					memorizePotentialAbsence = -1;
    			} else if(!isPresent && newPresence) {
   					presenceStarted = timeStamp;
   					memorizePotentialAbsence = -1;
   				}
   				tsBuilder.addValue(sv);
   				isPresent = newPresence;
   				//evalInstance.callListeners(CLEANED_PRESENCE_TS, timeStamp, val.value);
   				break;
    		case TEMP_IDX:
    			final ValueDuration tempVal = temperature.getCurrentValueDuration(idxOfEvaluationInput, sv, dataPoint, true);
    			lastTemperature = tempVal.value;
    		}
    		/** Note that the presence correction is not fully correct applied to the calculation of temperature when
    		 * present. This can more easily be done in another evaluation using this one as Pre-Evaluation.<br>
    		 * Note that we process this on every new value as the sum of all duration values should be exactly the
    		 * duration of the evaluation (except for gap times).
    		 */
    		if(isPresent && (!Float.isNaN(lastTemperature))) {
    			avTemperatureWhenPresence.addValue(lastTemperature, duration);
    		}
    	}
    	
    	// Gap notification usually is only required when a result time series is created, otherwise gap handling is
    	// done by the framework
    	@Override
    	protected void gapNotification(int idxOfRequestedInput, int idxOfEvaluationInput, int totalInputIdx, long timeStamp,
    			SampledValue sv, SampledValueDataPoint dataPoint, long duration) {
    		tsBuilder.addValue(new SampledValue(new FloatValue(Float.NaN), sv.getTimestamp(), Quality.BAD));
    	}
    }
    
 	/**
 	 * Define the results of the evaluation here including the final calculation
 	 */
    public final static GenericGaRoResultType CLEANED_PRESENCE_TS = new GenericGaRoResultType("Cleaned_Presence_TS") {
				@Override
				public SingleEvaluationResult getEvalResult(GenericGaRoEvaluationCore ec, ResultType rt,
						List<TimeSeriesData> inputData) {
					EvalCore cec = ((EvalCore)ec);
					return new TimeSeriesResultImpl(rt, cec.tsBuilder.getTimeSeries(), inputData);
				}
    };
    public final static GenericGaRoResultType AVERAGE_TEMPERATURE_PRESENCE_FIRSTGUESS = new GenericGaRoResultType("Avergage temperature when presence was detected (first guess)") {
				@Override
				public SingleEvaluationResult getEvalResult(GenericGaRoEvaluationCore ec, ResultType rt,
						List<TimeSeriesData> inputData) {
					EvalCore cec = ((EvalCore)ec);
					return new SingleValueResultImpl<Float>(rt, cec.avTemperatureWhenPresence.getAverage(), inputData);
				}
    };
    private static final List<GenericGaRoResultType> RESULTS = Arrays.asList(CLEANED_PRESENCE_TS,
    		AVERAGE_TEMPERATURE_PRESENCE_FIRSTGUESS);
    
	@Override
	protected List<GenericGaRoResultType> resultTypesGaRo() {
		return RESULTS;
	}

	@Override
	protected GenericGaRoEvaluationCore initEval(List<EvaluationInput> input, List<ResultType> requestedResults,
			Collection<ConfigurationInstance> configurations, EvaluationListener listener, long time, int size,
			int[] nrInput, int[] idxSumOfPrevious, long[] startEnd) {
		return new EvalCore(input, requestedResults, configurations, listener, time, size, nrInput, idxSumOfPrevious, startEnd);
	}
}
