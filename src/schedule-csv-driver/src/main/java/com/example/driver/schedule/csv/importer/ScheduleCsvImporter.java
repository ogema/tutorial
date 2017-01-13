package com.example.driver.schedule.csv.importer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

import com.example.driver.schedule.csv.importer.model.FolderConfiguration;
import com.example.driver.schedule.csv.importer.pattern.FolderConfigPattern;
import com.example.driver.schedule.csv.importer.patternlistener.FolderConfigListener;

import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.widgets.WidgetApp;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.navigation.NavigationMenu;
import de.iwes.widgets.pattern.widget.patternedit.PatternCreatorConfiguration;
import de.iwes.widgets.pattern.widget.patternedit.PatternPageUtil;

/**
 * A simple driver example for OGEMA. The driver watches a folder on the disk for new CSV files, 
 * and whenever one appears, it tries to parse its content as a time series, and write the 
 * result to an OGEMA schedule. The file name determines the schedule name. 
 * Multiple folder configurations can be created, and the driver
 * provides a very simple graphical user interface, where existing configurations can be edited, 
 * and new ones can be created.
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class ScheduleCsvImporter implements Application {
	
	public static final String urlPath = "/com/example/driver/schedule-csv-import";

    private OgemaLogger log;
    private ApplicationManager appManager;
    private ResourcePatternAccess patternAccess;
    private FolderConfigManager controller;
	private FolderConfigListener listener;
	private WidgetApp widgetApp;
	
	// only relevant if the driver itself provides a GUI
	@Reference
	private OgemaGuiService widgetService;

    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appMan) {

        // Remember framework references for later.
        appManager = appMan;
        patternAccess = appManager.getResourcePatternAccess();
        log = appManager.getLogger();
        controller = new FolderConfigManager(appMan);
        listener = new FolderConfigListener(patternAccess, log);
        patternAccess.addPatternDemand(FolderConfigPattern.class, listener, AccessPriority.PRIO_LOWEST);
        
        // create a simple GUI
        widgetApp = widgetService.createWidgetApp(urlPath, appMan);
        final PatternPageUtil patternPageUtil = PatternPageUtil.getInstance(appMan, widgetApp);
        // we want new configurations to be created below our global configuration resource
        PatternCreatorConfiguration<FolderConfigPattern, FolderConfiguration> config 
        		= new PatternCreatorConfiguration<>(controller.appConfigData.connections());
        // a page that allows the user to create new folder supervision configurations
        WidgetPage<?> createPage = patternPageUtil.newPatternCreatorPage(FolderConfigPattern.class, "config-creator.html", false, config, null).getPage();
        // a page that allows the user to edit existing folder supervision configurations
        WidgetPage<?> editPage = patternPageUtil.newPatternEditorPage(FolderConfigPattern.class, "config-editor.html", true, null).getPage();
        // add a navigation menu
        NavigationMenu menu = new NavigationMenu(" Browse pages");
        menu.addEntry("Create schedule import config", createPage);
        menu.addEntry("Edit schedule import configs", editPage);
        createPage.getMenuConfiguration().setCustomNavigation(menu);
		editPage.getMenuConfiguration().setCustomNavigation(menu);
        
        log.info("{} started", getClass().getName());
   }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
        log.info("{} being stopped", getClass().getName());
        if (widgetApp != null)
        	widgetApp.close();
        if (listener != null) {
        	patternAccess.removePatternDemand(FolderConfigPattern.class, listener);
        	listener.close();
        }
        listener = null;
        appManager = null;
        patternAccess = null;
        log = null;
        widgetApp = null;
    }
}
