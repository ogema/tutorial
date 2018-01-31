package com.example.app.evaluationofflinecontrol;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;

public class MultiServicePageController {

	public OgemaLogger log;
    public ApplicationManager appMan;

	public final MultiServicePageApp serviceAccess;
	
    public MultiServicePageController(ApplicationManager appMan,MultiServicePageApp evaluationOCApp) {
		this.appMan = appMan;
		this.log = appMan.getLogger();
		this.serviceAccess = evaluationOCApp;
	}

    
	public void close() {
    }
}
