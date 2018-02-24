package com.example.app.evaluationofflinecontrol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;

import com.example.app.evaluationofflinecontrol.gui.DetailPage;
import com.example.app.evaluationofflinecontrol.gui.MainPage;
import com.example.app.evaluationofflinecontrol.gui.complex.ComplexDependencyExamplePage;

import de.iwes.timeseries.eval.api.EvaluationProvider;
import de.iwes.timeseries.eval.base.provider.BasicEvaluationProvider;
import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.widgets.WidgetApp;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.navigation.MenuConfiguration;
import de.iwes.widgets.api.widgets.navigation.NavigationMenu;

/**
 * Template OGEMA application class
 */
@References({
	@Reference(
		name="evaluationProviders",
		referenceInterface=EvaluationProvider.class,
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
		policy=ReferencePolicy.DYNAMIC,
		bind="addEvalProvider",
		unbind="removeEvalProvider"),
})
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class MultiServicePageApp implements Application {
	public static final String urlPath = "/com/example/app/multiservicepageex";

    private OgemaLogger log;
    private ApplicationManager appMan;
    private MultiServicePageController controller;

	private WidgetApp widgetApp;

	@Reference
	private OgemaGuiService guiService;
	
	public MainPage mainPage;
	public DetailPage offlineEvalPage;
	public ComplexDependencyExamplePage complexExamplePage;
	
	private final Map<String,EvaluationProvider> evaluationProviders = Collections.synchronizedMap(new LinkedHashMap<String,EvaluationProvider>());
	public Map<String, EvaluationProvider> getEvaluations() {
		return evaluationProviders;
	}

	public BasicEvaluationProvider basicEvalProvider = null;

	/*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appManager) {

        // Remember framework references for later.
        appMan = appManager;
        log = appManager.getLogger();

        // 
        controller = new MultiServicePageController(appMan, this);
		
		//register a web page with dynamically generated HTML
		widgetApp = guiService.createWidgetApp(urlPath, appManager);
		WidgetPage<?> page = widgetApp.createStartPage();
		mainPage = new MainPage(page, controller);
		WidgetPage<?> page1 = widgetApp.createWidgetPage("Details.html");
		offlineEvalPage = new DetailPage(page1, controller);

		//This entry can be deleted if the complex page example is removed
		WidgetPage<?> page2 = widgetApp.createWidgetPage("ComplexExample.html");
		complexExamplePage = new ComplexDependencyExamplePage(page2, controller);

		NavigationMenu menu = new NavigationMenu("Select Page");
		menu.addEntry("Overview Page", page);
		menu.addEntry("Details Page", page1);
		menu.addEntry("Complex Example Page", page2);
		
		MenuConfiguration mc = page.getMenuConfiguration();
		mc.setCustomNavigation(menu);
		mc = page1.getMenuConfiguration();
		mc.setCustomNavigation(menu);
		mc = page2.getMenuConfiguration();
		mc.setCustomNavigation(menu);
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
    
    protected void addEvalProvider(EvaluationProvider provider) {
    	evaluationProviders.put(provider.id(), provider);
    	if((provider instanceof BasicEvaluationProvider)&&(basicEvalProvider == null)) {
    		basicEvalProvider = (BasicEvaluationProvider) provider;
    	}
    }
    
    protected void removeEvalProvider(EvaluationProvider provider) {
    	evaluationProviders.remove(provider.id());
    	//TODO: What should we do if electrcityEvalProvider is lost?
    }

}
