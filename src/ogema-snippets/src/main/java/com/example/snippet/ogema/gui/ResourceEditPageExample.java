package com.example.snippet.ogema.gui;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.tools.resource.util.ResourceUtils;
import org.smartrplace.util.directresourcegui.LabelProvider;
import org.smartrplace.util.directresourcegui.ResourceEditPage;
import org.smartrplace.util.directresourcegui.ResourceGUIHelper;

import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.form.button.Button;

/**
 * An example for the usage of {@link ResourceEditPage}. The template automatically creates a Dropdown
 * that allows to select a thermostat on the system.
 */
public class ResourceEditPageExample extends ResourceEditPage<Thermostat> {
	
	@Override
	protected String getOverviewPageUrl() {
		return "sema/index.html";
	}
	public ResourceEditPageExample(final WidgetPage<?> page, final ApplicationManager appMan,
			final OgemaGuiService guiService) {
		super(page, appMan, null, Thermostat.class, new LabelProvider<Thermostat>() {
			@Override
			public String getLabel(Thermostat item) {
				return ResourceUtils.getHumanReadableName(item.location().room());
			}
		});

		ResourceGUIHelper<Thermostat> mh = new ResourceGUIHelper<Thermostat>(
				page, init, appMan, false);
		
		
		StaticTable table = new StaticTable(3, 2, new int[]{4,2});
		int c = 0;
		//Note: The names of the sub-elements of thermostat have to be given here as Strings
		table.setContent(c, 0, "Temperaturefall Modus").
			setContent(c, 1, mh.stringLabel("HmParametersMaster.TEMPERATUREFALL_MODUS"));
		c++; //2
		table.setContent(c, 0, "Setpoint:").
			setContent(c, 1, mh.booleanEdit("temperatureSensor.settings.setpoint"));
		c++;
		table.setContent(c, 0, "Current valve position (0..1):").
			setContent(c, 1, mh.floatLabel("item.valve.setting.stateFeedback", "%.2f"));
		page.append(table);
		
		Button reCalcButton = new Button(page, "boostButton", "Start Boost mode") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onPOSTComplete(String data, OgemaHttpRequest req) {
				Thermostat rts = drop.getSelectedItem(req);
				BooleanResource boostSet = rts.getSubResource("setBoostMode", BooleanResource.class);
				if(boostSet.isActive()) boostSet.setValue(true);
			}
		};
		page.append(reCalcButton);
		
		finalize(table);
	}
	
	public WidgetPage<?> getPage() {
		return page;
	}
}
