package org.smartrplace.sim.simple.devices.switchbox.quantities;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.smartrplace.sim.simple.devices.switchbox.SwitchboxPattern;

public class FrequencyValue implements SimulatedQuantity {

	private final SwitchboxPattern pattern;
	
	public FrequencyValue(SwitchboxPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Frequency in Hz";
	}

	@Override
	public String getId() {
		return "Frequency";
	}

	@Override
	public SingleValueResource value() {
		return pattern.frequency;
	}


}
