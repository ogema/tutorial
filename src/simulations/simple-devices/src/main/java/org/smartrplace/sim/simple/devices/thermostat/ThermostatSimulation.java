package org.smartrplace.sim.simple.devices.thermostat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Room;
import org.ogema.tools.resource.util.ResourceUtils;
import org.ogema.tools.simulation.service.api.SimulationProvider;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.ogema.tools.simulation.service.api.model.SimulationConfiguration;

/**
 * A simulated thermostat. Note: this basic simulation simply creates one or 
 * more thermostats, and makes sure that user defined setpoints are converted
 * to device setpoints. It does not actually simulate any physical quantity,
 * such as the room temperature. The simulation is meant to be used only 
 * for testing purposes, in case an application needs to interact with Thermostats. 
 * @author cnoelle
 */
// TODO listener on ThermostatPatterns
public class ThermostatSimulation implements SimulationProvider<Thermostat>, PatternListener<ThermostatPattern>, 
		ResourceValueListener<TemperatureResource> {
	
	private final ApplicationManager am;
	private final Map<String,ThermostatPattern> simulatedPatterns  =new HashMap<>();
	static final String PROVIDER_ID = "Basic simulated thermostat";
	
	public ThermostatSimulation(ApplicationManager am) {
		this.am = am;
		am.getResourcePatternAccess().addPatternDemand(ThermostatPattern.class, this, AccessPriority.PRIO_LOWEST);
	}
	
	// TODO call
	public void close() {
		am.getResourcePatternAccess().removePatternDemand(ThermostatPattern.class, this);
		for (ThermostatPattern pattern: simulatedPatterns.values()) {
			pattern.setpoint.removeValueListener(this);
		}
	}
	
	@Override
	public String getDescription() {
		return PROVIDER_ID;
	}

	@Override
	public Thermostat createSimulatedObject(String deviceId) {
		try {
			ThermostatPattern pattern = am.getResourcePatternAccess().createResource(deviceId, ThermostatPattern.class);
			pattern.simulationProvider.setValue(PROVIDER_ID);
			pattern.simulationActive.setValue(true);
//			Room room = am.getResourceManagement().createResource(SimpleDevicesApp.ROOM_PATH, Room.class);
//			pattern.model.location().room().setAsReference(room);
//			room.activate(false);
			am.getResourcePatternAccess().activatePattern(pattern);
			return pattern.model;
		} catch (Exception e) {
			am.getLogger().warn("Could not create a simulated device " + deviceId,e);
			return null;
		}
	}

	@Override
	public List<SimulationConfiguration> getConfigurations(String arg0) {
		return Collections.emptyList();
	}

	@Override
	public String getProviderId() {
		return PROVIDER_ID;
	}

	@Override
	public List<Thermostat> getSimulatedObjects() {
		List<Thermostat> thermostats = new ArrayList<>();
		for (ThermostatPattern pat: simulatedPatterns.values()) {
			thermostats.add(pat.model);
		}
		return thermostats;
	}

	@Override
	public List<SimulatedQuantity> getSimulatedQuantities(String device) {
		ThermostatPattern pattern = simulatedPatterns.get(device);
		if (pattern == null)
			return null;
		List<SimulatedQuantity> list  =new ArrayList<>();
		list.add(new ThermostatSetpoint(pattern));
		return list;
	}

	@Override
	public Class<Thermostat> getSimulatedType() {
		return Thermostat.class;
	}

	@Override
	public boolean isSimulationActive(String device) {
		ThermostatPattern pattern = simulatedPatterns.get(device);
		if (pattern == null)
			return false;
		return pattern.simulationActive.getValue();
	}

	@Override
	public boolean startSimulation(String device) {
		ThermostatPattern pattern = simulatedPatterns.get(device);
		if (pattern == null)
			return false;
		boolean active = pattern.simulationActive.getValue();
		if (!active) {
			pattern.setpoint.addValueListener(this);
			pattern.simulationActive.setValue(true);
		}
		return true;
	}

	@Override
	public boolean stopSimulation(String device) {
		ThermostatPattern pattern = simulatedPatterns.get(device);
		if (pattern == null)
			return false;
		boolean active = pattern.simulationActive.getValue();
		if (active) {
			pattern.setpoint.removeValueListener(this);
			pattern.simulationActive.setValue(false);
		}
		return true;
	}

	@Override
	public void patternAvailable(ThermostatPattern pattern) {
		ThermostatPattern old = simulatedPatterns.put(pattern.model.getPath(), pattern);
		am.getResourcePatternAccess().createOptionalResourceFields(pattern, ThermostatPattern.class, false);
		if (!pattern.setpointFB.isActive()) {
			pattern.setpoint.setCelsius(20F);
			pattern.setpointFB.setCelsius(20F);
		}
		am.getResourcePatternAccess().activatePattern(pattern);
		if (old != null) // should not happen
			return;
		if (pattern.simulationActive.getValue())
			pattern.setpoint.addValueListener(this);
	}

	@Override
	public void patternUnavailable(ThermostatPattern pattern) {
		ThermostatPattern old = simulatedPatterns.remove(pattern.model.getPath());
		if (old == null)
			old = simulatedPatterns.remove(pattern.model.getLocation());
		if (old != null)
			old.setpoint.removeValueListener(this);
	}

	@Override
	public void resourceChanged(TemperatureResource resource) {
		ThermostatPattern pattern  = new ThermostatPattern(resource.getParent().getParent().getParent());
		if (!pattern.simulationActive.getValue())
			return;
		pattern.setpointFB.setValue(resource.getValue());
		pattern.setpointFB.activate(false);
		Room rm = ResourceUtils.getDeviceRoom(pattern.model);
		if (rm == null || !rm.temperatureSensor().reading().isActive())
			return;
		float currentTemp = rm.temperatureSensor().reading().getCelsius();
		pattern.temperature.setCelsius(currentTemp);
		float targetTemp = resource.getCelsius();
		if (currentTemp >= targetTemp)
			return;
		float valvePos;
		if (targetTemp- currentTemp < 2)
			valvePos = 0.2F;
		else if (targetTemp - currentTemp < 5)
			valvePos = 0.5F;
		else 
			valvePos = 1;
		pattern.valvePosition.setValue(valvePos);
	}

	@Override
	public boolean isSimulationActivatable(String deviceId) {
		return true;
	}
}
