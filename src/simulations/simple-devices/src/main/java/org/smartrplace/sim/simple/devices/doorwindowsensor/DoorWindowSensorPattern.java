package org.smartrplace.sim.simple.devices.doorwindowsensor;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.sensors.DoorWindowSensor;
import org.ogema.model.sensors.StateOfChargeSensor;

/**
 * Resource pattern for a temperature sensor
 */
public class DoorWindowSensorPattern extends ResourcePattern<DoorWindowSensor> {

	@Existence(required=CreateMode.OPTIONAL)
	public final BooleanResource open = model.reading();
	
//	public final ElectricityStorage battery = model.electricityStorage();
	public final ElectricityStorage battery = model.battery();
	
	public final StateOfChargeSensor stateOfChargeSensor = battery.chargeSensor();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final FloatResource batteryStatus = stateOfChargeSensor.reading();
	
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework.
	 */
	public DoorWindowSensorPattern(Resource device) {
		super(device);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(getClass())) 
			return false;
		DoorWindowSensorPattern other = (DoorWindowSensorPattern) obj;
		return this.model.equalsLocation(other.model);
	}
	
	@Override
	public int hashCode() {
		return model.getLocation().hashCode();
	}
	
	@Override
	public String toString() {
		return "Door/Window sensor pattern for " + model;
	}
	
	
}
