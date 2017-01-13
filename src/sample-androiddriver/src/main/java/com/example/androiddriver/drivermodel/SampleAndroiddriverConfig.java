package com.example.androiddriver.drivermodel;

import org.ogema.core.model.ResourceList;
import org.ogema.model.prototypes.Configuration;

/** 
 * The global configuration resource type for this app.
 */
public interface SampleAndroiddriverConfig extends Configuration {

	ResourceList<SampleAndroiddriverModel> connections();

	// TODO add global settings

}
