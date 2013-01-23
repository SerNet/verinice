package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MbZielobjTyp entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbZielobjTyp implements java.io.Serializable {

	// Fields

	private MbZielobjTypId id;
	private MsCmState msCmState;
	private SysImport sysImport;
	private MMetastatus MMetastatus;
	private NmbNotiz nmbNotiz;
	private MbZielobjTyp mbZielobjTypByFkZotZot;
	private MMetatyp MMetatyp;
	private MbZielobjTyp mbZielobjTypByFkMbZielobjTypMbZielobjTyp;
	private String link;
	private Integer metaVers;
	private Integer obsoletVers;
	private String guid;
	private Date timestamp;
	private Date loeschDatum;
	private Integer usn;
	private String erfasstDurch;
	private String geloeschtDurch;
	private Short impNeu;
	private String guidOrg;
	private Date changedOn;
	private String changedBy;
	private Short reihenfolge;
	private Date cmTimestamp;
	private String cmUsername;
	private Integer cmImpId;
	private Integer cmVerId1;
	private Short cmVerId2;
	private Set mbZielobjTypsForFkZotZot = new HashSet(0);
	private Set mbZielobjSubtyps = new HashSet(0);
	private Set mbZielobjTypsForFkMbZielobjTypMbZielobjTyp = new HashSet(0);

	// Constructors

	/** default constructor */
	public MbZielobjTyp() {
	}

	/** minimal constructor */
	public MbZielobjTyp(MbZielobjTypId id, SysImport sysImport,
			MMetastatus MMetastatus, NmbNotiz nmbNotiz,
			MbZielobjTyp mbZielobjTypByFkZotZot,
			MbZielobjTyp mbZielobjTypByFkMbZielobjTypMbZielobjTyp,
			Integer metaVers, String guid, Integer usn) {
		this.id = id;
		this.sysImport = sysImport;
		this.MMetastatus = MMetastatus;
		this.nmbNotiz = nmbNotiz;
		this.mbZielobjTypByFkZotZot = mbZielobjTypByFkZotZot;
		this.mbZielobjTypByFkMbZielobjTypMbZielobjTyp = mbZielobjTypByFkMbZielobjTypMbZielobjTyp;
		this.metaVers = metaVers;
		this.guid = guid;
		this.usn = usn;
	}

	/** full constructor */
	public MbZielobjTyp(MbZielobjTypId id, MsCmState msCmState,
			SysImport sysImport, MMetastatus MMetastatus, NmbNotiz nmbNotiz,
			MbZielobjTyp mbZielobjTypByFkZotZot, MMetatyp MMetatyp,
			MbZielobjTyp mbZielobjTypByFkMbZielobjTypMbZielobjTyp, String link,
			Integer metaVers, Integer obsoletVers, String guid, Date timestamp,
			Date loeschDatum, Integer usn, String erfasstDurch,
			String geloeschtDurch, Short impNeu, String guidOrg,
			Date changedOn, String changedBy, Short reihenfolge,
			Date cmTimestamp, String cmUsername, Integer cmImpId,
			Integer cmVerId1, Short cmVerId2, Set mbZielobjTypsForFkZotZot,
			Set mbZielobjSubtyps, Set mbZielobjTypsForFkMbZielobjTypMbZielobjTyp) {
		this.id = id;
		this.msCmState = msCmState;
		this.sysImport = sysImport;
		this.MMetastatus = MMetastatus;
		this.nmbNotiz = nmbNotiz;
		this.mbZielobjTypByFkZotZot = mbZielobjTypByFkZotZot;
		this.MMetatyp = MMetatyp;
		this.mbZielobjTypByFkMbZielobjTypMbZielobjTyp = mbZielobjTypByFkMbZielobjTypMbZielobjTyp;
		this.link = link;
		this.metaVers = metaVers;
		this.obsoletVers = obsoletVers;
		this.guid = guid;
		this.timestamp = timestamp;
		this.loeschDatum = loeschDatum;
		this.usn = usn;
		this.erfasstDurch = erfasstDurch;
		this.geloeschtDurch = geloeschtDurch;
		this.impNeu = impNeu;
		this.guidOrg = guidOrg;
		this.changedOn = changedOn;
		this.changedBy = changedBy;
		this.reihenfolge = reihenfolge;
		this.cmTimestamp = cmTimestamp;
		this.cmUsername = cmUsername;
		this.cmImpId = cmImpId;
		this.cmVerId1 = cmVerId1;
		this.cmVerId2 = cmVerId2;
		this.mbZielobjTypsForFkZotZot = mbZielobjTypsForFkZotZot;
		this.mbZielobjSubtyps = mbZielobjSubtyps;
		this.mbZielobjTypsForFkMbZielobjTypMbZielobjTyp = mbZielobjTypsForFkMbZielobjTypMbZielobjTyp;
	}

	// Property accessors

	public MbZielobjTypId getId() {
		return this.id;
	}

	public void setId(MbZielobjTypId id) {
		this.id = id;
	}

	public MsCmState getMsCmState() {
		return this.msCmState;
	}

	public void setMsCmState(MsCmState msCmState) {
		this.msCmState = msCmState;
	}

	public SysImport getSysImport() {
		return this.sysImport;
	}

	public void setSysImport(SysImport sysImport) {
		this.sysImport = sysImport;
	}

	public MMetastatus getMMetastatus() {
		return this.MMetastatus;
	}

	public void setMMetastatus(MMetastatus MMetastatus) {
		this.MMetastatus = MMetastatus;
	}

	public NmbNotiz getNmbNotiz() {
		return this.nmbNotiz;
	}

	public void setNmbNotiz(NmbNotiz nmbNotiz) {
		this.nmbNotiz = nmbNotiz;
	}

	public MbZielobjTyp getMbZielobjTypByFkZotZot() {
		return this.mbZielobjTypByFkZotZot;
	}

	public void setMbZielobjTypByFkZotZot(MbZielobjTyp mbZielobjTypByFkZotZot) {
		this.mbZielobjTypByFkZotZot = mbZielobjTypByFkZotZot;
	}

	public MMetatyp getMMetatyp() {
		return this.MMetatyp;
	}

	public void setMMetatyp(MMetatyp MMetatyp) {
		this.MMetatyp = MMetatyp;
	}

	public MbZielobjTyp getMbZielobjTypByFkMbZielobjTypMbZielobjTyp() {
		return this.mbZielobjTypByFkMbZielobjTypMbZielobjTyp;
	}

	public void setMbZielobjTypByFkMbZielobjTypMbZielobjTyp(
			MbZielobjTyp mbZielobjTypByFkMbZielobjTypMbZielobjTyp) {
		this.mbZielobjTypByFkMbZielobjTypMbZielobjTyp = mbZielobjTypByFkMbZielobjTypMbZielobjTyp;
	}

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
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

	public Short getReihenfolge() {
		return this.reihenfolge;
	}

	public void setReihenfolge(Short reihenfolge) {
		this.reihenfolge = reihenfolge;
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

	public Set getMbZielobjTypsForFkZotZot() {
		return this.mbZielobjTypsForFkZotZot;
	}

	public void setMbZielobjTypsForFkZotZot(Set mbZielobjTypsForFkZotZot) {
		this.mbZielobjTypsForFkZotZot = mbZielobjTypsForFkZotZot;
	}

	public Set getMbZielobjSubtyps() {
		return this.mbZielobjSubtyps;
	}

	public void setMbZielobjSubtyps(Set mbZielobjSubtyps) {
		this.mbZielobjSubtyps = mbZielobjSubtyps;
	}

	public Set getMbZielobjTypsForFkMbZielobjTypMbZielobjTyp() {
		return this.mbZielobjTypsForFkMbZielobjTypMbZielobjTyp;
	}

	public void setMbZielobjTypsForFkMbZielobjTypMbZielobjTyp(
			Set mbZielobjTypsForFkMbZielobjTypMbZielobjTyp) {
		this.mbZielobjTypsForFkMbZielobjTypMbZielobjTyp = mbZielobjTypsForFkMbZielobjTypMbZielobjTyp;
	}

}