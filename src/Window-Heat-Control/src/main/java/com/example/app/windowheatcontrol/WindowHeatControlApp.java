package com.example.app.windowheatcontrol;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;

import com.example.app.windowheatcontrol.gui.MainPage;

import org.apache.felix.scr.annotations.Reference;
import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.widgets.WidgetApp;
import de.iwes.widgets.api.widgets.WidgetPage;

/**
 * A sample heat control app, that takes into account a battery
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class WindowHeatControlApp implements Application {
	
	public static final String urlPath = "/com/example/app/windowheatcontrol";

    private ApplicationManager appMan;
    private WindowHeatControlController controller;
	private WidgetApp widgetApp;

	// service injected by OSGi
	@Reference
	private OgemaGuiService guiService;

    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appManager) {

        // Remember framework references for later.
        appMan = appManager;

        // contains the management logic
        controller = new WindowHeatControlController(appMan);
		
		//register a web page with dynamically generated HTML
		widgetApp = guiService.createWidgetApp(urlPath, appManager);
		WidgetPage<?> page = widgetApp.createStartPage();
		new MainPage(page, controller, controller.electricityStorageListener);
     }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
    	if (widgetApp != null) 
    		widgetApp.close();
		if (controller != null)
    		controller.close();
		if (appMan != null)
			appMan.getLogger().info("{} stopped", getClass().getName());
        widgetApp = null;
        controller = null;
        appMan = null;
    }
}
