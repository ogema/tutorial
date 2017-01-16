package com.example.app.windowheatcontrol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.windowheatcontrol.api.internal.RoomController;
import com.example.app.windowheatcontrol.config.RoomConfig;
import com.example.app.windowheatcontrol.pattern.ElectricityStoragePattern;
import com.example.app.windowheatcontrol.pattern.ThermostatPattern;
import com.example.app.windowheatcontrol.pattern.WindowSensorPattern;
import com.example.app.windowheatcontrol.patternlistener.ElectricityStorageListener;

import de.iwes.util.linkingresource.LinkingManagementAccess;

/**
 * A controller that manages a single room. It listens to changes in the window sensors
 * installed in this room. As soon as a window is opened, it reduces the temperature setpoint
 * for all thermostats in the room, and raises them to their previous value when the window is
 * closed again. When a window event is reported, it also checks whether a battery is available and fully charged, 
 * and if so, skips the temperature reduction.
 */
public class RoomControllerImpl implements RoomController {

	private final static Logger logger = LoggerFactory.getLogger(WindowHeatControlApp.class);
	// configuration resource for this controller; also references the room managed
	private final RoomConfig roomConfig;
	// provides access to the thermostats in this room, which may change dynamically
	private final LinkingManagementAccess<Room, ThermostatPattern> thermostats;
	// provides access to the window sensors in this room, which may change dynamically
	private final LinkingManagementAccess<Room, WindowSensorPattern> windowSensors;
	private final ElectricityStorageListener batteryListener;
	// internal state variables
	private final Map<Thermostat, Float> temperaturesBeforeWindowOpen = new HashMap<>();
	// volatile is necessary here, because these are accessed from the GUI, hence from outside the main app thread
	private volatile boolean active = false;
	private volatile boolean windowOpen = false;
	
	public RoomControllerImpl(RoomConfig roomConfig,
					      LinkingManagementAccess<Room, ThermostatPattern> thermostats,
					      LinkingManagementAccess<Room, WindowSensorPattern> windowSensors,
					      ElectricityStorageListener batteryListener) {
		this.roomConfig = roomConfig;
		this.thermostats = thermostats;
		this.windowSensors = windowSensors;
		this.batteryListener = batteryListener;
	}

	@Override
	public Room getRoom() {
		return roomConfig.targetRoom();
	}

	@Override
	public boolean isActive() {
		return active;
	}
	
	// the room is suitable for management if it contains at least one thermostat and one window sensor
	private void setActiveState() {
		active = (!thermostats.isEmpty() && !windowSensors.isEmpty());
	}

	@Override
	public boolean settingsChanged() {
		final boolean wasActive = active;
		setActiveState(); // may change state of active
		final boolean isActive = active;
		if (isActive && !wasActive) 
			start();
		else if (wasActive && !isActive) 
			stop();
		else 
			return false;
		return true;
	}

	@Override
	public void addWindowSensor(WindowSensorPattern sensor) {
		if (active) 
			sensor.open.addValueListener(windowListener);
		else
			settingsChanged();
	}

	@Override
	public void removeWindowSensor(WindowSensorPattern sensor) {
		if (active) 
			settingsChanged();
		sensor.open.removeValueListener(windowListener);
	}
	
	private void start() {
		logger.info("Window heat control app starting room management for {}",roomConfig.targetRoom().getLocation());
		for (WindowSensorPattern windowSensor : windowSensors.getElements()) {
			windowSensor.open.addValueListener(windowListener);
		}
		if (getWindowOpenStatus()) 
			windowListener.resourceChanged(null);
	}

	@Override
	public void stop() {
		active = false;
		logger.info("Window heat control app stopping room management for {}",roomConfig.targetRoom().getLocation());
		for (WindowSensorPattern windowSensor: windowSensors.getElements()) {
			windowSensor.open.removeValueListener(windowListener);
		}
	}

	@Override
	public boolean isWindowOpen() {
		return windowOpen;
	}
	
	@Override
	public int thermostatCount() {
		return thermostats.size();
	}
	
	@Override
	public int windowSensorCount() {
		return windowSensors.size();
	}
	
	private boolean getWindowOpenStatus() {
		for (WindowSensorPattern windowSensor: windowSensors.getElements()) {
			if (windowSensor.open.getValue())
				return true;
		}
		return false;
	}
	
	@Override
	public float getCurrentTemperatureSetpoint() {
		final List<ThermostatPattern> list = thermostats.getElements();
		if (list.isEmpty())
			return Float.NaN;
		float value = 0;
		for (ThermostatPattern thermostat : list) {
			value += thermostat.getTemperatureSetpointCelsius();
		}
		return value/list.size();
	}
	
	@Override
	public void setCurrentTemperatureSetpoint(float celsius) throws IllegalArgumentException {
		if (!Float.isFinite(celsius) || celsius < 0 || celsius > 35)
			throw new IllegalArgumentException("Invalid temperature " + celsius);
		for (ThermostatPattern thermostat : thermostats.getElements()) {
			thermostat.setpoint.setCelsius(celsius);
		}
	}
	
	@Override
	public float getWindowOpenTemperatureSetpoint() {
		return roomConfig.windowOpenTemperature().getCelsius();
	}
	
	@Override
	public void setWindowOpenTemperatureSetpoint(float celsius) throws IllegalArgumentException {
		if (!Float.isFinite(celsius) || celsius < 0 || celsius > 35)
			throw new IllegalArgumentException("Invalid temperature " + celsius);
		roomConfig.windowOpenTemperature().setCelsius(celsius);
	}
	
	private final ResourceValueListener<BooleanResource> windowListener = new ResourceValueListener<BooleanResource>() {
		
		// called whenever a window is opened or closed
		@Override
		public void resourceChanged(BooleanResource resource) {
			// there may be more than one window in the room, therefore it is not enough to evaluate the value of the changed resource only
			final boolean windowOpenNew = getWindowOpenStatus();
			logger.debug("Window status in room {} changed. Was open: {}, is open: {}", roomConfig.targetRoom().getLocation(), windowOpen, windowOpenNew);
			// state did not change; may happen, for instance, if a second window is opened, in which case we do not want to change the thermostat setpoints
			if (windowOpenNew == windowOpen)
				return;
			final ElectricityStoragePattern activeBattery = batteryListener.getActiveBattery();
			final boolean fullBatteryDetected = (activeBattery != null && activeBattery.soc.getValue() > 0.95F);
			for (ThermostatPattern th: thermostats.getElements()) {
				if (windowOpenNew && !fullBatteryDetected) {
					temperaturesBeforeWindowOpen.put(th.model, th.getTemperatureSetpointCelsius());
					float value = roomConfig.windowOpenTemperature().getCelsius();
					if (value <= 0 || value > 35) { // check if value is sensible
						logger.warn("Extreme temperature value {}°C found in configuration. Ignoring this and using the default temperature {}°C instead.",
								value, WindowHeatControlController.WINDOW_OPEN_TEMPERATURE);
						value = WindowHeatControlController.WINDOW_OPEN_TEMPERATURE;
					}
					th.setpoint.setCelsius(value);
				} else {
					Float celsius = temperaturesBeforeWindowOpen.remove(th.model);
					if (celsius == null)
						continue;
					th.setpoint.setCelsius(celsius);
				}
			}
			windowOpen = windowOpenNew;
		}
	};
	
}
