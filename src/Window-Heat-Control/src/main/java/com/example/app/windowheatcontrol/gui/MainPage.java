package com.example.app.windowheatcontrol.gui;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.model.locations.Room;
import org.ogema.tools.resource.util.ResourceUtils;

import com.example.app.windowheatcontrol.WindowHeatControlController;
import com.example.app.windowheatcontrol.api.internal.RoomController;
import com.example.app.windowheatcontrol.pattern.ElectricityStoragePattern;

import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.dynamics.TriggeredAction;
import de.iwes.widgets.api.widgets.dynamics.TriggeringAction;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.alert.Alert;
import de.iwes.widgets.html.complextable.DynamicTable;
import de.iwes.widgets.html.complextable.RowTemplate;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.html.form.label.HeaderData;
import de.iwes.widgets.html.form.label.Label;
import de.iwes.widgets.html.form.textfield.ValueInputField;
import de.iwes.widgets.object.widget.table.DefaultObjectRowTemplate;
import de.iwes.widgets.resource.widget.label.ValueResourceLabel;
import de.iwes.widgets.resource.widget.textfield.ValueResourceTextField;

/**
 * An HTML page, generated from the Java code.
 */
public class MainPage {
	
	public final long UPDATE_RATE = 5*1000;
	private final WidgetPage<?> page; 
	
	// the widgets, or building blocks, of the page; defined in the constructor
	private final Header header;
	private final ValueResourceLabel<FloatResource> batterySOC;
	private final ValueResourceTextField<TemperatureResource> defaultWindowOpenTemp;
	private final Header roomsHeader;
	private final DynamicTable<Room> roomTable;
	private final Alert alert;
	
	public MainPage(final WidgetPage<?> page, final WindowHeatControlController appController) {
		this.page = page;

		//init all widgets
		
		header = new Header(page, "header", "Battery-extended Window-Heat Control");
		header.addDefaultStyle(HeaderData.CENTERED);
		
		// displays messages to the user
		alert = new Alert(page, "alert", "");
		alert.setDefaultVisibility(false);

		// this widget simply displays the value of a resource, here the state of charge of the battery
		batterySOC = new ValueResourceLabel<FloatResource>(page, "batterySOC") {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onGET(OgemaHttpRequest req) {
				final ElectricityStoragePattern activeBattery = appController.batteryListener.getActiveBattery();
				if(activeBattery != null)
					selectItem(activeBattery.soc, req);
				else
					selectItem(null, req);
			}
		};

		// displays the value of a resource, and allows to change the value. The resource is completely static, 
		// hence there is no need to overwrite #onGET(), as for the batterySOC-widget above.
		defaultWindowOpenTemp = new ValueResourceTextField<TemperatureResource>(page, "defaultWindowOpenTemp",
				appController.getDefaultWindowOpenTemperatureSetting());
		
		this.roomsHeader = new Header(page, "roomsheader", "Controlled rooms");
		roomsHeader.addDefaultStyle(HeaderData.CENTERED);
		roomsHeader.setDefaultHeaderType(2);
		
		// the roomTable widget below shows one table row per room; here we define the row content
		RowTemplate<Room> roomTemplate = new DefaultObjectRowTemplate<Room>() {
			
			@Override
			public Map<String, Object> getHeader() {
				final Map<String,Object> header = new LinkedHashMap<>();
				// keys must be chosen in agreement with cells added in addRow method below
				header.put("roomname", "Room name");
				header.put("temperaturesetpoint", "Active temperature setpoint");
				header.put("windowopensetpoint", "Window open setpoint");
				header.put("nrWindowSensors", "Window sensors");
				header.put("nrThermostats", "Thermostats");
				header.put("windowstatus", "Window open");
				return header;
			}
			
			@Override
			public Row addRow(final Room room, final OgemaHttpRequest req) {
				final Row row = new Row();
				final String lineId = getLineId(room);
				// this widget displays the name of the room; since the content cannot change, we
				// simply set a default text in the constructor, and do not overwrite the onGET method
				Label name = new Label(page, "name_"+lineId, ResourceUtils.getHumanReadableName(room));
				// set first column content
				row.addCell("roomname",name);
				
				// this widget displays the current temperature setpoint for the room (onGET), and allows the user to change it (onPOST)
				final RoomController controller = appController.getController(room);
				ValueInputField<Float> setpoint = new ValueInputField<Float>(page, "setpoint_" + lineId, Float.TYPE) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onPOSTComplete(String data, OgemaHttpRequest req) {
						Float value = getNumericalValue(req);
						if (value == null) {
							alert.showAlert("Please enter a valid temperature", false, req);
							return;
						}
						try {
							controller.setCurrentTemperatureSetpoint(value);
							alert.showAlert("New temperature setpoint for room " + ResourceUtils.getHumanReadableName(room) + ": " + value + "°C", true, req);
						} catch (IllegalArgumentException e) {
							alert.showAlert(e.getMessage(), false, req);
						}
					}
					
					@Override
					public void onGET(OgemaHttpRequest req) {
						float temp = controller.getCurrentTemperatureSetpoint();
						setNumericalValue(temp, req);
					}
					
				};
				setpoint.setDefaultUnit("°C");
				setpoint.setDefaultPollingInterval(UPDATE_RATE);
				row.addCell("temperaturesetpoint",setpoint);
				//we have to make sure the value shown in the client is updated according to what the server accepted
				setpoint.registerDependentWidget(setpoint);
				// in the onPOSTComplete method of setpoint, we set a message to be displayed by alert, hence we need to reload the alert after the POST
				setpoint.registerDependentWidget(alert);
				
				// this widget displays the configured temperature setpoint for window open status in this room 
				ValueInputField<Float> windowOpenSetpoint = new ValueInputField<Float>(page, "windowOpenSetpoint_" + lineId, Float.TYPE) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onPOSTComplete(String data, OgemaHttpRequest req) {
						Float value = getNumericalValue(req);
						if (value == null) {
							alert.showAlert("Please enter a valid temperature", false, req);
							return;
						}
						try {
							controller.setWindowOpenTemperatureSetpoint(value);
							alert.showAlert("New window open temperature setpoint for room " + ResourceUtils.getHumanReadableName(room) + ": " + value + "°C", true, req);
						} catch (IllegalArgumentException e) {
							alert.showAlert(e.getMessage(), false, req);
						}
					}
					
					@Override
					public void onGET(OgemaHttpRequest req) {
						float temp = controller.getWindowOpenTemperatureSetpoint();
						setNumericalValue(temp, req);
					}
					
				};
				windowOpenSetpoint.setDefaultUnit("°C");
				row.addCell("windowopensetpoint",windowOpenSetpoint);
				windowOpenSetpoint.triggerAction(windowOpenSetpoint, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
				// in the onPOSTComplete method of windowOpenSetpoint, we set a message to be displayed by alert, hence we need to reload the alert after the POST
				windowOpenSetpoint.triggerAction(alert, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
				
				Label nrWindowSensors = new Label(page, "nrWindowSensors_" + lineId) {

					private static final long serialVersionUID = 1L;
					
					@Override
					public void onGET(OgemaHttpRequest req) {
						setText(String.valueOf(controller.windowSensorCount()), req);
					}
					
				};
				row.addCell("nrWindowSensors", nrWindowSensors);
				
				Label nrThermostats = new Label(page, "nrThermostats_" + lineId) {

					private static final long serialVersionUID = 1L;
					
					@Override
					public void onGET(OgemaHttpRequest req) {
						setText(String.valueOf(controller.thermostatCount()), req);
					}
					
				};
				row.addCell("nrThermostats", nrThermostats);
				
				Label openStatus = new Label(page, "openStatus_" + lineId) {
					
					private static final long serialVersionUID = 1L;
					
					@Override
					public void onGET(OgemaHttpRequest req) {
						setText(String.valueOf(controller.isWindowOpen()), req);
					}
					
				};
				
				openStatus.setDefaultPollingInterval(UPDATE_RATE);
				row.addCell("windowstatus", openStatus);
				return row;
			}
		};
		
		// the table shows the same rows, independently of the user session, hence it can be made global
		roomTable = new DynamicTable<Room>(page, "roomTable", true) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onGET(OgemaHttpRequest req) {
				//find all managed rooms
				updateRows(appController.getActiveRooms(), req);
			}
		};
		roomTable.setRowTemplate(roomTemplate);
		buildPage();
		setDependencies();
	}
	
	private final void buildPage() {
		page.append(header).linebreak().append(alert).linebreak();
		StaticTable table1 = new StaticTable(2, 2, new int[] {3,3}); 
		page.append(table1);
		table1.setContent(0, 0, "Battery SOC:").setContent(0, 1, batterySOC)
			.setContent(1, 0, "Default window open temperature").setContent(1, 1, defaultWindowOpenTemp);
		
		page.linebreak().append(roomsHeader).append(roomTable);
	}
	
	// the page is rather static, no dependencies to be set here
	private final void setDependencies() {
	}
	
}
