package org.smartrplace.sim.simple.devices.motiondetector;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.BrightnessResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.sensors.LightSensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.sensors.StateOfChargeSensor;

/**
 * Resource pattern for a temperature sensor
 */
public class MotionDetectorPattern extends ResourcePattern<SensorDevice> {

	public final ResourceList<Sensor> sensors = model.sensors();
	
	public final MotionSensor motionSensor = sensors.getSubResource("motionSensor", MotionSensor.class);
	
	@Existence(required=CreateMode.OPTIONAL)
	public final BooleanResource motion = motionSensor.reading();
	
	public final LightSensor brightnessSensor = sensors.getSubResource("brightnessSensor", LightSensor.class);
	
	@Existence(required=CreateMode.OPTIONAL)
	public final BrightnessResource brightness = brightnessSensor.reading();
	
	public final ElectricityStorage battery = model.electricityStorage();
	
	public final StateOfChargeSensor stateOfChargeSensor = battery.chargeSensor();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final FloatResource batteryStatus = stateOfChargeSensor.reading();
	
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework.
	 */
	public MotionDetectorPattern(Resource device) {
		super(device);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(getClass())) 
			return false;
		MotionDetectorPattern other = (MotionDetectorPattern) obj;
		return this.model.equalsLocation(other.model);
	}
	
	@Override
	public int hashCode() {
		return model.getLocation().hashCode();
	}
	

}
