package org.smartrplace.sim.simple.devices.thermostat;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.connectiondevices.ThermalValve;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.targetranges.TemperatureTargetRange;

/**
 * Resource pattern for a heating thermostat that connects to information provided by a
 * homematic device<br>
 * Note: Remove annotations '@Existence(required = CreateMode.OPTIONAL)' if you require an element
 * in your application, remove fields you do not need in your application at all<br>
 * Providing drivers: Homematic
 * 
 * @author David Nestle
 */
public class ThermostatPattern extends ResourcePattern<Thermostat> {
	
	/**Providing drivers: Homematic*/
	private final TemperatureSensor tempSens = model.temperatureSensor();
	
	/**Providing drivers: Homematic*/
	private final ThermalValve valve = model.valve();
	
	/** Device temperature reading<br>
	 * Providing drivers: Homematic*/
	@Existence(required=CreateMode.OPTIONAL)
	public final TemperatureResource temperature = tempSens.reading();
	
	public final TemperatureTargetRange tempSetting = tempSens.settings();

	/**Providing drivers: Homematic*/
	@Existence(required=CreateMode.OPTIONAL)
	public final TemperatureResource setpoint = tempSens.settings().setpoint();

	/**Providing drivers: Homematic*/
	@Existence(required=CreateMode.OPTIONAL)
	public final TemperatureResource setpointFB = tempSens.deviceFeedback().setpoint();

	/**Providing drivers: Homematic*/
	@Existence(required=CreateMode.OPTIONAL)
	public final FloatResource valvePosition = valve.setting().stateFeedback();
	
	public final StringResource simulationProvider = model.getSubResource("simulationProvider",StringResource.class);
	
	public final BooleanResource simulationActive = model.getSubResource("simulationActive",BooleanResource.class);
	
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework.
	 */
	public ThermostatPattern(Resource device) {
		super(device);
	}
	
	@Override
	public boolean accept() {
		return simulationProvider.getValue().equals(ThermostatSimulation.PROVIDER_ID);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(getClass())) 
			return false;
		ThermostatPattern other = (ThermostatPattern) obj;
		return this.model.equalsLocation(other.model);
	}
	
	@Override
	public int hashCode() {
		return model.getLocation().hashCode();
	}
	

}
