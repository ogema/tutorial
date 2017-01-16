package com.example.app.sensor.app;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.tools.resource.util.LoggingUtils;

import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.widgets.WidgetApp;
import de.iwes.widgets.pattern.widget.patternedit.PatternPageUtil;

/**
 * An OGEMA app that listens to sensors, and prints information about
 * new and disappearing sensors to the console. Furthermore, it keeps track
 * of the number of resources in an IntegerResource, and enables logging for 
 * it.
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class SensorApp implements Application {
	
	public static final String urlPath = "/com/example/app/sensor/app";
	
    private ApplicationManager appMan;
    private ResourcePatternAccess patternAccess;
    private SensorListener listener;
    private WidgetApp widgetApp;
    
    // injected by OSGi
    @Reference
    OgemaGuiService widgetService;

    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appManager) {

        // Remember framework references for later.
        appMan = appManager;
        patternAccess = appManager.getResourcePatternAccess();
        // retrieve all currently existing SensorPattern matches; below we will use a PatternListener to 
        // track new and removed sensors as well.
    	List<SensorPattern> sensors = patternAccess.getPatterns(SensorPattern.class, AccessPriority.PRIO_LOWEST);
    	System.out.println("Initial sensor count is " + sensors.size());
    	// create or retrieve a resource that will store the sensor count. The value will be persisted and be
    	// available after a restart of the system (unless it is clean start, which wipes out the resource database).
        IntegerResource nrSensors = appManager.getResourceManagement().createResource("sensorCount", IntegerResource.class);
        nrSensors.activate(false);
        // values of the nrSensors resource will be logged on every change
        LoggingUtils.activateLogging(nrSensors, -2); 
        listener = new SensorListener(nrSensors);
        // this will cause the listener to be informed about all existing as well as new and disappearing 
        // sensors in the system, which match the SensorPattern template.
        patternAccess.addPatternDemand(SensorPattern.class, listener, AccessPriority.PRIO_LOWEST);
        
        // GUI
        widgetApp = widgetService.createWidgetApp(urlPath, appManager);
        // a tool to create very simple user pages based on a ResourcePattern declaration
        PatternPageUtil ppu = PatternPageUtil.getInstance(appManager, widgetApp);
        // this will display all matches for the SensorPattern template on a page
        // in particular, registering the page as start page for this app (third argument: true)
        // implies that a tile will be created for our app on the standard OGEMA start screen
        ppu.newPatternDisplayPage(SensorPattern.class, "index.html", true, null);

     }

     /*
     * Callback called when the application is going to be stopped.
     * The object may still be around, and might be restarted later, therefore it makes sense to
     * clear references to global objects.
     */
    @Override
    public void stop(AppStopReason reason) {
    	if (patternAccess != null)
    		patternAccess.removePatternDemand(SensorPattern.class, listener);
    	if (widgetApp != null)
    		widgetApp.close();
        appMan = null;
        patternAccess = null;
        listener = null;
        widgetApp = null;
    }
    
}
