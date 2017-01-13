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
import org.ogema.tools.resource.util.ResourceUtils;
import org.smartrplace.external.windowheatcontrol.WindowHeatControlController;
import org.smartrplace.external.windowheatcontrol.pattern.DoorWindowSensorPattern;

import de.iwes.util.linkingresource.ExtendedAddElementResult;

/**
 * A pattern listener for the TemplatePattern. It is informed by the framework 
 * about new pattern matches and patterns that no longer match.
 */
public class DoorWindowSensorListener implements PatternListener<DoorWindowSensorPattern> {
	
	private final WindowHeatControlController app;
	public final List<DoorWindowSensorPattern> availablePatterns = new ArrayList<>();
	
 	public DoorWindowSensorListener(WindowHeatControlController templateProcess) {
		this.app = templateProcess;
	}
	
	@Override
	public void patternAvailable(DoorWindowSensorPattern pattern) {
		availablePatterns.add(pattern);
		
		ExtendedAddElementResult mgmtResult =
				app.windowRoomMmgt.addElementExtended(pattern);
		app.newInformationForRoom(mgmtResult.linkingResource);
	}
	@Override
	public void patternUnavailable(DoorWindowSensorPattern pattern) {
		app.windowRoomMmgt.removeElement(pattern);
		app.newInformationForRoom(ResourceUtils.getDeviceRoom(pattern.model.getLocationResource()));
		
		availablePatterns.remove(pattern);
	}
	
	
}
