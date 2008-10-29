package sernet.gs.ui.rcp.gsimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sernet.gs.model.Baustein;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbMassn;
import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.NZielobjekt;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.actions.AddITVerbundActionDelegate;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.CnAElementBuilder;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class ImportTask {

	Pattern pattern = Pattern.compile("(\\d+)\\.0*(\\d+)");
	private IProgress monitor;
	private GSVampire vampire;
	private TransferData transferData;

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

	private static final short BST_BEARBEITET_JA = 1;
	private static final short BST_BEARBEITET_ENTBEHRLICH = 3;
	private static final short BST_BEARBEITET_NEIN = 4;

	public void execute(IProgress monitor) throws Exception {
		this.monitor = monitor;
		File conf = new File(CnAWorkspace.getInstance().getConfDir()
				+ File.separator + "hibernate-vampire.cfg.xml");
		vampire = new GSVampire(conf.getAbsolutePath());
		transferData = new TransferData();
		importZielobjekte();

	}

	private void importZielobjekte() throws Exception {
		List<ZielobjektTypeResult> zielobjekte = vampire.findZielobjektTypAll();
		monitor.beginTask("Importiere Zielobjekte...", zielobjekte.size());

		// create a new ITVerbund first
		ITVerbund itverbund = (ITVerbund) CnAElementFactory.getInstance()
				.saveNew(CnAElementFactory.getCurrentModel(),
						ITVerbund.TYPE_ID, null);

		// gets only first ITVerbund:
		for (ZielobjektTypeResult result : zielobjekte) {
			if (ImportZielobjektTypUtil.translateZielobjektType(result.type,
					result.subtype).equals(ITVerbund.TYPE_ID)) {
				transferData.transfer((ITVerbund) itverbund, result);
				createBausteine(itverbund, result.zielobjekt);
				break;
			}
		}

		// create all Zielobjekte
		for (ZielobjektTypeResult result : zielobjekte) {
			String typeId = ImportZielobjektTypUtil.translateZielobjektType(
					result.type, result.subtype);
			CnATreeElement element = CnAElementBuilder.getInstance()
					.buildAndSave(itverbund, typeId);
			if (element != null) {
				transferData.transfer(element, result);
				createBausteine(element, result.zielobjekt);
			}
			monitor.worked(1);
		}
		monitor.done();
	}

	private void createBausteine(CnATreeElement element, NZielobjekt zielobjekt)
			throws Exception {
		List<BausteineMassnahmenResult> findBausteinMassnahmenByZielobjekt = vampire
				.findBausteinMassnahmenByZielobjekt(zielobjekt);

		// convert list to map: of bausteine and corresponding massnahmen:
		Map<MbBaust, List<BausteineMassnahmenResult>> resultMap = new HashMap<MbBaust, List<BausteineMassnahmenResult>>();
		for (BausteineMassnahmenResult result : findBausteinMassnahmenByZielobjekt) {
			List<BausteineMassnahmenResult> list = resultMap
					.get(result.baustein);
			if (list == null) {
				list = new ArrayList<BausteineMassnahmenResult>();
				resultMap.put(result.baustein, list);
			}
			list.add(result);
		}

		Set<MbBaust> keySet = resultMap.keySet();
		for (MbBaust mbBaust : keySet) {
			createBaustein(element, mbBaust, resultMap.get(mbBaust));
		}

	}

	private void transferMassnahmen(BausteinUmsetzung bausteinUmsetzung,
			List<BausteineMassnahmenResult> list) {
		List<MassnahmenUmsetzung> massnahmenUmsetzungen = bausteinUmsetzung
				.getMassnahmenUmsetzungen();
		for (MassnahmenUmsetzung massnahmenUmsetzung : massnahmenUmsetzungen) {
			BausteineMassnahmenResult vorlage = findVorlage(
					massnahmenUmsetzung, list);
			if (vorlage != null) {
				
				// copy umsetzung:
				Short bearbeitet = vorlage.zoBst.getBearbeitetOrg();
				if (bearbeitet == BST_BEARBEITET_ENTBEHRLICH)
					massnahmenUmsetzung
							.setUmsetzung(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
				else
					setUmsetzung(massnahmenUmsetzung, vorlage.umstxt.getName());
				
				// copy fields:
				transferMassnahme(massnahmenUmsetzung, vorlage);
				
			} else {
				// wenn diese massnahme unbearbeitet ist und keine vorlage
				// existiert,
				// kann trotzdem der gesamte baustein auf entbehrlich gesetzt
				// sein:
				if (list.iterator().hasNext()) {
					BausteineMassnahmenResult result = list.iterator().next();
					if (result.zoBst.getBearbeitetOrg() == BST_BEARBEITET_ENTBEHRLICH)
						massnahmenUmsetzung
								.setUmsetzung(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
				}
			}
		}
	}

	private void transferMassnahme(MassnahmenUmsetzung massnahmenUmsetzung,
			BausteineMassnahmenResult vorlage) {
		massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_ERLAEUTERUNG, 
				vorlage.obm.getUmsBeschr());
		massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_UMSETZUNGBIS, 
				parseDate(vorlage.obm.getUmsDatBis()));
		
		
		List<MbZeiteinheitenTxt> zeiten = vampire.findZeiteinheitenTxtAll();
		ImportKostenUtil.importKosten(massnahmenUmsetzung, vorlage, zeiten);
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

	private BausteineMassnahmenResult findVorlage(
			MassnahmenUmsetzung massnahmenUmsetzung,
			List<BausteineMassnahmenResult> list) {
		for (BausteineMassnahmenResult result : list) {
			if (massnahmenUmsetzung.getKapitelValue()[0] == result.massnahme
					.getMskId()
					&& massnahmenUmsetzung.getKapitelValue()[1] == result.massnahme
							.getNr()) {
				return result;
			}
		}
		return null;
	}

	private BausteinUmsetzung createBaustein(CnATreeElement element,
			MbBaust mbBaust, List<BausteineMassnahmenResult> list)
			throws Exception {
		Baustein baustein = BSIKatalogInvisibleRoot.getInstance().getBaustein(
				getId(mbBaust));
		if (baustein != null) {
			BausteinUmsetzung bausteinUmsetzung = (BausteinUmsetzung) CnAElementFactory
					.getInstance().saveNew(element, BausteinUmsetzung.TYPE_ID,
							new BuildInput<Baustein>(baustein));
			if (list.iterator().hasNext()) {
				BausteineMassnahmenResult queryresult = list.iterator().next();
				transferBaustein(bausteinUmsetzung, queryresult);
				transferMassnahmen(bausteinUmsetzung, list);
			}
			return bausteinUmsetzung;
		}
		return null;
	}

	private void transferBaustein(BausteinUmsetzung bausteinUmsetzung,
			BausteineMassnahmenResult queryresult) {
		bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERLAEUTERUNG,
				queryresult.zoBst.getBegruendung());
		bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERFASSTAM,
				parseDate(queryresult.zoBst.getDatum()));
		bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERFASSTDURCH,
				queryresult.zoBst.getErfasstDurch());
		
	}

	private String parseDate(Date date) {
		if (date  != null)
			return Long.toString(date.getTime());
		return "";
	}

	private String getId(MbBaust mbBaust) {
		Matcher match = pattern.matcher(mbBaust.getNr());
		if (match.matches())
			return "B " + match.group(1) + "."
					+ Integer.parseInt(match.group(2));
		return "";
	}

}
