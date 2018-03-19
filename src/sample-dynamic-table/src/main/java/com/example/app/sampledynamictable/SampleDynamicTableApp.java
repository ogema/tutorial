package com.example.app.sampledynamictable;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import com.example.app.sampledynamictable.gui.MainPage;
import org.apache.felix.scr.annotations.Reference;
import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.widgets.WidgetApp;
import de.iwes.widgets.api.widgets.WidgetPage;

/**
 * Template OGEMA application class
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class SampleDynamicTableApp implements Application {
	public static final String urlPath = "/com/example/app/sampledynamictable";

    private OgemaLogger log;
    private ApplicationManager appMan;

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

		
		//register a web page with dynamically generated HTML
		widgetApp = guiService.createWidgetApp(urlPath, appManager);
		WidgetPage<?> page_1 = widgetApp.createStartPage();
		mainPage = new MainPage(page_1, appMan);
     }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
    	if (widgetApp != null) widgetApp.close();
        log.info("{} stopped", getClass().getName());
    }
}
