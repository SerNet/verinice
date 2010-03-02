/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.hibernate.proxy.HibernateProxy;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.DAOFactory;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * This report prints out all items in a format that can be used for further
 * editing in OpenOffice Calc.
 * 
 * @author koderman@sernet.de
 *
 */
public class AllItemsReport extends BsiReport
	implements IBSIReport {

	public AllItemsReport(Properties reportProperties) {
		super(reportProperties);
	}

	private ArrayList<CnATreeElement> items;
	
	public String getTitle() {
		return "[Export] Vollständiger Export aller Elemente";
	}

	public ArrayList<CnATreeElement> getItems() {
		if (items != null)
			return items;
		items = new ArrayList<CnATreeElement>();
		
		ITVerbund verbund = getItverbund();
		addAllItems(verbund);
		return items;
	}

	
	/**
	 * Recursively add all children.
	 * @param verbund
	 */
	private void addAllItems(CnATreeElement parent) {
		items.add(parent);
		for (CnATreeElement child: parent.getChildren()) {
			addAllItems(child);
		}
	}

	public ArrayList<IOOTableRow> getReport(PropertySelection shownColumns) {
		ArrayList<IOOTableRow> rows = new ArrayList<IOOTableRow>();
		
			rows.add(new SimpleRow(IOOTableRow.ROW_STYLE_SUBHEADER, "Typ", "UUID", "Parent UUID", "Details"));
		
			for (CnATreeElement item : items) {
				rows.add(new CompletePropertiesRow(item));
				
			}

		
		return rows;
	}

	
}
