package com.example.androiddriver;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.user.PersonalDevicePresenceInfo;
import org.ogema.tools.resource.util.ResourceUtils;

import com.example.androiddriver.drivermodel.SampleAndroiddriverConfig;
import com.example.androiddriver.drivermodel.SampleAndroiddriverModel;

public class SampleAndroiddriverConectionManager {
	public OgemaLogger log;
    public ApplicationManager appMan;

	public SampleAndroiddriverConfig appConfigData;

    public SampleAndroiddriverConectionManager(ApplicationManager appMan) {
		this.appMan = appMan;
		this.log = appMan.getLogger();
		
        initConfigurationResource();
        
		ResourceValueListener<StringResource> listener = 
				new ResourceValueListener<StringResource>() {
			@Override
			public void resourceChanged(StringResource resource) {
				System.out.println("dataflowIn changed:"+resource);
				String val = resource.getValue();
				int i = val.indexOf("mobilid=");
				int j = val.indexOf("&ssid=");
				String mobilId, ssId;
				if(j<0) {
					if(val.length() > (i+8)) mobilId = val.substring(i+8);
					else mobilId = null;
					ssId = null;
				} else {
					mobilId = val.substring(i+8, j);
					ssId = val.substring(j+6);
				}
				processInflow(mobilId, ssId);
			}
		};
		appConfigData.dataInflow().addValueListener(listener, true);
		
 	}
    
	private PersonalDevicePresenceInfo processInflow(String mobilId, String ssId) {
		if(mobilId == null) {
			return null;
		}
		PersonalDevicePresenceInfo mcToUse = null;
		for(SampleAndroiddriverModel deviceData: appConfigData.connections().getAllElements()) {
			if(deviceData.target().mobilSerialId().getValue().equals(mobilId)) {
				mcToUse = deviceData.target();
				break;
			}
		}
		if(mcToUse == null) {
			//create new
			String mobilIdName = ResourceUtils.getValidResourceName(mobilId);
			//mcToUse = appConfigData.connections().addDecorator(mobilIdName, SampleAndroiddriverModel.class).target();
			mcToUse = appConfigData.mobileDeviceConfig().addDecorator(mobilIdName, PersonalDevicePresenceInfo.class);
			mcToUse.mobilSerialId().create();
			mcToUse.mobilSerialId().setValue(mobilId);
			mcToUse.trackPresence().create();
			mcToUse.trackPresence().setValue(true);
			mcToUse.messageToDevice().create();
			mcToUse.messageToDevice().setValue("");
			if(ssId != null) {
				mcToUse.ssId().create();
				mcToUse.ssId().setValue(ssId);
			}
			mcToUse.lastMessageReceived().create();
			mcToUse.activate(true);
			
			//create driver configuration resource
			SampleAndroiddriverModel comRes = appConfigData.connections().addDecorator(mobilIdName, SampleAndroiddriverModel.class);
			comRes.target().setAsReference(mcToUse);
			comRes.activate(true);

		}
		mcToUse.lastMessageReceived().setValue(appMan.getFrameworkTime());
		//mcToUse.presenceDetected().setValue(true);
		return mcToUse;
	}

    /** Call this method when information on a new connection is available. This data can be provided
     * by some auto-detect / plug&play mechanism or from a GUI page where the user enters configuration
     * data or some other source of information. The new connection will be detected and processed by
     * the driver via the pattern listener registered in the TemplateDriver class.
     * @param value the value to read/write to. If the connection reads/writes into more than one resource
     *  and/or the resources have a different type from FloatResource, adapt accordingly
     * @param configurationData adapt this to the information actually required for the driver's connections
     */
    public void createNewConnection(FloatResource value, Object configurationData ) {
    	SampleAndroiddriverModel newConnection = appConfigData.connections().add();
		newConnection.target().setAsReference(value);
		//TODO set other relevant data of the connection obtained from configurationData
		newConnection.activate(true);
	}

    /*
     * This app uses a central configuration resource, which is accessed here
     */
    private void initConfigurationResource() {
		String configResourceDefaultName = SampleAndroiddriverConfig.class.getSimpleName().substring(0, 1).toLowerCase()+SampleAndroiddriverConfig.class.getSimpleName().substring(1);
		final String name = appMan.getResourceManagement().getUniqueResourceName(configResourceDefaultName);
		appConfigData = appMan.getResourceAccess().getResource(name);
		if (appConfigData != null) { // resource already exists (appears in case of non-clean start)
			appMan.getLogger().debug("{} started with previously-existing config resource", getClass().getName());
		}
		else {
			appConfigData = (SampleAndroiddriverConfig) appMan.getResourceManagement().createResource(name, SampleAndroiddriverConfig.class);
			appConfigData.connections().create();
			appConfigData.mobileDeviceConfig().create();
			appConfigData.dataInflow().create();
			appConfigData.activate(true);
			appMan.getLogger().debug("{} started with new config resource", getClass().getName());
		}
    }
    
    public void close() {
    	
    }
}
