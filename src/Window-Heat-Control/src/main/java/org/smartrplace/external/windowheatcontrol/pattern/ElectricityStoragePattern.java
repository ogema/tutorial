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
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.storage.ElectricityStorage;

public class ElectricityStoragePattern extends ResourcePattern<ElectricityStorage> { 
	
	public FloatResource soc = model.chargeSensor().reading();
	
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework. Must be public.
	 */
	public ElectricityStoragePattern(Resource device) {
		super(device);
	}
}
