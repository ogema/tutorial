package com.example.eval.presence;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.tools.timeseries.iterator.api.SampledValueDataPoint;

import de.iwes.timeseries.eval.api.EvaluationInput;
import de.iwes.timeseries.eval.api.EvaluationInstance.EvaluationListener;
import de.iwes.timeseries.eval.api.EvaluationProvider;
import de.iwes.timeseries.eval.api.ResultType;
import de.iwes.timeseries.eval.api.SingleEvaluationResult;
import de.iwes.timeseries.eval.api.TimeSeriesData;
import de.iwes.timeseries.eval.api.configuration.ConfigurationInstance;
import de.iwes.timeseries.eval.base.provider.utils.EvaluationInputImpl;
import de.iwes.timeseries.eval.base.provider.utils.SingleValueResultImpl;
import de.iwes.timeseries.eval.garo.api.base.GaRoDataType;
import de.iwes.timeseries.eval.garo.multibase.generic.GenericGaRoEvaluationCore;
import de.iwes.timeseries.eval.garo.multibase.generic.GenericGaRoResultType;
import de.iwes.timeseries.eval.garo.multibase.generic.GenericGaRoSingleEvalProviderPreEval;
import de.iwes.timeseries.eval.online.utils.BaseOnlineEstimator;
import de.iwes.timeseries.eval.online.utils.BaseOnlineEstimator.AverageMode;
import de.iwes.timeseries.eval.online.utils.InputSeriesAggregator;
import de.iwes.timeseries.eval.online.utils.InputSeriesAggregator.AggregationMode;
import de.iwes.timeseries.eval.online.utils.InputSeriesAggregator.ValueDuration;

/**
 * Example GaRo EvaluationProvider
 */
@Service(EvaluationProvider.class)
@Component
public class ExampleDependentTemperatureEvalProvider extends GenericGaRoSingleEvalProviderPreEval {
	
	/** Adapt these values to your provider*/
    public final static String ID = "temperature_whenPresent_eval_provider";
    public final static String LABEL = "Temperature when present evaluation provider";
    public final static String DESCRIPTION = "Provides average room temperature when cleaned presence is detected";
    
    public ExampleDependentTemperatureEvalProvider() {
        super(ID, LABEL, DESCRIPTION);
    }

	@Override
	/** Provide your data types here*/
	public GaRoDataType[] getGaRoInputTypes() {
		return new GaRoDataType[] {
	        	GaRoDataType.PreEvaluated, //clean-up motion
	        	GaRoDataType.TemperatureMeasurementRoomSensor
		};
	}
	/** It is recommended to define the indices of your input here.*/
	public static final int MOTION_IDX = 0; 
	public static final int TEMP_IDX = 1; 
        
 	public class EvalCore extends GenericGaRoEvaluationCore {
    	final long totalTime;
    	
    	/** Application specific state variables, see also documentation of the util classes used*/
     	public final InputSeriesAggregator temperature;
    	public final BaseOnlineEstimator avTemperatureWhenPresence = new BaseOnlineEstimator(false, AverageMode.AVERAGE_ONLY);
 
    	//Before we get the first motion signal we assume no presence
    	private boolean isPresent = false;
    	private float lastTemperature = Float.NaN;
    	
    	//Pre-evaluation values
    	Float preVal;
    	
    	public EvalCore(List<EvaluationInput> input, List<ResultType> requestedResults,
    			Collection<ConfigurationInstance> configurations, EvaluationListener listener, long time,
    			int size, int[] nrInput, int[] idxSumOfPrevious, long[] startEnd) {
    		//example how to calculate total time assuming offline evaluation
    		totalTime = startEnd[1] - startEnd[0];
  
    		/**If there are several temperature sensors we always use the average of the current values*/
       		temperature = new InputSeriesAggregator(nrInput, idxSumOfPrevious,
    				TEMP_IDX, startEnd[1], null, AggregationMode.AVERAGING);

			preVal = getPreEvalRoomValue(PRESENCE_PROVIDER_ID, ExamplePresenceEvalProvider.AVERAGE_TEMPERATURE_PRESENCE_FIRSTGUESS.id());

    	}
    	
    	/** In processValue the core data processing takes place. This method is called for each input
    	 * value of any input time series.*/
    	@Override
    	protected void processValue(int idxOfRequestedInput, int idxOfEvaluationInput,
    			int totalInputIdx, long timeStamp,
    			SampledValue sv, SampledValueDataPoint dataPoint, long duration) {
     		
    		switch(idxOfRequestedInput) {
    		case MOTION_IDX:// temperature sensor value
   				//The Pre-evaluated time series already aggregates all motion sensors, so we do not have to
    			//use an aggregation util here
    			isPresent = sv.getValue().getFloatValue() > 0.5f;
   				break;
    		case TEMP_IDX:
    			final ValueDuration tempVal = temperature.getCurrentValueDuration(idxOfEvaluationInput, sv, dataPoint, true);
    			lastTemperature = tempVal.value;
    		}
     		if(isPresent && (!Float.isNaN(lastTemperature))) {
    			avTemperatureWhenPresence.addValue(lastTemperature, duration);
    		}
    	}
     }
    
 	/**
 	 * Define the results of the evaluation here including the final calculation
 	 */
    public final static GenericGaRoResultType AVERAGE_TEMPERATURE_PRESENCE = new GenericGaRoResultType("Average_with_presence_Temperature",
    		"Avergage temperature when presence was detected") {
				@Override
				public SingleEvaluationResult getEvalResult(GenericGaRoEvaluationCore ec, ResultType rt,
						List<TimeSeriesData> inputData) {
					EvalCore cec = ((EvalCore)ec);
					return new SingleValueResultImpl<Float>(rt, cec.avTemperatureWhenPresence.getAverage(), inputData);
				}
    };
    public final static GenericGaRoResultType DIFF_FIRSTGUESS = new GenericGaRoResultType("Difference_to_first_guess_Temperature",
    		"Difference to first guess of temperature with presence") {
				@Override
				public SingleEvaluationResult getEvalResult(GenericGaRoEvaluationCore ec, ResultType rt,
						List<TimeSeriesData> inputData) {
					EvalCore cec = ((EvalCore)ec);
					float myVal = cec.avTemperatureWhenPresence.getAverage();
					if(cec.preVal != null)
						return new SingleValueResultImpl<Float>(rt, myVal - cec.preVal, inputData);
					else
						return new SingleValueResultImpl<Float>(rt, Float.NaN, inputData);
				}
    };
    private static final List<GenericGaRoResultType> RESULTS = Arrays.asList(AVERAGE_TEMPERATURE_PRESENCE,
    		DIFF_FIRSTGUESS);
    
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

	public static final String PRESENCE_PROVIDER_ID = ExamplePresenceEvalProvider.class.getSimpleName();
	public static final PreEvaluationRequested CLEAN_PRESENCE_PROVIDER = new PreEvaluationRequested(
			PRESENCE_PROVIDER_ID);

	@Override
	public List<PreEvaluationRequested> preEvaluationsRequested() {
		return Arrays.asList(CLEAN_PRESENCE_PROVIDER);
	}
	
	@Override
	public List<EvaluationInputImpl> timeSeriesToInject() {
		return getRoomTimeSeriesInput(PRESENCE_PROVIDER_ID,
				new String[] {ExamplePresenceEvalProvider.CLEANED_PRESENCE_TS.id()});
	}
}
