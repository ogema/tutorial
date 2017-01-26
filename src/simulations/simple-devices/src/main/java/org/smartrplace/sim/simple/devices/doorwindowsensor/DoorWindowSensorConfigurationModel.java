package org.smartrplace.sim.simple.devices.doorwindowsensor;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.tools.simulation.service.apiplus.SimulationConfigurationModel;

public interface DoorWindowSensorConfigurationModel extends SimulationConfigurationModel {
	
	/**
	 * Probability that window is open, in each step of the simulation.
	 * Value range [0,1]: <br>
	 * 0: always closed<br>
	 * 1: always open
	 */
	FloatResource probabilityFactor();


}
