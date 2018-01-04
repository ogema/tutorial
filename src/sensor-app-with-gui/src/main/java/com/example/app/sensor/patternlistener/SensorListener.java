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
package com.example.app.sensor.patternlistener;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.model.sensors.Sensor;

import com.example.app.sensor.SensorAppController;
import com.example.app.sensor.pattern.SensorPattern;

/**
 * Keeps track of all {@link Sensor}s in the system. 
 */
public class SensorListener implements PatternListener<SensorPattern> {
	
    private final IntegerResource numberSensors;

 	public SensorListener(SensorAppController templateProcess) {
		this.numberSensors = templateProcess.nrSensors;
	}
	
 	@Override
 	public void patternAvailable(SensorPattern pattern) {
 		// we know that the pattern model is of type Sensor, but it could be a derived
 		// type, such as TemperatureSensor, PowerSensor, etc.
 		Class<? extends Resource> sensorType = pattern.model.getResourceType();
 		// the OGEMA resource path (database id) of the sensor
 		String path = pattern.model.getPath();
 		int nrSensors = numberSensors.getValue() + 1;
 		System.out.println("New sensor available. Type: " + sensorType.getSimpleName() + ", path: " + path
 				+ ". Total number of sensors: " + nrSensors);
 		numberSensors.setValue(nrSensors);
 	}

 	@Override
 	public void patternUnavailable(SensorPattern pattern) {
 		Class<? extends Resource> sensorType = pattern.model.getResourceType();
 		String path = pattern.model.getPath();
 		int nrSensors = numberSensors.getValue() -1;
 		System.out.println("Sensor gone. Type: " + sensorType.getSimpleName() + ", path: " + path
 				+ ". Total number of sensors: " + nrSensors);
 		numberSensors.setValue(nrSensors);
 	}
}
