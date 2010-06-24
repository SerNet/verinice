/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.IDatenschutzElement;

/**
 * This report prints all safeguards grouped by modules and assets.
 * "Basis-Sicherheitscheck"
 * 
 *   
 * @author koderman[at]sernet[dot]de
 *
 */
public class MassnahmenumsetzungReport extends BsiReport
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
		
		
			ITVerbund verbund  = getItverbund();
			items.add(verbund);
			if (! categories.contains(verbund.getParent()))
				categories.add(verbund.getParent());
			getStrukturElements(verbund);
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
				rows.add(new SimpleRow(IOOTableRow.ROW_STYLE_HEADER, category.getTitle()));
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
