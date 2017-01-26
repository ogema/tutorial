package org.smartrplace.sim.simple.devices.motiondetector;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;

public class BrightnessValue implements SimulatedQuantity {

	private final MotionDetectorPattern pattern;
	
	public BrightnessValue(MotionDetectorPattern pattern) {
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
