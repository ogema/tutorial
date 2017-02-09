package com.example.snippet.ogema.gui;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tools.resource.util.ResourceUtils;

import de.iwes.tools.collect.standardtypes.LocationUtils;
import de.iwes.widgets.api.extended.mode.UpdateMode;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.html.form.label.HeaderData;
import de.iwes.widgets.html.form.label.Label;
import de.iwes.widgets.resource.widget.dropdown.ResourceDropdown;
import de.iwes.widgets.resource.widget.label.ResourceLabel;
import de.iwes.widgets.resource.widget.textfield.ValueResourceTextField;

/**
 * An HTML page, generated from the Java code.
 */
public class MainPage {
	
	public final long UPDATE_RATE = 5*1000;
	private final WidgetPage<?> page; 
	
	public MainPage(final WidgetPage<?> page, final ApplicationManager appMan) {
		this.page = page;

		Header header = new Header(page, "header", "Template Page");
		header.addDefaultStyle(HeaderData.CENTERED);

		//init all widgets
		final ValueResourceTextField<StringResource> editDestinationTemp = new  ValueResourceTextField<>(page, "editDestinationTemp");
		final Label roomName = new Label(page, "roomName");
		final ResourceLabel<TemperatureResource> currentTemperature = new ResourceLabel<>(page, "currentTemperature");
		currentTemperature.setDefaultPollingInterval(UPDATE_RATE);
		
		final ResourceDropdown<TemperatureSensor> dropProgram =
    			new ResourceDropdown<TemperatureSensor>(page, "dropProgram", false, TemperatureSensor.class,
    					UpdateMode.AUTO_ON_GET, appMan.getResourceAccess()) {
 			private static final long serialVersionUID = 8696145677385119466L;

 			@Override
    		public void updateDependentWidgets(OgemaHttpRequest req) {
 				TemperatureSensor c = getSelectedItem(req);
 	     		if((c == null) || (!c.exists())) {
 	 				editDestinationTemp.selectItem(null, req);
 	 	     		roomName.setText("No device selected", req);
 	 				currentTemperature.selectItem(null, req);
 				} else {
 					if(!c.name().exists()) {
 						editDestinationTemp.selectItem(null, req);
 					} else {
 						editDestinationTemp.selectItem(c.name(), req);
 					}
 					roomName.setText("Room name:"+ResourceUtils.getHumanReadableName(ResourceUtils.getDeviceRoom(c)), req);
 	 				currentTemperature.selectItem(c.reading(), req);
 				}
    		}
    	};
		page.append(header);
    	dropProgram.registerDependentWidget(editDestinationTemp);
		page.append(dropProgram);
		StaticTable table1 = new StaticTable(2, 2);
		page.append(table1);
		table1.setContent(0, 0, roomName);
		table1.setContent(0, 1, editDestinationTemp);
		table1.setContent(1, 0, "Measurement value :");
		table1.setContent(1, 1, currentTemperature);
	}
	
	public WidgetPage<?> getPage() {
		return page;
	}
}
