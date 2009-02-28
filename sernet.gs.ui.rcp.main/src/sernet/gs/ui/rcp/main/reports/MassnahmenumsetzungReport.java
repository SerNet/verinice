package sernet.gs.ui.rcp.main.reports;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * This report prints all safeguards grouped by modules and assets.
 * "Basis-SIcherheitscheck"
 * 
 *   
 * @author koderman@sernet.de
 *
 */
public class MassnahmenumsetzungReport extends Report
	implements IBSIReport {

	
	public MassnahmenumsetzungReport(Properties reportProperties) {
		super(reportProperties);
		// TODO Auto-generated constructor stub
	}

	private ArrayList<CnATreeElement> items;
	private ArrayList<CnATreeElement> categories;
	
	
	public String getTitle() {
		return "[BSI] Basis-Sicherheitscheck";
	}

	private void getStrukturElements(CnATreeElement parent) {
		for (CnATreeElement child : parent.getChildren()) {
				if (! (child instanceof IDatenschutzElement)) {
					items.add(child);
					if (! categories.contains(child.getParent()))
						categories.add(child.getParent());
				}
				getStrukturElements(child);
		}
	}

	public ArrayList<CnATreeElement> getItems() {
		if (items != null)
			return items;
		items = new ArrayList<CnATreeElement>();
		categories = new ArrayList<CnATreeElement>();
		List<ITVerbund> itverbuende;
			BSIModel model = super.getModel();
			itverbuende = model.getItverbuende();
			for (ITVerbund verbund : itverbuende) {
				items.add(verbund);
				if (! categories.contains(verbund.getParent()))
					categories.add(verbund.getParent());
				getStrukturElements(verbund);
			}
		return items;
	}

	protected ArrayList<CnATreeElement> getItems(CnATreeElement category) {
		if (items == null)
			getItems();
		ArrayList<CnATreeElement> categoryItems = new ArrayList<CnATreeElement>();
		for (CnATreeElement item : items) {
			if (item.getParent().equals(category))
				categoryItems.add(item);
		}
		Collections.sort(categoryItems, new CnAElementByTitleComparator());
		return categoryItems;
	}
	
	public ArrayList<IOOTableRow> getReport(PropertySelection shownColumns) {
		ArrayList<IOOTableRow> rows = new ArrayList<IOOTableRow>();
		Collections.sort(categories, new CnAElementByTitleComparator());
		
		AllCategories: for (CnATreeElement category : categories) {
			ArrayList<IOOTableRow> categoryRows = new ArrayList<IOOTableRow>();
			
			AllItems: for (CnATreeElement child : getItems(category)) {
//				if ( child instanceof IBSIStrukturElement) {
//					List<String> columns = shownColumns.get(child.getEntity().getEntityType());
//					
//					categoryRows.add(new PropertiesRow(
//							child,
//							columns,
//							IOOTableRow.ROW_STYLE_SUBHEADER));
//				} else
				
				if (child instanceof BausteinUmsetzung) {
					List<String> columns = shownColumns.get(child.getEntity().getEntityType());
					BausteinUmsetzung bst = (BausteinUmsetzung) child;
					
					categoryRows.add(new PropertiesRow(
							child,
							columns,
							IOOTableRow.ROW_STYLE_SUBHEADER));
					categoryRows.add(new SimpleRow(IOOTableRow.ROW_STYLE_SUBHEADER,
							"Erreichte Siegelstufe: "
							+ Character.toString(bst.getErreichteSiegelStufe())
							));
					addMassnahmen(shownColumns, 
							categoryRows,
							bst);
					
				}
				// else: ignore item
			}

			// only add if header + items present:
			if (categoryRows.size() > 1) {
				rows.add(new SimpleRow(IOOTableRow.ROW_STYLE_HEADER, category.getTitel()));
				rows.addAll(categoryRows);
			}
				
		}
		return rows;
	}

	private void addMassnahmen(PropertySelection shownColumns, 
			ArrayList<IOOTableRow> categoryRows,
			BausteinUmsetzung baustein) {
		
		if (baustein.getChildren().size() < 1)
			 return;
		
		List<CnATreeElement> massnahmen = new ArrayList<CnATreeElement>(baustein.getChildren().size());
		massnahmen.addAll(baustein.getChildren());
		Collections.sort(massnahmen, new CnAElementByTitleComparator());

		// add one row with column headers:
		CnATreeElement firstMassnahme = massnahmen.get(0);
		List<String> columns = shownColumns.get(firstMassnahme.getEntity().getEntityType());
		if (columns.size() == 0)
			return;
		
		categoryRows.add(new PropertyHeadersRow(
				firstMassnahme, 
				columns, 
				IOOTableRow.ROW_STYLE_SUBHEADER));
		
		// add rows with contents:
		for (CnATreeElement massnahme : massnahmen) {
			categoryRows.add(new PropertiesRow(
					massnahme, 
					columns, 
					IOOTableRow.ROW_STYLE_ELEMENT));
			
		}
	}
}
