package com.example.app.windowheatcontrol.patternlistener;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.model.locations.Room;

import com.example.app.windowheatcontrol.api.internal.RoomManagement;
import com.example.app.windowheatcontrol.pattern.ThermostatPattern;

import de.iwes.util.linkingresource.LinkingResourceManagement;

/**
 * Keeps track of thermostats, and informs the appropriate RoomController about new and disappearing devices. 
 */
public class ThermostatListener implements PatternListener<ThermostatPattern> {
	
	// keeps a map <Room, List<ThermostatPattern>>
	private final LinkingResourceManagement<Room, ThermostatPattern> thermostats;
	private final RoomManagement rooms;
	private final ApplicationManager appMan;
	//private final Map<Thermostat, ScheduleViewerConfig[]> viewerConfigs = new HashMap<>();

	public ThermostatListener(LinkingResourceManagement<Room, ThermostatPattern> thermostats, 
			                    RoomManagement rooms,
			                    ApplicationManager appMan) {
		this.thermostats = thermostats;
		this.rooms = rooms;
		this.appMan = appMan;
	}
	
	@Override
	public void patternAvailable(ThermostatPattern thermostat) {
		final Room room = thermostat.room;
		thermostats.addElement(thermostat, room);
		rooms.getController(room).settingsChanged();
		
		// value logging
		// development version -> creates a schedule viewer configuration, which would be considered as resource spam in a deployed app
		// TODO: remove for deployment version
		//ScheduleViewerConfig viewerConfig1 = LogHelper.addResourceToRoomLog(thermostat.setpoint, "ThermostatSetpoint", null, appMan);
		//ScheduleViewerConfig viewerConfig2 = LogHelper.addResourceToRoomLog(thermostat.reading, "ThermostatTemperatureMeasurement", null, appMan);
		//viewerConfigs.put(thermostat.model, new ScheduleViewerConfig[] {viewerConfig1, viewerConfig2});
	}
	
	@Override
	public void patternUnavailable(ThermostatPattern thermostat) {
		// here we need to determine the previously assigned room; the current location of
		// thermostat.room may have changed (which might even have caused this callback)
		final Room room = thermostats.removeElement(thermostat);
		rooms.getController(room).settingsChanged();
		
		// value logging  
		// TODO: remove for deployment version
		//ScheduleViewerConfig[] viewerConfig = viewerConfigs.remove(thermostat.model);
		//if (viewerConfig != null) {
		//	for (ScheduleViewerConfig config: viewerConfig) {
		//		config.delete();
		//	}
		//}
	}

}
