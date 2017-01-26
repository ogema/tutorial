package org.smartrplace.sim.simple.devices.switchbox.quantities;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.smartrplace.sim.simple.devices.switchbox.SwitchboxPattern;

public class CurrentValue implements SimulatedQuantity {

	private final SwitchboxPattern pattern;
	
	public CurrentValue(SwitchboxPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Current in A";
	}

	@Override
	public String getId() {
		return "Current";
	}

	@Override
	public SingleValueResource value() {
		return pattern.current;
	}

}
