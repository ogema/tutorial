package com.example.app.evaluationofflinecontrol.gui.complex;

import java.io.File;
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
import de.iwes.timeseries.eval.garo.helper.jaxb.GaRoEvalHelperGeneric;
import de.iwes.timeseries.eval.garo.helper.jaxb.GaRoEvalHelperJAXB;
import de.iwes.timeseries.eval.garo.multibase.GaRoSingleEvalProvider;
import de.iwes.timeseries.eval.garo.multibase.GaRoSingleEvalProviderPreEvalRequesting;
import de.iwes.widgets.api.extended.WidgetData;
import de.iwes.widgets.api.widgets.OgemaWidget;
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

public class ComplexDependencyExamplePage {
	public static final String FILE_PATH = System.getProperty("de.iwes.tools.timeseries-multieval.resultpath", "../evaluationresults");
	//private final WidgetPage<?> page; 
	private final MultiServicePageController controller;
	
	//governing dropdown
	final TemplateDropdown<GaRoSingleEvalProvider> selectProvider;
	
	//further elements that need to be accessed across widgets
	private final TemplateMultiselect<String> multiSelectRooms;
	private final TemplateMultiselect<ResultType> multiSelectResults;
	private final Label resultsPerStepLabel;
	private final Label estimatedCalculationTimeLabel;
	private final TemplateDropdown<String> selectPreEval1;
	private final Label preEvalLabel1;
	private final TemplateDropdown<String> selectPreEval2;
	private final Label preEvalLabel2;
	private final Button startOfflineEval;
	
	public final long UPDATE_RATE = 5*1000;
	public EvaluationProvider selectEval;
	public ComplexDependencyExamplePage(final WidgetPage<?> page, final MultiServicePageController app) {
		
		this.controller = app;
		Header header = new Header(page, "header", "Offline Evaluation Control");
		header.addDefaultStyle(WidgetData.TEXT_ALIGNMENT_LEFT);
		page.append(header);
		
		List<String> singleValueOption = new ArrayList<>();
		singleValueOption.add("Days Value");
		singleValueOption.add("Weeks Value");
		singleValueOption.add("Months Value");
		
		/** Get parameter which initializes the governing dropdown (on which all or most widgets
		 * depend) and initialize widgets in the updateDependentWidgets method
		 */
		final TemplateInitSingleEmpty<GaRoSingleEvalProvider> init = new TemplateInitSingleEmpty<GaRoSingleEvalProvider>(page, "init", false) {
			private static final long serialVersionUID = 1L;

			/**If the page does not support configuration of the initial choice
			 * of the major dropdown (selectProvider in this case) you can set the type of the init to String
			 * and just return null here.
			 */
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
				/** Only the initializiation of multiSelectRooms would be required here
				if the page would not support configuration of the initial choice
			 	of the major dropdown (selectProvider in this case)
			 	*/
			    final String[] allRooms = {"Kitchen", "Living Room", "Bed Room"};
				multiSelectRooms.update(Arrays.asList(allRooms), req);
				multiSelectRooms.selectItems(Arrays.asList(allRooms), req);

				super.init(req);
				GaRoSingleEvalProvider item = getSelectedItem(req);
				selectProvider.update(getProviders(), req);
				selectProvider.selectItem(item, req);
				
				//Time consuming operation: Here we have to take care that operation is
				//only performed once for each user action
				estimatedCalculationTimeLabel.setText(getEstimatedCalculationTime(req), req);
			}
		};
		page.append(init);
		
		//governing drop-down
		selectProvider = new TemplateDropdown<GaRoSingleEvalProvider>(page, "selectProvider") {

			private static final long serialVersionUID = 1L;

			@Override
			public void updateDependentWidgets(OgemaHttpRequest req) {
				GaRoSingleEvalProvider eval = getSelectedItem(req);
				if(eval != null ) {
					List<ResultType> resultMultiSelection = eval.resultTypes();
					multiSelectResults.update(resultMultiSelection, req);
					multiSelectResults.selectItems(resultMultiSelection, req);
					
					//Here we update several widgets at once, organizing this top-down saves
					//implementation of a lot of onGET methods
					updatePreEvalWidgets(selectPreEval1, preEvalLabel1, 0, req);
					updatePreEvalWidgets(selectPreEval2, preEvalLabel2, 1, req);
				}
			}
			
			@Override
			public void onPOSTComplete(String data, OgemaHttpRequest req) {
				//Time consuming operation: Here we have to take care that operation is
				//only performed once for each user action
				estimatedCalculationTimeLabel.setText(getEstimatedCalculationTime(req), req);
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
			
			@Override
			public void onPOSTComplete(String data, OgemaHttpRequest req) {
				//Time consuming operation: Here we have to take care that operation is
				//only performed once for each user action
				estimatedCalculationTimeLabel.setText(getEstimatedCalculationTime(req), req);
			}
			
		};
		multiSelectResults.setDefaultWidth("100%");

		multiSelectResults.setTemplate(new DefaultDisplayTemplate<ResultType>() {
			@Override
			public String getLabel(ResultType object, OgemaLocale locale) {
				return object.label(OgemaLocale.ENGLISH);
			}
		});
		
		final MultiSelectExtended<ResultType> multiSelectResultsExtended = 
				new MultiSelectExtended<ResultType>(page, "resultSelectionExtended",
				multiSelectResults, true, "", true) {
			private static final long serialVersionUID = 2984111641629326499L;
			@Override
			protected void onSelectionEvent(boolean isSelection, OgemaHttpRequest req) {
				estimatedCalculationTimeLabel.setText(getEstimatedCalculationTime(req), req);
			}
		};

		
		multiSelectRooms = new TemplateMultiselect<String>(page, "roomSelectionMS") {

			private static final long serialVersionUID = 1L;
			
			@Override
			public void onPOSTComplete(String data, OgemaHttpRequest req) {
				//Time consuming operation: Here we have to take care that operation is
				//only performed once for each user action
				estimatedCalculationTimeLabel.setText(getEstimatedCalculationTime(req), req);
			}
		};
		multiSelectRooms.setDefaultWidth("100%");
		
		final MultiSelectExtended<String> multiSelectRoomsExtended = new MultiSelectExtended<String>(page, "roomsSelection",
				multiSelectRooms, true, "", true) {
			private static final long serialVersionUID = 7221204736660272350L;

			@Override
			protected void onSelectionEvent(boolean isSelection, OgemaHttpRequest req) {
				estimatedCalculationTimeLabel.setText(getEstimatedCalculationTime(req), req);
			}
		};

		resultsPerStepLabel = new Label(page, "resultsPerStep") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onGET(OgemaHttpRequest req) {
				//Simple operation: Here we do not care if the operation is done even when
				//input has not changed
				resultsPerStepLabel.setText(getResultPerStep(req), req);
			}
		};
		estimatedCalculationTimeLabel = new Label(page, "estimatedCalculationTime");
		
		//single value intervals drop-down
		final TemplateDropdown<String> selectSingleValueIntervals = 
				new TemplateDropdown<String>(page, "singleValueIntervals") {
			
			private static final long serialVersionUID = 1L;
			
			public void onGET(OgemaHttpRequest req) {
				setDefaultItems(singleValueOption);
				//Simple operation: Here we do not care if the other Multiselect performs the
				//same operation again
				resultsPerStepLabel.setText(getResultPerStep(req), req);
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
		selectPreEval1 = new TemplateDropdown<String>(page, "selectPreEval1");
		preEvalLabel1 = new Label(page, "selectPreEvalCount1");
		
		//second preEvaluation drop-down
		selectPreEval2 = new TemplateDropdown<String>(page, "selectPreEval2");
		preEvalLabel2 = new Label(page, "selectPreEvalCount2");
		
		//start offline evaluation for selected provider
		startOfflineEval = new Button(page, "startOfflineEval", "Start Offline Evaluation") {

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
				String newPath = evaluationResultFileName.getValue(req);
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
			    long startTime = 1483228800000l;
			    long endTime = 1485907200000l;
				if(eval instanceof GaRoSingleEvalProviderPreEvalRequesting) {
					GaRoSingleEvalProviderPreEvalRequesting peval = (GaRoSingleEvalProviderPreEvalRequesting)eval;
					switch(peval.preEvaluationsRequested().size()) {
					case 0:
						break;
					case 1:
						Path preEvalFile = Paths.get(FILE_PATH+"/"+selectPreEval1.getSelectedLabel(req));
						GaRoPreEvaluationProvider preProvider = 
								new GaRoStdPreEvaluationProvider<GaRoMultiResultDeser, GaRoSuperEvalResult<GaRoMultiResultDeser>>
						(GaRoSuperEvalResultDeser.class, preEvalFile.toString());
					
						GaRoEvalHelperGeneric.performGenericMultiEvalOverAllData(selectProvider.getSelectedItem(req).getClass(),
								controller.appMan,
								startTime, endTime,
								ChronoUnit.DAYS,
								null, false,
								new GaRoPreEvaluationProvider[] {preProvider}, resultsRequested, roomIDs, newPath);
						usedPreEval = true;
						break;
					case 2:
						Path preEvalFile1 = Paths.get(FILE_PATH+"/"+selectPreEval1.getSelectedLabel(req));
						GaRoPreEvaluationProvider preProvider1 = 
								new GaRoStdPreEvaluationProvider<GaRoMultiResultDeser, GaRoSuperEvalResult<GaRoMultiResultDeser>>
						(GaRoSuperEvalResultDeser.class, preEvalFile1.toString());
	
						Path preEvalFile2 = Paths.get(FILE_PATH+"/"+selectPreEval1.getSelectedLabel(req));
						GaRoPreEvaluationProvider preProvider2 = 
								new GaRoStdPreEvaluationProvider<GaRoMultiResultDeser, GaRoSuperEvalResult<GaRoMultiResultDeser>>
						(GaRoSuperEvalResultDeser.class, preEvalFile2.toString());

						
						GaRoEvalHelperJAXB.performGenericMultiEvalOverAllData(selectProvider.getSelectedItem(req),
								controller.appMan,
								startTime, endTime,
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
					startTime, endTime,
					ChronoUnit.DAYS,
					null, true, null, resultsRequested, roomIDs, newPath);
				} 
			}
		};
		
		startOfflineEval.addDefaultStyle(ButtonData.BOOTSTRAP_GREEN);

		int i = 0;
		StaticTable table1 = new StaticTable(10, 2, new int[] {4, 8});
		page.append(table1);
		table1.setContent(i, 0, "Name/ID");
		table1.setContent(i, 1, selectProvider);
		i++;
		table1.setContent(i, 0, "Result Selection");
		table1.setContent(i, 1, multiSelectResultsExtended);
		i++;
		table1.setContent(i, 0, "Rooms Selection" );
		table1.setContent(i, 1, multiSelectRoomsExtended    );
		i++;
		table1.setContent(i, 0, "Single Value Intervals");
		table1.setContent(i, 1, selectSingleValueIntervals);
		i++; //5
		table1.setContent(i, 0, "Results per stepsize");
		table1.setContent(i, 1, resultsPerStepLabel);
		i++;
		table1.setContent(i, 0, "Estimated calculation time (seconds)");
		table1.setContent(i, 1, estimatedCalculationTimeLabel);
		i++;
		table1.setContent(i, 0, preEvalLabel1 );
		table1.setContent(i, 1, selectPreEval1  );
		i++;
		table1.setContent(i, 0, preEvalLabel2 );
		table1.setContent(i, 1, selectPreEval2  );
		i++;
		table1.setContent(i, 0, "Result File Name");
		table1.setContent(i, 1, evaluationResultFileName);
		i++; //10
		table1.setContent(i, 0, startOfflineEval );
		table1.setContent(i, 1, "				");
	    
		/**Configure Dependencies
		 * Note that dependencies inside MultiSelectExtended are set in this PageSnippet*/
		selectProvider.triggerAction(evaluationResultFileName, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(multiSelectResults, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);

		selectProvider.triggerAction(selectPreEval1, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(preEvalLabel1, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(selectPreEval2, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		selectProvider.triggerAction(preEvalLabel2, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);

		/** Register dependencies of MultiSelects. We also have to register the dependencies
		 * for all dependency governors of the multiselects
		 */
		triggerMultiSelectDependencies(multiSelectResults);
		triggerMultiSelectDependencies(multiSelectRooms);
		triggerMultiSelectDependencies(selectProvider);
		triggerMultiSelectDependencies(multiSelectResultsExtended.selectAllButton);
		triggerMultiSelectDependencies(multiSelectResultsExtended.deselectAllButton);
		triggerMultiSelectDependencies(multiSelectRoomsExtended.selectAllButton);
		triggerMultiSelectDependencies(multiSelectRoomsExtended.deselectAllButton);
	}
	
	private void triggerMultiSelectDependencies(OgemaWidget parent) {
		parent.triggerAction(startOfflineEval, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		parent.triggerAction(resultsPerStepLabel, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		parent.triggerAction(estimatedCalculationTimeLabel, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);		
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
	
	/**We assume that this is a very time consuming calculation*/
	private String getEstimatedCalculationTime(OgemaHttpRequest req) {
		int resultNum = multiSelectResults.getSelectedItems(req).size();
		int roomNum = multiSelectRooms.getSelectedItems(req).size();
		//we assume some time consuming operation here
		return ""+(long) (0.001*Math.exp(resultNum+roomNum)+Math.sqrt(resultNum)*Math.pow(roomNum, 0.75));
	}

	private void updatePreEvalWidgets(TemplateDropdown<String> jsonDropdown,
			final Label selectPreEvalCount, int preEvalIdx,
			OgemaHttpRequest req) {
		List<String> jsonList = new ArrayList<>();
		File folder = new File(ComplexDependencyExamplePage.FILE_PATH);
		File[] allFiles = folder.listFiles();
		String preEval1 = "";
		int count = 0;
		GaRoSingleEvalProvider eval =  selectProvider.getSelectedItem(req);
		boolean used = false;
		if (eval instanceof GaRoSingleEvalProviderPreEvalRequesting) {
			GaRoSingleEvalProviderPreEvalRequesting gaRoEval = (GaRoSingleEvalProviderPreEvalRequesting)eval;
			if(gaRoEval.preEvaluationsRequested().size() > preEvalIdx) {
				preEval1 = gaRoEval.preEvaluationsRequested().get(preEvalIdx).getSourceProvider();
				for(int i = 0; i < allFiles.length; i++) {
					if(allFiles[i].isFile() && allFiles[i].getName().contains(gaRoEval.preEvaluationsRequested().get(preEvalIdx).getSourceProvider())) {
						jsonList.add(allFiles[i].getName());
						count += 1;
					}
				}
			
				jsonDropdown.update(jsonList, req);
				selectPreEvalCount.setText("PreEval"+preEvalIdx+": ("+count+") "+preEval1, req);
				jsonDropdown.setWidgetVisibility(true, req);
				used = true;
			}
		}
		if(!used) {
			jsonList.clear();
			count = 0;
			preEval1 = "";
			jsonDropdown.update(jsonList, req);
			selectPreEvalCount.setText("--"+preEval1, req);
			jsonDropdown.setWidgetVisibility(false, req);
		}		
	}
		
}
