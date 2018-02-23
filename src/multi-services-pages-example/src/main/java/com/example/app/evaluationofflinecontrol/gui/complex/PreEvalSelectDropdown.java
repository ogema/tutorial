package com.example.app.evaluationofflinecontrol.gui.complex;

import java.io.File;
import java.util.List;

import de.iwes.timeseries.eval.garo.multibase.GaRoSingleEvalProvider;
import de.iwes.timeseries.eval.garo.multibase.GaRoSingleEvalProviderPreEvalRequesting;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.form.dropdown.TemplateDropdown;
import de.iwes.widgets.html.form.label.Label;

public class PreEvalSelectDropdown extends TemplateDropdown<String> {

	private static final long serialVersionUID = 1L;
	final private List<String> jsonList;
	final TemplateDropdown<GaRoSingleEvalProvider> selectProvider;
	final Label selectPreEvalCount;
	final int preEvalIdx;
	
	public PreEvalSelectDropdown(WidgetPage<?> page, String id, List<String> jsonList,
			final TemplateDropdown<GaRoSingleEvalProvider> selectProvider,
			final Label selectPreEvalCount, int preEvalIdx) {
		super(page, id);
		this.jsonList = jsonList;
		this.selectProvider = selectProvider;
		this.selectPreEvalCount = selectPreEvalCount;
		this.preEvalIdx = preEvalIdx;
	}
		
	public void onGET(OgemaHttpRequest req) {
		jsonList.clear();
		File folder = new File(ComplexDependencyExample.FILE_PATH);
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
			
				update(jsonList, req);
				selectPreEvalCount.setText("PreEval"+preEvalIdx+": ("+count+") "+preEval1, req);
				setWidgetVisibility(true, req);
				used = true;
			}
		}
		if(!used) {
			jsonList.clear();
			count = 0;
			preEval1 = "";
			update(jsonList, req);
			selectPreEvalCount.setText("--"+preEval1, req);
			setWidgetVisibility(false, req);
		}
	}
}
