package com.example.driver.schedule.csv.importer.pattern;

import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

import com.example.driver.schedule.csv.importer.model.FolderConfiguration;

import de.iwes.widgets.pattern.widget.patternedit.PatternPageAnnotations.DisplayValue;
import de.iwes.widgets.pattern.widget.patternedit.PatternPageAnnotations.EntryType;

/**
 * A configuration for a folder that will be watched for new csv files. 
 * All schedules belonging to a single configuration will be created below the
 * respective {@link #target} resource.
 */
public class FolderConfigPattern extends ResourcePattern<FolderConfiguration> { 
	
	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework. Must be public.
	 */
	public FolderConfigPattern(Resource device) {
		super(device);
	}

	/**
	 * Directory to be supervised
	 */
	@Existence(required=CreateMode.MUST_EXIST)
	@ChangeListener(valueListener=true, callOnEveryUpdate=false)
	public final StringResource directory = model.directory();
	
	/**
	 * A reference to the target resource, below which the schedules will be
	 * added by the driver.
	 */
	@EntryType(setAsReference=true)
	@Existence(required=CreateMode.MUST_EXIST)
//	@ChangeListener(structureListener=true) // not required here
	public final FloatResource target = model.target();

	// only relevant for the GUI
	@DisplayValue
	public final String existingSchedules() {
		final List<Schedule> schedules = target.getSubResources(Schedule.class, false);
		final StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		for (Schedule s: schedules) {
			sb.append("<li>").append(s.getLocationResource()).append("<br>Values: ").append(s.getValues(Long.MIN_VALUE).size()); // FIXME replace by size() method
		}
		sb.append("</li>");
		return sb.toString();
	}
	
}
