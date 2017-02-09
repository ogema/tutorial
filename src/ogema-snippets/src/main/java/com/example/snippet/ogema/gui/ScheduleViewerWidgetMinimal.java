package com.example.snippet.ogema.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.schedulemanipulator.ScheduleManipulatorConfiguration;
import de.iwes.widgets.reswidget.scheduleviewer.ScheduleViewerBasic;
import de.iwes.widgets.reswidget.scheduleviewer.api.ScheduleViewerConfiguration;

/**
 * Simple page providing a widget of type {@link ScheduleViewerBasic}. The widgets displays hard coded schedules
 * (provided as resource paths).
 */
public class ScheduleViewerWidgetMinimal {
	
	public final long UPDATE_RATE = 5*1000;
	
	public ScheduleViewerWidgetMinimal(final WidgetPage<?> page, final ApplicationManager appMan,
			final List<String> schedulePaths) {

		ScheduleManipulatorConfiguration maipulatorConfig = new ScheduleManipulatorConfiguration(null, true, true);
		ScheduleViewerConfiguration config = new ScheduleViewerConfiguration(true, true, false, true, maipulatorConfig, true, null, null, null, null, 24*60*60*1000L);
        ScheduleViewerBasic<?> viewerWidget = new ScheduleViewerBasic<ReadOnlyTimeSeries>(page, "viewerWidget",
        		appMan, config, null) {
 			private static final long serialVersionUID = 1L;

			@Override
        	protected List<ReadOnlyTimeSeries> update(OgemaHttpRequest req) {
        		Collection<ReadOnlyTimeSeries> items = new ArrayList<>();
        		for(String path: schedulePaths)
        			addIfAvailable(path, items, appMan);
        		setSchedules(items, req);
        		selectSchedules(items, req);
        		return super.update(req);
        	}
        };
        viewerWidget.setDefaultSchedules(null);
        
        page.append(viewerWidget);
        	
        /**recommended for schedule viewer pages*/
        page.showOverlay(true);
	}
	
	private void addIfAvailable(String path, Collection<ReadOnlyTimeSeries> items, ApplicationManager appMan) {
		Resource r = appMan.getResourceAccess().getResource(path);
		if((r == null)||(!(r instanceof Schedule))) return;
		items.add((Schedule)r);
	}
}
