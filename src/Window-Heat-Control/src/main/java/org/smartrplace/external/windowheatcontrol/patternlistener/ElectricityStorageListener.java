/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.smartrplace.external.windowheatcontrol.patternlistener;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.resourcemanager.pattern.PatternListener;

import org.smartrplace.external.windowheatcontrol.WindowHeatControlController;
import org.smartrplace.external.windowheatcontrol.pattern.ElectricityStoragePattern;

/**
 * A pattern listener for the TemplatePattern. It is informed by the framework 
 * about new pattern matches and patterns that no longer match.
 */
public class ElectricityStorageListener implements PatternListener<ElectricityStoragePattern> {
	
	private final WindowHeatControlController app;
	public final List<ElectricityStoragePattern> availablePatterns = new ArrayList<>();
	
 	public ElectricityStorageListener(WindowHeatControlController templateProcess) {
		this.app = templateProcess;
	}
	
	@Override
	public void patternAvailable(ElectricityStoragePattern pattern) {
		availablePatterns.add(pattern);
		
		if(pattern.model.isTopLevel()) {
			if(app.batteryToUse != null) {
				app.log.warn("Two top-level batteries found!");
			}
			app.batteryToUse = pattern;
		}
	}
	@Override
	public void patternUnavailable(ElectricityStoragePattern pattern) {
		if(app.batteryToUse.model.equalsLocation(pattern.model)) {
			app.batteryToUse = null;
		}
		
		availablePatterns.remove(pattern);
	}
	
	
}
