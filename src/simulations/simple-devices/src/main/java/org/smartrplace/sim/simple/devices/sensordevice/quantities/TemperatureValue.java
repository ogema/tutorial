package org.smartrplace.sim.simple.devices.sensordevice.quantities;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.smartrplace.sim.simple.devices.sensordevice.SensorDevicePattern;

public class TemperatureValue implements SimulatedQuantity {

	private final SensorDevicePattern pattern;
	
	public TemperatureValue(SensorDevicePattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Current temperature in °C";
	}

	@Override
	public String getId() {
		return "Temperature";
	}

	@Override
	public SingleValueResource value() {
		return pattern.temperature;
	}
	
	
	
}
