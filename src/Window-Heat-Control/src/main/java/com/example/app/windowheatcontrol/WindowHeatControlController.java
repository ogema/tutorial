package com.example.app.windowheatcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.model.locations.Room;

import com.example.app.windowheatcontrol.api.internal.RoomController;
import com.example.app.windowheatcontrol.api.internal.RoomManagement;
import com.example.app.windowheatcontrol.config.WindowHeatControlConfig;
import com.example.app.windowheatcontrol.pattern.ElectricityStoragePattern;
import com.example.app.windowheatcontrol.pattern.ThermostatPattern;
import com.example.app.windowheatcontrol.pattern.WindowSensorPattern;
import com.example.app.windowheatcontrol.patternlistener.ElectricityStorageListener;
import com.example.app.windowheatcontrol.patternlistener.ThermostatListener;
import com.example.app.windowheatcontrol.patternlistener.WindowSensorListener;

import de.iwes.util.format.StringFormatHelper;
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
	
	// TODO
	private final Timer responseTimer;
	private final long startTime;
	
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
        
        this.startTime = appMan.getFrameworkTime();
        // XXX what's that?
        this.responseTimer = appMan.createTimer(100000, new TimerListener() {
			@Override
			public void timerElapsed(Timer timer) {
				if(appConfigData.helloWorldMessage().isActive()) {
					String message = "Responding at "+
							StringFormatHelper.getFullTimeDateInLocalTimeZone(appMan.getFrameworkTime()) +
							" to: " + appConfigData.helloWorldMessage().getValue();
					boolean newlyCreated = !appConfigData.response().exists();
					if (newlyCreated) appConfigData.response().create();
					appConfigData.response().setValue(message);
					if (newlyCreated) appConfigData.response().activate(false);
					if (appMan.getFrameworkTime() - startTime > 10*60000) {
						responseTimer.destroy();
					}
				}
			}
        });
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
			appConfigData = (WindowHeatControlConfig) appMan.getResourceManagement().createResource(configResourceDefaultName, WindowHeatControlConfig.class);
			appConfigData.helloWorldMessage().create();
			appConfigData.helloWorldMessage().setValue("Hello World!");
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
    		controller = new RoomControllerImpl(room, 
    											thermostats.getSingleResourceManagement(room), 
    											windowSensors.getSingleResourceManagement(room), 
    											electricityStorageListener);
    		roomControllers.put(room, controller);
    	}
    	return controller;
    }

}
