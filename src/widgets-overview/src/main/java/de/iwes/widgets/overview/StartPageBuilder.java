package de.iwes.widgets.overview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.ogema.tools.resource.util.ResourceUtils;

import de.iwes.widgets.api.extended.html.bricks.PageSnippet;
import de.iwes.widgets.api.extended.html.bricks.SampleImages;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.dynamics.TriggeredAction;
import de.iwes.widgets.api.widgets.dynamics.TriggeringAction;
import de.iwes.widgets.api.widgets.html.HtmlStyle;
import de.iwes.widgets.api.widgets.html.PageSnippetI;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.api.widgets.localisation.OgemaLocale;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.accordion.AccordionData;
import de.iwes.widgets.html.accordion.TemplateAccordion;
import de.iwes.widgets.html.alert.Alert;
import de.iwes.widgets.html.alert.AlertData;
import de.iwes.widgets.html.autocomplete.Autocomplete;
import de.iwes.widgets.html.buttonrow.ConfigButtonRow;
import de.iwes.widgets.html.calendar.datepicker.Datepicker;
import de.iwes.widgets.html.complextable.DynamicTable;
import de.iwes.widgets.html.complextable.DynamicTableData;
import de.iwes.widgets.html.complextable.RowTemplate;
import de.iwes.widgets.html.dragdropassign.Container;
import de.iwes.widgets.html.dragdropassign.DragDropAssign;
import de.iwes.widgets.html.dragdropassign.DragDropData;
import de.iwes.widgets.html.dragdropassign.Item;
import de.iwes.widgets.html.form.button.Button;
import de.iwes.widgets.html.form.button.ButtonData;
import de.iwes.widgets.html.form.checkbox.Checkbox;
import de.iwes.widgets.html.form.checkbox.SimpleCheckbox;
import de.iwes.widgets.html.form.dropdown.Dropdown;
import de.iwes.widgets.html.form.dropdown.DropdownOption;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.html.form.label.HeaderData;
import de.iwes.widgets.html.form.label.Label;
import de.iwes.widgets.html.form.slider.Slider;
import de.iwes.widgets.html.form.textfield.TextField;
import de.iwes.widgets.html.form.textfield.TextFieldType;
import de.iwes.widgets.html.html5.Flexbox;
import de.iwes.widgets.html.html5.Meter;
import de.iwes.widgets.html.html5.ProgressBar;
import de.iwes.widgets.html.html5.flexbox.JustifyContent;
import de.iwes.widgets.html.icon.Icon;
import de.iwes.widgets.html.icon.IconType;
import de.iwes.widgets.html.multiselect.Multiselect;
import de.iwes.widgets.html.plotflot.FlotDataSet;
import de.iwes.widgets.html.plotflot.PlotFlot;
import de.iwes.widgets.html.popup.Popup;
import de.iwes.widgets.html.textarea.TextArea;
import de.iwes.widgets.template.PageSnippetTemplate;

public class StartPageBuilder {

	public StartPageBuilder(final WidgetPage<?> page) {
		
		Header header = new Header(page, "header", "Widgets overview");
		header.addDefaultStyle(HeaderData.CENTERED);
		page.append(header).linebreak();
		
		// Button
		final AtomicLong lastSubmit = new AtomicLong();
		Button button = new Button(page, "button", "Click me") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onPOSTComplete(String data, OgemaHttpRequest req) {
				lastSubmit.set(System.currentTimeMillis());
			}
			
		};
		button.addDefaultStyle(ButtonData.BOOTSTRAP_BLUE);
		ConfigButtonRow configButtonRow = new ConfigButtonRow(page, "configButtonRow");
		
		Label buttonResponse = new Label(page, "buttonResponse", "") {

			private static final long serialVersionUID = 1L;

			public void onGET(OgemaHttpRequest req) {
				if (System.currentTimeMillis() - lastSubmit.get() < 5000) {
					setText("Thank you", req);
					setPollingInterval(6000, req);
				}
				else {
					setText("", req);
					setPollingInterval(0, req);
				}
				
			};
			
		};
		button.triggerAction(buttonResponse, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		StaticTable btnTable = new StaticTable(1, 2);
		btnTable.setContent(0, 0, button).setContent(0, 1, buttonResponse);
		
		// Label
		Label label = new Label(page, "label", "Some <i>random</i> text");
		// TextArea
		TextArea textArea = new TextArea(page, "textArea", "Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.");
		// TextField
		TextField textField = new TextField(page, "textField");
		textField.setDefaultPlaceholder("I am a placeholder");
		// Autocomplete
		Autocomplete autocomplete = new Autocomplete(page, "autocomplete");
		List<String> autocompleteOptions = new ArrayList<>();
		autocompleteOptions.addAll(Arrays.asList(new String[]{"Option1", "Option2"}));
		autocomplete.setDefaultAutocompleteOptions(autocompleteOptions);
		// Slider
		Slider slider = new Slider(page, "slider", 0, 100, 23);
		// Dropdown
		Dropdown dropdown = new Dropdown(page, "dropdown");
		List<DropdownOption> dropdownOptions = new ArrayList<>();
		dropdownOptions.add(new DropdownOption("option1", "Option1", true));
		dropdownOptions.add(new DropdownOption("option2", "Option2", false));
		dropdown.setDefaultOptions(dropdownOptions);
		// Multiselect
		Multiselect multiselect = new Multiselect(page, "multiselect");
		multiselect.setDefaultWidth("100%");
		List<DropdownOption> msOptions = new ArrayList<>();
		msOptions.add(new DropdownOption("option1", "Option1", true));
		msOptions.add(new DropdownOption("option2", "Option2", false));
		msOptions.add(new DropdownOption("option3", "Option3", true));
		msOptions.add(new DropdownOption("option4", "Option4", false));		
		multiselect.setDefaultOptions(msOptions);
		// Checkbox
		Checkbox checkbox = new SimpleCheckbox(page, "checkbox", "Select option"); // use Checkbox instead of SimpleCheckbox if you need more than one option
		// Alert
		Alert alert = new Alert(page, "alert", "This is an important message!");
		alert.addDefaultStyle(AlertData.BOOTSTRAP_INFO);
		
		// Popup
		final Popup popup = new Popup(page, "popup", true); // third argument: we make the popup global, it is the same for all user sessions
		popup.setDefaultTitle("A popup");
		popup.setDefaultHeaderHTML("Example of a popup menu");
		popup.setDefaultFooterHTML("Here goes the footer");
		PageSnippet popupBody = new PageSnippet(page, "popupBody",true); // use a PageSnippet as Popup body, then you can add arbitrary subwidgets to it 
		Label popupLabel1 = new Label(page, "popupLabel1", "Please enter a new id:");
		TextField popupField2 = new TextField(page, "popupField2");
		Label popupLabel3 = new Label(page, "popupLabel2", "Select an option");
		Dropdown popupDropdown4 = new Dropdown(page, "popupDropdown4");
		List<DropdownOption> ddOptions = new ArrayList<>();
		ddOptions.add(new DropdownOption("opt1", "A good selection", true));
		ddOptions.add(new DropdownOption("opt2", "An event better selection", false));
		ddOptions.add(new DropdownOption("opt3", "Some say this one is crap", false));
		popupDropdown4.setDefaultOptions(ddOptions);
		StaticTable popupTable = new StaticTable(2, 2);
		popupTable.setContent(0, 0, popupLabel1).setContent(0, 1, popupField2)
			.setContent(1, 0, popupLabel3).setContent(1, 1, popupDropdown4);
		popupBody.append(popupTable, null); // since the widget is global (see constructor), we can pass null as session identifier
		popup.setBody(popupBody, null); // since the widget is global (see constructor), we can pass null as session identifier
		
		Button popupTrigger = new Button(page, "popupTrigger", "Show Popup");
		popupTrigger.addDefaultStyle(ButtonData.BOOTSTRAP_GREEN);
		popupTrigger.triggerAction(popup, TriggeringAction.POST_REQUEST, TriggeredAction.SHOW_WIDGET);// Show the popup when the user clicks the button
		
		// Datepicker
		Datepicker datepicker = new Datepicker(page, "datepicker");
		// Meter
		Meter meter = new Meter(page, "meter");
		meter.setDefaultMax(100);
		meter.setDefaultHigh(80);
		meter.setDefaultLow(30);
		meter.setDefaultMin(0);
		meter.setDefaultValue(70);
		// ProgressBar
		ProgressBar progressBar = new ProgressBar(page, "progressBar") {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onGET(OgemaHttpRequest req) {
				float value = (getValue(req) + 10) % 100;
				setValue(value,req); // example: increase value by 10 in every GET request
			}
			
		};
		progressBar.setDefaultPollingInterval(5000); // poll every 5s
		progressBar.setDefaultMax(100);
		progressBar.setDefaultValue(40);
		// Icon; here we provide a method to change the selected icon, the icon widget itself is very simple if it only shows a fixed icon
		Button iconBtn = new Button(page, "iconBtn","Change icon");
		final Icon icon = new Icon(page, "icon") {
	
			private static final long serialVersionUID = 1L;
			private final IconType[] types = IconType.values();
			// ThreadLocal, so that each user sees their own copy of the counter
			private final ThreadLocal<Integer> counter = new ThreadLocal<Integer>()  {
			
				@Override
				protected Integer initialValue() {
					return 0;
				}
			};

			public void onGET(OgemaHttpRequest req) {
				int type = counter.get();
				if (type >= types.length)
					type = 0;
				counter.set(type+1);
				IconType newType = types[type];
				setIconType(newType, req);
			};
			
		};
		Label iconLabel = new Label(page, "iconLabel", "Selected icon") {

			private static final long serialVersionUID = 1L;
			
			@Override
			public void onGET(OgemaHttpRequest req) {
				String selected = icon.getIconType(req).toString(); 
				setText("Selected icon: " + selected, req);
			}
			
		};
		iconBtn.triggerAction(icon, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		// level 1: first trigger icon widget, then label, since the latter needs the new value of the former in its onGET method
		iconBtn.triggerAction(iconLabel, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST, 1); 
		StaticTable iconTab = new StaticTable(1, 3);
		iconTab.setContent(0, 0, iconLabel).setContent(0, 1, icon).setContent(0, 2, iconBtn);
		
		// Colorpicker
		final TextField colorpicker = new TextField(page, "colorpicker");
		colorpicker.setDefaultType(TextFieldType.COLOR);
		colorpicker.setDefaultValue("##00008B");
		Label colorLabel = new Label(page, "colorLabel","Selected color: ") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onGET(OgemaHttpRequest req) {
				String color = colorpicker.getValue(req);
				setText("Selected color: " + color, req);
				setColor(color, req);
			}
			
		};
		colorpicker.triggerAction(colorLabel, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		StaticTable colorTab = new StaticTable(1, 2);
		colorTab.setContent(0, 0, colorLabel).setContent(0, 1, colorpicker);
		
		// Flexbox
		Flexbox flexbox = new Flexbox(page, "flexbox"); // note: contrary to almost all other widgets, Flexbox' default constructor produces a global widget
		Button flexboxContent1 = new Button(page, "flexboxContent1", "I sit in a Flexbox");
		Label flexboxContent2 = new Label(page, "flexboxContent2","Me too") {

			private static final long serialVersionUID = 1L;
			
			private final ThreadLocal<Integer> counter = new ThreadLocal<Integer>() {
				@Override
				protected Integer initialValue() {
					return 0;
				}
			};
			
			@Override
			public void onGET(OgemaHttpRequest req) {
				int cnt = counter.get();
				counter.set(cnt+1);
				setText("Button pressed " + cnt + " times", req);
			}
			
		};
		flexboxContent1.triggerAction(flexboxContent2, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		flexbox.addItem(flexboxContent1, null).addItem(flexboxContent2, null);
		flexbox.setDefaultJustifyContent(JustifyContent.SPACE_AROUND);
		flexbox.setMargin(null, "10px", flexboxContent1,null);
		flexbox.setMargin(null, "10px", flexboxContent2,null);
		Map<String,String> css = new HashMap<>();
		css.put( "background-color", "#6699ff");
		flexbox.addCssItem(" .ogema-widget",css, null); // FIXME too complicated
		
		
		// DynamicTable
		final DynamicTable<String> dynamicTable = new DynamicTable<>(page, "dynamicTable", true); // global widget, each user sees the same rows
		RowTemplate<String> rowTemplate = new RowTemplate<String>() {

			@Override
			public Row addRow(String object, OgemaHttpRequest req) {
				Row row = new Row();
				String id = getLineId(object);
				Label col0 = new Label(page, "col0_" + id, "This is row " + id);
				row.addCell("col0", col0);
				final TextField col1 = new TextField(page, "col1_" + id);
				col1.setDefaultPlaceholder("Enter some text");
				row.addCell("col1", col1);
				Button col2 = new Button(page, "col2_" + id, "Click");
				col2.addDefaultStyle(ButtonData.BOOTSTRAP_LIGHT_BLUE);
				row.addCell("col2", col2);
				Label col3 = new Label(page, "col3_" + id) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onGET(OgemaHttpRequest req) {
						String text = col1.getValue(req);
						if (text == null || text.trim().isEmpty())
							text = "Nothing entered";
						setText(text, req);
					}
					
				};
				// press the button to update the content of the label
				col2.triggerAction(col3, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
				row.addCell("col3", col3);
				return row;
			}

			@Override
			public String getLineId(String object) {
				return ResourceUtils.getValidResourceName(object);
			}

			@Override
			public Map<String, Object> getHeader() {
				Map<String, Object> header = new LinkedHashMap<>();
				header.put("col0", "First column");
				header.put("col1", "Second column");
				header.put("col2", "Third column");
				header.put("col3", "Fourth column");
				return header;
			}
			
		};
		dynamicTable.setRowTemplate(rowTemplate);
		dynamicTable.setDefaultHeaderColor("#00008B");
		dynamicTable.setDefaultHeaderFontColor("#FFFAF0");
		dynamicTable.addDefaultStyle(DynamicTableData.BOLD_HEADER);
		Button tableTriggerButton = new Button(page, "tableTriggerButton", "Add a row") {

			private static final long serialVersionUID = 1L;
			private final AtomicInteger counter= new AtomicInteger(0);
			
			@Override
			public void onPrePOST(String data, OgemaHttpRequest req) {
				dynamicTable.addItem("line" + counter.getAndIncrement(), req);
			}
			
		};
		tableTriggerButton.addDefaultStyle(ButtonData.BOOTSTRAP_ORANGE);
		tableTriggerButton.triggerAction(dynamicTable, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		Button tableRemoveButton = new Button(page, "tableRemoveButton", "Remove a row") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onPrePOST(String data, OgemaHttpRequest req) {
				Set<String> rows  =dynamicTable.getRows(req);
				if (!rows.isEmpty()) {
					Iterator<String> it = rows.iterator();
					while(it.hasNext()) {
						String s = it.next();
						if (s.equals(DynamicTable.HEADER_ROW_ID))
							continue;
						dynamicTable.removeRow(s, req);
						break;
					}
				}
			}
			
		};
		tableRemoveButton.addDefaultStyle(ButtonData.BOOTSTRAP_RED);
		tableRemoveButton.triggerAction(dynamicTable, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		
		// Accordion; here we need a template for the accordion items
		PageSnippetTemplate<String> aaccordionTemplate = new PageSnippetTemplate<String>() {

			@Override
			public String getId(String object) {
				return ResourceUtils.getValidResourceName(object);
			}

			@Override
			public String getLabel(String object, OgemaLocale locale) {
				return object;
			}

			@Override
			public PageSnippetI getSnippet(String item, OgemaHttpRequest req) {
				String id = getId(item);
				PageSnippet accordionSnippet = new PageSnippet(page, "item_" + getId(id), true);
				Label a1 = new Label(page, "a1_" + id , "This is a label in an accordion");
				Label a2 = new Label(page, "a2_" + id, "Well, another one");
				Label a3 = new Label(page, "a3_" + id, "Slider label!");
				TextField b1 = new TextField(page, "b1_" + id);
				b1.setDefaultPlaceholder("Everyone likes TextFields!");
				Button b2 = new Button(page, "b2_" + id, "One more button");
				Slider b3 = new Slider(page, "b3_" +id, -7, 7, 5);
				StaticTable accordionTable = new StaticTable(3, 2, new int[] {2, 2});
				accordionTable.setContent(0, 0, a1).setContent(0, 1, b1).setContent(1, 0, a2).setContent(1, 1, b2).setContent(2, 0, a3).setContent(2, 1, b3);
				accordionSnippet.append(accordionTable, null);
				return accordionSnippet;
			}
			
		};
		
		TemplateAccordion<String> accordion = new TemplateAccordion<>(page, "accordion", true, aaccordionTemplate); // we make this one global too
		accordion.addDefaultStyle(AccordionData.BOOTSTRAP_GREEN);
		accordion.addItem("Item1", null); // global widget, so we can pass null as session identifier 
		accordion.addItem("Item2", null);

		// DragDropAssign
		List<Container> containers = new ArrayList<>();
		Container c0 = new Container("c0", "Container 1", SampleImages.BLUE_1); // these images are rather large for use as a container background...
		Container c1 = new Container("c1", "Container 2", SampleImages.GOLDEN_1);
		containers.add(c0); containers.add(c1);
		List<Item> items = new ArrayList<>();
		Item i0 = new Item(c0, "i0", "Item 0", "I am draggable", IconType.HELP_CONTENT.getBrowserPath());
		Item i1 = new Item(c1, "i1", "Item 1", "And me too", IconType.SETTINGS1.getBrowserPath());
		items.add(i0); items.add(i1);
		DragDropData ddData = new DragDropData(containers, items);
		DragDropAssign dragDropAssign = new DragDropAssign(page, "dragDropAssign", ddData, true) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onUpdate(Item item, Container from, Container to, OgemaHttpRequest req) {
				// do something...
			}
		};
		
		// Plot
		PlotFlot plotFlot = new PlotFlot(page, "plotFlot", true);
		JSONArray data1 = new JSONArray();
		JSONArray data2 =  new JSONArray();
		int nrPoints = 50;
		for (int i=0; i<nrPoints; i++) {
			JSONArray point = new JSONArray();
			point.put(i);
			point.put(Math.sin(2*Math.PI / nrPoints * i ));
			data1.put(point);
			JSONArray point2 = new JSONArray();
			point2.put(i);
			point2.put(Math.cos(2*Math.PI / nrPoints * i ));
			data2.put(point2);
		}
		FlotDataSet plotFlotDataSet1  = new FlotDataSet("sin", data1);
		FlotDataSet plotFlotDataSet2  = new FlotDataSet("cos", data2);
		plotFlot.getData(null).addRow(plotFlotDataSet1);
		plotFlot.getData(null).addRow(plotFlotDataSet2);
		
		
		// arrange all widgets in a static table with two columns, one for a label, one for the widget
		StaticTable table = new StaticTable(21, 2, new int[]{4,4});
		table.setContent(0, 0, "<b>Button</b>, also ButtonConfirm, RedirectButton").setContent(0, 1, btnTable)
			.setContent(1, 0, "<b>ConfigButtonRow</b>").setContent(1, 1, configButtonRow)
			.setContent(2, 0, "<b>Label</b>, also ResourceLabel, ValueResourceLabel, TimeResourceLabel, TimeIntervalLabel").setContent(2, 1, label)
			.setContent(3, 0, "<b>TextArea</b>").setContent(3, 1, textArea)
			.setContent(4, 0, "<b>TextField</b>, also NumberInputField, ResourceTextField, ValueResourceTextField, TimeResourceTextField").setContent(4, 1, textField)
			.setContent(5, 0, "<b>Autocomplete</b>, also ResourcePathAutocomplete").setContent(5, 1, autocomplete)
			.setContent(6, 0, "<b>Slider</b>").setContent(6, 1, slider)
			.setContent(7, 0, "<b>Dropdown</b>, also TemplateDropdown, ResourceDropdown, PatternDropdown, ContextPatternDropdown ResourceListDropdown, ResourceTypeDropdown")
				.setContent(7, 1, dropdown)
			.setContent(8, 0, "<b>Multiselect</b>, also TemplateMultiselect, ResourceMultiselect, PatternMultiselect, ContextPatternMultiselect").setContent(8, 1, multiselect)
			.setContent(9, 0, "<b>Checkbox</b>, also BooleanResourceCheckbox").setContent(9, 1, checkbox)
			.setContent(10, 0, "<b>Alert</b>").setContent(10, 1, alert)
			.setContent(11, 0, "<b>Popup</b>").setContent(11, 1, popupTrigger) // note: we also need to append the popup itself, this is done at the end of the page
			.setContent(12, 0, "<b>Datepicker</b>, also DatepickerTimeResource").setContent(12, 1, datepicker)
			.setContent(13, 0, "<b>Meter</b>").setContent(13, 1, meter)
			.setContent(14, 0, "<b>ProgressBar</b>").setContent(14, 1, progressBar)
			.setContent(15, 0, "<b>Icon</b>").setContent(15, 1, iconTab)
			.setContent(16, 0, "<b>Colorpicker</b> (TextField with type Color), also ResourceColorpicker").setContent(16, 1, colorTab)
			.setContent(17, 0, "<b>Flexbox</b>").setContent(17, 1, flexbox)
			.setContent(18, 0, "<b>Accordion</b>, also TemplateAccordion, PatternAccordion").setContent(18, 1, accordion)
			.setContent(19, 0, "<b>DragropAssign</b>, also PatternDragDropAssign").setContent(19, 1, dragDropAssign)
			.setContent(20, 0, "<b>PlotFlot</b> (experimental), also ResourcePlotFlot, SchedulePlotFlot").setContent(20, 1, plotFlot);
			
		page.append(table).linebreak();
			
		Header tableHeader = new Header(page, "tableHeader", "DynamicTable with variable number of rows");
		tableHeader.addDefaultStyle(HeaderData.CENTERED);
		tableHeader.setDefaultHeaderType(2);
		StaticTable buttonTable = new StaticTable(1, 2, new int[] {1,1}); 
		buttonTable.addStyle(HtmlStyle.ALIGNED_LEFT); // FIXME not working
		buttonTable.setContent(0, 0, tableTriggerButton).setContent(0, 1, tableRemoveButton);
		page.append(tableHeader).linebreak().append(buttonTable).linebreak().append(dynamicTable).linebreak();
		
		page.append(popup);
	}
	
}
