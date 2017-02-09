package com.example.snippet.ogema.config;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Configuration;

/** 
 * The global configuration resource type for this app.
 */
public interface OgemaSnippetsConfig extends Configuration {

	ResourceList<OgemaSnippetsProgramConfig> availablePrograms();
	
	StringResource sampleElement();
	
	// TODO add global settings

}
