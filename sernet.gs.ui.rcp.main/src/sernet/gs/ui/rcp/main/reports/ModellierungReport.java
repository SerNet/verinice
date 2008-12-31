package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * This report prints out the association of assets with modules.
 * This is done in reverse order, modules first, then assets.
 * 
 * @author koderman@sernet.de
 *
 */
public class ModellierungReport extends Report
	implements IBSIReport{

	private Modellierung modell = null;
	
	private class Modellierung {
		private HashMap<String, ZielobjektListe> zuordnungen = null;
		private ArrayList<CnATreeElement> flatlist = null;
		
		Modellierung() {
			zuordnungen = new HashMap<String, ZielobjektListe>();
			flatlist = new ArrayList<CnATreeElement>();
		}
		
		public void addUmsetzung(BausteinUmsetzung umsetzung) {
			// for every bundle, add list of target objects:
			ZielobjektListe zuordnung;
			if (zuordnungen.containsKey(umsetzung.getKapitel())) {
				zuordnung = zuordnungen.get(umsetzung.getKapitel());
			} else {
				zuordnung = new ZielobjektListe(umsetzung);
				zuordnungen.put(umsetzung.getKapitel(),zuordnung);
				flatlist.add(umsetzung);
			}
			zuordnung.add(umsetzung.getParent());
			flatlist.add(umsetzung.getParent());
		}
		
		public List<ZielobjektListe> getBausteineMitZielobjekten(int schicht) {
			List<ZielobjektListe> bausteine = new ArrayList<ZielobjektListe>(100);
			Bausteine: for (ZielobjektListe liste : zuordnungen.values()) {
				if (liste.bausteinUmsetzung.getKapitelValue()[0] != schicht)
					continue Bausteine;
				else
					bausteine.add(liste);
			}
			return bausteine;
		}

		public ArrayList<CnATreeElement> getFlatList() {
			return flatlist;
		}
	}
	
	private class ZielobjektListe {
		private List<CnATreeElement> zielobjekte = new ArrayList<CnATreeElement>(5);
		private BausteinUmsetzung bausteinUmsetzung;

		public ZielobjektListe(BausteinUmsetzung umsetzung) {
			this.bausteinUmsetzung = umsetzung;
		}

		public void add(CnATreeElement zielobjekt) {
			zielobjekte.add(zielobjekt);
		}
	}
	
	public String getTitle() {
		return "[BSI] Modellierung nach BSI-GS";
	}

	public ArrayList<CnATreeElement> getItems() {
		if (modell != null)
			return modell.getFlatList();

		modell = new Modellierung();

		List<ITVerbund> itverbuende = CnAElementHome.getInstance().getItverbuendeHydrated();
		for (ITVerbund verbund : itverbuende) {
			getModellierung(verbund);
		}
		return modell.getFlatList();
	}

	private void getModellierung(CnATreeElement parent) {
		for (CnATreeElement child : parent.getChildren()) {
			if (child instanceof BausteinUmsetzung) {
				modell.addUmsetzung((BausteinUmsetzung)child);
			}
			getModellierung(child);
		}
	}

	
	public ArrayList<IOOTableRow> getReport(PropertySelection shownColumns) {
		ArrayList<IOOTableRow> rows = new ArrayList<IOOTableRow>();
		
		int i=0;
		for (String schicht : BausteinUmsetzung.getSchichtenBezeichnung()) {
			++i;
			// print Header , i.e. "Infrastruktur"
			rows.add(new SimpleRow(IOOTableRow.ROW_STYLE_HEADER, schicht));
			List<ZielobjektListe> bausteineMitZielobjekten = 
				this.modell.getBausteineMitZielobjekten(i);
			
			for(ZielobjektListe zuordnungenVonBaustein : bausteineMitZielobjekten) {
				// print Baustein i.e. "B 3.301 Firewall"
				rows.add(new PropertyHeadersRow(
						zuordnungenVonBaustein.bausteinUmsetzung,
						shownColumns.get(
								zuordnungenVonBaustein.bausteinUmsetzung
								.getEntity().getEntityType()
								),
						IOOTableRow.ROW_STYLE_SUBHEADER));
				
				rows.add(new PropertiesRow(
						zuordnungenVonBaustein.bausteinUmsetzung,
						shownColumns.get(
								zuordnungenVonBaustein.bausteinUmsetzung
								.getEntity().getEntityType()
								),
						IOOTableRow.ROW_STYLE_SUBHEADER));

				for (CnATreeElement ziel : zuordnungenVonBaustein.zielobjekte) {
					// print target object with properties that were selected by the user:
					rows.add(new PropertiesRow(ziel, 
							shownColumns.get(ziel.getEntity().getEntityType()),
							IOOTableRow.ROW_STYLE_ELEMENT)
							
					);
				}
			}
		}
		return rows;
	}
	
	
	
}
