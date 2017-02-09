package com.example.snippet.ogema;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

import com.example.snippet.ogema.config.OgemaSnippetsConfig;
import de.iwes.util.resource.ResourceHelper;

// here the controller logic is implemented
public class OgemaSnippetsController {

	public OgemaLogger log;
    public ApplicationManager appMan;
    private ResourcePatternAccess advAcc;

	public OgemaSnippetsConfig appConfigData;
	
    public OgemaSnippetsController(ApplicationManager appMan) {
		this.appMan = appMan;
		this.log = appMan.getLogger();
		this.advAcc = appMan.getResourcePatternAccess();
		
        initConfigurationResource();
        initDemands();
	}

    /*
     * This app uses a central configuration resource, which is accessed here
     */
    private void initConfigurationResource() {
		String configResourceDefaultName = OgemaSnippetsConfig.class.getSimpleName().substring(0, 1).toLowerCase()+OgemaSnippetsConfig.class.getSimpleName().substring(1);
		appConfigData = appMan.getResourceAccess().getResource(configResourceDefaultName);
		if (appConfigData != null) { // resource already exists (appears in case of non-clean start)
			appMan.getLogger().debug("{} started with previously-existing config resource", getClass().getName());
		}
		else {
			appConfigData = (OgemaSnippetsConfig) appMan.getResourceManagement().createResource(configResourceDefaultName, OgemaSnippetsConfig.class);
			appConfigData.sampleElement().create();
			appConfigData.sampleElement().setValue("Example");
			appConfigData.activate(true);
			appMan.getLogger().debug("{} started with new config resource", getClass().getName());
		}
    }
    
    /*
     * register ResourcePatternDemands. The listeners will be informed about new and disappearing
     * patterns in the OGEMA resource tree
     */
    public void initDemands() {
    }

	public void close() {
    }

	/*
	 * if the app needs to consider dependencies between different pattern types,
	 * they can be processed here.
	 */
	public void processInterdependies() {
		// TODO Auto-generated method stub
		
	}
}
