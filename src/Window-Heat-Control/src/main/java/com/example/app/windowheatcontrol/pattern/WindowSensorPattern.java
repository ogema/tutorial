package com.example.app.windowheatcontrol.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.DoorWindowSensor;

public class WindowSensorPattern extends ResourcePattern<DoorWindowSensor> { 
	
	// this resource is typically a reference to the room the device is located in 
	// when the device is moved to another room, the reference is switched to another 
	// target resource, which causes a patternUnavailable followed by a patternAvailable callback
	@Existence(required = CreateMode.MUST_EXIST)
	public final Room room = model.location().room(); 
	
	public final BooleanResource open = model.reading();
	
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework. Must be public.
	 */
	public WindowSensorPattern(Resource device) {
		super(device);
	}

}
