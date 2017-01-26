package org.smartrplace.sim.simple.devices.sensordevice.quantities;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.smartrplace.sim.simple.devices.sensordevice.SensorDevicePattern;

public class BrightnessValue implements SimulatedQuantity {

	private final SensorDevicePattern pattern;
	
	public BrightnessValue(SensorDevicePattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Current light intensity in Lux";
	}

	@Override
	public String getId() {
		return "Brightness";
	}

	@Override
	public SingleValueResource value() {
		return pattern.brightness;
	}
	
	
	
}
