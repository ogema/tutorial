package org.smartrplace.sim.simple.devices.thermostat;

import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;

public class ThermostatSetpoint implements SimulatedQuantity {
	
	private final ThermostatPattern pattern;
	
	public ThermostatSetpoint(ThermostatPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "The temperature setpoint of the thermostat in &deg;C";
	}

	@Override
	public String getId() {
		return "Thermostat setpoint";
	}

	@Override
	public SingleValueResource value() {
		return pattern.setpointFB;
	}

}
