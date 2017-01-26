package org.smartrplace.sim.simple.devices.switchbox.quantities;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.smartrplace.sim.simple.devices.switchbox.SwitchboxPattern;

public class EnergyValue implements SimulatedQuantity {

	private final SwitchboxPattern pattern;
	
	public EnergyValue(SwitchboxPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Energy in J";
	}

	@Override
	public String getId() {
		return "Energy";
	}

	@Override
	public SingleValueResource value() {
		return pattern.energy;
	}


}
