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
	private NZielobjekt nZielobjektByFkZbZ;
	private ModZobjBst modZobjBst;
	private MGsiegel mGsiegel;
	private NZielobjekt nZielobjektByFkZbZ2;
	private NmbNotiz nmbNotiz;
	private MbBaust mbBaust;
	private MBstnStatus mBstnStatus;
	private MUmsetzStat mUmsetzStat;
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
	private Integer refZobId;

	// Constructors

	/**
     * @return the refZobId
     */
    public Integer getRefZobId() {
        return refZobId;
    }

    /**
     * @param refZobId the refZobId to set
     */
    public void setRefZobId(Integer refZobId) {
        this.refZobId = refZobId;
    }

    /** default constructor */
	public ModZobjBst() {
	}

	/** minimal constructor */
	public ModZobjBst(ModZobjBstId id, NZielobjekt nZielobjektByFkZbZ,
			ModZobjBst modZobjBst, MGsiegel mGsiegel,
			NZielobjekt nZielobjektByFkZbZ2, NmbNotiz nmbNotiz,
			MbBaust mbBaust, Integer orgImpId, Integer usn, String guid,
			Date timestamp, Short setDefault, Integer mmtId) {
		this.id = id;
		this.nZielobjektByFkZbZ = nZielobjektByFkZbZ;
		this.modZobjBst = modZobjBst;
		this.mGsiegel = mGsiegel;
		this.nZielobjektByFkZbZ2 = nZielobjektByFkZbZ2;
		this.nmbNotiz = nmbNotiz;
		this.mbBaust = mbBaust;
		this.orgImpId = orgImpId;
		this.usn = usn;
		this.guid = guid;
		this.timestamp = (timestamp != null) ? (Date)timestamp.clone() : null;
		this.setDefault = setDefault;
		this.mmtId = mmtId;
	}

	/** full constructor */
	public ModZobjBst(ModZobjBstId id, NZielobjekt nZielobjektByFkZbZ,
			ModZobjBst modZobjBst, MGsiegel mGsiegel,
			NZielobjekt nZielobjektByFkZbZ2, NmbNotiz nmbNotiz,
			MbBaust mbBaust, MBstnStatus MBstnStatus, MUmsetzStat mUmsetzStat,
			Integer orgImpId, String begruendung, Date datum, Short impNeu,
			Integer usn, String guid, String guidOrg, Date timestamp,
			Date loeschDatum, Short setDefault, String geloeschtDurch,
			Integer mmtId, String erfasstDurch, Short bearbeitetOrg,
			Date changedOn, String changedBy, Date cmTimestamp,
			String cmUsername, Integer cmImpId, Integer cmVerId1,
			Short cmVerId2, Short cmStaId, Set modZobjBsts, Integer refzobid) {
		this.id = id;
		this.nZielobjektByFkZbZ = nZielobjektByFkZbZ;
		this.modZobjBst = modZobjBst;
		this.mGsiegel = mGsiegel;
		this.nZielobjektByFkZbZ2 = nZielobjektByFkZbZ2;
		this.nmbNotiz = nmbNotiz;
		this.mbBaust = mbBaust;
		this.mBstnStatus = MBstnStatus;
		this.mUmsetzStat = mUmsetzStat;
		this.orgImpId = orgImpId;
		this.begruendung = begruendung;
		this.datum = (datum != null) ? (Date)datum.clone() : null;
		this.impNeu = impNeu;
		this.usn = usn;
		this.guid = guid;
		this.guidOrg = guidOrg;
		this.timestamp = (timestamp != null) ? (Date)timestamp.clone() : null;
		this.loeschDatum = (loeschDatum != null) ? (Date)loeschDatum.clone() : null;
		this.setDefault = setDefault;
		this.geloeschtDurch = geloeschtDurch;
		this.mmtId = mmtId;
		this.erfasstDurch = erfasstDurch;
		this.bearbeitetOrg = bearbeitetOrg;
		this.changedOn = (changedOn != null) ? (Date)changedOn.clone() : null;
		this.changedBy = changedBy;
		this.cmTimestamp = (cmTimestamp != null) ? (Date)cmTimestamp.clone() : null;
		this.cmUsername = cmUsername;
		this.cmImpId = cmImpId;
		this.cmVerId1 = cmVerId1;
		this.cmVerId2 = cmVerId2;
		this.cmStaId = cmStaId;
		this.modZobjBsts = modZobjBsts;
		this.refZobId = refzobid;
	}

	// Property accessors

	public ModZobjBstId getId() {
		return this.id;
	}

	public void setId(ModZobjBstId id) {
		this.id = id;
	}

	public NZielobjekt getNZielobjektByFkZbZ() {
		return this.nZielobjektByFkZbZ;
	}

	public void setNZielobjektByFkZbZ(NZielobjekt nZielobjektByFkZbZ) {
		this.nZielobjektByFkZbZ = nZielobjektByFkZbZ;
	}

	public ModZobjBst getModZobjBst() {
		return this.modZobjBst;
	}

	public void setModZobjBst(ModZobjBst modZobjBst) {
		this.modZobjBst = modZobjBst;
	}

	public MGsiegel getMGsiegel() {
		return this.mGsiegel;
	}

	public void setMGsiegel(MGsiegel mGsiegel) {
		this.mGsiegel = mGsiegel;
	}

	public NZielobjekt getNZielobjektByFkZbZ2() {
		return this.nZielobjektByFkZbZ2;
	}

	public void setNZielobjektByFkZbZ2(NZielobjekt nZielobjektByFkZbZ2) {
		this.nZielobjektByFkZbZ2 = nZielobjektByFkZbZ2;
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
		return this.mBstnStatus;
	}

	public void setMBstnStatus(MBstnStatus mBstnStatus) {
		this.mBstnStatus = mBstnStatus;
	}

	public MUmsetzStat getMUmsetzStat() {
		return this.mUmsetzStat;
	}

	public void setMUmsetzStat(MUmsetzStat mUmsetzStat) {
		this.mUmsetzStat = mUmsetzStat;
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
		return (this.datum != null) ? (Date)this.datum.clone() : null;
	}

	public void setDatum(Date datum) {
		this.datum = (datum != null) ? (Date)datum.clone() : null;
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
		return (this.timestamp != null) ? (Date)this.timestamp.clone() : null;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = (timestamp != null) ? (Date)timestamp.clone() : null;
	}

	public Date getLoeschDatum() {
		return (this.loeschDatum != null) ? (Date)this.loeschDatum.clone() : null;
	}

	public void setLoeschDatum(Date loeschDatum) {
		this.loeschDatum = (loeschDatum != null) ? (Date)loeschDatum.clone() : null;
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
		return (this.changedOn != null) ? (Date)this.changedOn.clone() : null;
	}

	public void setChangedOn(Date changedOn) {
		this.changedOn = (changedOn != null) ? (Date)changedOn.clone() : null;
	}

	public String getChangedBy() {
		return this.changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public Date getCmTimestamp() {
		return (this.timestamp != null) ? (Date)this.cmTimestamp.clone() : null;
	}

	public void setCmTimestamp(Date cmTimestamp) {
		this.cmTimestamp = (cmTimestamp != null) ? (Date)cmTimestamp.clone() : null;
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