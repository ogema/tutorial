package com.example.app.windowheatcontrol.patternlistener;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.model.locations.Room;

import com.example.app.windowheatcontrol.api.internal.RoomManagement;
import com.example.app.windowheatcontrol.pattern.WindowSensorPattern;

import de.iwes.util.linkingresource.LinkingResourceManagement;

/**
 * Keeps track of window sensors, and informs the appropriate RoomController about new and disappearing sensors. 
 */
public class WindowSensorListener implements PatternListener<WindowSensorPattern> {

	// keeps a map <Room, List<DoorWindowSensorPattern>>
	private final LinkingResourceManagement<Room, WindowSensorPattern> windowSensors;
	private final RoomManagement rooms;
	private final ApplicationManager appMan;
	// only for development
	//private final Map<DoorWindowSensor, ScheduleViewerConfig> viewerConfigs = new HashMap<>();
	
	public WindowSensorListener(LinkingResourceManagement<Room, WindowSensorPattern> windowSensors, 
							            RoomManagement rooms,
							            ApplicationManager appMan) {
		this.windowSensors = windowSensors;
		this.rooms = rooms;
		this.appMan = appMan;
	}
	
	@Override
	public void patternAvailable(WindowSensorPattern sensor) {
		final Room room = sensor.room;
		windowSensors.addElement(sensor, room);
		rooms.getController(room).addWindowSensor(sensor);
		
		// value logging
		// development version -> creates a schedule viewer configuration, which would be considered as resource spam in a deployed app
		// TODO: remove for deployment version
		//ScheduleViewerConfig viewerConfig = LogHelper.addResourceToRoomLog(sensor.open, "WindowSensor", null, appMan);
		//viewerConfigs.put(sensor.model, viewerConfig);
	}
	
	@Override
	public void patternUnavailable(WindowSensorPattern sensor) {
		final Room room = windowSensors.removeElement(sensor);
		if (room != null)
			rooms.getController(room).removeWindowSensor(sensor);
		
		// value logging
		// TODO: remove for deployment version
		//ScheduleViewerConfig config = viewerConfigs.remove(sensor.model);
		//if (config != null)
		//	config.delete();
	}
	

}
