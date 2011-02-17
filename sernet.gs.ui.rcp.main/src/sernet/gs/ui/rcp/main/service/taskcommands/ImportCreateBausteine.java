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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sernet.gs.model.Baustein;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.gsimport.ImportKostenUtil;
import sernet.gs.ui.rcp.gsimport.TransferData;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateBaustein;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Create BausteinUmsetzung objects during import for given target object and assigned Bausteine 
 * from source database. 
 * 
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ImportCreateBausteine extends GenericCommand {

	private CnATreeElement element;
	private Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap;
	private boolean importUmsetzung;
	private boolean kosten;
	private HashMap<MbBaust, BausteinUmsetzung> alleBausteineToBausteinUmsetzungMap;
	private HashMap<MbBaust, ModZobjBst> alleBausteineToZoBstMap;
	private List<MbZeiteinheitenTxt> zeiten;
	private HashMap<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmen;
	private List<Baustein> bausteine;

	private static final short BST_BEARBEITET_JA = 1;
	private static final short BST_BEARBEITET_ENTBEHRLICH = 3;
	private static final short BST_BEARBEITET_NEIN = 4;
	
	// umsetzungs patterns in verinice
	// leaving out "unbearbeitet" since this is the default:
	private static final String[] UMSETZUNG_STATI_VN = new String[] {
			MassnahmenUmsetzung.P_UMSETZUNG_NEIN,
			MassnahmenUmsetzung.P_UMSETZUNG_JA,
			MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE,
			MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH, };

	// umsetzungs patterns in gstool:
	private static final String[] UMSETZUNG_STATI_GST = new String[] { "nein",
			"ja", "teilweise", "entbehrlich", };



	public ImportCreateBausteine(CnATreeElement element,
			Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap,
			List<MbZeiteinheitenTxt> zeiten, boolean kosten, boolean importUmsetzung) {
		this.element = element;
		this.bausteineMassnahmenMap = bausteineMassnahmenMap;
		this.kosten = kosten;
		this.importUmsetzung = importUmsetzung;
		this.zeiten = zeiten;
	}

	public void execute() {
		try {
			IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOforTypedElement(element);
			dao.reload(element, element.getDbId());
			
			LoadBausteine command = new LoadBausteine();
			command = ServiceFactory.lookupCommandService().executeCommand(command);
			this.bausteine = command.getBausteine();
			
			Set<MbBaust> keySet = bausteineMassnahmenMap.keySet();
			for (MbBaust mbBaust : keySet) {
					createBaustein(element, mbBaust, bausteineMassnahmenMap
							.get(mbBaust));
			}
			
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
		
	}
	
	private BausteinUmsetzung createBaustein(CnATreeElement element,
			MbBaust mbBaust, List<BausteineMassnahmenResult> list)
			throws Exception {
		Baustein baustein = findBausteinForId(TransferData.getId(mbBaust));
		
		if (baustein != null) {
			CreateBaustein command = new CreateBaustein(element, baustein);
			command = ServiceFactory.lookupCommandService().executeCommand(command);
			BausteinUmsetzung bausteinUmsetzung = command.getNewElement();
			
			if (list.iterator().hasNext()) {
				BausteineMassnahmenResult queryresult = list.iterator().next();
				transferBaustein(bausteinUmsetzung, queryresult);
				transferMassnahmen(bausteinUmsetzung, list);
			}
			return bausteinUmsetzung;
		}
		return null;
	}
	
	private Baustein findBausteinForId(String id) {
		for (Baustein baustein : bausteine) {
			if (baustein.getId().equals(id))
				return baustein;
		}
		return null;
	}

	private void transferBaustein(BausteinUmsetzung bausteinUmsetzung,
			BausteineMassnahmenResult vorlage) {
		bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERLAEUTERUNG,
				vorlage.zoBst.getBegruendung());
		bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERFASSTAM,
				parseDate(vorlage.zoBst.getDatum()));

		// remember baustein for later:
		if (alleBausteineToBausteinUmsetzungMap == null)
			alleBausteineToBausteinUmsetzungMap = new HashMap<MbBaust, BausteinUmsetzung>();
		
		if (alleBausteineToZoBstMap == null)
			alleBausteineToZoBstMap = new HashMap<MbBaust, ModZobjBst>();
		
		alleBausteineToBausteinUmsetzungMap.put(vorlage.baustein,
				bausteinUmsetzung);
		alleBausteineToZoBstMap.put(vorlage.baustein, vorlage.zoBst);
	}
	
	private String parseDate(Date date) {
		if (date != null)
			return Long.toString(date.getTime());
		return "";
	}
	
	private void setUmsetzung(MassnahmenUmsetzung massnahmenUmsetzung,
			String gst_status) {
		for (int i = 0; i < UMSETZUNG_STATI_GST.length; i++) {
			if (UMSETZUNG_STATI_GST[i].equals(gst_status)) {
				massnahmenUmsetzung.setUmsetzung(UMSETZUNG_STATI_VN[i]);
				return;
			}
		}

	}
	
	private void transferMassnahmen(BausteinUmsetzung bausteinUmsetzung,
			List<BausteineMassnahmenResult> list) {
		List<MassnahmenUmsetzung> massnahmenUmsetzungen = bausteinUmsetzung
				.getMassnahmenUmsetzungen();
		for (MassnahmenUmsetzung massnahmenUmsetzung : massnahmenUmsetzungen) {
			BausteineMassnahmenResult vorlage = TransferData.findMassnahmenVorlage(
					massnahmenUmsetzung, list);
			if (vorlage != null) {

				if (importUmsetzung) {
					// copy umsetzung:
					Short bearbeitet = vorlage.zoBst.getBearbeitetOrg();
					if (bearbeitet == BST_BEARBEITET_ENTBEHRLICH)
						massnahmenUmsetzung
								.setUmsetzung(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
					else
						setUmsetzung(massnahmenUmsetzung, vorlage.umstxt
								.getName());
				}

				// copy fields:
				transferMassnahme(massnahmenUmsetzung, vorlage);

			} else {
				// wenn diese massnahme unbearbeitet ist und keine vorlage
				// existiert,
				// kann trotzdem der gesamte baustein auf entbehrlich gesetzt
				// sein:
				if (importUmsetzung) {
					if (list.iterator().hasNext()) {
						BausteineMassnahmenResult result = list.iterator()
								.next();
						if (result.zoBst.getBearbeitetOrg() == BST_BEARBEITET_ENTBEHRLICH)
							massnahmenUmsetzung
									.setUmsetzung(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
					}
				}
			}
		}
	}
	
	private void transferMassnahme(MassnahmenUmsetzung massnahmenUmsetzung,
			BausteineMassnahmenResult vorlage) {
		if (importUmsetzung) {
			// erlaeuterung und termin:
			massnahmenUmsetzung.setSimpleProperty(
					MassnahmenUmsetzung.P_ERLAEUTERUNG, vorlage.obm
							.getUmsBeschr());
			massnahmenUmsetzung.setSimpleProperty(
					MassnahmenUmsetzung.P_UMSETZUNGBIS, parseDate(vorlage.obm
							.getUmsDatBis()));
		}

		// transfer kosten:
		if (kosten) {
			ImportKostenUtil.importKosten(massnahmenUmsetzung, vorlage, zeiten);
		}

		// remember massnahme for later:
		if (alleMassnahmen == null)
			alleMassnahmen = new HashMap<ModZobjBstMass, MassnahmenUmsetzung>();
		alleMassnahmen.put(vorlage.obm, massnahmenUmsetzung);

	}
	

	

	public HashMap<MbBaust, BausteinUmsetzung> getAlleBausteineToBausteinUmsetzungMap() {
		return alleBausteineToBausteinUmsetzungMap;
	}

	public HashMap<MbBaust, ModZobjBst> getAlleBausteineToZoBstMap() {
		return alleBausteineToZoBstMap;
	}

	public HashMap<ModZobjBstMass, MassnahmenUmsetzung> getAlleMassnahmen() {
		return alleMassnahmen;
	}
	
	@Override
	public void clear() {
		// empty elements for transfer to client:
		element = null;
		bausteineMassnahmenMap = null;
		zeiten=null;
		bausteine = null;
	}

}
