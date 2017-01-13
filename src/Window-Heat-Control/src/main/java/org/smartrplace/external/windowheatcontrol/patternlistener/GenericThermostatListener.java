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
import org.smartrplace.external.windowheatcontrol.pattern.DoorWindowSensorPattern;
import org.smartrplace.external.windowheatcontrol.pattern.GenericThermostatPattern;

import de.iwes.util.linkingresource.ExtendedAddElementResult;
import de.iwes.util.linkingresource.RoomHelper;

/**
 * A pattern listener for the TemplatePattern. It is informed by the framework 
 * about new pattern matches and patterns that no longer match.
 */
public class GenericThermostatListener implements PatternListener<GenericThermostatPattern> {
	
	private final WindowHeatControlController app;
	public final List<GenericThermostatPattern> availablePatterns = new ArrayList<>();
	
 	public GenericThermostatListener(WindowHeatControlController templateProcess) {
		this.app = templateProcess;
	}
	
	@Override
	public void patternAvailable(GenericThermostatPattern pattern) {
		availablePatterns.add(pattern);
		
		ExtendedAddElementResult mgmtResult =
				app.thermostatRoomMmgt.addElementExtended(pattern);
		if(mgmtResult.addedForTheFirstTime) {
			app.newInformationForRoom(mgmtResult.linkingResource);
		}
	}
	@Override
	public void patternUnavailable(GenericThermostatPattern pattern) {
		
		if(app.thermostatRoomMmgt.isEmpty(app.thermostatRoomMmgt.removeElement(pattern))) {
			app.newInformationForRoom(RoomHelper.getResourceLocationRoom(pattern.model));
		}
		
		availablePatterns.remove(pattern);
	}
	
	
}
