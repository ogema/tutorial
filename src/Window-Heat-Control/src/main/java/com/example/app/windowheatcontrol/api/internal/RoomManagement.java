package com.example.app.windowheatcontrol.api.internal;

import java.util.List;

import org.ogema.model.locations.Room;

/**
 * Keeps track of room controllers
 */
public interface RoomManagement {
	
	/**
	 * Get or create the unique controller for a room
	 * @param room
	 * @return
	 *  	The associated room controller, never null.
	 */
	RoomController getController(Room room);
	
	/**
	 * Returns a list of all rooms which contain at least one thermostat and one 
	 * window sensor, so they are suitable for management by this app.
	 * @return
	 */
	List<Room> getActiveRooms();

}
