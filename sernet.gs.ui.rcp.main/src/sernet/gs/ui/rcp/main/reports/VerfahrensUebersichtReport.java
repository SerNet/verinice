package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

/**
 * Creates an overview of privacy requirements for the companies applications as
 * entered by the user.
 * 
 * @author koderman@sernet.de
 * 
 */
public class VerfahrensUebersichtReport extends Report implements IBSIReport {

	private ArrayList<CnATreeElement> items;

	private ArrayList<CnATreeElement> categories;

	public ArrayList<CnATreeElement> getItems() {
		if (items != null)
			return items;
		items = new ArrayList<CnATreeElement>();
		categories = new ArrayList<CnATreeElement>();
		Set<ITVerbund> itverbuende = CnAElementFactory.getCurrentModel()
				.getItverbuende();
		for (ITVerbund verbund : itverbuende) {
			getAnwendungen(verbund);
		}
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
					.getTitel()));

			AllItems: for (CnATreeElement dsElement : getItems(anwendung)) {
				ArrayList<IOOTableRow> categoryRows = new ArrayList<IOOTableRow>();
				List<String> columns = shownColumns.get(dsElement.getEntity()
						.getEntityType());
				if (columns.size() == 0)
					continue AllItems;

				categoryRows.add(new SimpleRow(IOOTableRow.ROW_STYLE_SUBHEADER,
						dsElement.getTitel()));

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
			PropertyType type = HUITypeFactory.getInstance().getPropertyType(
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
