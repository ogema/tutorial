package com.example.app.sampledynamictable.gui;

import java.util.LinkedHashMap;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.actors.Actor;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.Sensor;
import org.ogema.tools.resource.util.ResourceUtils;

import de.iwes.util.linkingresource.RoomHelper;
import de.iwes.widgets.api.extended.WidgetData;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.dynamics.TriggeredAction;
import de.iwes.widgets.api.widgets.dynamics.TriggeringAction;
import de.iwes.widgets.api.widgets.html.Linebreak;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.complextable.DynamicTable;
import de.iwes.widgets.html.complextable.DynamicTableData;
import de.iwes.widgets.html.complextable.RowTemplate;
import de.iwes.widgets.html.form.button.Button;
import de.iwes.widgets.html.form.label.Header;

/** A simple example for a dynamic table showing some information for each room on the OGEMA system.
 * An HTML page, generated from the Java code.
 */
public class MainPage {
	
	public final long UPDATE_RATE = 5*1000;
	private final WidgetPage<?> page; 
	private final ApplicationManager appMan;
	
	public MainPage(final WidgetPage<?> page, final ApplicationManager appMan) {
		this.page = page;
		this.appMan = appMan;
		initPage();

	}
	
	private void initPage() {
		
		if(appMan == null || page == null) {
			throw new ExceptionInInitializerError("Class "+this.getClass().getName() +"not initialized correctly");
		}
		
		Header header = new Header(page, "header", "Example for a Dynamic Table");
		header.addDefaultStyle(WidgetData.TEXT_ALIGNMENT_CENTERED);
		
		DynamicTable<Room> table = getDynamicTable();
		Button updateTableButton = getUpdateButton(table);
		page.append(header).append(Linebreak.getInstance());
		page.append(table).append(Linebreak.getInstance());
		page.append(updateTableButton);
		
		
	}

	public DynamicTable<Room> getDynamicTable() {
		DynamicTable<Room> table = new DynamicTable<Room>(page, "dynamicTable") {

			private static final long serialVersionUID = -7196007975249690185L;
			
			@Override
			public void onGET(OgemaHttpRequest req) {
				
				addStyle(DynamicTableData.BOLD_HEADER, req);
				addStyle(DynamicTableData.TABLE_STRIPED, req);				
				
				List<Room> rooms = appMan.getResourceAccess().getResources(Room.class);
				updateRows(rooms, req);
			}
			
		};
		table.setRowTemplate(new RowTemplate<Room>() {

			@Override
			public Row addRow(Room room, OgemaHttpRequest req) {
				Row row  = new Row();
				row.addCell("name", room.getName());
				row.addCell("type", RoomHelper.getRoomTypeString(room.type().getValue()));
				row.addCell("active", room.isActive());
				row.addCell("device", getDeviceInfos(room));
				return row;
			}

			@Override
			public String getLineId(Room room) {
				return room.getPath();
			}

			@Override
			public LinkedHashMap<String, Object> getHeader() {
				final LinkedHashMap<String, Object> header = new LinkedHashMap<>();
				header.put("name", "Name");
				header.put("type", "Type");
				header.put("active", "Active");
				header.put("device", "Associated devices");
				return header;
			}
		});
		table.setDefaultHeaderColor("428bca;");
		return table;
	}
	
	private Button getUpdateButton(final DynamicTable<Room> table) {
		Button button = new Button(page, "updateDynamicTableButton", "Update Table");
		button.triggerAction(table, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		return button;
	}
	

	
	private String getDeviceInfos(Room room) {
		
		List<Actor> actors = ResourceUtils.getDevicesFromRoom(appMan.getResourceAccess(), Actor.class, room); 
		List<Sensor> sensors = ResourceUtils.getDevicesFromRoom(appMan.getResourceAccess(), Sensor.class, room); 
		StringBuilder sb = new StringBuilder("room associated with ");		
		sb.append(sensors.size());
		
		if(sensors.isEmpty() == false) {
			sb.append(" Sensors: ");
		}else {
			sb.append(" Sensors");
		}
		
		for(Sensor sensor : sensors) {
			sb.append(sensor.getName());
			if(sensor.reading() instanceof FloatResource) {
				sb.append("(").append(((FloatResource)sensor.reading()).getValue()).append(")");
		
			}
			sb.append(", ");
		}		
		
		sb.append(" and ").append(actors.size()).append(" Actor");
		if(actors.isEmpty() == false) {
			sb.append("s: ");
		}
		for(Actor actor : actors) {
			
			sb.append(actor.getName());
			if(actor.stateFeedback() instanceof FloatResource) {
				sb.append("(").append(((FloatResource)actor.stateFeedback()).getValue()).append(")");
			}
			sb.append(", ");
		}		
		
		return sb.toString();
	}
	
	public WidgetPage<?> getPage() {
		return page;
	}
}
