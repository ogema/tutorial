package org.smartrplace.sim.simple.devices.motiondetector;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.tools.simulation.service.apiplus.SimulationConfigurationModel;

public interface MotionDetectorConfigurationModel extends SimulationConfigurationModel {
	
	/**
	 * Probability that motion will be detected in each step of the simulation.
	 * Value range [0,1]: <br>
	 * 0: no motion will be detected<br>
	 * 1: motion will be detected for sure
	 */
	FloatResource probabilityFactor();


}
