package org.smartrplace.sim.simple.devices.sensordevice;

import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.ranges.TemperatureRange;
import org.ogema.tools.simulation.service.apiplus.SimulationConfigurationModel;

/**
 * Just a marker model
 */
public interface SensorDeviceConfigurationModel extends SimulationConfigurationModel {
	
	/**
	 * The simulation will assure that the temperature values remain within
	 * the specified range. This is a configuration pattern for the 
	 * simulation.
	 */
	TemperatureRange ratedValues();

	@Override
	SensorDevice target();
}
