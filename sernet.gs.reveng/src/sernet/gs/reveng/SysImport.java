package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * SysImport entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class SysImport implements java.io.Serializable {

	// Fields

	private Integer impId;
	private SysImport sysImport;
	private Integer trgImpId;
	private Integer srcImpId;
	private String impTyp;
	private String impStatus;
	private String name;
	private String beschreibung;
	private Date impDatumAnlage;
	private String impExpTyp;
	private String impExpName;
	private String impExpBeschreibung;
	private String impExpSignaturQuelle;
	private String impExpSignaturZiel;
	private Date impExpDatumFrist;
	private Integer impUsnBaseline;
	private Integer impUsnCurrent;
	private Byte impLoeschen;
	private Short impKonkret;
	private Byte impModell;
	private Short impMapuserdef;
	private String guid;
	private Date timestamp;
	private Date loeschDatum;
	private String geloeschtDurch;
	private Integer cmImpId;
	private Integer cmVerId;
	private Integer editImpId;
	private Short cmLevel;
	private Short impNurMeta;
	private Integer metaVers;
	private Date impDatumImport;
	private Short impMapGuid;
	private Short impMapNur;
	private Set sysImports = new HashSet(0);
	private Set mbDringlichkeits = new HashSet(0);
	private Set mbBausts = new HashSet(0);
	private Set sysImports_1 = new HashSet(0);
	private Set mbSchichts = new HashSet(0);
	private Set mbZielobjTyps = new HashSet(0);
	private Set mbStatuses = new HashSet(0);
	private Set mbZielobjSubtyps = new HashSet(0);

	// Constructors

	/** default constructor */
	public SysImport() {
	}

	/** minimal constructor */
	public SysImport(Integer impId, String impTyp, String name,
			Date impDatumAnlage, Integer impUsnBaseline, Integer impUsnCurrent,
			Byte impLoeschen, Short impKonkret, Byte impModell,
			Short impMapuserdef, Short impNurMeta, Short impMapGuid,
			Short impMapNur) {
		this.impId = impId;
		this.impTyp = impTyp;
		this.name = name;
		this.impDatumAnlage = impDatumAnlage;
		this.impUsnBaseline = impUsnBaseline;
		this.impUsnCurrent = impUsnCurrent;
		this.impLoeschen = impLoeschen;
		this.impKonkret = impKonkret;
		this.impModell = impModell;
		this.impMapuserdef = impMapuserdef;
		this.impNurMeta = impNurMeta;
		this.impMapGuid = impMapGuid;
		this.impMapNur = impMapNur;
	}

	/** full constructor */
	public SysImport(Integer impId, SysImport sysImport, Integer trgImpId,
			Integer srcImpId, String impTyp, String impStatus, String name,
			String beschreibung, Date impDatumAnlage, String impExpTyp,
			String impExpName, String impExpBeschreibung,
			String impExpSignaturQuelle, String impExpSignaturZiel,
			Date impExpDatumFrist, Integer impUsnBaseline,
			Integer impUsnCurrent, Byte impLoeschen, Short impKonkret,
			Byte impModell, Short impMapuserdef, String guid, Date timestamp,
			Date loeschDatum, String geloeschtDurch, Integer cmImpId,
			Integer cmVerId, Integer editImpId, Short cmLevel,
			Short impNurMeta, Integer metaVers, Date impDatumImport,
			Short impMapGuid, Short impMapNur, Set sysImports,
			Set mbDringlichkeits, Set mbBausts, Set sysImports_1,
			Set mbSchichts, Set mbZielobjTyps, Set mbStatuses,
			Set mbZielobjSubtyps) {
		this.impId = impId;
		this.sysImport = sysImport;
		this.trgImpId = trgImpId;
		this.srcImpId = srcImpId;
		this.impTyp = impTyp;
		this.impStatus = impStatus;
		this.name = name;
		this.beschreibung = beschreibung;
		this.impDatumAnlage = impDatumAnlage;
		this.impExpTyp = impExpTyp;
		this.impExpName = impExpName;
		this.impExpBeschreibung = impExpBeschreibung;
		this.impExpSignaturQuelle = impExpSignaturQuelle;
		this.impExpSignaturZiel = impExpSignaturZiel;
		this.impExpDatumFrist = impExpDatumFrist;
		this.impUsnBaseline = impUsnBaseline;
		this.impUsnCurrent = impUsnCurrent;
		this.impLoeschen = impLoeschen;
		this.impKonkret = impKonkret;
		this.impModell = impModell;
		this.impMapuserdef = impMapuserdef;
		this.guid = guid;
		this.timestamp = timestamp;
		this.loeschDatum = loeschDatum;
		this.geloeschtDurch = geloeschtDurch;
		this.cmImpId = cmImpId;
		this.cmVerId = cmVerId;
		this.editImpId = editImpId;
		this.cmLevel = cmLevel;
		this.impNurMeta = impNurMeta;
		this.metaVers = metaVers;
		this.impDatumImport = impDatumImport;
		this.impMapGuid = impMapGuid;
		this.impMapNur = impMapNur;
		this.sysImports = sysImports;
		this.mbDringlichkeits = mbDringlichkeits;
		this.mbBausts = mbBausts;
		this.sysImports_1 = sysImports_1;
		this.mbSchichts = mbSchichts;
		this.mbZielobjTyps = mbZielobjTyps;
		this.mbStatuses = mbStatuses;
		this.mbZielobjSubtyps = mbZielobjSubtyps;
	}

	// Property accessors

	public Integer getImpId() {
		return this.impId;
	}

	public void setImpId(Integer impId) {
		this.impId = impId;
	}

	public SysImport getSysImport() {
		return this.sysImport;
	}

	public void setSysImport(SysImport sysImport) {
		this.sysImport = sysImport;
	}

	public Integer getTrgImpId() {
		return this.trgImpId;
	}

	public void setTrgImpId(Integer trgImpId) {
		this.trgImpId = trgImpId;
	}

	public Integer getSrcImpId() {
		return this.srcImpId;
	}

	public void setSrcImpId(Integer srcImpId) {
		this.srcImpId = srcImpId;
	}

	public String getImpTyp() {
		return this.impTyp;
	}

	public void setImpTyp(String impTyp) {
		this.impTyp = impTyp;
	}

	public String getImpStatus() {
		return this.impStatus;
	}

	public void setImpStatus(String impStatus) {
		this.impStatus = impStatus;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBeschreibung() {
		return this.beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public Date getImpDatumAnlage() {
		return this.impDatumAnlage;
	}

	public void setImpDatumAnlage(Date impDatumAnlage) {
		this.impDatumAnlage = impDatumAnlage;
	}

	public String getImpExpTyp() {
		return this.impExpTyp;
	}

	public void setImpExpTyp(String impExpTyp) {
		this.impExpTyp = impExpTyp;
	}

	public String getImpExpName() {
		return this.impExpName;
	}

	public void setImpExpName(String impExpName) {
		this.impExpName = impExpName;
	}

	public String getImpExpBeschreibung() {
		return this.impExpBeschreibung;
	}

	public void setImpExpBeschreibung(String impExpBeschreibung) {
		this.impExpBeschreibung = impExpBeschreibung;
	}

	public String getImpExpSignaturQuelle() {
		return this.impExpSignaturQuelle;
	}

	public void setImpExpSignaturQuelle(String impExpSignaturQuelle) {
		this.impExpSignaturQuelle = impExpSignaturQuelle;
	}

	public String getImpExpSignaturZiel() {
		return this.impExpSignaturZiel;
	}

	public void setImpExpSignaturZiel(String impExpSignaturZiel) {
		this.impExpSignaturZiel = impExpSignaturZiel;
	}

	public Date getImpExpDatumFrist() {
		return this.impExpDatumFrist;
	}

	public void setImpExpDatumFrist(Date impExpDatumFrist) {
		this.impExpDatumFrist = impExpDatumFrist;
	}

	public Integer getImpUsnBaseline() {
		return this.impUsnBaseline;
	}

	public void setImpUsnBaseline(Integer impUsnBaseline) {
		this.impUsnBaseline = impUsnBaseline;
	}

	public Integer getImpUsnCurrent() {
		return this.impUsnCurrent;
	}

	public void setImpUsnCurrent(Integer impUsnCurrent) {
		this.impUsnCurrent = impUsnCurrent;
	}

	public Byte getImpLoeschen() {
		return this.impLoeschen;
	}

	public void setImpLoeschen(Byte impLoeschen) {
		this.impLoeschen = impLoeschen;
	}

	public Short getImpKonkret() {
		return this.impKonkret;
	}

	public void setImpKonkret(Short impKonkret) {
		this.impKonkret = impKonkret;
	}

	public Byte getImpModell() {
		return this.impModell;
	}

	public void setImpModell(Byte impModell) {
		this.impModell = impModell;
	}

	public Short getImpMapuserdef() {
		return this.impMapuserdef;
	}

	public void setImpMapuserdef(Short impMapuserdef) {
		this.impMapuserdef = impMapuserdef;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getLoeschDatum() {
		return this.loeschDatum;
	}

	public void setLoeschDatum(Date loeschDatum) {
		this.loeschDatum = loeschDatum;
	}

	public String getGeloeschtDurch() {
		return this.geloeschtDurch;
	}

	public void setGeloeschtDurch(String geloeschtDurch) {
		this.geloeschtDurch = geloeschtDurch;
	}

	public Integer getCmImpId() {
		return this.cmImpId;
	}

	public void setCmImpId(Integer cmImpId) {
		this.cmImpId = cmImpId;
	}

	public Integer getCmVerId() {
		return this.cmVerId;
	}

	public void setCmVerId(Integer cmVerId) {
		this.cmVerId = cmVerId;
	}

	public Integer getEditImpId() {
		return this.editImpId;
	}

	public void setEditImpId(Integer editImpId) {
		this.editImpId = editImpId;
	}

	public Short getCmLevel() {
		return this.cmLevel;
	}

	public void setCmLevel(Short cmLevel) {
		this.cmLevel = cmLevel;
	}

	public Short getImpNurMeta() {
		return this.impNurMeta;
	}

	public void setImpNurMeta(Short impNurMeta) {
		this.impNurMeta = impNurMeta;
	}

	public Integer getMetaVers() {
		return this.metaVers;
	}

	public void setMetaVers(Integer metaVers) {
		this.metaVers = metaVers;
	}

	public Date getImpDatumImport() {
		return this.impDatumImport;
	}

	public void setImpDatumImport(Date impDatumImport) {
		this.impDatumImport = impDatumImport;
	}

	public Short getImpMapGuid() {
		return this.impMapGuid;
	}

	public void setImpMapGuid(Short impMapGuid) {
		this.impMapGuid = impMapGuid;
	}

	public Short getImpMapNur() {
		return this.impMapNur;
	}

	public void setImpMapNur(Short impMapNur) {
		this.impMapNur = impMapNur;
	}

	public Set getSysImports() {
		return this.sysImports;
	}

	public void setSysImports(Set sysImports) {
		this.sysImports = sysImports;
	}

	public Set getMbDringlichkeits() {
		return this.mbDringlichkeits;
	}

	public void setMbDringlichkeits(Set mbDringlichkeits) {
		this.mbDringlichkeits = mbDringlichkeits;
	}

	public Set getMbBausts() {
		return this.mbBausts;
	}

	public void setMbBausts(Set mbBausts) {
		this.mbBausts = mbBausts;
	}

	public Set getSysImports_1() {
		return this.sysImports_1;
	}

	public void setSysImports_1(Set sysImports_1) {
		this.sysImports_1 = sysImports_1;
	}

	public Set getMbSchichts() {
		return this.mbSchichts;
	}

	public void setMbSchichts(Set mbSchichts) {
		this.mbSchichts = mbSchichts;
	}

	public Set getMbZielobjTyps() {
		return this.mbZielobjTyps;
	}

	public void setMbZielobjTyps(Set mbZielobjTyps) {
		this.mbZielobjTyps = mbZielobjTyps;
	}

	public Set getMbStatuses() {
		return this.mbStatuses;
	}

	public void setMbStatuses(Set mbStatuses) {
		this.mbStatuses = mbStatuses;
	}

	public Set getMbZielobjSubtyps() {
		return this.mbZielobjSubtyps;
	}

	public void setMbZielobjSubtyps(Set mbZielobjSubtyps) {
		this.mbZielobjSubtyps = mbZielobjSubtyps;
	}

}