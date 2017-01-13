package com.example.driver.schedule.csv.importer.model;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.communication.CommunicationInformation;

/** 
 * Persistently stores a directory path for supervision by the driver,
 * as well as the associated target resource.
 */
public interface FolderConfiguration extends CommunicationInformation {

	/**
	 * Directory to be supervised
	 * @return
	 */
	StringResource directory();
	
	/**
	 * A reference to the target resource, below which the schedules will be
	 * added by the driver.
	 * @return 
	 */
	FloatResource target();
}
