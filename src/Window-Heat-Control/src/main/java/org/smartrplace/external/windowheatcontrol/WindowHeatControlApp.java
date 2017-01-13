package org.smartrplace.external.windowheatcontrol;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.smartrplace.external.windowheatcontrol.gui.MainPage;
import org.apache.felix.scr.annotations.Reference;
import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.widgets.WidgetApp;
import de.iwes.widgets.api.widgets.WidgetPage;

/**
 * Template OGEMA application class
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class WindowHeatControlApp implements Application {
	public static final String urlPath = "/org/smartrplace/external/windowheatcontrol";

    private OgemaLogger log;
    private ApplicationManager appMan;
    private WindowHeatControlController controller;

	private WidgetApp widgetApp;

	@Reference
	private OgemaGuiService guiService;

	public MainPage mainPage;

    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appManager) {

        // Remember framework references for later.
        appMan = appManager;
        log = appManager.getLogger();

        // 
        controller = new WindowHeatControlController(appMan);
		
		//register a web page with dynamically generated HTML
		widgetApp = guiService.createWidgetApp(urlPath, appManager);
		WidgetPage<?> page = widgetApp.createStartPage();
		mainPage = new MainPage(page, controller);
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
