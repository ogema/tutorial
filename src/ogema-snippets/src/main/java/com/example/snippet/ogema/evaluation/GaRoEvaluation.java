package com.example.snippet.ogema.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ogema.core.timeseries.ReadOnlyTimeSeries;

import de.iwes.timeseries.eval.api.EvaluationInput;
import de.iwes.timeseries.eval.api.EvaluationInstance;
import de.iwes.timeseries.eval.api.EvaluationResult;
import de.iwes.timeseries.eval.api.ResultType;
import de.iwes.timeseries.eval.api.TimeSeriesData;
import de.iwes.timeseries.eval.api.configuration.ConfigurationInstance;
import de.iwes.timeseries.eval.api.helper.EvalHelperExtended;
import de.iwes.timeseries.eval.base.provider.utils.EvaluationInputImpl;
import de.iwes.timeseries.eval.base.provider.utils.EvaluationUtils;
import de.iwes.timeseries.eval.base.provider.utils.TimeSeriesDataImpl;
import de.iwes.timeseries.eval.garo.api.helper.GaRoEvalHelper;
import de.iwes.timeseries.winopen.provider.WinHeatEvalProvider;

/** Example how to start GaRo evaluation
 * @param evalProvider this can just be a new instance of your evaluation provider
 * @param configurations can be null or empty
 *
 */
public class GaRoEvaluation {
	public static Map<String, String> runSampleEvaluationOffline(ReadOnlyTimeSeries temperatureMeasurementLogData,
			ReadOnlyTimeSeries windowOpenLogDataWindow1, ReadOnlyTimeSeries windowOpenLogDataWindow2,
			ReadOnlyTimeSeries valvePositionLogData, String label, String description,
			WinHeatEvalProvider evalProvider, long startTime, long endTime,
			Collection<ConfigurationInstance> configurations) {
		//Requested input 1 of EvaluProvider: temperature data
		final List<TimeSeriesData> timeSeriesDataTemperature = new ArrayList<>();
		TimeSeriesDataImpl dataImpl = new TimeSeriesDataImpl(temperatureMeasurementLogData, label+"_Room-Temperature", 
				description, null);
		timeSeriesDataTemperature.add(dataImpl);
		
		//Requested input 2 of EvaluProvider: window opening measurement data
		final List<TimeSeriesData> timeSeriesDataWindows = new ArrayList<>();
		dataImpl = new TimeSeriesDataImpl(windowOpenLogDataWindow1, label+"_Window1", 
				description, null);
		timeSeriesDataTemperature.add(dataImpl);
		if(windowOpenLogDataWindow2 != null) {
			dataImpl = new TimeSeriesDataImpl(windowOpenLogDataWindow2, label+"_Window2", 
					description, null);
			timeSeriesDataWindows.add(dataImpl);
		}
		
		//Requested input 3 of EvaluProvider: valve position data
		final List<TimeSeriesData> timeSeriesDataValve = new ArrayList<>();
		dataImpl = new TimeSeriesDataImpl(valvePositionLogData, label+"_Valve", 
				description, null);
		timeSeriesDataValve.add(dataImpl);

		/**Build input data set to evaluation*/
		final List<EvaluationInput> inputs = Arrays.<EvaluationInput> asList(new EvaluationInput[]{
				new EvaluationInputImpl(timeSeriesDataTemperature),
				new EvaluationInputImpl(timeSeriesDataWindows),
				new EvaluationInputImpl(timeSeriesDataValve)});
		
		/**Change this if not all results offered by the provider shall be calculated*/
		List<ResultType> requestedResults = evalProvider.resultTypes();
		
		/**Provide start end end time with configurations*/
		Collection<ConfigurationInstance> configurationsAll = EvalHelperExtended.addStartEndTime(startTime, endTime, null);
		if(configurations != null) configurationsAll.addAll(configurations);
		
		/**Finally start evaluation*/
		EvaluationInstance instance = EvaluationUtils.performEvaluationBlocking(evalProvider, inputs, requestedResults , configurationsAll);

		/**Get results*/
		final Map<ResultType, EvaluationResult> results = instance.getResults();
		
		GaRoEvalHelper.printAllResults("Room_"+label, results, EvaluationUtils.getStartAndEndTime(configurationsAll, inputs, false));
		Map<String, String> evalResults = EvalHelperExtended.getResults(instance);
		return evalResults;
	}
}
