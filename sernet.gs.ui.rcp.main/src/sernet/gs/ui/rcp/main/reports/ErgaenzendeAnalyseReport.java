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
import java.util.List;
import java.util.Properties;

import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Schutzbedarf;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This report prints out the inventory of all recorded
 * IT assets such as clients, applications, rooms etc.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class ErgaenzendeAnalyseReport extends BsiReport
	implements IBSIReport {

	
	public ErgaenzendeAnalyseReport(Properties reportProperties) {
		super(reportProperties);
	}

	private ArrayList<CnATreeElement> items;
	private ArrayList<CnATreeElement> categories;
	
	
	public String getTitle() {
		return "[BSI] Ergänzende Sicherheitsanalyse";
	}

	private void getStrukturElements(CnATreeElement parent) {
		for (CnATreeElement child : parent.getChildren()) {
				if (child instanceof IBSIStrukturElement
						&& !(child instanceof Person)
						&& analyseNeeded(child)) {
					items.add(child);
					if (! categories.contains(child.getParent()))
						categories.add(child.getParent());
				}
				getStrukturElements(child);
		}
	}

	private boolean analyseNeeded(CnATreeElement child) {
		if (child.isAdditionalMgmtReviewNeeded())
			return true;
		
		if (child instanceof NetzKomponente) {
			// include all critical connections:
			return true;
		}
		
		if (child.isSchutzbedarfProvider()) {
			boolean integritaet = child.getSchutzbedarfProvider()
				.getIntegritaet() > Schutzbedarf.NORMAL;
			boolean verfuegbarkeit = child.getSchutzbedarfProvider()
				.getVerfuegbarkeit() > Schutzbedarf.NORMAL;
			boolean vertraulichkeit = child.getSchutzbedarfProvider()
				.getVertraulichkeit() > Schutzbedarf.NORMAL;
				
			return (integritaet || verfuegbarkeit || vertraulichkeit);
			
		}
		
		return false;
	}

	public ArrayList<CnATreeElement> getItems() {
		if (items != null)
			return items;
		items = new ArrayList<CnATreeElement>();
		categories = new ArrayList<CnATreeElement>();
		
		getStrukturElements(getItverbund());
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
				rows.add(new SimpleRow(IOOTableRow.ROW_STYLE_HEADER, category.getTitle()));
				rows.addAll(categoryRows);
			}
				
		}
		return rows;
	}

	
}
