package com.example.androiddriver;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Application.AppStopReason;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

import com.example.androiddriver.pattern.SampleAndroiddriverPattern;
import com.example.androiddriver.patternlistener.SampleAndroiddriverListener;

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
    private SampleAndroiddriverListener connectionListener;
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
        connectionListener = new SampleAndroiddriverListener(appManager);
        patternAccess.addPatternDemand(SampleAndroiddriverPattern.class, connectionListener, AccessPriority.PRIO_LOWEST);    
        log.info("{} started", getClass().getName());
   }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
        log.info("{} being stopped", getClass().getName());
        patternAccess.removePatternDemand(SampleAndroiddriverPattern.class, connectionListener);
        if(connectionManager != null) {
        	connectionManager.close();
        	connectionManager = null;
        }
        connectionListener = null;
        appManager = null;
        patternAccess = null;
        log = null;
    }
}
