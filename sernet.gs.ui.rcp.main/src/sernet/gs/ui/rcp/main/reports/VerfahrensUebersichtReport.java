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
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.IDatenschutzElement;

/**
 * Creates an overview of privacy requirements for the companies applications as
 * entered by the user.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class VerfahrensUebersichtReport extends BsiReport implements IBSIReport {

	public VerfahrensUebersichtReport(Properties reportProperties) {
		super(reportProperties);
		// TODO Auto-generated constructor stub
	}

	private ArrayList<CnATreeElement> items;

	private ArrayList<CnATreeElement> categories;

	public ArrayList<CnATreeElement> getItems() {
		if (items != null)
			return items;
		items = new ArrayList<CnATreeElement>();
		categories = new ArrayList<CnATreeElement>();
		
		ITVerbund verbund = getItverbund();
		getAnwendungen(verbund);
		return items;
	}

	private void getAnwendungen(ITVerbund verbund) {
		for (CnATreeElement kategorie : verbund.getChildren()) {
			if (kategorie instanceof AnwendungenKategorie) {
				for (CnATreeElement anwendung : kategorie.getChildren()) {
					//items.add(anwendung);
					categories.add(anwendung);
					getDatenschutzElements(anwendung);
				}
			}
		}
	}

	private void getDatenschutzElements(CnATreeElement anwendung) {
		for (CnATreeElement child : anwendung.getChildren()) {
			if (child instanceof IDatenschutzElement) {
				items.add(child);
			}
		}
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

		AllCategories: for (CnATreeElement anwendung : categories) {
			rows.add(new SimpleRow(IOOTableRow.ROW_STYLE_HEADER, anwendung
					.getTitle()));

			AllItems: for (CnATreeElement dsElement : getItems(anwendung)) {
				ArrayList<IOOTableRow> categoryRows = new ArrayList<IOOTableRow>();
				List<String> columns = shownColumns.get(dsElement.getEntity()
						.getEntityType());
				if (columns.size() == 0)
					continue AllItems;

				categoryRows.add(new SimpleRow(IOOTableRow.ROW_STYLE_SUBHEADER,
						dsElement.getTitle()));

				addProperties(categoryRows, dsElement, columns);
				
				// only add if header + items present:
				if (categoryRows.size() > 1) {
					rows.addAll(categoryRows);
				}
			}

		}
		return rows;
	}

	private void addProperties(ArrayList<IOOTableRow> categoryRows,
			CnATreeElement dsElement, List<String> columns) {
		for (String column : columns) {
			
			PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(
					dsElement.getEntity().getEntityType(), column);

			categoryRows.add(new SimpleRow(IOOTableRow.ROW_STYLE_ELEMENT,
					type.getName(),
					dsElement.getEntity().getSimpleValue(column)));
		}
	}

	public String getTitle() {
		return "[DS] Verfahrensübersicht";
	}

}
