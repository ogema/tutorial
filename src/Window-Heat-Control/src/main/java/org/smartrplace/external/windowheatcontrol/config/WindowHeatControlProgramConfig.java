package org.smartrplace.external.windowheatcontrol.config;

import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Configuration;

/** 
 * Put an instance of this resource type for each program into a ResourceList
 */
public interface WindowHeatControlProgramConfig extends Configuration {

	StringResource programId();
	
	TimeResource maxDuration();
	
	// TODO add required setting resources for programs

}
