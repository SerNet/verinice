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
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Prints the definitions for the protection levels defined for this
 * <code>ITVerbund</code>
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class SchutzbedarfsDefinitionReport extends BsiReport implements IBSIReport {

	public SchutzbedarfsDefinitionReport(Properties reportProperties) {
		super(reportProperties);
		// TODO Auto-generated constructor stub
	}

	private List<CnATreeElement> items;

	public List<CnATreeElement> getItems() {
		if (items != null){
			return items;
		}
		items = new ArrayList<CnATreeElement>();
			
		ITVerbund verbund = getItverbund();
		items.add(verbund);
		return items;
	}




	public List<IOOTableRow> getReport(PropertySelection shownColumns) {
		ArrayList<IOOTableRow> rows = new ArrayList<IOOTableRow>();

		String[] stufen = new String[] {"Normal", "Hoch", "Sehr Hoch"};
		String[] stufenId = new String[] {"_normal_", "_hoch_", "_sehrhoch_"};
		
		for (int i=0; i < stufen.length; i++) {
			rows.add(new SimpleRow(IOOTableRow.ROW_STYLE_HEADER, stufen[i]));

			List<String> columns = shownColumns.get(items.get(0).getEntity()
					.getEntityType());
			addProperties(rows, items.get(0), columns, stufenId[i]);
			
		}
		
		return rows;
	}

	private void addProperties(List<IOOTableRow> categoryRows,
			CnATreeElement dsElement, List<String> columns, String stufenId) {
		for (String column : columns) {
			
			PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(
					dsElement.getEntity().getEntityType(), column);

			if (type.getId().indexOf(stufenId)!=-1) {
				categoryRows.add(new SimpleRow(IOOTableRow.ROW_STYLE_ELEMENT,
						type.getName(),
						dsElement.getEntity().getSimpleValue(column)));
			}
		}
	}

	public String getTitle() {
		return "[BSI] Schutzbedarfsdefinition";
	}

}
