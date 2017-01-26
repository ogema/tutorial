package org.smartrplace.sim.simple.devices.doorwindowsensor;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;

public class WindowStatus implements SimulatedQuantity {

	private final DoorWindowSensorPattern pattern;
	
	public WindowStatus(DoorWindowSensorPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Is door/window open?";
	}

	@Override
	public String getId() {
		return "Door/Window status";
	}

	@Override
	public SingleValueResource value() {
		return pattern.open;
	}
	
	
	
}
