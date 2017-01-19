package com.example.app.windowheatcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.model.locations.Room;

import com.example.app.windowheatcontrol.api.internal.RoomController;
import com.example.app.windowheatcontrol.api.internal.RoomManagement;
import com.example.app.windowheatcontrol.config.RoomConfig;
import com.example.app.windowheatcontrol.config.WindowHeatControlConfig;
import com.example.app.windowheatcontrol.pattern.ElectricityStoragePattern;
import com.example.app.windowheatcontrol.pattern.ThermostatPattern;
import com.example.app.windowheatcontrol.pattern.WindowSensorPattern;
import com.example.app.windowheatcontrol.patternlistener.ElectricityStorageListener;
import com.example.app.windowheatcontrol.patternlistener.ThermostatListener;
import com.example.app.windowheatcontrol.patternlistener.WindowSensorListener;

import de.iwes.util.linkingresource.LinkingResourceManagement;

// here the controller logic is implemented
public class WindowHeatControlController implements RoomManagement {

	protected static final float WINDOW_OPEN_TEMPERATURE = 12.0f;
	private final ApplicationManager appMan;
	private final ResourcePatternAccess patternAccess;
	private final OgemaLogger log;
	
	private final WindowHeatControlConfig appConfigData;
	// keeps track of all rooms in the system which are equipped with at least one window sensor and thermostat, each.
	// we access this from the GUI, hence we better use concurrent hash map (alternatively, synchronize access to roomControllers)
	private final Map<Room, RoomController> roomControllers = new ConcurrentHashMap<>();
	// keeps track of window sensor -> room associations
	private final LinkingResourceManagement<Room, WindowSensorPattern> windowSensors;
	// keeps track of thermostat -> room associations
	private final LinkingResourceManagement<Room, ThermostatPattern> thermostats;
	final ElectricityStorageListener electricityStorageListener;
	private final ThermostatListener thermostatListener;
	private final WindowSensorListener windowSensorListener;
	
    public WindowHeatControlController(final ApplicationManager appMan) {
		this.appMan = appMan;
		this.log = appMan.getLogger();
		this.patternAccess = appMan.getResourcePatternAccess();
		
        this.appConfigData = initConfigurationResource();
        this.electricityStorageListener = new ElectricityStorageListener();
        this.windowSensors = new LinkingResourceManagement<>();
        this.windowSensorListener = new WindowSensorListener(windowSensors, this, appMan);
        this.thermostats = new LinkingResourceManagement<>();
        this.thermostatListener = new ThermostatListener(thermostats, this, appMan);
        initDemands();
	}
    
    
    private final void initDemands() {
    	 patternAccess.addPatternDemand(ElectricityStoragePattern.class, electricityStorageListener, AccessPriority.PRIO_LOWEST);
    	 patternAccess.addPatternDemand(ThermostatPattern.class, thermostatListener, AccessPriority.PRIO_LOWEST);
    	 patternAccess.addPatternDemand(WindowSensorPattern.class, windowSensorListener, AccessPriority.PRIO_LOWEST);
    }
    
	public void close() {
		patternAccess.removePatternDemand(ElectricityStoragePattern.class, electricityStorageListener);
	   	patternAccess.removePatternDemand(ThermostatPattern.class, thermostatListener);
	   	patternAccess.removePatternDemand(WindowSensorPattern.class, windowSensorListener);
		for (RoomController controller: roomControllers.values()) {
			controller.stop();
		}
    }
    
    /*
     * This app uses a central configuration resource, which is accessed here
     */
    private final WindowHeatControlConfig initConfigurationResource() {
		String configResourceDefaultName = WindowHeatControlConfig.class.getSimpleName().substring(0, 1).toLowerCase()+WindowHeatControlConfig.class.getSimpleName().substring(1);
		WindowHeatControlConfig appConfigData = appMan.getResourceAccess().getResource(configResourceDefaultName);
		if (appConfigData != null) { // resource already exists (appears in case of non-clean start)
			appMan.getLogger().debug("{} started with previously-existing config resource", getClass().getName());
		}
		else {
			appConfigData = appMan.getResourceManagement().createResource(configResourceDefaultName, WindowHeatControlConfig.class);
			appConfigData.defaultWindowOpenTemperature().<TemperatureResource> create().setCelsius(WINDOW_OPEN_TEMPERATURE);
			appConfigData.roomConfigurations().create();
			appConfigData.activate(true);
			appMan.getLogger().debug("{} started with new config resource", getClass().getName());
		}
		return appConfigData;
    }
    
    @Override
    public List<Room> getActiveRooms() {
    	final List<Room> list = new ArrayList<>();
    	for (RoomController c : roomControllers.values()) {
    		if (c.isActive())
    			list.add(c.getRoom());
    	}
    	return list;
    }
	
	@Override
    public RoomController getController(Room room) {
    	room = room.getLocationResource();
    	RoomController controller = roomControllers.get(room);
    	if (controller == null) {
    		// persistent controller configuration (type RoomConfig) may exist already from previous start,
    		// even if the controller is not active yet
    		RoomConfig config = null;
    		for (RoomConfig existingConfig: appConfigData.roomConfigurations().getAllElements()) {
    			if (existingConfig.targetRoom().equalsLocation(room)) {
    				config = existingConfig;
    				break;
    			}
    		}
    		if (config == null) {
	    		config = appConfigData.roomConfigurations().add();
	    		config.targetRoom().setAsReference(room);
	    		// initialize with default value
	    		config.windowOpenTemperature().<TemperatureResource> create().setCelsius(appConfigData.defaultWindowOpenTemperature().getCelsius());
	    		config.activate(true);
    		}
    		controller = new RoomControllerImpl(config, 
    											thermostats.getSingleResourceManagement(room), 
    											windowSensors.getSingleResourceManagement(room), 
    											electricityStorageListener);
    		roomControllers.put(room, controller);
    	}
    	return controller;
    }

	@Override
	public TemperatureResource getDefaultWindowOpenTemperatureSetting() {
		return appConfigData.defaultWindowOpenTemperature();
	}
	
}
