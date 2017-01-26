package org.smartrplace.sim.simple.devices.motiondetector;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.tools.simulation.service.apiplus.SimulationPattern;

public class MotionDetectorConfigurationPattern extends SimulationPattern<MotionDetectorConfigurationModel>{
	
	public final FloatResource probabilityFactor = model.probabilityFactor();

	public MotionDetectorConfigurationPattern(Resource res) {
		super(res);
	}

}
