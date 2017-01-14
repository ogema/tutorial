package com.example.app.windowheatcontrol.api.internal;

import org.ogema.model.locations.Room;

import com.example.app.windowheatcontrol.pattern.WindowSensorPattern;

/**
 * A controller that manages an individual room. It tracks thermostats and window
 * sensors in the room, and reacts to changes in the window status (opened or closed),
 * by adapting the temperature setpoints of the thermostats.  
 */
public interface RoomController {
	
	/**
	 * Returns the room managed by this controller
	 * @return
	 */
	Room getRoom();
	
	/**
	 * Indicates whether the room is currently managed (true iff there is at
	 * least one thermostat and window sensor, each, in the room). 
	 * @return
	 */
	boolean isActive();
	
	/**
	 * To be called by thermostat management when a new thermostat is available or
	 * removed from the room. Also used internally by the room controller.
	 * @return
	 */
	boolean settingsChanged();
	
	/**
	 * To be called by window sensor management when a new sensor is available in the room.
	 * @param sensor
	 */
	void addWindowSensor(WindowSensorPattern sensor);
	
	/**
	 * To be called by window sensor management when a sensor in the room becomes unavailable 
	 * (e.g. moved to another room).
	 * @param sensor
	 */
	void removeWindowSensor(WindowSensorPattern sensor);
	
	/**
	 * For use by a GUI.
	 * Returns true if any window in the room is currently open, false otherwise.
	 * @return
	 */
	boolean isWindowOpen();
	
	/**
	 * For use by a GUI. Returns an average of the temperature setpoints of all thermostats in
	 * the room.
	 * @return 
	 * 		temperature in Celsius
	 */
	float getTemperatureSetpoint();
	
	/**
	 * For use by a GUI.
	 * Set the temperature setpoint for all thermostats in the room.
	 * @param celsius
	 */
	void setTemperatureSetpoint(float celsius);
	
	/**
	 * For use by a GUI.
	 * Get the number of window sensors in the room
	 * @return
	 */
	int windowSensorCount();
	
	/**
	 * For use by a GUI.
	 * Get the number of thermostats in the room
	 * @return
	 */
	int thermostatCount();
	
	/**
	 * Indicate that the app is being stopped.
	 */
	void stop();
	
}
