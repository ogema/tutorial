package com.example.snippet.ogema.gui;

import de.iwes.widgets.api.widgets.OgemaWidget;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.html.form.button.Button;
import de.iwes.widgets.html.form.textfield.TextField;
import de.iwes.widgets.html.html5.Flexbox;
import de.iwes.widgets.html.html5.flexbox.FlexWrap;
import de.iwes.widgets.html.html5.flexbox.JustifyContent;

public class StaticTableExample {
	public StaticTableExample(final WidgetPage<?> page) {
		 final TextField editNewProgramName = new TextField(page, "editNewProgramName");
	       	final Button buttonSaveAsNewProgram = new Button(page, "buttonSaveAsNewProgram", "Save as new Program");

	        StaticTable table2 = new StaticTable(2, 2);
	        page.append(table2);
	        table2.setContent(0, 0, editNewProgramName);
	        table2.setContent(0, 1, buttonSaveAsNewProgram);
	        table2.setContent(1, 0, "Now both elements in a nested row");
	        table2.setContent(1, 1, getFlexBox(page, editNewProgramName, buttonSaveAsNewProgram, "twocolUpdate"));
	}
	
	public static Flexbox getFlexBox(WidgetPage<?> page, OgemaWidget w1, OgemaWidget w2, String id) {
		Flexbox flex = new Flexbox(page, id, true);
		flex.addItem(w1, null).addItem(w2, null);
		flex.setJustifyContent(JustifyContent.SPACE_AROUND, null);
		flex.setDefaultFlexWrap(FlexWrap.NOWRAP);
		return flex;
	}

}
