package com.example.app.sensor.gui;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.model.sensors.Sensor;
import org.ogema.tools.resource.util.ValueResourceUtils;
import org.smartrplace.util.directresourcegui.ResourceGUIHelper;
import org.smartrplace.util.directresourcegui.ResourceGUITablePage;

import com.example.app.sensor.pattern.SensorPattern;

import de.iwes.util.resource.ResourceHelper;
import de.iwes.util.resource.ResourceHelper.DeviceInfo;
import de.iwes.widgets.api.extended.WidgetData;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.complextable.RowTemplate.Row;
import de.iwes.widgets.html.form.label.Header;

/**
 * An HTML page, generated from the Java code.
 */
public class MainPage extends ResourceGUITablePage<Sensor> {
	
	public final long UPDATE_RATE = 5*1000;
	
	public MainPage(final WidgetPage<?> page, final ApplicationManager appMan) {
		super(page, appMan, Sensor.class);
	}
	
	@Override
	public void addWidgetsAboveTable() {
		Header header = new Header(page, "header", "Organizations in Appstore Admin GUI");
		header.addDefaultStyle(WidgetData.TEXT_ALIGNMENT_CENTERED);
		page.append(header);		
	}
	@Override
	public List<Sensor> getResourcesInTable(OgemaHttpRequest req) {
		List<SensorPattern> patterns = appMan.getResourcePatternAccess().getPatterns(SensorPattern.class, AccessPriority.PRIO_LOWEST);
		List<Sensor> result = new ArrayList<>();
		for(SensorPattern pat: patterns) {
			result.add(pat.model);
		}
		return result;
	}
	
	@Override
	public void addWidgets(final Sensor object,
			final ResourceGUIHelper<Sensor> vh, final String id, OgemaHttpRequest req, Row row,
			ApplicationManager appMan) {
		vh.stringLabel("Location", id, object.getLocation(), row);
		DeviceInfo devInfo = object!=null?ResourceHelper.getDeviceInformation(object):null;
		vh.stringLabel("Device name", id, devInfo!=null?devInfo.getDeviceName():"-", row);
		vh.stringLabel("Value", id, ValueResourceUtils.getValue((SingleValueResource)object.reading()), row);
	}
}
