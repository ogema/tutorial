package com.example.app.sensor.app;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.sensors.Sensor;

/**
 * A template for resources of type Sensor. Here we can define a set of subresources
 * which must be present on a Sensor resource in order to match our pattern. The
 * only condition in this case is the existence of a "reading"-subresource.
 */
public class SensorPattern extends ResourcePattern<Sensor> {

	/**
	 * ResourcePatterns need a one-Resource-argument constructor, and must
	 * be public.
	 * @param match
	 */
	public SensorPattern(Resource match) {
		super(match);
	}

	/**
	 * Only those sensors match the pattern declaration, whose "reading"-subresource
	 * exists and is active.
	 */
	@Existence(required=CreateMode.MUST_EXIST)
	public final ValueResource reading = model.reading();
	
}
