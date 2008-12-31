package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

/**
 * Prints the definitions for the protection levels defined for this
 * <code>ITVerbund</code>
 * 
 * @author koderman@sernet.de
 * 
 */
public class SchutzbedarfsDefinitionReport extends Report implements IBSIReport {

	private ArrayList<CnATreeElement> items;

	private ArrayList<CnATreeElement> categories;

	public ArrayList<CnATreeElement> getItems() {
		if (items != null)
			return items;
		items = new ArrayList<CnATreeElement>();
		categories = new ArrayList<CnATreeElement>();
		List<ITVerbund> itverbuende = CnAElementHome.getInstance().getItverbuendeHydrated();
		for (ITVerbund verbund : itverbuende) {
			items.add(verbund);
		}
		return items;
	}




	public ArrayList<IOOTableRow> getReport(PropertySelection shownColumns) {
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

	private void addProperties(ArrayList<IOOTableRow> categoryRows,
			CnATreeElement dsElement, List<String> columns, String stufenId) {
		for (String column : columns) {
			PropertyType type = HUITypeFactory.getInstance().getPropertyType(
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
