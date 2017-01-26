package org.smartrplace.sim.simple.devices.switchbox.quantities;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.smartrplace.sim.simple.devices.switchbox.SwitchboxPattern;

public class VoltageValue implements SimulatedQuantity {

	private final SwitchboxPattern pattern;
	
	public VoltageValue(SwitchboxPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Voltage in V";
	}

	@Override
	public String getId() {
		return "Voltage";
	}

	@Override
	public SingleValueResource value() {
		return pattern.voltage;
	}


}
