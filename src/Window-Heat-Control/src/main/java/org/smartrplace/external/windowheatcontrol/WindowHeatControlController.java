package org.smartrplace.external.windowheatcontrol;

import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.DoorWindowSensor;
import org.ogema.tools.resourcemanipulator.timer.CountDownDelayedExecutionTimer;
import org.smartrplace.external.windowheatcontrol.config.WindowHeatControlConfig;
import org.smartrplace.external.windowheatcontrol.pattern.DoorWindowSensorPattern;
import org.smartrplace.external.windowheatcontrol.pattern.ElectricityStoragePattern;
import org.smartrplace.external.windowheatcontrol.pattern.GenericThermostatPattern;
import org.smartrplace.external.windowheatcontrol.patternlistener.DoorWindowSensorListener;
import org.smartrplace.external.windowheatcontrol.patternlistener.ElectricityStorageListener;
import org.smartrplace.external.windowheatcontrol.patternlistener.GenericThermostatListener;

import de.iwes.util.collectionother.LogHelper;
import de.iwes.util.format.StringFormatHelper;
import de.iwes.util.linkingresource.LinkingRoomManagement;
import de.iwes.util.resource.ResourceHelper;

// here the controller logic is implemented
public class WindowHeatControlController {

	protected static final float WINDOW_OPEN_TEMPERATURE = 12.0f;
	public OgemaLogger log;
    public ApplicationManager appMan;
    private ResourcePatternAccess advAcc;

	public WindowHeatControlConfig appConfigData;
	
	Timer responseTimer;
	long startTime;
	
	public LinkingRoomManagement<DoorWindowSensorPattern> windowRoomMmgt;
	public LinkingRoomManagement<GenericThermostatPattern> thermostatRoomMmgt;
	
	public ElectricityStoragePattern batteryToUse = null;
    public WindowHeatControlController(final ApplicationManager appMan) {
		this.appMan = appMan;
		this.log = appMan.getLogger();
		this.advAcc = appMan.getResourcePatternAccess();
		
        initConfigurationResource();
        
        windowRoomMmgt = new LinkingRoomManagement<>(appMan.getResourceAccess());
        thermostatRoomMmgt = new LinkingRoomManagement<>(appMan.getResourceAccess());

        initDemands();
        
        new CountDownDelayedExecutionTimer(appMan, 10000) {
			@Override
			public void delayedExecution() {
				List<Thermostat> thList = appMan.getResourceAccess().getResources(Thermostat.class);
				for(Thermostat th: thList) {
					if(th.temperatureSensor().settings().setpoint().isActive()) {
						LogHelper.addResourceToRoomLogHM(th.temperatureSensor().settings().setpoint(),
								"ThermostatSetpoint", null, appMan);
					}
					if(th.temperatureSensor().reading().isActive()) {
						LogHelper.addResourceToRoomLogHM(th.temperatureSensor().reading(),
								"ThermostatTemperatureMeasurement", null, appMan);
					}
				}
				List<DoorWindowSensor> sensList = appMan.getResourceAccess().getResources(DoorWindowSensor.class);
				for(DoorWindowSensor th: sensList) {
					if(th.reading().isActive()) {
						LogHelper.addResourceToRoomLogHM(th.reading(),
								"WindowSensor", null, appMan);
					}
				}
			}
		};
        
        startTime = appMan.getFrameworkTime();
        responseTimer = appMan.createTimer(100000, new TimerListener() {
			@Override
			public void timerElapsed(Timer timer) {
				if(appConfigData.helloWorldMessage().isActive()) {
					String message = "Responding at "+
							StringFormatHelper.getFullTimeDateInLocalTimeZone(appMan.getFrameworkTime()) +
							" to:"+appConfigData.helloWorldMessage().getValue();
					boolean newlyCreated = !appConfigData.response().exists();
					if(newlyCreated) appConfigData.response().create();
					appConfigData.response().setValue(message);
					if(newlyCreated) appConfigData.response().activate(false);
					if(appMan.getFrameworkTime() - startTime > 10*60000) {
						responseTimer.destroy();
					}
				}
			}
        });
	}

	public DoorWindowSensorListener doorWindowSensorListener;
	public GenericThermostatListener genericThermostatListener;
	public ElectricityStorageListener electricityStorageListener;

    /*
     * This app uses a central configuration resource, which is accessed here
     */
    private void initConfigurationResource() {
		String configResourceDefaultName = WindowHeatControlConfig.class.getSimpleName().substring(0, 1).toLowerCase()+WindowHeatControlConfig.class.getSimpleName().substring(1);
		final String name = ResourceHelper.getUniqueResourceName(configResourceDefaultName);
		appConfigData = appMan.getResourceAccess().getResource(name);
		if (appConfigData != null) { // resource already exists (appears in case of non-clean start)
			appMan.getLogger().debug("{} started with previously-existing config resource", getClass().getName());
		}
		else {
			appConfigData = (WindowHeatControlConfig) appMan.getResourceManagement().createResource(name, WindowHeatControlConfig.class);
			appConfigData.helloWorldMessage().create();
			appConfigData.helloWorldMessage().setValue("Hello World!");
			appConfigData.activate(true);
			appMan.getLogger().debug("{} started with new config resource", getClass().getName());
		}
    }
    
    /*
     * register ResourcePatternDemands. The listeners will be informed about new and disappearing
     * patterns in the OGEMA resource tree
     */
    public void initDemands() {
		doorWindowSensorListener = new DoorWindowSensorListener(this);
		advAcc.addPatternDemand(DoorWindowSensorPattern.class, doorWindowSensorListener, AccessPriority.PRIO_LOWEST);
		genericThermostatListener = new GenericThermostatListener(this);
		advAcc.addPatternDemand(GenericThermostatPattern.class, genericThermostatListener, AccessPriority.PRIO_LOWEST);
		electricityStorageListener = new ElectricityStorageListener(this);
		advAcc.addPatternDemand(ElectricityStoragePattern.class, electricityStorageListener, AccessPriority.PRIO_LOWEST);
    }

	public void close() {
		advAcc.removePatternDemand(DoorWindowSensorPattern.class, doorWindowSensorListener);
		advAcc.removePatternDemand(GenericThermostatPattern.class, genericThermostatListener);
		advAcc.removePatternDemand(ElectricityStoragePattern.class, electricityStorageListener);
    }

	public void newInformationForRoom(Room room) {
		List<DoorWindowSensorPattern> windowSensors =
				windowRoomMmgt.getElements(room);
		if((windowSensors == null) || windowSensors.isEmpty()) {
			return;
		}
		List<GenericThermostatPattern> thermostats =
				thermostatRoomMmgt.getElements(room);
		if((thermostats == null)||thermostats.isEmpty()) {
			removeWindowListeners(windowSensors);
		} else {
			setAllWindowListeners(windowSensors, room);
		}
	}
	
	private class WindowListener implements ResourceValueListener<BooleanResource> {
		Room room;
		public WindowListener(Room room) {
			this.room = room;
		}

		@Override
		public void resourceChanged(BooleanResource resource) {
			List<GenericThermostatPattern> thermostats =
					thermostatRoomMmgt.getElements(room);
			if(thermostats != null) {
				setThermostats(resource.getValue(), thermostats);
			}
		}
		private void setThermostats(boolean isWindowOpen, List<GenericThermostatPattern> thermostats) {
			for(GenericThermostatPattern th: thermostats) {
				boolean fullBatteryDetected = false;
				if((batteryToUse != null)&&(batteryToUse.soc.getValue() >= 0.99f)) {
					fullBatteryDetected = true;
				}
				if(isWindowOpen&&(!fullBatteryDetected)) {
					th.temperatureBeforeWindowOpen = th.setpoint.getValue();
					th.setpoint.setCelsius(WINDOW_OPEN_TEMPERATURE);
				} else {
					if(th.temperatureBeforeWindowOpen < 0) continue;
					th.setpoint.setValue(th.temperatureBeforeWindowOpen);
					th.temperatureBeforeWindowOpen = -1;
				}
			}			
		}
	}
	
	private void setAllWindowListeners(List<DoorWindowSensorPattern> windowSensors, 
			final Room room) {
		for(DoorWindowSensorPattern p: windowSensors) {
			if(p.openListener == null) {
				p.openListener = new WindowListener(room);
				p.open.addValueListener(p.openListener, false);
			}
		}
	}

	private void removeWindowListeners(List<DoorWindowSensorPattern> windowSensors) {
		for(DoorWindowSensorPattern p: windowSensors) {
			if(p.openListener != null) {
				p.open.removeValueListener(p.openListener);
				p.openListener = null;
			}
		}
	}
}
