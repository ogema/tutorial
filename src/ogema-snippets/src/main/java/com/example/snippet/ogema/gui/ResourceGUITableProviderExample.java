package com.example.snippet.ogema.gui;

import java.util.List;
import java.util.Map;

import org.ogema.core.application.ApplicationManager;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Room;
import org.smartrplace.util.directresourcegui.DetailPopupButton;
import org.smartrplace.util.directresourcegui.KnownWidgetHolder;
import org.smartrplace.util.directresourcegui.ResourceGUIHelper;
import org.smartrplace.util.directresourcegui.ResourceGUITableProvider;
import org.smartrplace.util.directresourcegui.ResourceGUITableTemplate;
import org.smartrplace.widget.extensions.GUIUtilHelper;

import de.iwes.widgets.api.extended.WidgetData;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.alert.Alert;
import de.iwes.widgets.html.complextable.RowTemplate.Row;
import de.iwes.widgets.html.form.button.Button;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.object.widget.popup.ClosingPopup;
import de.iwes.widgets.object.widget.popup.ClosingPopup.ClosingMode;
import de.iwes.widgets.object.widget.popup.WidgetEntryData;
import de.iwes.widgets.resource.widget.table.ResourceTable;

/**
 * An HTML page, generated from the Java code.
 */
public class ResourceGUITableProviderExample implements ResourceGUITableProvider<Thermostat> {
	public final long UPDATE_RATE = 5*1000;
	private final WidgetPage<?> page;
	private final ResourceGUITableTemplate<Thermostat> mainTableRowTemplate;
	private final ResourceTable<Thermostat> mainTable;
	ResourceGUIHelper<Thermostat> vhGlobal;
	
	final ClosingPopup<Thermostat> popMore1;
	final KnownWidgetHolder<Thermostat> knownWidgets;
	final Alert alert;
	
	public ResourceGUITableProviderExample(final WidgetPage<?> page, final ApplicationManager appMan) {
		this.page = page;

		Header header = new Header(page, "header", "Thermostat Admin GUI");
		header.addDefaultStyle(WidgetData.TEXT_ALIGNMENT_CENTERED);

		//init all widgets
		alert = new Alert(page, "alert", "");
		
		knownWidgets = new KnownWidgetHolder<Thermostat>(page, "knownWidgets");
		page.append(knownWidgets);
		popMore1 = new ClosingPopup<Thermostat>(page, "popMore1",
				"More Information", true, ClosingMode.CLOSE) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onGET(OgemaHttpRequest req) {
				Thermostat item = getItem(req);
				if(item == null) return;
			}
		};
		page.append(popMore1);

		mainTableRowTemplate = new ResourceGUITableTemplate<Thermostat>(
				new ResourceGUITableTemplate.TableProvider<Thermostat>() {

					@Override
					public ResourceTable<Thermostat> getTable(OgemaHttpRequest req) {
						return mainTable;
					}
					
				}, Thermostat.class, appMan) {

			@Override
			protected Row addRow(final Thermostat object,
					final ResourceGUIHelper<Thermostat> vh, final String id, OgemaHttpRequest req) {
				final Row row = new Row();
				addWidgets(object, vh, id, req, row, appMan);
				return row;
			}
		};
		mainTable = new ResourceTable<Thermostat>(page, "appTable", mainTableRowTemplate) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onGET(OgemaHttpRequest req) {
				List<Thermostat> data = appMan.getResourceAccess().getResources(Thermostat.class);
				updateRows(data, req);
			}
		};
		
		//build page
		page.append(header);
		page.append(alert);
		page.append(mainTable);
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
}
