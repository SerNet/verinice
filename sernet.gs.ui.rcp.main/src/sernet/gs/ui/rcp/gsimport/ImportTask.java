package sernet.gs.ui.rcp.gsimport;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;
import org.hibernate.exception.SQLGrammarException;

import com.heatonresearch.datamover.DataMover;
import com.heatonresearch.datamover.db.Database;
import com.heatonresearch.datamover.db.DatabaseException;
import com.heatonresearch.datamover.db.DerbyDatabase;
import com.heatonresearch.datamover.db.MDBFileDatabase;
import com.heatonresearch.datamover.db.MySQL;

import sernet.gs.model.Baustein;
import sernet.gs.reveng.MSchutzbedarfkategTxt;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbBaustId;
import sernet.gs.reveng.MbMassn;
import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstId;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.ModZobjBstMassId;
import sernet.gs.reveng.NZielobjekt;
import sernet.gs.reveng.NZobSb;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.actions.AddITVerbundActionDelegate;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.CnAElementBuilder;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.NKKategorie;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * Import GSTOOL(tm) databases using the GSVampire. Maps GStool-database objects
 * to Verinice-Objects and fields.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class ImportTask {

	public static final int TYPE_SQLSERVER = 1;
	public static final int TYPE_MDB = 2;

	Pattern pattern = Pattern.compile("(\\d+)\\.0*(\\d+)");
	private IProgress monitor;
	private GSVampire vampire;
	private TransferData transferData;

	private List<MbZeiteinheitenTxt> zeiten;
	private Map<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmen;
	private Map<NZielobjekt, CnATreeElement> alleZielobjekte = new HashMap<NZielobjekt, CnATreeElement>();
	private List<Person> allePersonen = new ArrayList<Person>();
	private boolean importBausteine;
	private boolean massnahmenPersonen;
	private boolean bausteinPersonen;
	private boolean zielObjekteZielobjekte;
	private boolean schutzbedarf;
	private boolean importRollen;
	private boolean kosten;
	private boolean importUmsetzung;

	private HashMap<MbBaust, BausteinUmsetzung> alleBausteineToBausteinUmsetzungMap;
	private HashMap<MbBaust, ModZobjBst> alleBausteineToZoBstMap;

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

	public ImportTask(boolean bausteine, boolean massnahmenPersonen,
			boolean zielObjekteZielobjekte, boolean schutzbedarf,
			boolean importRollen, boolean kosten, boolean umsetzung,
			boolean bausteinPersonen) {
		this.importBausteine = bausteine;
		this.massnahmenPersonen = massnahmenPersonen;
		this.zielObjekteZielobjekte = zielObjekteZielobjekte;
		this.schutzbedarf = schutzbedarf;
		this.importRollen = importRollen;
		this.kosten = kosten;
		this.importUmsetzung = umsetzung;
		this.bausteinPersonen = bausteinPersonen;
	}

	public void execute(int importType, IProgress monitor) throws Exception {
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		String sourceDbUrl = prefs.getString(PreferenceConstants.GS_DB_URL);
		if (sourceDbUrl.indexOf("odbc") > -1) {
			copyMDBToTempDB(sourceDbUrl);
		}

		this.monitor = monitor;
		File conf = new File(CnAWorkspace.getInstance().getConfDir()
				+ File.separator + "hibernate-vampire.cfg.xml");
		vampire = new GSVampire(conf.getAbsolutePath());
		transferData = new TransferData(vampire, importRollen);
		importZielobjekte();

	}

	private void copyMDBToTempDB(String sourceDbUrl) {
		Database source = new MDBFileDatabase();
		Database target = new DerbyDatabase();
		try {
//			// delete temp dir if it exists:
//			String dirName = CnAWorkspace.getInstance().getTempImportDbDirName();
//			File file = new File(dirName);
//			if (file.exists() && file.isDirectory()) {
//				delete(file);
//			}
			
			// copy contents of MDB file to temporary derby db:
			String tempDbUrl = CnAWorkspace.getInstance()
					.createTempImportDbUrl();
			
			DataMover mover = new DataMover();

			source.connect(PreferenceConstants.GS_DB_DRIVER_ODBC, sourceDbUrl);
			target.connect(PreferenceConstants.DB_DRIVER_DERBY, tempDbUrl);

			mover.setSource(source);
			mover.setTarget(target);
			mover.exportDatabse();
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Import aus MDB Datei über temporäre Derby-DB.");
		} finally {
			try {
				source.close();
				target.close();
				String dirName = CnAWorkspace.getInstance().getTempImportDbDirName();
//				
//				File file = new File(dirName);
//				if (file.exists() && file.isDirectory()) {
//					delete(file);
//				}
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).debug(
						"Konnte temporäre Import DB nicht schließen.", e);
				
//				// try again to delete dir at least:
//				String dirName = CnAWorkspace.getInstance().getTempImportDbDirName();
//				File file = new File(dirName);
//				if (file.exists() && file.isDirectory()) {
//					delete(file);
//				}
			}
		}

	}
	
	public boolean delete(File dir) {
		if (dir.isDirectory()) {
			String[] subdirs = dir.list();
			for (int i = 0; i < subdirs.length; i++) {
				boolean success = delete(new File(dir, subdirs[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	private void importZielobjekte() throws Exception {
		CnAElementHome.getInstance().startApplicationTransaction();

		List<ZielobjektTypeResult> zielobjekte;
		try {
			zielobjekte = vampire.findZielobjektTypAll();
		} catch (Exception e) {
			if (e instanceof SQLGrammarException) {
				SQLGrammarException sqlException = (SQLGrammarException) e;
				// wrong db version has columns missing, i.e. "GEF_ID":
				if (sqlException.getSQLException().getMessage().indexOf(
						"GEF_OK") > -1)
					ExceptionUtil
							.log(
									sqlException.getSQLException(),
									"Fehler beim Laden der Zielobjekte. Möglicherweise falsche Datenbankversion des GSTOOL? "
											+ "\nEs wird nur der Import der letzten Version (>4.5) des GSTOOL unterstützt.");
			}
			throw e;
		}
		if (this.importBausteine)
			monitor.beginTask("Lese Zielobjekte, Bausteine und Massnahmen...",
					zielobjekte.size());
		else
			monitor.beginTask("Lese Zielobjekte...", zielobjekte.size());

		// create all found ITVerbund first
		List<ITVerbund> neueVerbuende = new ArrayList<ITVerbund>();
		for (ZielobjektTypeResult result : zielobjekte) {
			if (ImportZielobjektTypUtil.translateZielobjektType(result.type,
					result.subtype).equals(ITVerbund.TYPE_ID)) {

				ITVerbund itverbund = (ITVerbund) CnAElementFactory
						.getInstance().saveNew(
								CnAElementFactory.getCurrentModel(),
								ITVerbund.TYPE_ID, null);
				neueVerbuende.add(itverbund);
				monitor.worked(1);

				// save element for later:
				alleZielobjekte.put(result.zielobjekt, itverbund);

				transferData.transfer((ITVerbund) itverbund, result);
				createBausteine(itverbund, result.zielobjekt);
			}
		}

		// create all Zielobjekte in first ITVerbund,
		// TODO: tag them with every ITVerbund the've been in
		for (ZielobjektTypeResult result : zielobjekte) {
			String typeId = ImportZielobjektTypUtil.translateZielobjektType(
					result.type, result.subtype);
			CnATreeElement element = CnAElementBuilder.getInstance()
					.buildAndSave(neueVerbuende.get(0), typeId);
			if (element != null) {
				// save element for later:
				alleZielobjekte.put(result.zielobjekt, element);

				// aditionally save persons:
				if (element instanceof Person) {
					allePersonen.add((Person) element);
				}

				transferData.transfer(element, result);
				monitor.subTask(element.getTitel());
				createBausteine(element, result.zielobjekt);
			}
			monitor.worked(1);
		}

		monitor.subTask("Schreibe alle Objekte in Verinice-Datenbank...");
		CnAElementHome.getInstance().endApplicationTransaction();

		CnAElementHome.getInstance().startApplicationTransaction();
		importMassnahmenVerknuepfungen();
		monitor.subTask("Schreibe alle Objekte in Verinice-Datenbank...");
		CnAElementHome.getInstance().endApplicationTransaction();

		CnAElementHome.getInstance().startApplicationTransaction();
		importBausteinPersonVerknuepfungen();
		monitor.subTask("Schreibe alle Objekte in Verinice-Datenbank...");
		CnAElementHome.getInstance().endApplicationTransaction();

		CnAElementHome.getInstance().startApplicationTransaction();
		importZielobjektVerknüpfungen();
		monitor.subTask("Schreibe alle Objekte in Verinice-Datenbank...");
		CnAElementHome.getInstance().endApplicationTransaction();

		CnAElementHome.getInstance().startApplicationTransaction();
		importSchutzbedarf();
		monitor.subTask("Schreibe alle Objekte in Verinice-Datenbank...");
		CnAElementHome.getInstance().endApplicationTransaction();

		monitor.done();
	}

	private void importSchutzbedarf() {
		if (!schutzbedarf)
			return;

		monitor.beginTask("Importiere Schutzbedarf für alle Zielobjekte...",
				alleZielobjekte.size());
		Set<Entry<NZielobjekt, CnATreeElement>> alleZielobjekteEntries = alleZielobjekte
				.entrySet();
		for (Entry<NZielobjekt, CnATreeElement> entry : alleZielobjekteEntries) {
			List<NZobSb> schutzbedarf = vampire
					.findSchutzbedarfByZielobjekt(entry.getKey());
			for (NZobSb schubeda : schutzbedarf) {
				transferData.transferSchutzbedarf(entry.getValue(), schubeda);
			}
		}

	}

	private void importZielobjektVerknüpfungen() {
		if (!this.zielObjekteZielobjekte)
			return;

		monitor.beginTask("Importiere Verknüpfungen von Zielobjekten...",
				alleZielobjekte.size());
		Set<NZielobjekt> allElements = alleZielobjekte.keySet();
		for (NZielobjekt zielobjekt : allElements) {
			monitor.worked(1);
			CnATreeElement dependant = alleZielobjekte.get(zielobjekt);
			List<NZielobjekt> dependencies = vampire
					.findLinksByZielobjekt(zielobjekt);
			for (NZielobjekt dependency : dependencies) {
				monitor.subTask(dependant.getTitel());
				CnATreeElement dependencyElement = findZielobjektFor(dependency);
				if (dependencyElement == null) {
					Logger.getLogger(this.getClass()).debug(
							"Kein Ziel gefunden für Verknüpfung "
									+ dependency.getName());
					continue;
				}
				Logger.getLogger(this.getClass()).debug(
						"Neue Verknüpfung von " + dependant.getTitel() + " zu "
								+ dependencyElement.getTitel());

				// verinice models dependencies DOWN, not UP as the gstool.
				// therefore we need to turn things around, except for persons,
				// networks and itverbund
				// (look at it in the tree and it will make sense):
				CnATreeElement from;
				CnATreeElement to;
				if (dependencyElement instanceof Person
						|| dependencyElement instanceof NetzKomponente
						|| dependant instanceof ITVerbund) {
					from = dependant;
					to = dependencyElement;
				} else {
					from = dependencyElement;
					to = dependant;
				}

				CnALink link = new CnALink(from, to);
				try {
					CnAElementHome.getInstance().save(link);
				} catch (Exception e) {
					Logger.getLogger(this.getClass()).debug(
							"Saving link failed."); //$NON-NLS-1$
					link.remove();
				}
			}
		}
	}

	private CnATreeElement findZielobjektFor(NZielobjekt dependency) {
		for (NZielobjekt zielobjekt : alleZielobjekte.keySet()) {
			if (zielobjekt.getId().equals(dependency.getId()))
				return alleZielobjekte.get(zielobjekt);
		}
		return null;
	}

	private void importMassnahmenVerknuepfungen() {
		if (!this.massnahmenPersonen || !this.importBausteine)
			return;

		monitor.beginTask(
				"Verknüpfe verantwortliche Ansprechpartner mit Massnahmen...",
				alleMassnahmen.size());
		for (ModZobjBstMass obm : alleMassnahmen.keySet()) {
			monitor.worked(1);
			monitor.subTask(alleMassnahmen.get(obm).getTitel());
			// transferiere individuell verknüpfte verantowrtliche in massnahmen
			// (TAB "Verantwortlich" im GSTOOL):
			Set<NZielobjekt> personenSrc = vampire
					.findVerantowrtlicheMitarbeiterForMassnahme(obm.getId());
			if (personenSrc != null && personenSrc.size() > 0) {
				List<Person> dependencies = findPersonen(personenSrc);
				if (dependencies.size() != personenSrc.size())
					Logger
							.getLogger(this.getClass())
							.debug(
									"ACHTUNG: Es wurde mindestens eine Person für die "
											+ "zu verknüpfenden Verantwortlichen nicht gefunden.");
				MassnahmenUmsetzung dependantMassnahme = alleMassnahmen
						.get(obm);
				for (Person personToLink : dependencies) {
					Logger.getLogger(this.getClass()).debug(
							"Verknüpfe Massnahme "
									+ dependantMassnahme.getTitel()
									+ " mit Person " + personToLink.getTitel());
					dependantMassnahme.addUmsetzungDurch(personToLink);
				}
			}
		}
	}

	private void importBausteinPersonVerknuepfungen() {
		if (!this.bausteinPersonen || !this.importBausteine)
			return;

		monitor.beginTask("Verknüpfe befragte Personen mit Bausteinen...",
				alleBausteineToBausteinUmsetzungMap.size());
		Set<MbBaust> keySet = alleBausteineToBausteinUmsetzungMap.keySet();
		for (MbBaust mbBaust : keySet) {
			monitor.worked(1);
			BausteinUmsetzung bausteinUmsetzung = alleBausteineToBausteinUmsetzungMap
					.get(mbBaust);
			if (bausteinUmsetzung != null) {

				NZielobjekt interviewer = alleBausteineToZoBstMap.get(mbBaust)
						.getNZielobjektByFkZbZ2();
				if (interviewer != null) {
					HashSet<NZielobjekt> set = new HashSet<NZielobjekt>();
					set.add(interviewer);
					List<Person> personen = findPersonen(set);
					if (personen != null && personen.size() > 0) {
						Logger.getLogger(this.getClass()).debug(
								"Befragung für Baustein "
										+ bausteinUmsetzung.getTitel()
										+ " durchgeführt von "
										+ personen.get(0));
						bausteinUmsetzung.addBefragungDurch(personen.get(0));
					}
				}

				Set<NZielobjekt> befragteMitarbeiter = vampire
						.findBefragteMitarbeiterForBaustein(alleBausteineToZoBstMap
								.get(mbBaust).getId());
				if (befragteMitarbeiter != null
						&& befragteMitarbeiter.size() > 0) {
					List<Person> dependencies = findPersonen(befragteMitarbeiter);
					if (dependencies.size() != befragteMitarbeiter.size())
						Logger
								.getLogger(this.getClass())
								.debug(
										"ACHTUNG: Es wurde mindestens eine Person für die "
												+ "zu verknüpfenden Interviewpartner nicht gefunden.");
					monitor.subTask(bausteinUmsetzung.getTitel());
					for (Person personToLink : dependencies) {
						Logger.getLogger(this.getClass()).debug(
								"Verknüpfe Baustein "
										+ bausteinUmsetzung.getTitel()
										+ " mit befragter Person "
										+ personToLink.getTitel());
						bausteinUmsetzung.addBefragtePersonDurch(personToLink);
					}
				}
			}
		}
	}

	private List<Person> findPersonen(Set<NZielobjekt> personen) {
		List<Person> result = new ArrayList<Person>();
		alleZielobjekte: for (NZielobjekt nzielobjekt : personen) {
			for (Person person : allePersonen) {
				if (person.getKuerzel().equals(nzielobjekt.getKuerzel())
						&& person.getErlaeuterung().equals(
								nzielobjekt.getBeschreibung())) {
					result.add(person);
					continue alleZielobjekte;
				}
			}
		}
		return result;
	}

	private void createBausteine(CnATreeElement element, NZielobjekt zielobjekt)
			throws Exception {
		if (!importBausteine)
			return;

		List<BausteineMassnahmenResult> findBausteinMassnahmenByZielobjekt = vampire
				.findBausteinMassnahmenByZielobjekt(zielobjekt);

		// convert list to map: of bausteine and corresponding massnahmen:
		Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap = new HashMap<MbBaust, List<BausteineMassnahmenResult>>();
		for (BausteineMassnahmenResult result : findBausteinMassnahmenByZielobjekt) {
			List<BausteineMassnahmenResult> list = bausteineMassnahmenMap
					.get(result.baustein);
			if (list == null) {
				list = new ArrayList<BausteineMassnahmenResult>();
				bausteineMassnahmenMap.put(result.baustein, list);
			}
			list.add(result);
		}

		Set<MbBaust> keySet = bausteineMassnahmenMap.keySet();
		for (MbBaust mbBaust : keySet) {
			createBaustein(element, mbBaust, bausteineMassnahmenMap
					.get(mbBaust));
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
			if (zeiten == null)
				zeiten = vampire.findZeiteinheitenTxtAll();
			ImportKostenUtil.importKosten(massnahmenUmsetzung, vorlage, zeiten);
		}

		// remember massnahme for later:
		if (alleMassnahmen == null)
			alleMassnahmen = new HashMap<ModZobjBstMass, MassnahmenUmsetzung>();
		alleMassnahmen.put(vorlage.obm, massnahmenUmsetzung);

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
			BausteineMassnahmenResult vorlage) {
		monitor.subTask(bausteinUmsetzung.getTitel());
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

	private String getId(MbBaust mbBaust) {
		Matcher match = pattern.matcher(mbBaust.getNr());
		if (match.matches())
			return "B " + match.group(1) + "."
					+ Integer.parseInt(match.group(2));
		return "";
	}

}
