package org.smartrplace.sim.simple.devices.sensordevice;

import java.util.Map;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulationResourceConfiguration;

public class UpperLimitConfiguration implements SimulationResourceConfiguration {

	private final SensorDeviceConfigurationPattern pattern;
	
	public UpperLimitConfiguration(SensorDeviceConfigurationPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Set temperature simulation upper limit, in °C";
	}

	@Override
	public String getId() {
		return "Temperature upper limit";
	}

	// FIXME?
	@Override
	public Map<String, String> getOptions() {
		return null;
	}

	@Override
	public SingleValueResource value() {
		return pattern.upperTemperature;
	}

}
