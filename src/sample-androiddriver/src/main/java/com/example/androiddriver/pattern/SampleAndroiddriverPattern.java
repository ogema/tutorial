package com.example.androiddriver.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.communication.DeviceAddress;
import org.ogema.model.communication.IPAddressV4;
import org.ogema.model.user.PersonalDevicePresenceInfo;

import com.example.androiddriver.drivermodel.SampleAndroiddriverModel;

/**
 * Every instance of this class (a "pattern match") represents one driver configuration,
 * corresponding e.g. to the data point of an external device, that can be read or 
 * written by the driver. The pattern provides access to the communication address and the 
 * OGEMA resource that shall store the value of the data point.
 */
public class SampleAndroiddriverPattern extends ResourcePattern<SampleAndroiddriverModel> { 
	
	public final DeviceAddress address = model.comAddress();
	
	/**
	 * TODO this is an example for drivers based on IP communication; adapt to your needs
	 */
	@Existence(required=CreateMode.OPTIONAL)
	public final IPAddressV4 ipAddress = address.ipV4Address();
	
	public final PersonalDevicePresenceInfo target = model.target();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final TimeResource pollingInterval = model.pollingConfiguration().pollingInterval();

	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework. Must be public.
	 */
	public SampleAndroiddriverPattern(Resource device) {
		super(device);
	}

}
