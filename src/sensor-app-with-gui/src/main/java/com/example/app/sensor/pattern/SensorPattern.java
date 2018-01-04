/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package com.example.app.sensor.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.sensors.Sensor;

public class SensorPattern extends ResourcePattern<Sensor> { 
	
	 /**
	  * Only those sensors match the pattern declaration, whose "reading"-subresource
	  * exists and is active.
	  */
	@Existence(required=CreateMode.MUST_EXIST)
	public final ValueResource reading = model.reading();

	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework. Must be public.
	 */
	public SensorPattern(Resource device) {
		super(device);
	}

}
