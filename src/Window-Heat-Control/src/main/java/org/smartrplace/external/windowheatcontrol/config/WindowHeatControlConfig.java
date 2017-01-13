package org.smartrplace.external.windowheatcontrol.config;

import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Configuration;

/** 
 * The global configuration resource type for this app.
 */
public interface WindowHeatControlConfig extends Configuration {

	StringResource helloWorldMessage();
	StringResource response();
}
