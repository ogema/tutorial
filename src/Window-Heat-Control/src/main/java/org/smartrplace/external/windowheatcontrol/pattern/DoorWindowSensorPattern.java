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
package org.smartrplace.external.windowheatcontrol.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.sensors.DoorWindowSensor;

public class DoorWindowSensorPattern extends ResourcePattern<DoorWindowSensor> { 
	
	public BooleanResource open = model.reading();
	
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework. Must be public.
	 */
	public DoorWindowSensorPattern(Resource device) {
		super(device);
	}
	
	public ResourceValueListener<BooleanResource> openListener = null;
}
