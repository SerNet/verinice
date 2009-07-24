/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * This report prints all safeguards that still have to be completed
 * 
 *   
 * @author koderman@sernet.de
 *
 */
// TODO change structure to list just the safeguards grouped by asset
public class MassnahmenTodoReport extends Report
	implements IBSIReport {

	public MassnahmenTodoReport(Properties reportProperties) {
		super(reportProperties);
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
		return categoryItems;
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
	private ArrayList<CnATreeElement> items;
	private ArrayList<CnATreeElement> categories;
	
	private String umgesetzt = MassnahmenUmsetzung.P_UMSETZUNG_JA + 
		MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH;
	
	public String getTitle() {
		return "[BSI] Realisierungsplan";
	}
	
	public ArrayList<IOOTableRow> getReport(PropertySelection shownColumns) {
		ArrayList<IOOTableRow> rows = new ArrayList<IOOTableRow>();
		
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
					categoryRows.add(new SimpleRow(
							IOOTableRow.ROW_STYLE_SUBHEADER,
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
		 if (!baustein.getChildren().iterator().hasNext())
			 return;
		 
		MassnahmenUmsetzung massnahme 
			= (MassnahmenUmsetzung) baustein.getChildren().iterator().next();
		List<String> columns = shownColumns.get(massnahme.getEntity().getEntityType());
		if (columns.size() == 0)
			return;
		
		categoryRows.add(new PropertyHeadersRow(
				massnahme, 
				columns, 
				IOOTableRow.ROW_STYLE_ELEMENT));
	

		for (CnATreeElement child : baustein.getChildren()) {
			massnahme = (MassnahmenUmsetzung) child;
			
			if (!(umgesetzt.indexOf(massnahme.getUmsetzung()) > -1)) {
				categoryRows.add(new PropertiesRow(
						massnahme, 
						columns, 
						IOOTableRow.ROW_STYLE_ELEMENT));
			}
			
			
		}
	}
	
}
