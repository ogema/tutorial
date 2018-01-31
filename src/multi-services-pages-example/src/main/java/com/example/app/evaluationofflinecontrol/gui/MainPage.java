package com.example.app.evaluationofflinecontrol.gui;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.app.evaluationofflinecontrol.MultiServicePageController;

import de.iwes.timeseries.eval.api.EvaluationProvider;
import de.iwes.widgets.api.extended.WidgetData;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.localisation.OgemaLocale;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.complextable.DynamicTable;
import de.iwes.widgets.html.complextable.RowTemplate;
import de.iwes.widgets.html.form.button.TemplateRedirectButton;
import de.iwes.widgets.html.form.label.Header;


/**
 * An HTML page, generated from the Java code.
 */
public class MainPage {
	
	private final DynamicTable<EvaluationProvider> table;

	public MainPage(final WidgetPage<?> page, final MultiServicePageController app) {
		
		Header header = new Header(page, "header", "Multi Service Page Example");
		header.addDefaultStyle(WidgetData.TEXT_ALIGNMENT_LEFT);
		page.append(header);
		
		table = new DynamicTable<EvaluationProvider>(page, "evalviewtable") {

			private static final long serialVersionUID = 1L;
			
			@Override
			public void onGET(OgemaHttpRequest req) {
				Collection<EvaluationProvider> providers = app.serviceAccess.getEvaluations().values(); 
				updateRows(providers, req);
			}
		};
		
		table.setRowTemplate(new RowTemplate<EvaluationProvider>() {

			@Override
			public Row addRow(EvaluationProvider eval, OgemaHttpRequest req) {
				Row row = new Row();
				String lineId = getLineId(eval);
				row.addCell("name", eval.id());
				row.addCell("description", eval.description(OgemaLocale.ENGLISH));
				TemplateRedirectButton<EvaluationProvider> detailPageButton = new TemplateRedirectButton<EvaluationProvider>(
						table, "detailPageButton"+lineId, "Details", "", req) {

					private static final long serialVersionUID = 1L;
					
					@Override
					public void onPrePOST(String data, OgemaHttpRequest req) {
						selectItem(eval, req);
						setUrl("Details.html", req);
					}
					@Override
					protected String getConfigId(EvaluationProvider object) {
						return object.id();
					}
				};
												
				row.addCell("detailPageButton", detailPageButton);
				
				return row;
			}

			@Override
			public String getLineId(EvaluationProvider object) {
				return object.id();
			}

			@Override
			public Map<String, Object> getHeader() {
				final Map<String, Object> header = new LinkedHashMap<>();
				header.put("name", "Name/ID");
				header.put("description", "Description");
				header.put("detailPageButton", "Open Detail Page");
				return header;
			}
		});
		
		page.append(table).linebreak();	
	}
}