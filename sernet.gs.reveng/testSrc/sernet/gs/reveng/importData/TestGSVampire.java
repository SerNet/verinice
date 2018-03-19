package sernet.gs.reveng.importData;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.StyleContext;
import javax.swing.text.rtf.RTFEditorKit;

import org.junit.Before;
import org.junit.Test;

import sernet.gs.reveng.MSchutzbedarfkategTxt;
import sernet.gs.reveng.MbBaustId;
import sernet.gs.reveng.MbRolleTxt;
import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.NZielobjekt;
import sernet.gs.reveng.NZobEsa;
import sernet.gs.reveng.NZobSb;

public class TestGSVampire {

	private GSVampire vampire;
	
	

	@Before
	public void setup() {
		vampire = new GSVampire("src/hibernate-vampire.cfg.xml");
	}

	@Test
	public void testfindZeiteinheitenTxtAll() throws Exception {
		List<MbZeiteinheitenTxt> findZeiteinheitenTxtAll = vampire
				.findZeiteinheitenTxtAll();
		assertTrue(findZeiteinheitenTxtAll.size() > 0);
	}

	// @Test
	// public void testPrintZielobjektTypAll() {
	// List<ZielobjektTypeResult> all = importData.findZielobjektTypAll();
	// assertTrue(all != null && all.size() > 0);
	// //
	// // for (ZielobjektTypeResult zielobjektTypeResult : all) {
	// // System.out
	// //
	// .println("----------------------------------------------------------");
	// // System.out.println(zielobjektTypeResult.type + "°"
	// // + zielobjektTypeResult.subtype);
	// // List<BausteineMassnahmenResult> massnahmenByZielobjekt = importData
	// // .findBausteinMassnahmenByZielobjekt(zielobjektTypeResult.zielobjekt);
	// // for (BausteineMassnahmenResult massnahmenResult :
	// massnahmenByZielobjekt) {
	// // System.out.println(massnahmenResult.baustein.getNr() + ", "
	// // + massnahmenResult.massnahme.getLink());
	// // }
	// // System.out
	// //
	// .println("----------------------------------------------------------");
	// // }
	// }

	@Test
	public void testFindBausteinMassnahmenByZielobjekt() throws Exception {
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);
			List<BausteineMassnahmenResult> massnahmenByZielobjekt = vampire
					.findBausteinMassnahmenByZielobjekt(zielobjekt);
			// assertTrue(massnahmenByZielobjekt.size() > 0);
			// only try first one:
			// return;
		}
	}

	@Test
	public void testFindNotizenByZielobjekt() throws Exception {
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		boolean foundSome = false;
		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);
			List<NotizenMassnahmeResult> notizen = vampire
					.findNotizenForZielobjekt(zielobjekt.getName());

			if (foundSome == false && notizen != null && notizen.size() > 0) {
				foundSome = true;
			}

			if (notizen != null && notizen.size() > 0) {
				System.out.println("\n\nNotes for " + zielobjekt.getName());
				System.out.println("----------------------------------------------------------------------------------\n");
				for (NotizenMassnahmeResult result : notizen) {
					String notizText = result.notiz.getNotizText();
					//System.out.println(vampire.convertRtf(notizText));
					System.out.println("Raw RTF, unconverted: " + notizText);
				}
			}

		}
		assertTrue("No notes found in database.", foundSome);
	}

	@Test
	public void testFindVerantowrtlicheMitarbeiterForMassnahme() {
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		int personenCount = 0;
		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);

			List<BausteineMassnahmenResult> massnahmenByZielobjekt = vampire
					.findBausteinMassnahmenByZielobjekt(zielobjekt);
			for (BausteineMassnahmenResult bausteineMassnahmenResult : massnahmenByZielobjekt) {
				Set<NZielobjekt> findVerantowrtlicheMitarbeiterForMassnahme = vampire
						.findVerantowrtlicheMitarbeiterForMassnahme(bausteineMassnahmenResult.obm
								.getId());
				if (findVerantowrtlicheMitarbeiterForMassnahme.size() > 0) {
					System.out.println("Gefunden für Baustein "
							+ bausteineMassnahmenResult.baustein.getNr()
							+ ", Massnahme "
							+ bausteineMassnahmenResult.massnahme.getNr());
					for (NZielobjekt mitarbeiter : findVerantowrtlicheMitarbeiterForMassnahme) {
						System.out.println(mitarbeiter.getKuerzel() + " "
								+ mitarbeiter.getName());
					}
					personenCount = personenCount
							+ findVerantowrtlicheMitarbeiterForMassnahme.size();
				}
			}
		}
		assertTrue("Keine verknüpften Mitarbeiter zu Massnahmen gefunden.",
				personenCount > 0);
	}

	@Test
	public void testAttachFile() {
		try {
			vampire.attachFile("tempbsidb", "C:\\BSIDB_V41_gP.MDF",
					"jdbc:jtds:sqlserver://172.16.32.2/msdb", "sa", "geheim");
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testRestoreFromFile() {
		try {
			BackupFileLocation backupFileNames = vampire.getBackupFileNames(
					"testRestoreDb", "C:\\testrestore.bak",
					"jdbc:jtds:sqlserver://172.16.32.2:1135/msdb", "sa",
					"geheim");

			vampire.restoreBackupFile("testRestoreDb", "C:\\testrestore.bak",
					"jdbc:jtds:sqlserver://172.16.32.2:1135/msdb", "sa",
					"geheim", backupFileNames.getMdfLogicalName(),
					"c:\\newtestmdf", backupFileNames.getLdfLogicalName(),
					"c:\\newtestldf");
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail();
		}

	}

	@Test
	public void testFindBefragteMitarbeiterForBaustein() {
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		int personenCount = 0;
		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);

			List<MbBaustId> visitedBausteine = new ArrayList<MbBaustId>();
			List<BausteineMassnahmenResult> massnahmenByZielobjekt = vampire
					.findBausteinMassnahmenByZielobjekt(zielobjekt);
			alleMassnahmen: for (BausteineMassnahmenResult bausteineMassnahmenResult : massnahmenByZielobjekt) {
				if (visitedBausteine
						.contains(bausteineMassnahmenResult.baustein.getId()))
					continue alleMassnahmen;
				visitedBausteine
						.add(bausteineMassnahmenResult.baustein.getId());

				Set<NZielobjekt> mitarbeiterForBaustein = vampire
						.findBefragteMitarbeiterForBaustein(bausteineMassnahmenResult.zoBst
								.getId());
				if (mitarbeiterForBaustein.size() > 0) {
					System.out.println("Gefunden für Baustein "
							+ bausteineMassnahmenResult.baustein.getNr());
					for (NZielobjekt mitarbeiter : mitarbeiterForBaustein) {
						System.out.println(mitarbeiter.getKuerzel() + " "
								+ mitarbeiter.getName());
					}
					personenCount = personenCount
							+ mitarbeiterForBaustein.size();
				}
			}
		}
		assertTrue("Keine verknüpften Mitarbeiter zu Bausteinen gefunden.",
				personenCount > 0);
	}

	@Test
	public void testFindZielobjektTypAll() throws Exception {
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);
		}
	}

	@Test
	public void findRollenByZielobjekt() {
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		boolean foundroles = false;
		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);
			List<MbRolleTxt> rollen = vampire
					.findRollenByZielobjekt(zielobjekt);
			for (MbRolleTxt mbRolleTxt : rollen) {
				foundroles = true;
				System.out.println("Gefunden Rolle '" + mbRolleTxt.getName()
						+ "' fuer '" + zielobjekt.getName() + "'");
			}
		}
		assertTrue(foundroles);
	}

	@Test
	public void findLinksByZielobjekt() {
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		boolean foundlinks = false;
		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);
			List<NZielobjekt> dependants = vampire
					.findLinksByZielobjektId(zielobjekt.getId());
			for (NZielobjekt dependant : dependants) {
				foundlinks = true;
				System.out.println("Gefunden: Verknüpfung von "
						+ zielobjekt.getName() + " zu " + dependant.getName());
			}
		}
		assertTrue(foundlinks);

	}
	
	@Test
	public void testFindErgaenzendeSchutzbedarfsanalyseByZielobjekt() {
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);
			List result = vampire.findESAByZielobjekt(zielobjekt);
			Iterator iterator = result.iterator();
			if (!iterator.hasNext()) {
				System.out.println("No ESA for: "  +zielobjekt.getName());
				continue;
			}
			 ESAResult esa = (ESAResult) iterator.next();
			 System.out.println("ESA for    : " + zielobjekt.getName());
			 System.out.println("unj        : "+esa.getUnj());
			 System.out.println("modllierung: " +esa.getModellierung());
			 System.out.println("einsatz    : "+esa.getEinsatz());
		}
	}
	
	@Test
	public void testFindRAGefaehrdungenForZielobjekt() {
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);
			// we should only load RA if ESA_RA is true
			// and even then do not load when every gefaehrdung has all fields "unbearbeitet" 
			// and text fields empty
			List<RAGefaehrdungenResult> result = vampire.findRAGefaehrdungenForZielobjekt(zielobjekt);
			Iterator<RAGefaehrdungenResult> iterator = result.iterator();
			if (!iterator.hasNext()) {
				System.out.println("No RA for: "  +zielobjekt.getName());
				continue;
			}
			System.out.println("RA for " + zielobjekt.getName());
			for (RAGefaehrdungenResult ragResult : result) {
				
				// this is how the displayed "number" has to be determined:
				if (ragResult.getGefaehrdung().getUserdef() == GSDBConstants.USERDEF_YES) {
					System.out.println("bG " + ragResult.getGefaehrdung().getGfkId()
							+ "." + ragResult.getGefaehrdung().getNr());
				}
				else {
					System.out.println("G " + ragResult.getGefaehrdung().getGfkId()
							+ "." + ragResult.getGefaehrdung().getNr());
				}
				
				System.out.println(ragResult.getGefaehrdungTxt().getName());
				System.out.println(" "+ragResult.getRisikobehandlungABCD());
				System.out.println("---");
			}
		}
	
	}
	
	@Test
	public void testFindRAGefaehrdungsMassnahmenForZielobjekt() {
		int counter =0;
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		Set beenThere = new HashSet<NZielobjekt>();
		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);
			if (beenThere.contains(zielobjekt))
				continue;
			beenThere.add(zielobjekt);
			List<RAGefaehrdungenResult> result = vampire.findRAGefaehrdungenForZielobjekt(zielobjekt);
			Iterator<RAGefaehrdungenResult> iterator = result.iterator();
			if (!iterator.hasNext()) {
				continue;
			}
			
			System.out.println("RA for " + zielobjekt.getName());
			
			for (RAGefaehrdungenResult ragResult : result) {
				if (ragResult.getRisikobehandlungABCD() == GSDBConstants.RA_BEHAND_A_REDUKTION) {
					List<RAGefaehrdungsMassnahmenResult> ragmResults = 
							vampire.findRAGefaehrdungsMassnahmenForZielobjekt(zielobjekt, 
									ragResult.getGefaehrdung());
					for (RAGefaehrdungsMassnahmenResult ragmResult : ragmResults) {
						counter++;
						if (ragmResult.getMassnahme().getUserdef() == GSDBConstants.USERDEF_YES) {
							System.out.println("bM " + ragmResult.getMassnahme().getMskId() 
									+ "." + ragmResult.getMassnahme().getNr()
									);
						} else {
							System.out.println("M " + ragmResult.getMassnahme().getMskId() 
									+ "." + ragmResult.getMassnahme().getNr()
									);
						}
						System.out.println(ragmResult.getMassnahmeTxt().getName());
						System.out.println("---");
					}
				}
			}
		}
		System.out.println("Numer of RAs that have associated MNs: " + counter);
		assertTrue(counter>0);
	}

	@Test
	public void findSchutzbedarfByZielobjekt() {
		List<ZielobjektTypeResult> findZielobjektTypAll = vampire
				.findZielobjektTypAll();
		boolean foundlinks = false;

		for (ZielobjektTypeResult zielobjektTypeResult : findZielobjektTypAll) {
			NZielobjekt zielobjekt = zielobjektTypeResult.zielobjekt;
			assertNotNull(zielobjekt);
			List<NZobSb> schutzbedarf = vampire
					.findSchutzbedarfByZielobjektId(zielobjekt.getId());
			for (NZobSb schubeda : schutzbedarf) {
				foundlinks = true;
				MSchutzbedarfkategTxt vertr = vampire
						.findSchutzbedarfNameForId(schubeda.getZsbVertrSbkId());
				MSchutzbedarfkategTxt verfu = vampire
						.findSchutzbedarfNameForId(schubeda.getZsbVerfuSbkId());
				MSchutzbedarfkategTxt integ = vampire
						.findSchutzbedarfNameForId(schubeda.getZsbIntegSbkId());

				String vertrName = vertr != null ? vertr.getName() : "null";
				String verfuName = vertr != null ? verfu.getName() : "null";
				String integName = vertr != null ? integ.getName() : "null";

				System.out.println("Gefunden: Schutzbedarf für "
						+ zielobjekt.getName() + " (Vertrau / Verfü / Integ): "
						+ "\t\t\t\t" + vertrName + " : " + verfuName + " : "
						+ integName + " : ");
			}
		}
		assertTrue(foundlinks);
	}

	@Test
	public void testFindSubtypesAll() throws Exception {
		List<String[]> findSubtypesAll = vampire.findSubtypesAll();
		assertNotNull(findSubtypesAll);
		assertTrue(findSubtypesAll.size() > 0);
	}

}
