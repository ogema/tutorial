package org.smartrplace.sim.simple.devices.doorwindowsensor;

import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.DoorWindowSensor;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.ogema.tools.simulation.service.api.model.SimulationConfiguration;
import org.ogema.tools.simulation.service.apiplus.SimulationBase;
import org.smartrplace.sim.simple.devices.SimpleDevicesApp;
import org.smartrplace.sim.simple.devices.switchbox.SwitchboxPattern;

/**
 */
public class DoorWindowSensorSimulation extends SimulationBase<DoorWindowSensorConfigurationPattern, DoorWindowSensorPattern>
		implements ResourceValueListener<FloatResource> {
	
	private static final long TEMPSENS_UPDATE_INTERVAL = 10000;
	static final String PROVIDER_ID = "Basic door or window sensor simulation";

	public DoorWindowSensorSimulation(ApplicationManager am) {
		super(am, DoorWindowSensorPattern.class,true, DoorWindowSensorConfigurationPattern.class);  
	}	

	@Override
	public String getProviderId() {
		return PROVIDER_ID;
	}
	
	@Override
	public Class<? extends Resource> getSimulatedType() {
		return DoorWindowSensor.class;
	}

	/** createSimulatedObject is called by the simulation framework when a new resource is created via the
	 * simulation GUI or a similar mechanism and the resource shall be simulation by this provider.
	 * TODO: Should we also create an entry in the SimulationConfigurationModel list automatically?
	 * TODO: provide this in the framework
	 */
	// TODO check that deviceId is a valid resource name(?) Or do that in framework?
	@Override
	public DoorWindowSensor createSimulatedObject(String deviceId) {
		DoorWindowSensorPattern pattern = getTargetPattern(deviceId);		
		if (pattern == null) {
			try {
				// currently, the simulation GUI requires top level resources
//				@SuppressWarnings("unchecked")
//				ResourceList<TemperatureSensor> base = am.getResourceManagement().createResource(BASE_RESOURCE_SIM_OBJECTS, ResourceList.class);
//				base.setElementType(TemperatureSensor.class);
//				tempSens = rpa.addDecorator(base,deviceId, TemperatureSensorPattern.class);
				if (deviceId.indexOf('/') > 0) {
					final int i = deviceId.lastIndexOf('/');
					Resource parent = appManager.getResourceAccess().getResource(deviceId.substring(0, i));
					if (parent == null || !parent.exists()) 
						throw new IllegalArgumentException("Specified parent resource " +deviceId.substring(0, i) + " does not exist");
					pattern = resourcePatternAccess.addDecorator(parent, deviceId.substring(i+1), DoorWindowSensorPattern.class);
				}
				else {
					pattern = resourcePatternAccess.createResource(deviceId, DoorWindowSensorPattern.class);
				}
				if (!pattern.stateOfChargeSensor.exists())
					throw new VirtualResourceException("Resource unexpectedly found virtual: " + pattern.stateOfChargeSensor);
//				if (tempSens == null) return null; 
				pattern.model.name().create();
//				switchBox.simulationProvider.setValue(PROVIDER_ID);
				pattern.model.name().setValue("Simulated door/window sensor" + (simulatedDevices.isEmpty() ? "" : " " + simulatedDevices.size()));
//				Room room = appManager.getResourceManagement().createResource(SimpleDevicesApp.ROOM_PATH, Room.class);
//				room.activate(false);
//				pattern.model.location().room().setAsReference(room);
				logger.info("New door/window sensor created "+  pattern.model.name());
				super.addConfigResource(pattern, TEMPSENS_UPDATE_INTERVAL); // activates all resources
//				switchBox.model.activate(true);  // done in addConfigResource already
			} catch (ResourceAlreadyExistsException e) {
				return null;
			}
		} else {
			patternAvailable(getSimPattern(deviceId));
		}
		return pattern.model;
	}
	
	@Override
	public void buildConfigurations(DoorWindowSensorPattern pattern, List<SimulationConfiguration> cfgs,DoorWindowSensorConfigurationPattern simPattern) {
		cfgs.add(new ProbabilityConfiguration(simPattern));
	}
	
	@Override
	public void buildQuantities(DoorWindowSensorPattern pattern, List<SimulatedQuantity> quantities,DoorWindowSensorConfigurationPattern simPattern) {
		quantities.add(new WindowStatus(pattern));
	}
	
	@Override
	public String getDescription() {
		return "Simulated door/window sensor";
	}
	
	/** 
	 * The targetPattern points to the simulated resource (typically a device). The
	 * configPattern points to the simulation configuration resource indicating the
	 * simulation time interval etc.
	 * @param timeStep time since last simulation step in milliseconds
	 */
	@Override
	public void simTimerElapsed(DoorWindowSensorPattern targetPattern, DoorWindowSensorConfigurationPattern configPattern, Timer t, long timeStep) {
		float prob = configPattern.probabilityFactor.getValue();
		boolean on = (Math.random() > (1-prob));
		targetPattern.open.setValue(on);

	}

	// TODO test whether deactivation and reactivation works
	// TODO ensure this is called before patterns are activated(?) ... probably not the case
	// TODO do we need to register a value listener for the interval?
	@Override
	protected void initSimulation(DoorWindowSensorPattern targetPattern, DoorWindowSensorConfigurationPattern configPattern) {
		// FIXME
		System.out.println("    ~~~ initSimulation called for doorWindowPattern");
		resourcePatternAccess.createOptionalResourceFields(targetPattern, DoorWindowSensorPattern.class, false);
		configPattern.probabilityFactor.setValue(0.3F);
		configPattern.probabilityFactor.addValueListener(this,true);
		targetPattern.batteryStatus.setValue(0.95F);
		resourcePatternAccess.activatePattern(targetPattern); // activates also the optional elements
	}
	
	@Override
	protected void removeSimulation(DoorWindowSensorPattern targetPattern, DoorWindowSensorConfigurationPattern configPattern) {
		configPattern.probabilityFactor.removeValueListener(this);
	}

	/**
	 * The only purpose is to check that the probability value is in its allowed value range
	 */
	@Override
	public void resourceChanged(FloatResource resource) {
		float probability = resource.getValue();
		if (probability < 0)
			resource.setValue(0);
		else if (probability > 1)
			resource.setValue(1);
		return;
	}

	
}
