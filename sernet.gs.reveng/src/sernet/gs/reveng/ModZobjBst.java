package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * ModZobjBst entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class ModZobjBst implements java.io.Serializable {

	// Fields

	private ModZobjBstId id;
	private NZielobjekt NZielobjektByFkZbZ;
	private ModZobjBst modZobjBst;
	private MGsiegel MGsiegel;
	private NZielobjekt NZielobjektByFkZbZ2;
	private NmbNotiz nmbNotiz;
	private MbBaust mbBaust;
	private MBstnStatus MBstnStatus;
	private MUmsetzStat MUmsetzStat;
	private Integer orgImpId;
	private String begruendung;
	private Date datum;
	private Short impNeu;
	private Integer usn;
	private String guid;
	private String guidOrg;
	private Date timestamp;
	private Date loeschDatum;
	private Short setDefault;
	private String geloeschtDurch;
	private Integer mmtId;
	private String erfasstDurch;
	private Short bearbeitetOrg;
	private Date changedOn;
	private String changedBy;
	private Date cmTimestamp;
	private String cmUsername;
	private Integer cmImpId;
	private Integer cmVerId1;
	private Short cmVerId2;
	private Short cmStaId;
	private Set modZobjBsts = new HashSet(0);

	// Constructors

	/** default constructor */
	public ModZobjBst() {
	}

	/** minimal constructor */
	public ModZobjBst(ModZobjBstId id, NZielobjekt NZielobjektByFkZbZ,
			ModZobjBst modZobjBst, MGsiegel MGsiegel,
			NZielobjekt NZielobjektByFkZbZ2, NmbNotiz nmbNotiz,
			MbBaust mbBaust, Integer orgImpId, Integer usn, String guid,
			Date timestamp, Short setDefault, Integer mmtId) {
		this.id = id;
		this.NZielobjektByFkZbZ = NZielobjektByFkZbZ;
		this.modZobjBst = modZobjBst;
		this.MGsiegel = MGsiegel;
		this.NZielobjektByFkZbZ2 = NZielobjektByFkZbZ2;
		this.nmbNotiz = nmbNotiz;
		this.mbBaust = mbBaust;
		this.orgImpId = orgImpId;
		this.usn = usn;
		this.guid = guid;
		this.timestamp = timestamp;
		this.setDefault = setDefault;
		this.mmtId = mmtId;
	}

	/** full constructor */
	public ModZobjBst(ModZobjBstId id, NZielobjekt NZielobjektByFkZbZ,
			ModZobjBst modZobjBst, MGsiegel MGsiegel,
			NZielobjekt NZielobjektByFkZbZ2, NmbNotiz nmbNotiz,
			MbBaust mbBaust, MBstnStatus MBstnStatus, MUmsetzStat MUmsetzStat,
			Integer orgImpId, String begruendung, Date datum, Short impNeu,
			Integer usn, String guid, String guidOrg, Date timestamp,
			Date loeschDatum, Short setDefault, String geloeschtDurch,
			Integer mmtId, String erfasstDurch, Short bearbeitetOrg,
			Date changedOn, String changedBy, Date cmTimestamp,
			String cmUsername, Integer cmImpId, Integer cmVerId1,
			Short cmVerId2, Short cmStaId, Set modZobjBsts) {
		this.id = id;
		this.NZielobjektByFkZbZ = NZielobjektByFkZbZ;
		this.modZobjBst = modZobjBst;
		this.MGsiegel = MGsiegel;
		this.NZielobjektByFkZbZ2 = NZielobjektByFkZbZ2;
		this.nmbNotiz = nmbNotiz;
		this.mbBaust = mbBaust;
		this.MBstnStatus = MBstnStatus;
		this.MUmsetzStat = MUmsetzStat;
		this.orgImpId = orgImpId;
		this.begruendung = begruendung;
		this.datum = datum;
		this.impNeu = impNeu;
		this.usn = usn;
		this.guid = guid;
		this.guidOrg = guidOrg;
		this.timestamp = timestamp;
		this.loeschDatum = loeschDatum;
		this.setDefault = setDefault;
		this.geloeschtDurch = geloeschtDurch;
		this.mmtId = mmtId;
		this.erfasstDurch = erfasstDurch;
		this.bearbeitetOrg = bearbeitetOrg;
		this.changedOn = changedOn;
		this.changedBy = changedBy;
		this.cmTimestamp = cmTimestamp;
		this.cmUsername = cmUsername;
		this.cmImpId = cmImpId;
		this.cmVerId1 = cmVerId1;
		this.cmVerId2 = cmVerId2;
		this.cmStaId = cmStaId;
		this.modZobjBsts = modZobjBsts;
	}

	// Property accessors

	public ModZobjBstId getId() {
		return this.id;
	}

	public void setId(ModZobjBstId id) {
		this.id = id;
	}

	public NZielobjekt getNZielobjektByFkZbZ() {
		return this.NZielobjektByFkZbZ;
	}

	public void setNZielobjektByFkZbZ(NZielobjekt NZielobjektByFkZbZ) {
		this.NZielobjektByFkZbZ = NZielobjektByFkZbZ;
	}

	public ModZobjBst getModZobjBst() {
		return this.modZobjBst;
	}

	public void setModZobjBst(ModZobjBst modZobjBst) {
		this.modZobjBst = modZobjBst;
	}

	public MGsiegel getMGsiegel() {
		return this.MGsiegel;
	}

	public void setMGsiegel(MGsiegel MGsiegel) {
		this.MGsiegel = MGsiegel;
	}

	public NZielobjekt getNZielobjektByFkZbZ2() {
		return this.NZielobjektByFkZbZ2;
	}

	public void setNZielobjektByFkZbZ2(NZielobjekt NZielobjektByFkZbZ2) {
		this.NZielobjektByFkZbZ2 = NZielobjektByFkZbZ2;
	}

	public NmbNotiz getNmbNotiz() {
		return this.nmbNotiz;
	}

	public void setNmbNotiz(NmbNotiz nmbNotiz) {
		this.nmbNotiz = nmbNotiz;
	}

	public MbBaust getMbBaust() {
		return this.mbBaust;
	}

	public void setMbBaust(MbBaust mbBaust) {
		this.mbBaust = mbBaust;
	}

	public MBstnStatus getMBstnStatus() {
		return this.MBstnStatus;
	}

	public void setMBstnStatus(MBstnStatus MBstnStatus) {
		this.MBstnStatus = MBstnStatus;
	}

	public MUmsetzStat getMUmsetzStat() {
		return this.MUmsetzStat;
	}

	public void setMUmsetzStat(MUmsetzStat MUmsetzStat) {
		this.MUmsetzStat = MUmsetzStat;
	}

	public Integer getOrgImpId() {
		return this.orgImpId;
	}

	public void setOrgImpId(Integer orgImpId) {
		this.orgImpId = orgImpId;
	}

	public String getBegruendung() {
		return this.begruendung;
	}

	public void setBegruendung(String begruendung) {
		this.begruendung = begruendung;
	}

	public Date getDatum() {
		return this.datum;
	}

	public void setDatum(Date datum) {
		this.datum = datum;
	}

	public Short getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Short impNeu) {
		this.impNeu = impNeu;
	}

	public Integer getUsn() {
		return this.usn;
	}

	public void setUsn(Integer usn) {
		this.usn = usn;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getGuidOrg() {
		return this.guidOrg;
	}

	public void setGuidOrg(String guidOrg) {
		this.guidOrg = guidOrg;
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

	public Short getSetDefault() {
		return this.setDefault;
	}

	public void setSetDefault(Short setDefault) {
		this.setDefault = setDefault;
	}

	public String getGeloeschtDurch() {
		return this.geloeschtDurch;
	}

	public void setGeloeschtDurch(String geloeschtDurch) {
		this.geloeschtDurch = geloeschtDurch;
	}

	public Integer getMmtId() {
		return this.mmtId;
	}

	public void setMmtId(Integer mmtId) {
		this.mmtId = mmtId;
	}

	public String getErfasstDurch() {
		return this.erfasstDurch;
	}

	public void setErfasstDurch(String erfasstDurch) {
		this.erfasstDurch = erfasstDurch;
	}

	public Short getBearbeitetOrg() {
		return this.bearbeitetOrg;
	}

	public void setBearbeitetOrg(Short bearbeitetOrg) {
		this.bearbeitetOrg = bearbeitetOrg;
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

	public Short getCmStaId() {
		return this.cmStaId;
	}

	public void setCmStaId(Short cmStaId) {
		this.cmStaId = cmStaId;
	}

	public Set getModZobjBsts() {
		return this.modZobjBsts;
	}

	public void setModZobjBsts(Set modZobjBsts) {
		this.modZobjBsts = modZobjBsts;
	}

}