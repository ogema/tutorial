package com.example.androiddriver.drivermodel;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Configuration;
import org.ogema.model.user.PersonalDevicePresenceInfo;

/** 
 * The global configuration resource type for this app.
 */
public interface SampleAndroiddriverConfig extends Configuration {
	/** Connection information kept separate from mobile devices for compatibility with standard driver model*/
	ResourceList<SampleAndroiddriverModel> connections();

	/** Device information. The mobile device will also read/write with this resource via the REST interface*/
	ResourceList<PersonalDevicePresenceInfo> mobileDeviceConfig();
	
	/**Receive initial information from mobile device here*/
	StringResource dataInflow();
}
