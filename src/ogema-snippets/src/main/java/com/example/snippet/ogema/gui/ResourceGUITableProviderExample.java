package com.example.snippet.ogema.gui;

import java.util.Map;

import org.ogema.core.application.ApplicationManager;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Room;
import org.smartrplace.util.directresourcegui.DetailPopupButton;
import org.smartrplace.util.directresourcegui.ResourceGUIHelper;
import org.smartrplace.util.directresourcegui.ResourceGUITablePage;
import org.smartrplace.widget.extensions.GUIUtilHelper;

import de.iwes.widgets.api.extended.WidgetData;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.complextable.RowTemplate.Row;
import de.iwes.widgets.html.form.button.Button;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.object.widget.popup.WidgetEntryData;

/**
 * An HTML page, generated from the Java code.
 */
public class ResourceGUITableProviderExample extends ResourceGUITablePage<Thermostat> {
	public final long UPDATE_RATE = 5*1000;
	
	public ResourceGUITableProviderExample(final WidgetPage<?> page, final ApplicationManager appMan) {
		super(page, appMan, Thermostat.class);
	}
	
	@Override
	public void addWidgets(final Thermostat object,
			final ResourceGUIHelper<Thermostat> vh, final String id, OgemaHttpRequest req, Row row,
			ApplicationManager appMan) {
		vh.stringLabel("Room", id, object.location().room().name(), row);
		vh.floatEdit("Thermostat setpoint", id, object.temperatureSensor().settings().setpoint(), row, alert, 4.5f, 30f, "Value outside range!");
		if((req != null)&&(row != null)) {
			DetailPopupButton<Thermostat> popUpOpenButton = new DetailPopupButton<Thermostat>(mainTable, "popUpOpenButton_"+id, "more...", req,
					popMore1, object, appMan, id, knownWidgets, this);
			row.addCell("more", popUpOpenButton);
		} else {
			vh.registerHeaderEntry("more");
		}

		//The elements declared after this statement are not in the main table, but in a detail popup
		vh.inDetailSection(true);

		Map<Room, String> valuesToSet = GUIUtilHelper.getValuesToSetForReferencingDropdown(
				Room.class, appMan);
		vh.referenceDropdownFixedChoice("Room Switching", id, object.location().room(), row, valuesToSet);
		
		if(req != null) {
			Button deleteButton = new Button(mainTable, "deleteButton_"+id, "delete", req) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onPOSTComplete(String data, OgemaHttpRequest req) {
					object.delete();
				}
			};
			if(row != null) row.addCell("delete", deleteButton);
			else vh.popTableData.add(new WidgetEntryData("delete", deleteButton));
			deleteButton.registerDependentWidget(mainTable);
		} else
			vh.registerHeaderEntry("delete");
	}
	
	public WidgetPage<?> getPage() {
		return page;
	}

	@Override
	public void addWidgetsAboveTable() {
		Header header = new Header(page, "header", "Thermostat Admin GUI");
		header.addDefaultStyle(WidgetData.TEXT_ALIGNMENT_CENTERED);
		page.append(header);
	}

}
