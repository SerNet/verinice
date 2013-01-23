package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MbBaust entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbBaust implements java.io.Serializable {

	// Fields

	private MbBaustId id;
	private MsCmState msCmState;
	private MbBaust mbBaust;
	private MMetastatus MMetastatus;
	private MbSchicht mbSchicht;
	private MMetatyp MMetatyp;
	private SysImport sysImport;
	private NmbNotiz nmbNotiz;
	private NZielobjekt NZielobjekt;
	private Integer nrNum;
	private String nr;
	private String link;
	private Byte auditrelevantJn;
	private Integer metaVers;
	private Integer obsoletVers;
	private Date loeschDatum;
	private String guid;
	private Date timestamp;
	private Integer usn;
	private String erfasstDurch;
	private String geloeschtDurch;
	private Short impNeu;
	private String guidOrg;
	private Date changedOn;
	private String changedBy;
	private Date cmTimestamp;
	private String cmUsername;
	private Integer cmImpId;
	private Integer cmVerId1;
	private Short cmVerId2;
	private Set modZobjBsts = new HashSet(0);
	private Set mbBausts = new HashSet(0);

	// Constructors

	/** default constructor */
	public MbBaust() {
	}

	/** minimal constructor */
	public MbBaust(MbBaustId id, MMetastatus MMetastatus, MbSchicht mbSchicht,
			SysImport sysImport, NmbNotiz nmbNotiz, NZielobjekt NZielobjekt,
			String nr, Byte auditrelevantJn, Integer metaVers, String guid,
			Date timestamp, Integer usn) {
		this.id = id;
		this.MMetastatus = MMetastatus;
		this.mbSchicht = mbSchicht;
		this.sysImport = sysImport;
		this.nmbNotiz = nmbNotiz;
		this.NZielobjekt = NZielobjekt;
		this.nr = nr;
		this.auditrelevantJn = auditrelevantJn;
		this.metaVers = metaVers;
		this.guid = guid;
		this.timestamp = timestamp;
		this.usn = usn;
	}

	/** full constructor */
	public MbBaust(MbBaustId id, MsCmState msCmState, MbBaust mbBaust,
			MMetastatus MMetastatus, MbSchicht mbSchicht, MMetatyp MMetatyp,
			SysImport sysImport, NmbNotiz nmbNotiz, NZielobjekt NZielobjekt,
			Integer nrNum, String nr, String link, Byte auditrelevantJn,
			Integer metaVers, Integer obsoletVers, Date loeschDatum,
			String guid, Date timestamp, Integer usn, String erfasstDurch,
			String geloeschtDurch, Short impNeu, String guidOrg,
			Date changedOn, String changedBy, Date cmTimestamp,
			String cmUsername, Integer cmImpId, Integer cmVerId1,
			Short cmVerId2, Set modZobjBsts, Set mbBausts) {
		this.id = id;
		this.msCmState = msCmState;
		this.mbBaust = mbBaust;
		this.MMetastatus = MMetastatus;
		this.mbSchicht = mbSchicht;
		this.MMetatyp = MMetatyp;
		this.sysImport = sysImport;
		this.nmbNotiz = nmbNotiz;
		this.NZielobjekt = NZielobjekt;
		this.nrNum = nrNum;
		this.nr = nr;
		this.link = link;
		this.auditrelevantJn = auditrelevantJn;
		this.metaVers = metaVers;
		this.obsoletVers = obsoletVers;
		this.loeschDatum = loeschDatum;
		this.guid = guid;
		this.timestamp = timestamp;
		this.usn = usn;
		this.erfasstDurch = erfasstDurch;
		this.geloeschtDurch = geloeschtDurch;
		this.impNeu = impNeu;
		this.guidOrg = guidOrg;
		this.changedOn = changedOn;
		this.changedBy = changedBy;
		this.cmTimestamp = cmTimestamp;
		this.cmUsername = cmUsername;
		this.cmImpId = cmImpId;
		this.cmVerId1 = cmVerId1;
		this.cmVerId2 = cmVerId2;
		this.modZobjBsts = modZobjBsts;
		this.mbBausts = mbBausts;
	}

	// Property accessors

	public MbBaustId getId() {
		return this.id;
	}

	public void setId(MbBaustId id) {
		this.id = id;
	}

	public MsCmState getMsCmState() {
		return this.msCmState;
	}

	public void setMsCmState(MsCmState msCmState) {
		this.msCmState = msCmState;
	}

	public MbBaust getMbBaust() {
		return this.mbBaust;
	}

	public void setMbBaust(MbBaust mbBaust) {
		this.mbBaust = mbBaust;
	}

	public MMetastatus getMMetastatus() {
		return this.MMetastatus;
	}

	public void setMMetastatus(MMetastatus MMetastatus) {
		this.MMetastatus = MMetastatus;
	}

	public MbSchicht getMbSchicht() {
		return this.mbSchicht;
	}

	public void setMbSchicht(MbSchicht mbSchicht) {
		this.mbSchicht = mbSchicht;
	}

	public MMetatyp getMMetatyp() {
		return this.MMetatyp;
	}

	public void setMMetatyp(MMetatyp MMetatyp) {
		this.MMetatyp = MMetatyp;
	}

	public SysImport getSysImport() {
		return this.sysImport;
	}

	public void setSysImport(SysImport sysImport) {
		this.sysImport = sysImport;
	}

	public NmbNotiz getNmbNotiz() {
		return this.nmbNotiz;
	}

	public void setNmbNotiz(NmbNotiz nmbNotiz) {
		this.nmbNotiz = nmbNotiz;
	}

	public NZielobjekt getNZielobjekt() {
		return this.NZielobjekt;
	}

	public void setNZielobjekt(NZielobjekt NZielobjekt) {
		this.NZielobjekt = NZielobjekt;
	}

	public Integer getNrNum() {
		return this.nrNum;
	}

	public void setNrNum(Integer nrNum) {
		this.nrNum = nrNum;
	}

	public String getNr() {
		return this.nr;
	}

	public void setNr(String nr) {
		this.nr = nr;
	}

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Byte getAuditrelevantJn() {
		return this.auditrelevantJn;
	}

	public void setAuditrelevantJn(Byte auditrelevantJn) {
		this.auditrelevantJn = auditrelevantJn;
	}

	public Integer getMetaVers() {
		return this.metaVers;
	}

	public void setMetaVers(Integer metaVers) {
		this.metaVers = metaVers;
	}

	public Integer getObsoletVers() {
		return this.obsoletVers;
	}

	public void setObsoletVers(Integer obsoletVers) {
		this.obsoletVers = obsoletVers;
	}

	public Date getLoeschDatum() {
		return this.loeschDatum;
	}

	public void setLoeschDatum(Date loeschDatum) {
		this.loeschDatum = loeschDatum;
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

	public Integer getUsn() {
		return this.usn;
	}

	public void setUsn(Integer usn) {
		this.usn = usn;
	}

	public String getErfasstDurch() {
		return this.erfasstDurch;
	}

	public void setErfasstDurch(String erfasstDurch) {
		this.erfasstDurch = erfasstDurch;
	}

	public String getGeloeschtDurch() {
		return this.geloeschtDurch;
	}

	public void setGeloeschtDurch(String geloeschtDurch) {
		this.geloeschtDurch = geloeschtDurch;
	}

	public Short getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Short impNeu) {
		this.impNeu = impNeu;
	}

	public String getGuidOrg() {
		return this.guidOrg;
	}

	public void setGuidOrg(String guidOrg) {
		this.guidOrg = guidOrg;
	}

	public Date getChangedOn() {
		return this.changedOn;
	}

	public void setChangedOn(Date changedOn) {
		this.changedOn = changedOn;
	}

	public String getChangedBy() {
		return this.changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public Date getCmTimestamp() {
		return this.cmTimestamp;
	}

	public void setCmTimestamp(Date cmTimestamp) {
		this.cmTimestamp = cmTimestamp;
	}

	public String getCmUsername() {
		return this.cmUsername;
	}

	public void setCmUsername(String cmUsername) {
		this.cmUsername = cmUsername;
	}

	public Integer getCmImpId() {
		return this.cmImpId;
	}

	public void setCmImpId(Integer cmImpId) {
		this.cmImpId = cmImpId;
	}

	public Integer getCmVerId1() {
		return this.cmVerId1;
	}

	public void setCmVerId1(Integer cmVerId1) {
		this.cmVerId1 = cmVerId1;
	}

	public Short getCmVerId2() {
		return this.cmVerId2;
	}

	public void setCmVerId2(Short cmVerId2) {
		this.cmVerId2 = cmVerId2;
	}

	public Set getModZobjBsts() {
		return this.modZobjBsts;
	}

	public void setModZobjBsts(Set modZobjBsts) {
		this.modZobjBsts = modZobjBsts;
	}

	public Set getMbBausts() {
		return this.mbBausts;
	}

	public void setMbBausts(Set mbBausts) {
		this.mbBausts = mbBausts;
	}

}