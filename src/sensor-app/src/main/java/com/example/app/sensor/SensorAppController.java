package com.example.app.sensor;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.tools.resource.util.LoggingUtils;

import com.example.app.sensor.pattern.SensorPattern;
import com.example.app.sensor.patternlistener.SensorListener;

// here the controller logic is implemented
public class SensorAppController {

	public OgemaLogger log;
    public ApplicationManager appMan;
    private ResourcePatternAccess advAcc;

	public SensorListener sensorListener;
    
	public SensorAppController(ApplicationManager appMan) {
		this.appMan = appMan;
		this.log = appMan.getLogger();
		this.advAcc = appMan.getResourcePatternAccess();
		
         // create or retrieve a resource that will store the sensor count. The value will be persisted and be
         // available after a restart of the system (unless it is clean start, which wipes out the resource database).
         nrSensors = appMan.getResourceManagement().createResource("sensorCount", IntegerResource.class);
         nrSensors.activate(false);
         // values of the nrSensors resource will be logged on every change
         LoggingUtils.activateLogging(nrSensors, -2); 

         initDemands();
    }

	public IntegerResource nrSensors;

    /*
     * register ResourcePatternDemands. The listeners will be informed about new and disappearing
     * patterns in the OGEMA resource tree
     */
    public void initDemands() {
		sensorListener = new SensorListener(this);
		advAcc.addPatternDemand(SensorPattern.class, sensorListener, AccessPriority.PRIO_LOWEST);
    }

	public void close() {
		advAcc.removePatternDemand(SensorPattern.class, sensorListener);
    }

	/*
	 * if the app needs to consider dependencies between different pattern types,
	 * they can be processed here.
	 */
	public void processInterdependies() {
		// TODO Auto-generated method stub
		
	}
}
