package com.example.snippet.ogema.gui;

import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.ResourceList;
import org.ogema.model.stakeholders.Language;
import org.ogema.model.stakeholders.LegalEntity;
import org.smartrplace.util.directresourcegui.GUIHelperExtension;
import org.smartrplace.util.directresourcegui.ReferencingResourceListSetter;
import org.smartrplace.util.directresourcegui.ResourceGUIHelper;
import org.smartrplace.util.directresourcegui.ResourceGUITablePage;

import de.iwes.widgets.api.extended.WidgetData;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.complextable.RowTemplate.Row;
import de.iwes.widgets.html.form.label.Header;

/**
 * An HTML page, generated from the Java code.
 */
public class ResourceGUITablePageExample extends ResourceGUITablePage<LegalEntity> {
	
	public final long UPDATE_RATE = 5*1000;
	private ResourceList<LegalEntity> elementList;
	
	public ResourceGUITablePageExample(final WidgetPage<?> page, final ApplicationManager appMan,
			ResourceList<LegalEntity> elementList) {
		super(page, appMan, LegalEntity.class);
		this.elementList = elementList;
	}
	
	@Override
	public void addWidgetsAboveTable() {
		Header header = new Header(page, "header", "Organizations in Appstore Admin GUI");
		header.addDefaultStyle(WidgetData.TEXT_ALIGNMENT_CENTERED);
		page.append(header);		
	}
	@Override
	public List<LegalEntity> getResourcesInTable(OgemaHttpRequest req) {
		return elementList.getAllElements();
	}
	
	@Override
	public void addWidgets(final LegalEntity object,
			final ResourceGUIHelper<LegalEntity> vh, final String id, OgemaHttpRequest req, Row row,
			ApplicationManager appMan) {
		vh.stringLabel("Location", id, object.getLocation(), row);
		vh.stringEdit("Full name", id, object.name(), row, alert);
		if((req != null)&&(row != null)) {
			ReferencingResourceListSetter<Language> userMulti = new ReferencingResourceListSetter<Language>(
					mainTable, id, object.knownLanguages(),
					null, req);
			row.addCell("selectUsers", userMulti.multiSelect);
			row.addCell("submitUsers", userMulti.submit);
		} else {
			vh.registerHeaderEntry("selectUsers");
			vh.registerHeaderEntry("submitUsers");
		}

		//TODO: Remove one or both entries if deleting / creating of elements shall not be provided by page
		GUIHelperExtension.addDeleteButton(elementList, object, mainTable, id, alert, row, vh, req);
		GUIHelperExtension.addCopyButton(elementList, object, mainTable, id, alert, row, vh, req, appMan);
	}
}
