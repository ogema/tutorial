package com.example.app.windowheatcontrol.patternlistener;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.windowheatcontrol.WindowHeatControlApp;
import com.example.app.windowheatcontrol.pattern.ElectricityStoragePattern;

/**
 * A pattern listener for the ElectricityStoragePattern. It is informed by the framework 
 * about new pattern matches and patterns that no longer match.
 */
public class ElectricityStorageListener implements PatternListener<ElectricityStoragePattern> {
	
	private final static Logger logger = LoggerFactory.getLogger(WindowHeatControlApp.class);
	private final List<ElectricityStoragePattern> availablePatterns = new ArrayList<>();
	// volatile is necessary here because we access the battery both from the app thread 
	// (RoomController via listener callbacks) and a GUI thread (see MainPage)
	private volatile ElectricityStoragePattern activeBattery = null;

	@Override
	public void patternAvailable(ElectricityStoragePattern pattern) {
		availablePatterns.add(pattern);
		if (isStandaloneBattery(pattern)) {
			if (activeBattery != null) {
				logger.warn("Two applicable batteries found!");
			}
			logger.info("New battery found: {}", pattern.model.getLocation());
			activeBattery = pattern;
		}
	}
	
	@Override
	public void patternUnavailable(ElectricityStoragePattern pattern) {
		availablePatterns.remove(pattern);
		if (activeBattery.model.equalsLocation(pattern.model)) {
			activeBattery = null;
			// check whether we can use another battery instead
			for (ElectricityStoragePattern battery: availablePatterns) {
				if (isStandaloneBattery(battery)) {
					logger.info("Changing active battery to {}", battery.model.getLocation());
					activeBattery = battery;
					break;
				}
			}
		}
	}
	
	/** 
	* Here we decide whether or not to use the battery for management.
	* Using only top-level resources ensures, that the battery is not part of some other device.
	* Batteries are often included in radio devices, for instances, in which case they are normally 
	* modeled as subresources of the device, but might also have a link to the parent device
	* via their <tt>location().device()</tt> subresource.<br>
	* Additional criteria could be included here, such as a minimum capacity.
	*/
	private static boolean isStandaloneBattery(ElectricityStoragePattern battery) {
		return (battery.model.isTopLevel() && !battery.model.location().device().isActive());
	}
	
	/**
	 * May be null, if no battery is available
	 * @return
	 */
	public ElectricityStoragePattern getActiveBattery() {
		return activeBattery;
	}
}
