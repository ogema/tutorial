package org.smartrplace.sim.simple.devices.switchbox;

import java.util.Iterator;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.ogema.tools.simulation.service.api.model.SimulationConfiguration;
import org.ogema.tools.simulation.service.apiplus.SimulationBase;
import org.smartrplace.sim.simple.devices.switchbox.quantities.CurrentValue;
import org.smartrplace.sim.simple.devices.switchbox.quantities.EnergyValue;
import org.smartrplace.sim.simple.devices.switchbox.quantities.FrequencyValue;
import org.smartrplace.sim.simple.devices.switchbox.quantities.PowerValue;
import org.smartrplace.sim.simple.devices.switchbox.quantities.VoltageValue;

/**
 */
public class SwitchboxSimulation extends SimulationBase<SwitchboxConfigurationPattern, SwitchboxPattern>
		implements ResourceValueListener<BooleanResource>{
	
	private static final long SWITCHBOX_UPDATE_INTERVAL = 4000;
	static final String PROVIDER_ID = "Basic switchbox simulation";

	private final OgemaLogger logger;
	
	public SwitchboxSimulation(ApplicationManager am) {
		super(am, SwitchboxPattern.class,true, SwitchboxConfigurationPattern.class);  
		this.logger = am.getLogger();
	}	

	@Override
	public String getProviderId() {
		return PROVIDER_ID;
	}
	
	@Override
	public Class<? extends Resource> getSimulatedType() {
		return SingleSwitchBox.class;
	}

	/** createSimulatedObject is called by the simulation framework when a new resource is created via the
	 * simulation GUI or a similar mechanism and the resource shall be simulated by this provider.
	 * TODO: Should we also create an entry in the SimulationConfigurationModel list automatically?
	 * TODO: provide this in the framework
	 */
	@Override
	public SingleSwitchBox createSimulatedObject(String deviceId) {
		SwitchboxPattern switchBox = getTargetPattern(deviceId);		
		if (switchBox == null) {
			try {
				switchBox = resourcePatternAccess.createResource(deviceId, SwitchboxPattern.class);
				if (switchBox == null) return null; 
				switchBox.model.name().create();
//				switchBox.simulationProvider.setValue(PROVIDER_ID);
				switchBox.model.name().setValue("Simulated Switchbox" + (simulatedDevices.isEmpty() ? "" : " " + simulatedDevices.size()));
//				Room room = appManager.getResourceManagement().createResource(SimpleDevicesApp.ROOM_PATH, Room.class);
//				switchBox.model.location().room().setAsReference(room);
//				room.activate(false);
				logger.info("New switchbox created "+  switchBox.model.name());
				super.addConfigResource(switchBox, SWITCHBOX_UPDATE_INTERVAL); // activates all resources
//				switchBox.model.activate(true);  // done in addConfigResource already
			} catch (ResourceAlreadyExistsException e) {
				return null;
			}
		} else {
			patternAvailable(getSimPattern(deviceId));
		}
		return switchBox.model;
	}
	
	@Override
	public void buildConfigurations(SwitchboxPattern pattern, List<SimulationConfiguration> cfgs,SwitchboxConfigurationPattern simPattern) {
	}
	
	@Override
	public void buildQuantities(SwitchboxPattern pattern, List<SimulatedQuantity> quantities,SwitchboxConfigurationPattern simPattern) {
		quantities.add(new PowerValue(pattern));
		quantities.add(new CurrentValue(pattern));
		quantities.add(new VoltageValue(pattern));
		quantities.add(new EnergyValue(pattern));
		quantities.add(new FrequencyValue(pattern));
	}
	
	@Override
	public String getDescription() {
		return "Simulated switchbox";
	}
	
	/** The targetPattern points to the simulated resource (typically a device). The
	 * configPattern points to the simulation configuration resource indicating the
	 * simulation time interval etc.
	 * @param timeStep time since last simulation step in milliseconds
	 */
	@Override
	public void simTimerElapsed(SwitchboxPattern targetPattern,	SwitchboxConfigurationPattern configPattern, Timer t, long timeStep) {
		boolean isOn = targetPattern.stateFeedback.getValue();
		float oldPower = targetPattern.power.getValue();		
		float newEnergy = targetPattern.energy.getValue() + oldPower * timeStep / 1000;
		float newVoltage = 230 + ((float) Math.random() * 10 - 5);
		float newCurrent = 0;
		float newPower = 0;
		if (isOn) {
			newCurrent = (float) Math.random() * 15; // TODO configurable
			newPower = newVoltage * newCurrent; 
		}
		targetPattern.power.setValue(newPower);
		targetPattern.energy.setValue(newEnergy);
		targetPattern.current.setValue(newCurrent);
		targetPattern.voltage.setValue(newVoltage);
		targetPattern.frequency.setValue(50 + (float) (Math.random() * 0.04 - 0.02));
		//TODO: perform simulation here
	}
	
	/** 
	 * FIXME: this is never called, what is it good for? -> introduce in super class, and make sure it is actually called
	 * -> actually need to pass targetPattern, so that listeners can be unregistered!
	 */
	public void close() {
		//TODO: perform closing operations if the simulation requires such
	}

	// TODO test whether deactivation and reactivation works
	@Override
	protected void initSimulation(SwitchboxPattern targetPattern,SwitchboxConfigurationPattern pattern) {
		resourcePatternAccess.createOptionalResourceFields(targetPattern, SwitchboxPattern.class, false);
		targetPattern.controllable.setValue(true);
		targetPattern.stateControl.addValueListener(this, true);
		resourcePatternAccess.activatePattern(targetPattern); // activates also the optional elements
	}
	
	@Override
	protected void removeSimulation(SwitchboxPattern targetPattern,SwitchboxConfigurationPattern configPattern) {
		targetPattern.stateControl.removeValueListener(this);
	}
	
	@Override
	public void resourceChanged(BooleanResource resource) {
		BooleanResource feedback = resource.<OnOffSwitch> getParent().stateFeedback();
		boolean newValue = resource.getValue();
		feedback.setValue(newValue);
		// we must trigger a simTimerElapsed here!
		
		Timer timer = getTimer(resource.getParent().getParent());
		long intv = timer.getTimingInterval();
		timer.stop();
		Iterator<TimerListener> listeners = timer.getListeners().iterator();
		while (listeners.hasNext()) 
			listeners.next().timerElapsed(timer);
		timer.setTimingInterval(intv);
		timer.resume();
	}
	
}
