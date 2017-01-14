package com.example.app.windowheatcontrol.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;

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
	@Existence(required = CreateMode.MUST_EXIST)
	private final TemperatureSensor tempSens = model.temperatureSensor();
	
	/**Providing drivers: Homematic*/
	@Existence(required = CreateMode.MUST_EXIST)
	@Access(mode = AccessMode.SHARED)
	public final TemperatureResource setpoint = tempSens.settings().setpoint();

	@Existence(required = CreateMode.OPTIONAL)
	private final TemperatureResource setpointFeedback = tempSens.deviceFeedback().setpoint();
	
	@Existence(required = CreateMode.OPTIONAL)
	public final TemperatureResource reading = tempSens.reading();
	
	// this resource is typically a reference to the room the device is located in 
	// when the device is moved to another room, the reference is switched to another 
	// target resource, which causes a patternUnavailable followed by a patternAvailable callback
	@Existence(required = CreateMode.MUST_EXIST)
	public final Room room = model.location().room(); 
	
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework.
	 */
	public ThermostatPattern(Resource device) {
		super(device);
	}
	
	public float getTemperatureSetpointCelsius() {
		// the current thermostat temperature setpoint may differ from the one set in the gateway, because the user may 
		// have changed it manually. Therefore, setpointFeedback is a better guess for the currently active
		// setpoint than setpoint. If the former is not available, we content ourselves with the setpoint configured in the gateway, however.
		if (setpointFeedback.isActive())
			return setpointFeedback.getCelsius();
		else
			return setpoint.getCelsius();
		
	}

}
