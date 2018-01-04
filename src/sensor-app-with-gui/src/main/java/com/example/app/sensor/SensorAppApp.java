package com.example.app.sensor;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

import com.example.app.sensor.gui.MainPage;
import com.example.app.sensor.pattern.SensorPattern;

import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.widgets.WidgetApp;
import de.iwes.widgets.api.widgets.WidgetPage;

/**
 * Template OGEMA application class
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class SensorAppApp implements Application {
	public static final String urlPath = "/org/smartrplace/external/sensorappv2";

    private OgemaLogger log;
    private ApplicationManager appMan;
    private SensorAppController controller;

    // injected by OSGi
    @Reference
    OgemaGuiService widgetService;
    private WidgetApp widgetApp;

	public MainPage mainPage;
   
    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appManager) {

        // Remember framework references for later.
        appMan = appManager;
        log = appManager.getLogger();

        controller = new SensorAppController(appMan);
        
        // GUI
        widgetApp = widgetService.createWidgetApp(urlPath, appManager);
        // a tool to create very simple user pages based on a ResourcePattern declaration
        //PatternPageUtil ppu = PatternPageUtil.getInstance(appManager, widgetApp);
        // this will display all matches for the SensorPattern template on a page
        // in particular, registering the page as start page for this app (third argument: true)
        // implies that a tile will be created for our app on the standard OGEMA start screen
        //ppu.newPatternDisplayPage(SensorPattern.class, "index.html", true, null);
        
        ResourcePatternAccess patternAccess = appMan.getResourcePatternAccess();
        // retrieve all currently existing SensorPattern matches; below we will use a PatternListener to 
        // track new and removed sensors as well.
        List<SensorPattern> sensors = patternAccess.getPatterns(SensorPattern.class, AccessPriority.PRIO_LOWEST);
        System.out.println("Initial sensor count is " + sensors.size());
		
		//register a web page with dynamically generated HTML
		WidgetPage<?> page_1 = widgetApp.createStartPage();
		mainPage = new MainPage(page_1, appMan);
     }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
    	if (widgetApp != null) widgetApp.close();
		if (controller != null)
    		controller.close();
        log.info("{} stopped", getClass().getName());
    }
}
