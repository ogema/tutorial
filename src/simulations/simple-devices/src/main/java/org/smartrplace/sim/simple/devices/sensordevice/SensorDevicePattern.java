package org.smartrplace.sim.simple.devices.sensordevice;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.BrightnessResource;
import org.ogema.core.model.units.ConcentrationResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.sensors.CO2Sensor;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.LightSensor;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.sensors.StateOfChargeSensor;
import org.ogema.model.sensors.TemperatureSensor;

/**
 * Resource pattern for a temperature sensor
 */
public class SensorDevicePattern extends ResourcePattern<SensorDevice> {
	
	public final ResourceList<Sensor> sensors = model.sensors();
	
	public final TemperatureSensor temperatureSensor = sensors.getSubResource("temperatureSensor", TemperatureSensor.class);
	
	@Existence(required=CreateMode.OPTIONAL)
	public final TemperatureResource temperature = temperatureSensor.reading();
	
	public final HumiditySensor humiditySensor = sensors.getSubResource("humiditySensor", HumiditySensor.class);
	
	@Existence(required=CreateMode.OPTIONAL)
	public final FloatResource humidity = humiditySensor.reading();
	
	public final CO2Sensor co2sensor = sensors.getSubResource("co2Sensor", CO2Sensor.class);
	
	@Existence(required=CreateMode.OPTIONAL)
	public final ConcentrationResource co2concentration = co2sensor.reading();
	
	public final LightSensor lightSensor = sensors.getSubResource("lightSensor", LightSensor.class);
	
	@Existence(required=CreateMode.OPTIONAL)
	public final BrightnessResource brightness  = lightSensor.reading();

	public final ElectricityStorage battery = model.electricityStorage();
	
	public final StateOfChargeSensor stateOfChargeSensor = battery.chargeSensor();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final FloatResource batteryStatus = stateOfChargeSensor.reading();
	
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework.
	 */
	public SensorDevicePattern(Resource device) {
		super(device);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(getClass())) 
			return false;
		SensorDevicePattern other = (SensorDevicePattern) obj;
		return this.model.equalsLocation(other.model);
	}
	
	@Override
	public int hashCode() {
		return model.getLocation().hashCode();
	}
	

}
