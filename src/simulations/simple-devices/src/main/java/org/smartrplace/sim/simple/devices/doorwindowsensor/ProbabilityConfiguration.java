package org.smartrplace.sim.simple.devices.doorwindowsensor;

import java.util.Map;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulationResourceConfiguration;

public class ProbabilityConfiguration implements SimulationResourceConfiguration {

	private final DoorWindowSensorConfigurationPattern pattern;
	
	public ProbabilityConfiguration(DoorWindowSensorConfigurationPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Set probability for open window; value between 0 and 1.";
	}

	@Override
	public String getId() {
		return "Probability";
	}

	@Override
	public Map<String, String> getOptions() {
		return null;
	}

	@Override
	public SingleValueResource value() {
		return pattern.probabilityFactor;
	}

}
