package org.smartrplace.sim.simple.devices.doorwindowsensor;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.tools.simulation.service.apiplus.SimulationPattern;

public class DoorWindowSensorConfigurationPattern extends SimulationPattern<DoorWindowSensorConfigurationModel>{
	
	public final FloatResource probabilityFactor = model.probabilityFactor();

	public DoorWindowSensorConfigurationPattern(Resource res) {
		super(res);
	}

}
