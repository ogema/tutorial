package com.example.app.evaluationofflinecontrol.gui;

import java.util.Collection;

import com.example.app.evaluationofflinecontrol.MultiServicePageController;

import de.iwes.timeseries.eval.api.EvaluationProvider;
import de.iwes.widgets.api.extended.WidgetData;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.dynamics.TriggeredAction;
import de.iwes.widgets.api.widgets.dynamics.TriggeringAction;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.api.widgets.localisation.OgemaLocale;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.form.button.TemplateInitSingleEmpty;
import de.iwes.widgets.html.form.dropdown.TemplateDropdown;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.html.form.label.Label;
import de.iwes.widgets.template.DisplayTemplate;

public class DetailPage {
	private final MultiServicePageController controller;
	final TemplateDropdown<EvaluationProvider> selectProvider;
	
	public DetailPage(final WidgetPage<?> page, final MultiServicePageController app) {
		this.controller = app;
		
		TemplateInitSingleEmpty<EvaluationProvider> init = new TemplateInitSingleEmpty<EvaluationProvider>(page, "init", false) {
			private static final long serialVersionUID = 1L;

			@Override
			protected EvaluationProvider getItemById(String configId) {
				for(EvaluationProvider eval: controller.serviceAccess.getEvaluations().values()) {
					if(eval.id().equals(configId)) return eval;
				}
				return null;
			}
			@Override
			public void updateDependentWidgets(OgemaHttpRequest req) {
				Collection<EvaluationProvider> items = controller.serviceAccess.getEvaluations().values();
				selectProvider.update(items , req);
				EvaluationProvider eval = getSelectedItem(req);
				selectProvider.selectItem(eval, req);
			}
		};
		page.append(init);
		
		Header header = new Header(page, "header", "Multi-Service Detail Page");
		header.addDefaultStyle(WidgetData.TEXT_ALIGNMENT_LEFT);
		page.append(header);
		
		// provider selection
		selectProvider = new  TemplateDropdown<EvaluationProvider>(page, "selectProvider");
		selectProvider.setTemplate(new DisplayTemplate<EvaluationProvider>() {
			@Override
			public String getId(EvaluationProvider object) {
				return object.id();
			}

			@Override
			public String getLabel(EvaluationProvider object, OgemaLocale locale) {
				return object.label(locale);
			}
			
		});
		
		final Label label = new Label(page, "label") {

			private static final long serialVersionUID = 1L;
			
			@Override
			public void onGET(OgemaHttpRequest req) {
				String selectedEvalProvider = selectProvider.getSelectedItem(req).id();

				setText(selectedEvalProvider+"Result.json", req);
			}
			
		};
		
		selectProvider.triggerAction(label, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
	
		StaticTable table1 = new StaticTable(2, 3);
		page.append(table1);
		table1.setContent(0, 0, "Name/ID");
		table1.setContent(0, 1, selectProvider);
		table1.setContent(0, 2, " ");
		
		table1.setContent(1, 0, "Evaluation Acronym");
		table1.setContent(1, 1, label);
		table1.setContent(1, 2, "       ");
	}
}
