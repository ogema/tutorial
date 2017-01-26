package org.smartrplace.sim.simple.devices.sensordevice.quantities;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.smartrplace.sim.simple.devices.sensordevice.SensorDevicePattern;

public class CO2Value implements SimulatedQuantity {

	private final SensorDevicePattern pattern;
	
	public CO2Value(SensorDevicePattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Current CO2 concentration in ppm.";
	}

	@Override
	public String getId() {
		return "CO2 concentration";
	}

	@Override
	public SingleValueResource value() {
		return pattern.co2concentration;
	}
	
	
	
}
