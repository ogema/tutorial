package org.smartrplace.sim.simple.devices.sensordevice.quantities;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.smartrplace.sim.simple.devices.sensordevice.SensorDevicePattern;

public class HumidityValue implements SimulatedQuantity {

	private final SensorDevicePattern pattern;
	
	public HumidityValue(SensorDevicePattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Current humidity. Unit 1.";
	}

	@Override
	public String getId() {
		return "Humidity";
	}

	@Override
	public SingleValueResource value() {
		return pattern.humidity;
	}
	
	
	
}
