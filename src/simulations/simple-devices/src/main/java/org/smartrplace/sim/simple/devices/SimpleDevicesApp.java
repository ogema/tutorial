package org.smartrplace.sim.simple.devices;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.tools.simulation.service.api.SimulationService;
import org.smartrplace.sim.simple.devices.doorwindowsensor.DoorWindowSensorSimulation;
import org.smartrplace.sim.simple.devices.motiondetector.MotionDetectorSimulation;
import org.smartrplace.sim.simple.devices.sensordevice.SensorDeviceSimulation;
import org.smartrplace.sim.simple.devices.switchbox.SwitchboxSimulation;
import org.smartrplace.sim.simple.devices.thermostat.ThermostatSimulation;

/**
 * Template OGEMA application class
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class SimpleDevicesApp implements Application {

//	public static final String ROOM_PATH = "simpleDevicesSimRoom";
    private OgemaLogger log;
    
    private ThermostatSimulation thermostatSimulation;
    private SwitchboxSimulation switchboxSimulation;
    private SensorDeviceSimulation sensorDeviceSimulation; 
    private MotionDetectorSimulation motionDetectorSimulation;
    private DoorWindowSensorSimulation doorWindowSimulation;

    @Reference
    private SimulationService simulationService;
    
   /*
     * This is the entry point to the application.
     */
 	@SuppressWarnings("unchecked")
	@Override
    public void start(ApplicationManager appManager) {

        // Remember framework references for later.
        log = appManager.getLogger();
        thermostatSimulation = new ThermostatSimulation(appManager);
        simulationService.registerSimulationProvider(thermostatSimulation);
        switchboxSimulation = new SwitchboxSimulation(appManager);
        simulationService.registerSimulationProvider(switchboxSimulation);
        sensorDeviceSimulation = new SensorDeviceSimulation(appManager);
        simulationService.registerSimulationProvider(sensorDeviceSimulation);
        motionDetectorSimulation = new MotionDetectorSimulation(appManager);
        simulationService.registerSimulationProvider(motionDetectorSimulation);
        doorWindowSimulation = new DoorWindowSensorSimulation(appManager);
        simulationService.registerSimulationProvider(doorWindowSimulation);
        // 
     }

     /*
     * Callback called when the application is going to be stopped.
     */
    @SuppressWarnings("unchecked")
	@Override
    public void stop(AppStopReason reason) {
    	if (thermostatSimulation != null) { 
    		simulationService.unregisterSimulationProvider(thermostatSimulation);
    		thermostatSimulation.close();
    	}
    	if (switchboxSimulation != null) {
    		simulationService.unregisterSimulationProvider(switchboxSimulation);
    		switchboxSimulation.close();
    	}
    	if (sensorDeviceSimulation != null) {
    		simulationService.unregisterSimulationProvider(sensorDeviceSimulation);
//    		sensorDeviceSimulation.close();
    	} 
    	if (motionDetectorSimulation != null)
    		simulationService.unregisterSimulationProvider(motionDetectorSimulation);
    	if (doorWindowSimulation != null)
    		simulationService.unregisterSimulationProvider(doorWindowSimulation);
        log.info("{} stopped", getClass().getName());
        thermostatSimulation = null;
        switchboxSimulation = null;
        sensorDeviceSimulation = null;
        motionDetectorSimulation  =null;
        doorWindowSimulation = null;
        log = null;
    }
    
 
}
