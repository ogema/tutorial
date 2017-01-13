package com.example.driver.schedule.csv.importer.model;

import org.ogema.core.model.ResourceList;
import org.ogema.model.prototypes.Configuration;

/** 
 * The global configuration resource type for this app.
 * TODO add some setting parameter
 */
public interface ScheduleCsvConfig extends Configuration {

	ResourceList<FolderConfiguration> connections();

}
