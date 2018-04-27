package com.example.snippet.ogema.evaluation;

import java.util.Collection;
import java.util.List;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.serialization.jaxb.Resource;
import org.ogema.tools.resource.util.ValueResourceUtils;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;

import de.iwes.timeseries.eval.api.ResultType;
import de.iwes.timeseries.eval.api.SingleEvaluationResult;
import de.iwes.timeseries.eval.api.SingleEvaluationResult.SingleValueResult;
import de.iwes.timeseries.eval.api.SingleEvaluationResult.TimeSeriesResult;
import de.iwes.timeseries.eval.api.configuration.ConfigurationInstance;
import de.iwes.timeseries.eval.api.extended.MultiEvaluationInputGeneric;
import de.iwes.timeseries.eval.api.helper.EfficientTimeSeriesArray;
import de.iwes.timeseries.eval.garo.api.base.GaRoSuperEvalResult;
import de.iwes.timeseries.eval.garo.multibase.GaRoMultiResultExtended;

/** 
 * This is an example how to provide overall evaluation results covering multiple gateways.<br>
 * This example chooses the time series with the smallest gap time from all gateway input and returns
 * this time series as result (as a simple example)
 */
public class ExampleOverallGaRoMultiResult extends GaRoMultiResultExtended {
	/**KPI values are just provided as public fields or public getter methods*/
	public int gwWithDataCount = 0;
	
	/** Example for provision of a time series*/
	public float generalOutsideTemperatureAv;
	FloatTimeSeries generalOutsideTemperature;
	public EfficientTimeSeriesArray getGeneralOutsideTemperatureValues() {
		return EfficientTimeSeriesArray.getInstance(generalOutsideTemperature);
	}
	public void setGeneralOutsideTemperatureValues(EfficientTimeSeriesArray value) {
		generalOutsideTemperature = value.toFloatTimeSeries(); //EfficientTimeSeriesArray.setValue(value);
	}
	public FloatTimeSeries nonForJsonGeneralOutsideTemperature() {
		return generalOutsideTemperature;
	}
	
	private final ResultType singleRoomTimeSeries;
	private final ResultType singleRoomGapTimeResult;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ExampleOverallGaRoMultiResult(List<MultiEvaluationInputGeneric<Resource>> inputData, long start, long end,
			Collection<ConfigurationInstance> configurations,
			ResultType singleRoomTimeSeries, ResultType singleRoomGapTimeResult) {
		super((List)inputData, start, end, configurations);
		this.singleRoomTimeSeries = singleRoomTimeSeries;
		this.singleRoomGapTimeResult = singleRoomGapTimeResult;
	}
	
	/**!! To be used only by JSON deserialization !!*/
	public ExampleOverallGaRoMultiResult() {
		this(null, 0, 0, null, null, null);
	}	
	
	@Override
	public void finishRoom(GaRoMultiResultExtended result, String roomId) {}

	@Override
	public void finishGateway(GaRoMultiResultExtended result, String gw) {
		gwWithDataCount++;
	}

	@Override
	public void finishTimeStep(GaRoMultiResultExtended result) {
		ExampleOverallGaRoMultiResult oresult = (ExampleOverallGaRoMultiResult) result;
		RoomData lowSensor = null;
		
		long minimumGapTime = Long.MAX_VALUE;
		//first look for minimum gap time
		
		for(RoomData evalData: result.roomEvals) {
			SingleEvaluationResult a = evalData.evalResultObjects().get(singleRoomGapTimeResult);
			@SuppressWarnings("unchecked")
			long val = ((SingleValueResult<Long>)a).getValue();
			if((val < minimumGapTime)||
					((val == minimumGapTime))) {
				lowSensor = evalData;
				minimumGapTime = val;
			}
		}

		if(lowSensor == null) return;
		SingleEvaluationResult b = lowSensor.evalResultObjects().get(singleRoomTimeSeries);
		ReadOnlyTimeSeries sourceTs = ((TimeSeriesResult)b).getValue();
		oresult.generalOutsideTemperature = new FloatTreeTimeSeries();
		for(SampledValue v: sourceTs.getValues(Long.MIN_VALUE)) {
			oresult.generalOutsideTemperature.addValue(new SampledValue(
					new FloatValue(v.getValue().getFloatValue()), v.getTimestamp(), Quality.GOOD));
		}
		oresult.generalOutsideTemperatureAv = ValueResourceUtils.getAverage(oresult.generalOutsideTemperature,
				result.startTime, result.endTime);
	}
	@Override
	public void finishTotal(GaRoSuperEvalResult<?> result) {}

}
