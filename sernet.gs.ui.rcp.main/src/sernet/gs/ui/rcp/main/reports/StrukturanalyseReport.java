package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * This report prints out the inventory of all recorded
 * IT assets such as clients, applications, rooms etc.
 * 
 * @author koderman@sernet.de
 *
 */
public class StrukturanalyseReport extends Report
	implements IBSIReport {

	
	public StrukturanalyseReport(Properties reportProperties) {
		super(reportProperties);
	}

	private ArrayList<CnATreeElement> items;
	private ArrayList<CnATreeElement> categories;
	
	
	public String getTitle() {
		return "[BSI] Strukturanalyse nach BSI-GS";
	}

	private void getStrukturElements(CnATreeElement parent) {
		for (CnATreeElement child : parent.getChildren()) {
				if (child instanceof IBSIStrukturElement) {
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
			getStrukturElements(verbund);
		}
		return items;
	}

	private ArrayList<CnATreeElement> getItems(CnATreeElement category) {
		if (items == null)
			getItems();
		ArrayList<CnATreeElement> categoryItems = new ArrayList<CnATreeElement>();
		for (CnATreeElement item : items) {
			if (item.getParent().equals(category))
				categoryItems.add(item);
		}
		return categoryItems;
	}
	
	public ArrayList<IOOTableRow> getReport(PropertySelection shownColumns) {
		ArrayList<IOOTableRow> rows = new ArrayList<IOOTableRow>();
		
		AllCategories: for (CnATreeElement category : categories) {
			ArrayList<IOOTableRow> categoryRows = new ArrayList<IOOTableRow>();
			boolean wroteHeader = false;
			
			/* fill rows with
			 * categoryrow ("Mitarbeiter")
			 * propertyheadersrow ("Name, Vorname, ...")
			 * propertiesrow1 (Meier, Klaus)
			 * propertiesrow2 (Müller, Heinz)
			 * etc...
			 */
			
			
			AllItems: for (CnATreeElement child : getItems(category)) {
				List<String> columns = shownColumns.get(child.getEntity().getEntityType());
				if (columns.size() == 0)
					break AllItems;
				
				if (!wroteHeader) {
					categoryRows.add(new PropertyHeadersRow(
							child,
							columns,
							IOOTableRow.ROW_STYLE_SUBHEADER));
					wroteHeader = true;
				}
				categoryRows.add(new PropertiesRow(
						child, 
						columns,
						IOOTableRow.ROW_STYLE_ELEMENT));
			}

			// only add if header + items present:
			if (categoryRows.size() > 1) {
				rows.add(new SimpleRow(IOOTableRow.ROW_STYLE_HEADER, category.getTitel()));
				rows.addAll(categoryRows);
			}
				
		}
		return rows;
	}

	
}
