package com.example.app.hello.ogema;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.model.simple.StringResource;

import de.iwes.util.format.StringFormatHelper;

/**
 * A simple app that creates one resource and a timer, and periodically prints 
 * the resource content to the console. 
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class HelloOgemaApp implements Application {
	
    private ApplicationManager appMan;
    private long startTime;
    private Timer responseTimer;

    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appManager) {

        // Remember framework references for later.
        appMan = appManager;
        startTime = appManager.getFrameworkTime();
        // create a new Resource; if a top-level Resource with the given name already exists, it is returned 
        final StringResource helloResource = appManager.getResourceManagement().createResource("helloOGEMA", StringResource.class);
        if (!helloResource.isActive()) { // in an unclean start the resource could be there already
        	helloResource.setValue("Hello OGEMA!");
        	helloResource.activate(false);
        }
        // a timer that is triggered every 10s
        this.responseTimer = appMan.createTimer(10000, new TimerListener() {
			
			@Override
			public void timerElapsed(Timer timer) {
				// print the content of helloResource to the console
				System.out.println("Timer elapsed at " + StringFormatHelper.getFullTimeDateInLocalTimeZone(appMan.getFrameworkTime()) + "; message: "
						+ helloResource.getValue());
				// destroy timer after 5 minutes
				if (appMan.getFrameworkTime() - startTime > 5*60000) {
					responseTimer.destroy();
				}
			}
			
        });

     }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
    	if (responseTimer.isRunning())
    		responseTimer.destroy();
        appMan = null;
        responseTimer = null;
    }
}
