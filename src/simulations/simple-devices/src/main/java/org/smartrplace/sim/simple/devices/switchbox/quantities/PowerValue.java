package org.smartrplace.sim.simple.devices.switchbox.quantities;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.smartrplace.sim.simple.devices.switchbox.SwitchboxPattern;

public class PowerValue implements SimulatedQuantity {

	private final SwitchboxPattern pattern;
	
	public PowerValue(SwitchboxPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Current power in W";
	}

	@Override
	public String getId() {
		return "Power value";
	}

	@Override
	public SingleValueResource value() {
		return pattern.power;
	}
	
	
	
}
