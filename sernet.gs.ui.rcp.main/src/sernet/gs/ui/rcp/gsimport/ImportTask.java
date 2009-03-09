/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
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
import sernet.gs.ui.rcp.main.bsi.model.Schutzbedarf;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.ImportCreateBausteine;
import sernet.gs.ui.rcp.main.service.taskcommands.ImportTransferSchutzbedarf;

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
		
		this.alleBausteineToBausteinUmsetzungMap = new HashMap<MbBaust, BausteinUmsetzung>();
		this.alleBausteineToZoBstMap = new HashMap<MbBaust, ModZobjBst>();
		this.alleMassnahmen  = new HashMap<ModZobjBstMass, MassnahmenUmsetzung>();
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
		
		zeiten = vampire.findZeiteinheitenTxtAll();
		
		transferData = new TransferData(vampire, importRollen);
		importZielobjekte();
		CnAElementFactory.getInstance().reloadModelFromDatabase();

	}

	private void copyMDBToTempDB(String sourceDbUrl) {
		Database source = new MDBFileDatabase();
		Database target = new DerbyDatabase();
		try {
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
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).debug(
						"Konnte temporäre Import DB nicht schließen.", e);
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
								CnAElementFactory.getLoadedModel(),
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
		// TODO tag them with every ITVerbund the've been in
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
				CnAElementHome.getInstance().update(element);
			}
			monitor.worked(1);
		}

		monitor.subTask("Schreibe alle Objekte in Verinice-Datenbank...");

		importMassnahmenVerknuepfungen();
		monitor.subTask("Schreibe alle Objekte in Verinice-Datenbank...");

		importBausteinPersonVerknuepfungen();
		monitor.subTask("Schreibe alle Objekte in Verinice-Datenbank...");

		importZielobjektVerknüpfungen();
		monitor.subTask("Schreibe alle Objekte in Verinice-Datenbank...");

		importSchutzbedarf();
		monitor.subTask("Schreibe alle Objekte in Verinice-Datenbank...");
		
		monitor.done();
	}

	private void importSchutzbedarf() throws Exception {
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
				CnATreeElement element = entry.getValue();
				
				MSchutzbedarfkategTxt vertr = vampire.findSchutzbedarfNameForId(schubeda.getZsbVertrSbkId());
				MSchutzbedarfkategTxt verfu = vampire.findSchutzbedarfNameForId(schubeda.getZsbVerfuSbkId());
				MSchutzbedarfkategTxt integ = vampire.findSchutzbedarfNameForId(schubeda.getZsbIntegSbkId());
				
				int vertraulichkeit 	= (vertr != null) 
					? transferData.translateSchutzbedarf(vertr.getName())
					: Schutzbedarf.UNDEF;
					
				
				int verfuegbarkeit 		= (verfu != null) 
					? transferData.translateSchutzbedarf(verfu.getName())
				    : Schutzbedarf.UNDEF;
				
				int integritaet 		= (integ != null) 
					? transferData.translateSchutzbedarf(integ.getName())
				    : Schutzbedarf.UNDEF;
				
				String vertrBegruendung = schubeda.getZsbVertrBegr();
				String verfuBegruendung = schubeda.getZsbVerfuBegr();
				String integBegruendung = schubeda.getZsbIntegBegr();
				
				Short isPersonenbezogen = schubeda.getZsbPersDaten();
				
				ImportTransferSchutzbedarf command = new ImportTransferSchutzbedarf(element, 
						vertraulichkeit, verfuegbarkeit, integritaet,
						vertrBegruendung, verfuBegruendung, integBegruendung,
						isPersonenbezogen);
				command = ServiceFactory.lookupCommandService().executeCommand(
						command);
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

				try {
					CnAElementHome.getInstance().createLink(from, to);
				} catch (Exception e) {
					Logger.getLogger(this.getClass()).debug(
							"Saving link failed."); //$NON-NLS-1$
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

		this.monitor.subTask("Erstelle " + zielobjekt.getName() 
				+ " mit " + bausteineMassnahmenMap.keySet().size() + " Bausteinen und "
				+ getAnzahlMassnahmen(bausteineMassnahmenMap) + " Massnahmen...");
		ImportCreateBausteine command = new ImportCreateBausteine(
				element, bausteineMassnahmenMap, zeiten,
				kosten, importUmsetzung);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		
		if (command.getAlleBausteineToBausteinUmsetzungMap()!=null)
			this.alleBausteineToBausteinUmsetzungMap.putAll(command.getAlleBausteineToBausteinUmsetzungMap());
		
		if (command.getAlleBausteineToZoBstMap()!=null)
			this.alleBausteineToZoBstMap.putAll(command.getAlleBausteineToZoBstMap());
		
		if (command.getAlleMassnahmen()!=null)
			this.alleMassnahmen.putAll(command.getAlleMassnahmen());
		
	}

	private int getAnzahlMassnahmen(
			Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap) {
		Set<MbBaust> keys = bausteineMassnahmenMap.keySet();
		int result = 0;
		for (MbBaust baust : keys) {
			result += bausteineMassnahmenMap.get(baust).size();
		}
		return result;
	}
}
