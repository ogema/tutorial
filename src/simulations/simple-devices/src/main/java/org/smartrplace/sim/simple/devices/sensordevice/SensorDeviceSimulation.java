package org.smartrplace.sim.simple.devices.sensordevice;

import java.util.Iterator;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.model.Resource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.Transaction;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.tools.simulation.service.api.model.SimulatedQuantity;
import org.ogema.tools.simulation.service.api.model.SimulationConfiguration;
import org.ogema.tools.simulation.service.apiplus.SimulationBase;
import org.smartrplace.sim.simple.devices.motiondetector.MotionDetectorPattern;
import org.smartrplace.sim.simple.devices.sensordevice.quantities.BrightnessValue;
import org.smartrplace.sim.simple.devices.sensordevice.quantities.CO2Value;
import org.smartrplace.sim.simple.devices.sensordevice.quantities.HumidityValue;
import org.smartrplace.sim.simple.devices.sensordevice.quantities.TemperatureValue;

/**
 */
public class SensorDeviceSimulation extends SimulationBase<SensorDeviceConfigurationPattern, SensorDevicePattern>
		implements ResourceValueListener<TemperatureResource>{
	
	private static final long TEMPSENS_UPDATE_INTERVAL = 4000;
	static final String PROVIDER_ID = "Basic sensor devices simulation";
	private static final String BASE_RESOURCE_SIM_OBJECTS = "basicSimulatedTemperatureSensors";

	public SensorDeviceSimulation(ApplicationManager am) {
		super(am, SensorDevicePattern.class,true, SensorDeviceConfigurationPattern.class);  
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
		SensorDevicePattern pattern = getTargetPattern(deviceId);		
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
					pattern = resourcePatternAccess.addDecorator(parent, deviceId.substring(i+1), SensorDevicePattern.class);
				}
				else {
					pattern = resourcePatternAccess.createResource(deviceId, SensorDevicePattern.class);
				}
				pattern.temperature.setCelsius(20F);
//				if (tempSens == null) return null; 
				pattern.model.name().create();
//				switchBox.simulationProvider.setValue(PROVIDER_ID);
				pattern.model.name().setValue("Simulated sensor device" + (simulatedDevices.isEmpty() ? "" : " " + simulatedDevices.size()));
//				Room room = appManager.getResourceManagement().createResource(SimpleDevicesApp.ROOM_PATH, Room.class);
//				room.activate(false);
//				pattern.model.location().room().setAsReference(room);
				logger.info("New sensor device created "+  pattern.model.name());
				super.addConfigResource(pattern, TEMPSENS_UPDATE_INTERVAL); // activates all resources
//				switchBox.model.activate(true);  // done in addConfigResource already
			} catch (ResourceAlreadyExistsException e) {
				return null;
			}
		} else {
			// FIXME should we call initSimulation explicitly here? Since non-persistent resource values must be reinitialized...
			// or is this called anyway, even on unclean start... presumably not ->??
			patternAvailable(getSimPattern(deviceId));
		}
		return pattern.model;
	}
	
	@Override
	public void buildConfigurations(SensorDevicePattern pattern, List<SimulationConfiguration> cfgs,SensorDeviceConfigurationPattern simPattern) {
		cfgs.add(new LowerLimitConfiguration(simPattern));
		cfgs.add(new UpperLimitConfiguration(simPattern));
	}
	
	@Override
	public void buildQuantities(SensorDevicePattern pattern, List<SimulatedQuantity> quantities,SensorDeviceConfigurationPattern simPattern) {
		quantities.add(new TemperatureValue(pattern));
		quantities.add(new HumidityValue(pattern));
		quantities.add(new CO2Value(pattern));
		quantities.add(new BrightnessValue(pattern));
	}
	
	@Override
	public String getDescription() {
		return "Simulated temperature sensor, humidity sensor, CO2 sensor and brightness sensor";
	}
	
	/** The targetPattern points to the simulated resource (typically a device). The
	 * configPattern points to the simulation configuration resource indicating the
	 * simulation time interval etc.
	 * @param timeStep time since last simulation step in milliseconds
	 */
	@Override
	public void simTimerElapsed(SensorDevicePattern targetPattern, SensorDeviceConfigurationPattern configPattern, Timer t, long timeStep) {
		// temperature
		float lower = configPattern.lowerTemperature.getValue();
		float upper = configPattern.upperTemperature.getValue();
		if (upper < lower) {
			logger.warn("Inconsistent setting:  lower limit > upper limit: " + lower + ", " + upper + "; applying defaults");
			upper = 38 - 273.15F;
			lower = 10 - 273.15F;
			Transaction trans = appManager.getResourceAccess().createTransaction();
			trans.setFloat(configPattern.lowerTemperature, lower); 
			trans.setFloat(configPattern.upperTemperature, upper);
			trans.write();
			return; // changing the limits will cause another callback
		}
		float currentValue = targetPattern.temperature.getValue();
		if (lower > currentValue)
			currentValue  = lower;
		if (upper < currentValue) 
			currentValue = upper;
		float diff = upper-lower;
		currentValue = lower + (float) (Math.random() * diff); // FIXME smoother curve
		targetPattern.temperature.setValue(currentValue);
		if (logger.isTraceEnabled())
			logger.trace(" ~ new simulated temperature value " + targetPattern.temperature.getCelsius() +
				" in range [" + configPattern.lowerTemperature.getCelsius() + ", " + configPattern.upperTemperature.getCelsius() + "]");
		
		// humidity
		targetPattern.humidity.setValue((float) Math.random());
		// co2 sensor
		targetPattern.co2concentration.setValue(300 + (float) (Math.random() * 5000)); // TODO scale configurable (ppm)
		// brightness
		targetPattern.brightness.setValue((float) (Math.random() * 150000)); // TODO scale configurable (lux)
	}

	// TODO test whether deactivation and reactivation works
	// TODO ensure this is called before patterns are activated(?) ... probably not the case
	// TODO do we need to register a value listener for the interval?
	@Override
	protected void initSimulation(SensorDevicePattern targetPattern,SensorDeviceConfigurationPattern configPattern) {
		resourcePatternAccess.createOptionalResourceFields(targetPattern, SensorDevicePattern.class, false);
		targetPattern.temperature.setCelsius(20F);
		targetPattern.batteryStatus.setValue(0.95F);
		configPattern.lowerTemperature.setCelsius(10);
		configPattern.upperTemperature.setCelsius(38);
		configPattern.lowerTemperature.addValueListener(this, true);
		configPattern.upperTemperature.addValueListener(this,true);
		resourcePatternAccess.activatePattern(targetPattern); // activates also the optional elements
	}
	
	@Override
	protected void removeSimulation(SensorDevicePattern targetPattern,SensorDeviceConfigurationPattern configPattern) {
		configPattern.lowerTemperature.removeValueListener(this);
		configPattern.upperTemperature.removeValueListener(this);
	}
	
	// TODO
	@Override
	public void resourceChanged(TemperatureResource resource) {
		// no explicit action, simply trigger a simTimerElapsed action
		// we must trigger a simTimerElapsed here!
		SensorDeviceConfigurationPattern configPattern = new SensorDeviceConfigurationPattern(resource.getParent().getParent());
		Timer timer = getTimer(configPattern);
		long intv = timer.getTimingInterval();
		timer.stop();
		Iterator<TimerListener> listeners = timer.getListeners().iterator();
		while (listeners.hasNext()) 
			listeners.next().timerElapsed(timer);
		timer.setTimingInterval(intv);
		timer.resume();
	}
	
}
