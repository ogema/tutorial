package org.smartrplace.sim.simple.devices.motiondetector;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;

public class MotionValue implements SimulatedQuantity {

	private final MotionDetectorPattern pattern;
	
	public MotionValue(MotionDetectorPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Motion detected?";
	}

	@Override
	public String getId() {
		return "Motion detector";
	}

	@Override
	public SingleValueResource value() {
		return pattern.motion;
	}
	
	
	
}
