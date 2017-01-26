package org.smartrplace.sim.simple.devices.sensordevice;

import java.util.Map;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulationResourceConfiguration;

public class LowerLimitConfiguration implements SimulationResourceConfiguration {

	private final SensorDeviceConfigurationPattern pattern;
	
	public LowerLimitConfiguration(SensorDeviceConfigurationPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Set temperature simulation lower limit, in °C";
	}

	@Override
	public String getId() {
		return "Temperature lower limit";
	}

	// FIXME?
	@Override
	public Map<String, String> getOptions() {
		return null;
	}

	@Override
	public SingleValueResource value() {
		return pattern.lowerTemperature;
	}

}
