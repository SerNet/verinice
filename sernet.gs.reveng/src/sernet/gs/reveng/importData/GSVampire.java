package sernet.gs.reveng.importData;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.hibernate.Hibernate;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Transaction;

import sernet.gs.reveng.HibernateSessionFactory;
import sernet.gs.reveng.MSchutzbedarfkategTxt;
import sernet.gs.reveng.MSchutzbedarfkategTxtDAO;
import sernet.gs.reveng.MUmsetzStatTxt;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbDringlichkeitDAO;
import sernet.gs.reveng.MbDringlichkeitTxt;
import sernet.gs.reveng.MbDringlichkeitTxtDAO;
import sernet.gs.reveng.MbMassn;
import sernet.gs.reveng.MbRolleTxt;
import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.MbZeiteinheitenTxtDAO;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstId;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.ModZobjBstMassId;
import sernet.gs.reveng.ModZobjBstMassMitarb;
import sernet.gs.reveng.NZielobjekt;
import sernet.gs.reveng.NZielobjektDAO;
import sernet.gs.reveng.NZobSb;
import sernet.gs.reveng.NZobSbDAO;
import sernet.gs.reveng.NmbNotiz;

public class GSVampire {
    
    

	List<MSchutzbedarfkategTxt> allSchutzbedarf;

	private static final String QUERY_ZIELOBJEKT_TYP = "select zo, txt.name, subtxt.name "
			+ "			from NZielobjekt zo, MbZielobjTypTxt txt, MbZielobjSubtypTxt subtxt "
			+ "			where zo.mbZielobjSubtyp.id.zotId = txt.id.zotId "
			+ "			and txt.id.sprId = 1 "
			+ "			and zo.mbZielobjSubtyp.id.zosId = subtxt.id.zosId "
			+ "			and subtxt.id.sprId = 1" + "			and zo.loeschDatum = null";

//	private static final String QUERY_BAUSTEIN_ZIELOBJEKT = "select zo.name, zo.id.zobId, bst.nr, "
//			+ "zo_bst.begruendung, zo_bst.bearbeitet, zo_bst.datum "
//			+ "from ModZobjBst zo_bst, NZielobjekt zo, MbBaust bst "
//			+ "where zo_bst.id.bauId = bst.id.bauId "
//			+ "and zo_bst.id.zobId = zo.id.zobId " + "order by zo.id.zobId "
//			+ "and zo_bst.loeschDatum = null";

	private static final String QUERY_BAUSTEIN_ZIELOBJEKT_MASSNAHME_FOR_ZIELOBJEKT = "select bst, mn, umstxt, zo_bst, obm "
			+ "from ModZobjBstMass obm, "
			+ "	MUmsetzStatTxt umstxt, "
			+ "	NZielobjekt zo, "
			+ "	MbBaust bst, "
			+ "	MbMassn mn, "
			+ "   ModZobjBst zo_bst "
			+ "where zo.id.zobImpId = :zobImpId "
			+ "and zo.id.zobId = :zobId "
			+ "and umstxt.id.sprId = 1 "
			+ "and obm.ustId = umstxt.id.ustId "
			+ "and obm.id.zobImpId = zo.id.zobImpId "
			+ "and obm.id.zobId 	= zo.id.zobId "
			+ "and obm.id.bauId 	= bst.id.bauId "
			+ "and obm.id.bauImpId = bst.id.bauImpId "
			+ "and obm.id.masId 	= mn.id.masId "
			+ "and obm.id.masImpId = mn.id.masImpId "
			+ "and zo_bst.id.zobId = zo.id.zobId "
			+ "and zo_bst.id.bauId = bst.id.bauId "
			+ "and obm.loeschDatum = null ";

	private static final String QUERY_NOTIZEN_FOR_ZIELOBJEKT_NAME = "select bst, mn, umstxt, zo_bst, obm, notiz "
		+ "from ModZobjBstMass obm, "
		+ "	MUmsetzStatTxt umstxt, "
		+ "	NZielobjekt zo, "
		+ "	MbBaust bst, "
		+ "	MbMassn mn, "
		+ " ModZobjBst zo_bst, " 
		+ " NmbNotiz notiz "
		+ "where zo.name = :name "
		+ "and zo.id.zobImpId = 1 "
		+ "and umstxt.id.sprId = 1 "
		+ "and obm.ustId = umstxt.id.ustId "
		+ "and obm.id.zobImpId = zo.id.zobImpId "
		+ "and obm.id.zobId 	= zo.id.zobId "
		+ "and obm.id.bauId 	= bst.id.bauId "
		+ "and obm.id.bauImpId = bst.id.bauImpId "
		+ "and obm.id.masId 	= mn.id.masId "
		+ "and obm.id.masImpId = mn.id.masImpId "
		+ "and zo_bst.id.zobId = zo.id.zobId "
		+ "and zo_bst.id.bauId = bst.id.bauId " +
		  "and obm.notizId = notiz.id.notizId "
		+ "and obm.loeschDatum = null ";
	
	private static final String QUERY_MITARBEITER_FOR_MASSNAHME = "select mitarbeiter "
			+ "from ModZobjBstMassMitarb obmm, "
			+ "NZielobjekt mitarbeiter "
			+ "where obmm.id.zobImpId = :zobImpId "
			+ "and obmm.id.bauId = :bauId "
			+ "and obmm.id.masId = :masId "
			+ "and obmm.id.zobIdMit = mitarbeiter.id.zobId " 
			+ "and obmm.loeschDatum = null";
	
	private static final String QUERY_MITARBEITER_FOR_BAUSTEIN = "select mitarbeiter " + 
			"	from " +
			"		ModZobjBstMitarb obm, " + 
			"		NZielobjekt mitarbeiter " + 
			"	where " +
			"		obm.id.bauId = :bauId " +
			"       and obm.id.zobId = :zobId " + 
			"		and obm.id.zobIdMit = mitarbeiter.id.zobId " + 
			"		and obm.loeschDatum = null";

	private static final String QUERY_ROLLE_FOR_MITARBEITER = "select rolle "
			+ "			from MbRolleTxt rolle, " + "				NZielobjekt zo, "
			+ "				NZielobjektRollen zr "
			+ "			where rolle.id.rolId = zr.id.rolId "
			+ "			and rolle.id.sprId = 1 "
			+ "			and zr.id.zobId = zo.id.zobId "
			+ "			and zo.id.zobId = :zobId" + "			and zr.loeschDatum = null";

	private static final String QUERY_SCHUTZBEDARF_FOR_ZIELOBJEKT = "select zsb "
			+ "from NZobSb zsb where zsb.id.zobId = :zobId";

	// private static final String QUERY_ZEITEINHEITEN_TXT_ALL = "select zeittxt
	// " +
	// "from MbZeiteinheitenTxt zeittxt";

	private static final String QUERY_ALLSUBTYPES = "select txt.name, subtxt.name "
			+ "			from MbZielobjTypTxt txt, MbZielobjSubtypTxt subtxt "
			+ "			where txt.id.sprId = 1 "
			+ "			and txt.id.zotId = subtxt.id.zotId "
			+ "			and subtxt.id.sprId = 1";

	private static final String QUERY_LINKS_FOR_ZIELOBJEKT = "select dependant "
			+ "	from NZielobjZielobj link, "
			+ "		NZielobjekt dependant "
			+ "	where link.id.zobId1 = :zobId "
			+ "	and link.id.zobId2 = dependant.id.zobId "
			+ "	and link.loeschDatum = null";

	public GSVampire(String configFile) {
		HibernateSessionFactory.setConfigFile(configFile);
	}

	public List<ZielobjektTypeResult> findZielobjektTypAll() {
		List<ZielobjektTypeResult> result = new ArrayList<ZielobjektTypeResult>();
		NZielobjektDAO dao = new NZielobjektDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		Query query = dao.getSession().createQuery(QUERY_ZIELOBJEKT_TYP);
		Iterator iterate = query.iterate();
	
		loop: while (iterate.hasNext()) {
			Object[] next = (Object[]) iterate.next();
			
			// skip deleted objects:
			if ( ((NZielobjekt)next[0]).getLoeschDatum() != null )
				continue loop;
			
			result.add(new ZielobjektTypeResult((NZielobjekt) next[0],
					(String) next[1], (String) next[2]));
		}
		transaction.commit();
		dao.getSession().close();
		return result;
	}

	/**
	 * Finds notes that are attached to "massnahmen" by target object.
	 * @param name Name of the target object 
	 * 
	 * @return
	 */
	public List<NotizenMassnahmeResult> findNotizenForZielobjekt(String name) {

		List<NotizenMassnahmeResult> result = new ArrayList<NotizenMassnahmeResult>();
		NZielobjektDAO dao = new NZielobjektDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		Query query = dao.getSession().createQuery(
				QUERY_NOTIZEN_FOR_ZIELOBJEKT_NAME);
		query.setParameter("name", name, Hibernate.STRING);
		Iterator iterate = query.iterate();
		while (iterate.hasNext()) {
				Object[] next = (Object[]) iterate.next();
				result.add(new NotizenMassnahmeResult((MbBaust) next[0],
						(MbMassn) next[1], (MUmsetzStatTxt) next[2],
						(ModZobjBst) next[3], (ModZobjBstMass) next[4], (NmbNotiz) next[5])
				);
		}
		transaction.commit();
		dao.getSession().close();
		return result;
	
	}
	
	public List<MbZeiteinheitenTxt> findZeiteinheitenTxtAll() {
		MbZeiteinheitenTxtDAO dao = new MbZeiteinheitenTxtDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		List<MbZeiteinheitenTxt> result = dao.findAll();
		transaction.commit();
		dao.getSession().close();
		return result;
	}

	public List<MSchutzbedarfkategTxt> findSchutzbedarfAll() {
		MSchutzbedarfkategTxtDAO dao = new MSchutzbedarfkategTxtDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		List<MSchutzbedarfkategTxt> all = dao.findAll();
		transaction.commit();
		dao.getSession().close();
		return all;
	}
	
	public List<MbDringlichkeitTxt> findDringlichkeitAll() {
		MbDringlichkeitTxtDAO dao = new MbDringlichkeitTxtDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		List<MbDringlichkeitTxt> all = dao.findAll();
		transaction.commit();
		dao.getSession().close();
		return all;
	}

	public Set<NZielobjekt> findVerantowrtlicheMitarbeiterForMassnahme(
			ModZobjBstMassId id) {
		Set<NZielobjekt> result = new HashSet<NZielobjekt>();
		NZielobjektDAO dao = new NZielobjektDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		Query query = dao.getSession().createQuery(
				QUERY_MITARBEITER_FOR_MASSNAHME);
		query.setProperties(id);
		Iterator iterate = query.iterate();
		while (iterate.hasNext()) {
			result.add((NZielobjekt) iterate.next());
		}
		transaction.commit();
		dao.getSession().close();
		return result;
	}
	
	
	
	public void attachFile(String databaseName, String fileName, String url, String user, String pass) throws SQLException, ClassNotFoundException {
		Class.forName("net.sourceforge.jtds.jdbc.Driver"); //$NON-NLS-1$
		Connection con = DriverManager.getConnection(
				url, user,
				pass);
		Statement stmt = con.createStatement();
		try {
			stmt
			.execute("sp_attach_single_file_db " +
					"@dbname= N\'" + databaseName + "\', " +
					"@physname= N\'"+ fileName + "\'"); //$NON-NLS-1$
		} catch (Exception e) {
			try {
				// if database is attached, try to drop and attach again:
				stmt.execute("sp_detach_db \'" + databaseName + "\'");
			} catch (Exception e2) {
				// do nothing
			}
			stmt
			.execute("sp_attach_single_file_db " +
					"@dbname= N\'" + databaseName + "\', " +
					"@physname= N\'"+ fileName + "\'"); //$NON-NLS-1$
		}
		stmt.close();
		con.close();
	}
	
	public BackupFileLocation getBackupFileNames(String databaseName, String fileName, String url, String user, String pass)
	throws SQLException, ClassNotFoundException {

		BackupFileLocation result = null;
		Class.forName("net.sourceforge.jtds.jdbc.Driver"); //$NON-NLS-1$
		Connection con = DriverManager.getConnection(
				url, user,
				pass);
		Statement stmt = con.createStatement();
		try {
			ResultSet rs = stmt
			.executeQuery("RESTORE FILELISTONLY" +
					 " FROM DISK = '" + fileName + "' ");
			result = new BackupFileLocation();
			if (rs.next()) {
				result.setMdfLogicalName(rs.getString("LogicalName"));
				result.setMdfFileName(rs.getString("PhysicalName"));
			}
			if (rs.next()) {
				result.setLdfLogicalName(rs.getString("LogicalName"));
				result.setLdfFileName(rs.getString("PhysicalName"));
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		stmt.close();
		con.close();
		return result;
	}
	
	public void restoreBackupFile(String databaseName, String fileName, String url, String user, String pass,
			String mdfName, String mdfFile, String ldfName, String ldfFile) 
	throws SQLException, ClassNotFoundException {
		Class.forName("net.sourceforge.jtds.jdbc.Driver"); //$NON-NLS-1$
		Connection con = DriverManager.getConnection(
				url, user,
				pass);
		Statement stmt = con.createStatement();
		try {
			String query = "RESTORE DATABASE " +
			 databaseName + " FROM DISK = '" + fileName + "' " +
			 		"WITH MOVE '" + mdfName +
			 		"' TO '" + mdfFile + 
			 		"', MOVE '" + ldfName +
			 		"' TO '" + ldfFile +
			 		"'"; //$NON-NLS-1$
			stmt
			.execute(query);
		} catch (SQLException e) {
			try {
				stmt.close();
				con.close();
			} catch (Exception e1) {
				// do nothing
			}
			throw e;
		}
		stmt.close();
		con.close();
	}
	
	public Set<NZielobjekt> findBefragteMitarbeiterForBaustein(
			ModZobjBstId id) {
		Set<NZielobjekt> result = new HashSet<NZielobjekt>();
		NZielobjektDAO dao = new NZielobjektDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		Query query = dao.getSession().createQuery(
				QUERY_MITARBEITER_FOR_BAUSTEIN);
		query.setProperties(id);
		Iterator iterate = query.iterate();
		while (iterate.hasNext()) {
			result.add((NZielobjekt) iterate.next());
		}
		transaction.commit();
		dao.getSession().close();
		return result;
	}

	public List<BausteineMassnahmenResult> findBausteinMassnahmenByZielobjekt(
			NZielobjekt zielobjekt) {
		List<BausteineMassnahmenResult> result = new ArrayList<BausteineMassnahmenResult>();
		NZielobjektDAO dao = new NZielobjektDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		Query query = dao.getSession().createQuery(
				QUERY_BAUSTEIN_ZIELOBJEKT_MASSNAHME_FOR_ZIELOBJEKT);
		query.setProperties(zielobjekt.getId());
		Iterator iterate = query.iterate();
		while (iterate.hasNext()) {
			Object[] next = (Object[]) iterate.next();
			result.add(new BausteineMassnahmenResult((MbBaust) next[0],
					(MbMassn) next[1], (MUmsetzStatTxt) next[2],
					(ModZobjBst) next[3], (ModZobjBstMass) next[4]));
		}
		transaction.commit();
		dao.getSession().close();
		return result;
	}

	public List<MbRolleTxt> findRollenByZielobjekt(NZielobjekt zielobjekt) {
		List<MbRolleTxt> result = new ArrayList<MbRolleTxt>();
		NZielobjektDAO dao = new NZielobjektDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		Query query = dao.getSession().createQuery(QUERY_ROLLE_FOR_MITARBEITER);
		query.setProperties(zielobjekt.getId());
		Iterator iterate = query.iterate();
		while (iterate.hasNext()) {
			result.add((MbRolleTxt) iterate.next());
		}
		transaction.commit();
		dao.getSession().close();
		return result;
	}

	public List<NZielobjekt> findLinksByZielobjekt(NZielobjekt zielobjekt) {
		List<NZielobjekt> result = new ArrayList<NZielobjekt>();
		NZielobjektDAO dao = new NZielobjektDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		Query query = dao.getSession().createQuery(QUERY_LINKS_FOR_ZIELOBJEKT);
		query.setProperties(zielobjekt.getId());
		Iterator iterate = query.iterate();
		while (iterate.hasNext()) {
			result.add((NZielobjekt) iterate.next());
		}
		transaction.commit();
		dao.getSession().close();
		return result;
	}

	public List<NZobSb> findSchutzbedarfByZielobjekt(NZielobjekt zielobjekt) {
		List<NZobSb> result = new ArrayList<NZobSb>();
		NZobSbDAO dao = new NZobSbDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		Query query = dao.getSession().createQuery(
				QUERY_SCHUTZBEDARF_FOR_ZIELOBJEKT);
		query.setProperties(zielobjekt.getId());
		Iterator iterate = query.iterate();
		while (iterate.hasNext()) {
			result.add((NZobSb) iterate.next());
		}
		transaction.commit();
		dao.getSession().close();
		return result;
	}

	public MSchutzbedarfkategTxt findSchutzbedarfNameForId(Short zsbVerfuSbkId) {
		if (this.allSchutzbedarf == null) {
			allSchutzbedarf = findSchutzbedarfAll();
		}

		for (MSchutzbedarfkategTxt kateg : allSchutzbedarf) {
			if (kateg.getId().getSprId() == 1
					&& kateg.getId().getSbkId().equals(zsbVerfuSbkId))
				return kateg;
		}
		return null;
	}

	public List<String[]> findSubtypesAll() {
		List<String[]> result = new ArrayList<String[]>();
		NZielobjektDAO dao = new NZielobjektDAO();
		Transaction transaction = dao.getSession().beginTransaction();
		Query query = dao.getSession().createQuery(QUERY_ALLSUBTYPES);
		Iterator iterate = query.iterate();
		while (iterate.hasNext()) {
			Object[] next = (Object[]) iterate.next();
			result.add(new String[] { (String) next[0], (String) next[1] });
		}
		transaction.commit();
		dao.getSession().close();
		return result;
	}

}
