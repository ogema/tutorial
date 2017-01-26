package org.smartrplace.sim.simple.devices.sensordevice;

import org.ogema.core.model.Resource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.model.ranges.TemperatureRange;
import org.ogema.tools.simulation.service.apiplus.SimulationPattern;

public class SensorDeviceConfigurationPattern extends SimulationPattern<SensorDeviceConfigurationModel>{
	
	/**
	 * The simulation will assure that the temperature values remain within
	 * the specified range. This is a configuration pattern for the 
	 * simulation.
	 */
	public final TemperatureRange temperatureRange = model.ratedValues();
	
	public final TemperatureResource lowerTemperature = temperatureRange.lowerLimit();
	
	public final TemperatureResource upperTemperature = temperatureRange.upperLimit();

	public SensorDeviceConfigurationPattern(Resource res) {
		super(res);
	}

}
