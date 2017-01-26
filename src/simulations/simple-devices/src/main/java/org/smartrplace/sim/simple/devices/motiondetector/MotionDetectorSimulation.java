package org.smartrplace.sim.simple.devices.motiondetector;

import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.ogema.tools.simulation.service.api.model.SimulationConfiguration;
import org.ogema.tools.simulation.service.apiplus.SimulationBase;

/**
 */
public class MotionDetectorSimulation extends SimulationBase<MotionDetectorConfigurationPattern, MotionDetectorPattern>
		implements ResourceValueListener<FloatResource> {
	
	private static final long TEMPSENS_UPDATE_INTERVAL = 4000;
	static final String PROVIDER_ID = "Basic motion detector simulation";
	private static final String BASE_RESOURCE_SIM_OBJECTS = "basicSimulatedTemperatureSensors";

	public MotionDetectorSimulation(ApplicationManager am) {
		super(am, MotionDetectorPattern.class,true, MotionDetectorConfigurationPattern.class);  
	}	

	@Override
	public String getProviderId() {
		return PROVIDER_ID;
	}
	
	@Override
	public Class<? extends Resource> getSimulatedType() {
		return SensorDevice.class;
	}

	/** createSimulatedObject is called by the simulation framework when a new resource is created via the
	 * simulation GUI or a similar mechanism and the resource shall be simulation by this provider.
	 * TODO: Should we also create an entry in the SimulationConfigurationModel list automatically?
	 * TODO: provide this in the framework
	 */
	// TODO check that deviceId is a valid resource name(?) Or do that in framework?
	@Override
	public SensorDevice createSimulatedObject(String deviceId) {
		MotionDetectorPattern pattern = getTargetPattern(deviceId);		
		if (pattern == null) {
			try {
				// currently, the simulation GUI requires top level resources
//				@SuppressWarnings("unchecked")
//				ResourceList<TemperatureSensor> base = am.getResourceManagement().createResource(BASE_RESOURCE_SIM_OBJECTS, ResourceList.class);
//				base.setElementType(TemperatureSensor.class);
//				tempSens = rpa.addDecorator(base,deviceId, TemperatureSensorPattern.class);
				
				pattern = resourcePatternAccess.createResource(deviceId, MotionDetectorPattern.class);
//				if (tempSens == null) return null; 
				pattern.model.name().create();
//				switchBox.simulationProvider.setValue(PROVIDER_ID);
				pattern.model.name().setValue("Simulated motion detector" + (simulatedDevices.isEmpty() ? "" : " " + simulatedDevices.size()));
//				Room room = appManager.getResourceManagement().createResource(SimpleDevicesApp.ROOM_PATH, Room.class);
//				room.activate(false);
//				pattern.model.location().room().setAsReference(room);
				logger.info("New motion detector created "+  pattern.model.name());
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
	public void buildConfigurations(MotionDetectorPattern pattern, List<SimulationConfiguration> cfgs,MotionDetectorConfigurationPattern simPattern) {
		cfgs.add(new MotionDetectorConfiguration(simPattern));
	}
	
	@Override
	public void buildQuantities(MotionDetectorPattern pattern, List<SimulatedQuantity> quantities,MotionDetectorConfigurationPattern simPattern) {
		quantities.add(new MotionValue(pattern));
		quantities.add(new BrightnessValue(pattern));
	}
	
	@Override
	public String getDescription() {
		return "Simulated motion detector";
	}
	
	/** The targetPattern points to the simulated resource (typically a device). The
	 * configPattern points to the simulation configuration resource indicating the
	 * simulation time interval etc.
	 * @param timeStep time since last simulation step in milliseconds
	 */
	@Override
	public void simTimerElapsed(MotionDetectorPattern targetPattern, MotionDetectorConfigurationPattern configPattern, Timer t, long timeStep) {
		float prob = configPattern.probabilityFactor.getValue();
		boolean on = (Math.random() > (1-prob));
		targetPattern.motion.setValue(on);
	}


	// TODO test whether deactivation and reactivation works
	// TODO ensure this is called before patterns are activated(?) ... probably not the case
	// TODO do we need to register a value listener for the interval?
	@Override
	protected void initSimulation(MotionDetectorPattern targetPattern,MotionDetectorConfigurationPattern configPattern) {
		resourcePatternAccess.createOptionalResourceFields(targetPattern, MotionDetectorPattern.class, false);
		configPattern.probabilityFactor.setValue(0.3F);
		configPattern.probabilityFactor.addValueListener(this,true);
		targetPattern.batteryStatus.setValue(0.95F);
		resourcePatternAccess.activatePattern(targetPattern); // activates also the optional elements
	}
	
	@Override
	protected void removeSimulation(MotionDetectorPattern targetPattern, MotionDetectorConfigurationPattern configPattern) {
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
