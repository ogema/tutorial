package com.example.app.evaluationofflinecontrol.gui.complex;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.ogema.serialization.jaxb.Resource;

import com.example.app.evaluationofflinecontrol.MultiServicePageController;

import de.iwes.timeseries.eval.api.EvaluationProvider;
import de.iwes.timeseries.eval.api.ResultType;
import de.iwes.timeseries.eval.garo.api.base.GaRoPreEvaluationProvider;
import de.iwes.timeseries.eval.garo.api.base.GaRoStdPreEvaluationProvider;
import de.iwes.timeseries.eval.garo.api.base.GaRoSuperEvalResult;
import de.iwes.timeseries.eval.garo.api.helper.base.GaRoEvalHelper;
import de.iwes.timeseries.eval.garo.multibase.GaRoSingleEvalProvider;
import de.iwes.timeseries.eval.garo.multibase.GaRoSingleEvalProviderPreEvalRequesting;
import de.iwes.widgets.api.extended.WidgetData;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.dynamics.TriggeredAction;
import de.iwes.widgets.api.widgets.dynamics.TriggeringAction;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.api.widgets.localisation.OgemaLocale;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.form.button.Button;
import de.iwes.widgets.html.form.button.ButtonData;
import de.iwes.widgets.html.form.button.TemplateInitSingleEmpty;
import de.iwes.widgets.html.form.dropdown.TemplateDropdown;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.html.form.label.Label;
import de.iwes.widgets.html.form.textfield.TextField;
import de.iwes.widgets.html.multiselect.TemplateMultiselect;
import de.iwes.widgets.multiselect.extended.MultiSelectExtended;
import de.iwes.widgets.template.DefaultDisplayTemplate;

public class ComplexDependencyExample {
    private static long[] startEnd = {1483228800000l, 1485907200000l};
    
    private static final String[] allRooms = {"Kitchen", "Living Room", "Bed Room"};
    
	public static final String FILE_PATH = System.getProperty("de.iwes.tools.timeseries-multieval.resultpath", "../evaluationresults");
	//private final WidgetPage<?> page; 
	private final MultiServicePageController controller;
	private final TemplateMultiselect<String> multiSelectRooms;
//	final TemplateMultiselect<ResultType> resultsSelection;
	private List<ResultType> resultMultiSelection = new ArrayList<ResultType>();
	private final TemplateMultiselect<ResultType> multiSelectResults;
	final Label resultsPerStep;
	final Label estimatedCalculationTime;
	
	private List<String> jsonList1 = new ArrayList<>();
	private List<String> jsonList2 = new ArrayList<>();
	
	public final long UPDATE_RATE = 5*1000;
	public EvaluationProvider selectEval;
	public ComplexDependencyExample(final WidgetPage<?> page, final MultiServicePageController app) {
		
		this.controller = app;
		Header header = new Header(page, "header", "Offline Evaluation Control");
		header.addDefaultStyle(WidgetData.TEXT_ALIGNMENT_LEFT);
		page.append(header);
		
		List<String> configOptions = new ArrayList<>();
		configOptions.add("TestOneWeek");
		configOptions.add("TestOneMonth");
		configOptions.add("TestFiveMonths");
		
		List<String> singleValueOption = new ArrayList<>();
		singleValueOption.add("Days Value");
		singleValueOption.add("Weeks Value");
		singleValueOption.add("Months Value");
		
		/** Get parameter which initializes the governing dropdown (on which all or most widgets
		 * depend) and initialize widgets in the updateDependentWidgets method
		 */
		final TemplateInitSingleEmpty<GaRoSingleEvalProvider> init = new TemplateInitSingleEmpty<GaRoSingleEvalProvider>(page, "init", false) {
			private static final long serialVersionUID = 1L;

			@Override
			protected GaRoSingleEvalProvider getItemById(String configId) {
				for(GaRoSingleEvalProvider p: getProviders()) {
					if(p.id().equals(configId)) return p;
				}
				if(getProviders().isEmpty())
					return null;
				else return getProviders().get(0);
			}
			
			@Override
			public void init(OgemaHttpRequest req) {
				multiSelectRooms.selectItems(Arrays.asList(allRooms), req);
			}
		};
		page.append(init);
		
		final Label selectPreEval1Count = new Label(page, "selectPreEvalCount1");
		final Label selectPreEval2Count = new Label(page, "selectPreEvalCount2");

		//governing drop-down
		final TemplateDropdown<GaRoSingleEvalProvider> selectProvider 
			= new TemplateDropdown<GaRoSingleEvalProvider>(page, "selectProvider") {

				private static final long serialVersionUID = 1L;

			@Override
			public void onGET(OgemaHttpRequest req) {
				update(getProviders(), req);
					
				final EvaluationProvider provider = init.getSelectedItem(req);
				selectItem((GaRoSingleEvalProvider) provider, req);
			}
			
			@Override
			public void updateDependentWidgets(OgemaHttpRequest req) {
				GaRoSingleEvalProvider eval = getSelectedItem(req);
				if(eval != null ) {
					resultMultiSelection = eval.resultTypes();
					multiSelectResults.update(resultMultiSelection, req);
					multiSelectResults.selectItems(resultMultiSelection, req);
					//Time consuming operation: Here we have to take care that operation is
					//only performed once for each user action
					estimatedCalculationTime.setText(getEstimatedCalculationTime(req), req);
				}
			}
			
		};
		
		selectProvider.setTemplate(new DefaultDisplayTemplate<GaRoSingleEvalProvider>() {
			@Override
			public String getLabel(GaRoSingleEvalProvider object, OgemaLocale locale) {
				return object.getClass().getSimpleName();
			}
		});

		//result multi-selection
		multiSelectResults = new TemplateMultiselect<ResultType>(page, "resultSelections") {

			private static final long serialVersionUID = 1L;
			
			public void onGET(OgemaHttpRequest req) {
				//String selectedProvider  = selectProvider.getSelectedLabel(req);
				EvaluationProvider eval =  selectProvider.getSelectedItem(req); //getEvalationProviderByName(selectedProvider, providers);
				if(eval != null ) { //&& selectedProvider.equals(eval.getClass().getSimpleName())) {
					resultMultiSelection = eval.resultTypes();
					update(resultMultiSelection, req);
				}
			}

			@Override
			public void onPOSTComplete(String data, OgemaHttpRequest req) {
				//Time consuming operation: Here we have to take care that operation is
				//only performed once for each user action
				estimatedCalculationTime.setText(getEstimatedCalculationTime(req), req);
			}
			
		};
		multiSelectResults.setDefaultWidth("100%");
		multiSelectResults.selectDefaultItems(resultMultiSelection);

		multiSelectResults.setTemplate(new DefaultDisplayTemplate<ResultType>() {
			@Override
			public String getLabel(ResultType object, OgemaLocale locale) {
				return object.label(OgemaLocale.ENGLISH);
			}
		});
		
		final MultiSelectExtended<ResultType> multiSelectResultsExtended = 
				new MultiSelectExtended<ResultType>(page, "resultSelectionExtended",
				multiSelectResults, true, "", true);

		
		multiSelectRooms = new TemplateMultiselect<String>(page, "roomSelectionMS") {

			private static final long serialVersionUID = 1L;
			
			@Override
			public void onGET(OgemaHttpRequest req) {
				
				update(Arrays.asList(allRooms), req);

			}
			@Override
			public void onPOSTComplete(String data, OgemaHttpRequest req) {
				//Time consuming operation: Here we have to take care that operation is
				//only performed once for each user action
				estimatedCalculationTime.setText(getEstimatedCalculationTime(req), req);
			}
		};
		multiSelectRooms.setDefaultWidth("100%");
		
		final MultiSelectExtended<String> multiSelectRoomsExtended = new MultiSelectExtended<String>(page, "gateWaySelection",
				multiSelectRooms, true, "", true);

		resultsPerStep = new Label(page, "resultsPerStep") {
			@Override
			public void onGET(OgemaHttpRequest req) {
				//Simple operation: Here we do not care if the operation is done even when
				//input has not changed
				resultsPerStep.setText(getResultPerStep(req), req);
			}
		};
		estimatedCalculationTime = new Label(page, "estimatedCalculationTime");
		
		//single value intervals drop-down
		final TemplateDropdown<String> selectSingleValueIntervals = 
				new TemplateDropdown<String>(page, "singleValueIntervals") {
			
			private static final long serialVersionUID = 1L;
			
			public void onGET(OgemaHttpRequest req) {
				setDefaultItems(singleValueOption);
				//Simple operation: Here we do not care if the other Multiselect performs the
				//same operation again
				resultsPerStep.setText(getResultPerStep(req), req);
			}
		};
		
		//json file name of a selected evaluation provider 
		final TextField evaluationResultFileName = new TextField(page, "label") {

			private static final long serialVersionUID = 1L;
			
			@Override
			public void onGET(OgemaHttpRequest req) {
				String selectedEvalProvider = selectProvider.getSelectedLabel(req);
				setValue(selectedEvalProvider+"Result.json", req);
			}
		};

		//first preEvaluation drop-down
		final TemplateDropdown<String> selectPreEval1 = new PreEvalSelectDropdown(page, "selectPreEval1",
				jsonList1, selectProvider, selectPreEval1Count, 0);
		
		//second preEvaluation drop-down
		final TemplateDropdown<String> selectPreEval2 = new PreEvalSelectDropdown(page, "selectPreEval2",
				jsonList2, selectProvider, selectPreEval2Count, 1);
		
		//start offline evaluation for selected provider
		final Button startOfflineEval = new Button(page, "startOfflineEval", "Start Offline Evaluation") {

			private static final long serialVersionUID = 1L;
			public void onGET(OgemaHttpRequest req) {
				if(multiSelectResults.getSelectedItems(req).isEmpty() || multiSelectRooms.getSelectedItems(req).isEmpty()) {
					disable(req);
				} else enable(req);
			};
			
			@Override
			public void onPrePOST(String data, OgemaHttpRequest req) {
				String selectedEvalProvider = selectProvider.getSelectedLabel(req);
								
				List<ResultType> resultsRequested = multiSelectResults.getSelectedItems(req);
				List<String> roomIDs = (List<String>) multiSelectRoomsExtended.multiSelect.getSelectedLabels(req);
				
				GaRoSingleEvalProvider eval = selectProvider.getSelectedItem(req);
				
				int i = 0;
				int j = 1;
				String newPath = "";
				Path providerPath = Paths.get(FILE_PATH+"/"+selectedEvalProvider+"Result.json");
				if(!Files.isRegularFile(providerPath)) {
					newPath = selectedEvalProvider+"Result.json";
				} else {
					while(i < j) {
						providerPath = Paths.get(j < 10?FILE_PATH+"/"+selectedEvalProvider+"Result_0"+j+".json":
									FILE_PATH+"/"+selectedEvalProvider+"Result_"+j+".json");
						
						if(!Files.isRegularFile(providerPath)) {
							newPath = j < 10?selectedEvalProvider+"Result_0"+j+".json":selectedEvalProvider+"Result_"+j+".json";
							i++;
						}
						j++;
						i++;
					}
				}
				boolean usedPreEval = false;
				if(eval instanceof GaRoSingleEvalProviderPreEvalRequesting) {
					GaRoSingleEvalProviderPreEvalRequesting peval = (GaRoSingleEvalProviderPreEvalRequesting)eval;
					switch(peval.preEvaluationsRequested().size()) {
					case 0:
						break;
					case 1:
						Path preEvalFile = Paths.get(FILE_PATH+"/"+selectPreEval1.getSelectedLabel(req));
						GaRoPreEvaluationProvider<Resource> preProvider = 
								new GaRoStdPreEvaluationProvider<Resource, GaRoMultiResultDeser, GaRoSuperEvalResult<Resource, GaRoMultiResultDeser>>
						(GaRoSuperEvalResultDeser.class, preEvalFile.toString());
					
						GaRoEvalHelper.performGenericMultiEvalOverAllData(selectProvider.getSelectedItem(req).getClass(),
								controller.appMan,
								startEnd[0], startEnd[1],
								ChronoUnit.DAYS,
								null, false,
								new GaRoPreEvaluationProvider[] {preProvider}, resultsRequested, roomIDs, newPath);
						usedPreEval = true;
						break;
					case 2:
						Path preEvalFile1 = Paths.get(FILE_PATH+"/"+selectPreEval1.getSelectedLabel(req));
						GaRoPreEvaluationProvider<Resource> preProvider1 = 
								new GaRoStdPreEvaluationProvider<Resource, GaRoMultiResultDeser, GaRoSuperEvalResult<Resource, GaRoMultiResultDeser>>
						(GaRoSuperEvalResultDeser.class, preEvalFile1.toString());
	
						Path preEvalFile2 = Paths.get(FILE_PATH+"/"+selectPreEval1.getSelectedLabel(req));
						GaRoPreEvaluationProvider<Resource> preProvider2 = 
								new GaRoStdPreEvaluationProvider<Resource, GaRoMultiResultDeser, GaRoSuperEvalResult<Resource, GaRoMultiResultDeser>>
						(GaRoSuperEvalResultDeser.class, preEvalFile2.toString());

						
						GaRoEvalHelper.performGenericMultiEvalOverAllData(selectProvider.getSelectedItem(req).getClass(),
								controller.appMan,
								startEnd[0], startEnd[1],
								ChronoUnit.DAYS,
								null, false,
								new GaRoPreEvaluationProvider[] {preProvider1, preProvider2}, resultsRequested, roomIDs, newPath);
						usedPreEval = true;
						break;
					default:
						throw new IllegalStateException("maximum 3 PreEvaluation supported!");
					}
				}

				if(!usedPreEval) {
					GaRoEvalHelper.performGenericMultiEvalOverAllData(selectProvider.getSelectedItem(req).getClass(),
					controller.appMan,
					startEnd[0], startEnd[1],
					ChronoUnit.DAYS,
					null, true, null, resultsRequested, roomIDs, newPath);
				} 
			}
		};
		
		startOfflineEval.addDefaultStyle(ButtonData.BOOTSTRAP_GREEN);

		int i = 0;
		StaticTable table1 = new StaticTable(8, 3);
		page.append(table1);
		table1.setContent(i, 0, "Name/ID");
		table1.setContent(i, 1, selectProvider);
		table1.setContent(i, 2, " ");
		i++;
		table1.setContent(i, 0, "Result Selection");
		table1.setContent(i, 1, multiSelectResultsExtended);
		table1.setContent(i, 2, "       ");
		i++;
		table1.setContent(i, 0, "Gateways Selection" );
		table1.setContent(i, 1, multiSelectRoomsExtended    );
		table1.setContent(i, 2, "                  ");
		i++;
		table1.setContent(i, 0, "Single Value Intervals");
		table1.setContent(i, 1, selectSingleValueIntervals);
		table1.setContent(i, 2, "       ");
		i++;
		table1.setContent(i, 0, selectPreEval1Count );
		table1.setContent(i, 1, selectPreEval1  );
		table1.setContent(i, 2, "preEvalInfo");
		i++;
			table1.setContent(i, 0, selectPreEval2Count );
			table1.setContent(i, 1, selectPreEval2  );
			table1.setContent(i, 2, "preEvalInfo");
			i++;
		table1.setContent(i, 0, "Evaluation Acronym");
		table1.setContent(i, 1, evaluationResultFileName);
		table1.setContent(i, 2, "       ");
		i++;
		table1.setContent(i, 0, startOfflineEval );
		table1.setContent(i, 1, "				");
		table1.setContent(i, 2, "       	   	");
	    
		/**Configure Dependencies
		 * Note that dependencies inside MultiSelectExtended are set in this PageSnippet*/
		selectProvider.triggerAction(evaluationResultFileName, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(selectPreEval1, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(selectPreEval2, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(multiSelectResults, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(startOfflineEval, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(selectPreEval1Count, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(selectPreEval1Count, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(selectPreEval2Count, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(selectPreEval2Count, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		multiSelectResults.triggerAction(startOfflineEval, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		multiSelectRooms.triggerAction(startOfflineEval, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
	}
	
	private List<GaRoSingleEvalProvider> getProviders() {
		return 	controller.serviceAccess.getEvaluations().values().stream()
				.filter(provider -> provider instanceof GaRoSingleEvalProvider)
				.map(provider -> (GaRoSingleEvalProvider) provider)
				.collect(Collectors.toList());

	}
	
	private String getResultPerStep(OgemaHttpRequest req) {
		int resultNum = multiSelectResults.getSelectedItems(req).size();
		int roomNum = multiSelectRooms.getSelectedItems(req).size();
		return ""+(resultNum*roomNum);
	}
	
	private String getEstimatedCalculationTime(OgemaHttpRequest req) {
		int resultNum = multiSelectResults.getSelectedItems(req).size();
		int roomNum = multiSelectRooms.getSelectedItems(req).size();
		//we assume some time consuming operation here
		return ""+(long) (0.001*Math.exp(resultNum+roomNum)+Math.sqrt(resultNum)*Math.pow(roomNum, 0.75));
	}

}
