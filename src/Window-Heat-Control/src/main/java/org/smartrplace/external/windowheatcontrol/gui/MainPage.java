package org.smartrplace.external.windowheatcontrol.gui;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.model.locations.Room;
import org.ogema.tools.resource.util.ResourceUtils;
import org.smartrplace.external.windowheatcontrol.WindowHeatControlController;
import org.smartrplace.external.windowheatcontrol.pattern.DoorWindowSensorPattern;
import org.smartrplace.external.windowheatcontrol.pattern.GenericThermostatPattern;

import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.complextable.DynamicTable;
import de.iwes.widgets.html.complextable.RowTemplate;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.html.form.label.HeaderData;
import de.iwes.widgets.html.form.label.Label;
import de.iwes.widgets.object.widget.table.DefaultObjectRowTemplate;
import de.iwes.widgets.resource.widget.label.ValueResourceLabel;
import de.iwes.widgets.resource.widget.textfield.ResourceTextField;
import de.iwes.widgets.resource.widget.textfield.ValueResourceTextField;

/**
 * An HTML page, generated from the Java code.
 */
public class MainPage {
	
	public final long UPDATE_RATE = 5*1000;
	private final WidgetPage<?> page; 
	
	final ValueResourceLabel<FloatResource> batterySOC;
	final DynamicTable<Room> roomTable;
	//final ResourceDropdown<Room> dropDetailRoom;
	//final DynamicTable<DoorWindowSensorPattern> windowSensorDetails;
	//final DynamicTable<GenericThermostatPattern> thermostatDetails;
	final ApplicationManager appMan;
	
	public MainPage(final WidgetPage<?> page, final WindowHeatControlController app) {
		this.page = page;
		this.appMan = app.appMan;

		Header header = new Header(page, "header", "Battery-extended Window-Heat Control");
		header.addDefaultStyle(HeaderData.CENTERED);

		//init all widgets
		batterySOC = new ValueResourceLabel<FloatResource>(page, "batterySOC") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onGET(OgemaHttpRequest req) {
				selectItem(app.batteryToUse.soc, req);
			}
		};
		
		RowTemplate<Room> roomTemplate = new DefaultObjectRowTemplate<Room>() {
			@Override
			public Row addRow(final Room listElement, OgemaHttpRequest req) {
				Row row = new Row();
				String lineId = getLineId(listElement);
				Label name = new Label(page, "name_"+lineId) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onGET(OgemaHttpRequest req) {
						setText(ResourceUtils.getHumanReadableName(listElement), req);
					}
				};
				row.addCell(name, 2);
				ValueResourceTextField<TemperatureResource> setpoint =
						new ValueResourceTextField<TemperatureResource>(page, "setPoint_"+lineId) {
					private static final long serialVersionUID = 1L;
					@Override
					public void onGET(OgemaHttpRequest req) {
						GenericThermostatPattern thPattern =
								app.thermostatRoomMmgt.getFirstElement(listElement);
						if(thPattern != null) {
							selectItem(thPattern.setpoint, req);
						} else {
							selectItem(null, req);
						}
					}
				};
				setpoint.setDefaultPollingInterval(UPDATE_RATE);
				row.addCell(setpoint, 2);
				row.addCell("Window open status:", 2);
				ValueResourceLabel<BooleanResource> openStatus =
						new ValueResourceLabel<BooleanResource>(page, "openStatus_"+lineId) {
					private static final long serialVersionUID = 1L;
					@Override
					public void onGET(OgemaHttpRequest req) {
						DoorWindowSensorPattern winPattern =
								app.windowRoomMmgt.getFirstElement(listElement);
						if(winPattern != null) {
							selectItem(winPattern.open, req);
						} else {
							selectItem(null, req);
						}
					}
				};
				openStatus.setDefaultPollingInterval(UPDATE_RATE);
				row.addCell(openStatus, 2);
				return row;
			}
		};
		
		roomTable = new DynamicTable<Room>(page, "roomTable", true) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onGET(OgemaHttpRequest req) {
				//find all managed rooms
				List<String> thList = app.thermostatRoomMmgt.getLinkingResourceList();
				List<String> sensList = app.windowRoomMmgt.getLinkingResourceList();
				thList.retainAll(sensList);
				List<Room> roomList = new ArrayList<>();
				for(String s: thList) {
					roomList.add((Room) appMan.getResourceAccess().getResource(s));
				}
				updateRows(roomList, req);
			}
		};
		roomTable.setRowTemplate(roomTemplate);
		
		page.append(header);
    	//dropDetailRoom.registerDependentWidget(windowSensorDetails);
    	//dropDetailRoom.registerDependentWidget(thermostatDetails);
		StaticTable table1 = new StaticTable(1, 2);
		page.append(table1);
		table1.setContent(0, 0, "Battery SOC:").setContent(0, 1, batterySOC);
		page.append(roomTable);
		//page.append(dropDetailRoom);
		StaticTable table2 = new StaticTable(1, 2);
		page.append(table2);
		//table2.setContent(0, 0, windowSensorDetails).setContent(0, 1, thermostatDetails);
	}
	
	public WidgetPage<?> getPage() {
		return page;
	}
}
