package com.example.androiddriver;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

/**
 * Template OGEMA driver class
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class SampleAndroiddriverApp implements Application {
	public static final String urlPath = "/org/smartrplace/external/sampleandroiddriver";

    private OgemaLogger log;
    private ApplicationManager appManager;
    private ResourcePatternAccess patternAccess;
	private SampleAndroiddriverConectionManager connectionManager;

    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appMan) {

        // Remember framework references for later.
        appManager = appMan;
        patternAccess = appManager.getResourcePatternAccess();
        log = appManager.getLogger();
        connectionManager = new SampleAndroiddriverConectionManager(appManager);
        log.info("{} started", getClass().getName());
   }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
        log.info("{} being stopped", getClass().getName());
        if(connectionManager != null) {
        	connectionManager.close();
        	connectionManager = null;
        }
        appManager = null;
        patternAccess = null;
        log = null;
    }
}
