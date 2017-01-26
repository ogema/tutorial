package org.smartrplace.sim.simple.devices.switchbox;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.FrequencyResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.sensors.ElectricCurrentSensor;
import org.ogema.model.sensors.ElectricFrequencySensor;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.ElectricVoltageSensor;
import org.ogema.model.sensors.EnergyAccumulatedSensor;

/**
 * Resource pattern for a switchbox, such as the one provided by the Homematic driver
 */
public class SwitchboxPattern extends ResourcePattern<SingleSwitchBox> {
	
	public final OnOffSwitch onOffSwitch = model.onOffSwitch();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final BooleanResource stateControl = onOffSwitch.stateControl();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final BooleanResource stateFeedback = onOffSwitch.stateFeedback();
	
	public final BooleanResource controllable = onOffSwitch.controllable();
	
	public final ElectricityConnection connection = model.electricityConnection();
	
	public final ElectricPowerSensor powerSensor = connection.powerSensor();

	@Existence(required=CreateMode.OPTIONAL)
	public final PowerResource power = powerSensor.reading();
	
	public final ElectricCurrentSensor currentSensor = connection.currentSensor();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final ElectricCurrentResource current = currentSensor.reading();
	
	public final ElectricVoltageSensor voltageSensor = connection.voltageSensor();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final VoltageResource voltage = voltageSensor.reading();
	
	public final ElectricFrequencySensor frequencySensor = connection.frequencySensor();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final FrequencyResource frequency = frequencySensor.reading();
	
	public final EnergyAccumulatedSensor energySensor = connection.energySensor();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final EnergyResource energy = energySensor.reading();
		
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework.
	 */
	public SwitchboxPattern(Resource device) {
		super(device);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(getClass())) 
			return false;
		SwitchboxPattern other = (SwitchboxPattern) obj;
		return this.model.equalsLocation(other.model);
	}
	
	@Override
	public int hashCode() {
		return model.getLocation().hashCode();
	}
	
	

}
