package org.smartrplace.sim.simple.devices.motiondetector;

import java.util.Map;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulationResourceConfiguration;

public class MotionDetectorConfiguration implements SimulationResourceConfiguration {

	private final MotionDetectorConfigurationPattern pattern;
	
	public MotionDetectorConfiguration(MotionDetectorConfigurationPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Set motion detection probability; value between 0 and 1.";
	}

	@Override
	public String getId() {
		return "Detection probability";
	}

	// FIXME?
	@Override
	public Map<String, String> getOptions() {
		return null;
	}

	@Override
	public SingleValueResource value() {
		return pattern.probabilityFactor;
	}

}
