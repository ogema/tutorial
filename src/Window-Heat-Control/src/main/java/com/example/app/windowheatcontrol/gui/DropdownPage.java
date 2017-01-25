package com.example.app.windowheatcontrol.gui;

import org.ogema.model.locations.Room;
import org.ogema.tools.resource.util.ResourceUtils;

import com.example.app.windowheatcontrol.WindowHeatControlController;
import com.example.app.windowheatcontrol.api.internal.RoomController;

import de.iwes.util.collectionother.IntegerEnumHelper;
import de.iwes.widgets.api.extended.resource.DefaultResourceTemplate;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.api.widgets.localisation.OgemaLocale;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.alert.Alert;
import de.iwes.widgets.html.form.dropdown.TemplateDropdown;
import de.iwes.widgets.html.form.textfield.ValueInputField;
import de.iwes.widgets.resource.widget.label.ResourceLabel;
import de.iwes.widgets.template.DisplayTemplate;

/**
 * An HTML page, generated from the Java code.
 */
public class DropdownPage {
	
	public final long UPDATE_RATE = 5*1000;
	private final WidgetPage<?> page; 
	
	// the widgets, or building blocks, of the page; defined in the constructor
	private final TemplateDropdown<Room> roomDrop;
	private final ResourceLabel<Room> name;
	private final ValueInputField<Float> setpoint;
	private final Alert alert;
	
	public DropdownPage(final WidgetPage<?> page, final WindowHeatControlController appController) {
		this.page = page;

		//init all widgets
		
		// displays messages to the user
		alert = new Alert(page, "alert", "");
		alert.setDefaultVisibility(false);

		DisplayTemplate<Room> dropDownTemplate = new DefaultResourceTemplate<Room>() {
			@Override
			public String getLabel(Room room, OgemaLocale locale) {
				if(room.type().isActive())
					return ResourceUtils.getHumanReadableName(room)+" Type:"+IntegerEnumHelper.getRoomTypeString(room.type().getValue());
				else return super.getLabel(room, locale);
			}
		};
		roomDrop = new TemplateDropdown<Room>(page, "roomDrop") {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onGET(OgemaHttpRequest req) {
				//find all managed rooms
				update(appController.getActiveRooms(), req);
			}
			@Override
			public void updateDependentWidgets(OgemaHttpRequest req) {
				Room room = getSelectedItem(req);
				name.selectItem(room, req);
			}
		};
		roomDrop.setTemplate(dropDownTemplate);
		
		// this widget displays the name of the room; since the content cannot change, we
		name = new ResourceLabel<Room>(page, "name");
		
		// this widget displays the current temperature setpoint for the room (onGET), and allows the user to change it (onPOST)
		setpoint = new ValueInputField<Float>(page, "setpoint", Float.TYPE) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onPOSTComplete(String data, OgemaHttpRequest req) {
				Room room = roomDrop.getSelectedItem(req);
				final RoomController controller = appController.getController(room);
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
				Room room = roomDrop.getSelectedItem(req);
				final RoomController controller = appController.getController(room);
				float temp = controller.getCurrentTemperatureSetpoint();
				setNumericalValue(temp, req);
			}
			
		};
		setpoint.setDefaultUnit("°C");
		setpoint.setDefaultPollingInterval(UPDATE_RATE);
		
		buildPage();
		setDependencies();
	}
	
	private final void buildPage() {
		StaticTable table1 = new StaticTable(2, 2, new int[] {3,3}); 
		page.append(table1);
		table1.setContent(0, 0, "Choose room:").setContent(0, 1, roomDrop)
			.setContent(1, 0, "Room name").setContent(1, 1, name)
			.setContent(2, 0, "temperaturesetpoint").setContent(2, 1, setpoint);
	}
	
	// the page is rather static, no dependencies to be set here
	private final void setDependencies() {
		//we have to make sure the value shown in the client is updated according to what the server accepted
		setpoint.registerDependentWidget(setpoint);
		// in the onPOSTComplete method of setpoint, we set a message to be displayed by alert, hence we need to reload the alert after the POST
		setpoint.registerDependentWidget(alert);
		roomDrop.registerDependentWidget(name);
		roomDrop.registerDependentWidget(setpoint);
	}
	
}
