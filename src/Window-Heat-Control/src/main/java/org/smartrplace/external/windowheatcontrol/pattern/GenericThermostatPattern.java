/**
 * Copyright 2009 - 2016
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.smartrplace.external.windowheatcontrol.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.connectiondevices.ThermalValve;
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
public class GenericThermostatPattern extends ResourcePattern<Thermostat> {

	/**Providing drivers: Homematic*/
	@Existence(required = CreateMode.OPTIONAL)
	private final TemperatureSensor tempSens = model.temperatureSensor();
	
	/**Providing drivers: Homematic*/
	@Existence(required = CreateMode.MUST_EXIST)
	@Access(mode = AccessMode.SHARED)
	public final TemperatureResource setpoint = tempSens.settings().setpoint();

	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework.
	 */
	public GenericThermostatPattern(Resource device) {
		super(device);
	}

	/**
	 * Provide any initial values that shall be set (this overrides any initial values set by simulation
	 * components themselves)
	 * Configure logging
	 */
	@Override
	public boolean accept() {
		return true;
	}
	
	public float temperatureBeforeWindowOpen = -1;
}
