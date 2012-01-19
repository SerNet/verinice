package sernet.gs.reveng;

import java.util.Date;

/**
 * ModZobjBstMass entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class ModZobjBstMass implements java.io.Serializable {

	// Fields

	private ModZobjBstMassId id;
	private Integer id_1;
	private Integer orgImpId;
	private Short ustId;
	private Integer waeId;
	private Integer waeImpId;
	private Integer notizId;
	private String dateiKosten;
	private String dateiProzess;
	private Date umsDatVon;
	private Date umsDatBis;
	private String umsBeschr;
	private Double kostPersFix;
	private Double kostPersVar;
	private Integer kostPersZeiId;
	private Integer kostPersZeiImpId;
	private Double kostSachFix;
	private Double kostSachVar;
	private Integer kostSachZeiId;
	private Integer kostSachZeiImpId;
	private String revBeschr;
	private Date revDat;
	private Integer revZobIdMit;
	private Date revDatNext;
	private Integer revZobIdMitNext;
	private Short impNeu;
	private Integer usn;
	private String guid;
	private String guidOrg;
	private Date timestamp;
	private Date loeschDatum;
	private String geloeschtDurch;
	private String erfasstDurch;
	private Date changedOn;
	private String changedBy;
	private Date cmTimestamp;
	private String cmUsername;
	private Integer cmImpId;
	private Integer cmVerId1;
	private Short cmVerId2;
	private Short cmStaId;
	private Short zykId;

	// Constructors

	/** default constructor */
	public ModZobjBstMass() {
	}

	/** minimal constructor */
	public ModZobjBstMass(ModZobjBstMassId id, Integer id_1, Integer orgImpId,
			Short ustId, Integer usn, String guid, Short zykId) {
		this.id = id;
		this.id_1 = id_1;
		this.orgImpId = orgImpId;
		this.ustId = ustId;
		this.usn = usn;
		this.guid = guid;
		this.zykId = zykId;
	}

	/** full constructor */
	public ModZobjBstMass(ModZobjBstMassId id, Integer id_1, Integer orgImpId,
			Short ustId, Integer waeId, Integer waeImpId, Integer notizId,
			String dateiKosten, String dateiProzess, Date umsDatVon,
			Date umsDatBis, String umsBeschr, Double kostPersFix,
			Double kostPersVar, Integer kostPersZeiId,
			Integer kostPersZeiImpId, Double kostSachFix, Double kostSachVar,
			Integer kostSachZeiId, Integer kostSachZeiImpId, String revBeschr,
			Date revDat, Integer revZobIdMit, Date revDatNext,
			Integer revZobIdMitNext, Short impNeu, Integer usn, String guid,
			String guidOrg, Date timestamp, Date loeschDatum,
			String geloeschtDurch, String erfasstDurch, Date changedOn,
			String changedBy, Date cmTimestamp, String cmUsername,
			Integer cmImpId, Integer cmVerId1, Short cmVerId2, Short cmStaId,
			Short zykId) {
		this.id = id;
		this.id_1 = id_1;
		this.orgImpId = orgImpId;
		this.ustId = ustId;
		this.waeId = waeId;
		this.waeImpId = waeImpId;
		this.notizId = notizId;
		this.dateiKosten = dateiKosten;
		this.dateiProzess = dateiProzess;
		this.umsDatVon = umsDatVon;
		this.umsDatBis = umsDatBis;
		this.umsBeschr = umsBeschr;
		this.kostPersFix = kostPersFix;
		this.kostPersVar = kostPersVar;
		this.kostPersZeiId = kostPersZeiId;
		this.kostPersZeiImpId = kostPersZeiImpId;
		this.kostSachFix = kostSachFix;
		this.kostSachVar = kostSachVar;
		this.kostSachZeiId = kostSachZeiId;
		this.kostSachZeiImpId = kostSachZeiImpId;
		this.revBeschr = revBeschr;
		this.revDat = revDat;
		this.revZobIdMit = revZobIdMit;
		this.revDatNext = revDatNext;
		this.revZobIdMitNext = revZobIdMitNext;
		this.impNeu = impNeu;
		this.usn = usn;
		this.guid = guid;
		this.guidOrg = guidOrg;
		this.timestamp = timestamp;
		this.loeschDatum = loeschDatum;
		this.geloeschtDurch = geloeschtDurch;
		this.erfasstDurch = erfasstDurch;
		this.changedOn = changedOn;
		this.changedBy = changedBy;
		this.cmTimestamp = cmTimestamp;
		this.cmUsername = cmUsername;
		this.cmImpId = cmImpId;
		this.cmVerId1 = cmVerId1;
		this.cmVerId2 = cmVerId2;
		this.cmStaId = cmStaId;
		this.zykId = zykId;
	}

	// Property accessors

	public ModZobjBstMassId getId() {
		return this.id;
	}

	public void setId(ModZobjBstMassId id) {
		this.id = id;
	}

	public Integer getId_1() {
		return this.id_1;
	}

	public void setId_1(Integer id_1) {
		this.id_1 = id_1;
	}

	public Integer getOrgImpId() {
		return this.orgImpId;
	}

	public void setOrgImpId(Integer orgImpId) {
		this.orgImpId = orgImpId;
	}

	public Short getUstId() {
		return this.ustId;
	}

	public void setUstId(Short ustId) {
		this.ustId = ustId;
	}

	public Integer getWaeId() {
		return this.waeId;
	}

	public void setWaeId(Integer waeId) {
		this.waeId = waeId;
	}

	public Integer getWaeImpId() {
		return this.waeImpId;
	}

	public void setWaeImpId(Integer waeImpId) {
		this.waeImpId = waeImpId;
	}

	public Integer getNotizId() {
		return this.notizId;
	}

	public void setNotizId(Integer notizId) {
		this.notizId = notizId;
	}

	public String getDateiKosten() {
		return this.dateiKosten;
	}

	public void setDateiKosten(String dateiKosten) {
		this.dateiKosten = dateiKosten;
	}

	public String getDateiProzess() {
		return this.dateiProzess;
	}

	public void setDateiProzess(String dateiProzess) {
		this.dateiProzess = dateiProzess;
	}

	public Date getUmsDatVon() {
		return this.umsDatVon;
	}

	public void setUmsDatVon(Date umsDatVon) {
		this.umsDatVon = umsDatVon;
	}

	public Date getUmsDatBis() {
		return this.umsDatBis;
	}

	public void setUmsDatBis(Date umsDatBis) {
		this.umsDatBis = umsDatBis;
	}

	public String getUmsBeschr() {
		return this.umsBeschr;
	}

	public void setUmsBeschr(String umsBeschr) {
		this.umsBeschr = umsBeschr;
	}

	public Double getKostPersFix() {
		return this.kostPersFix;
	}

	public void setKostPersFix(Double kostPersFix) {
		this.kostPersFix = kostPersFix;
	}

	public Double getKostPersVar() {
		return this.kostPersVar;
	}

	public void setKostPersVar(Double kostPersVar) {
		this.kostPersVar = kostPersVar;
	}

	public Integer getKostPersZeiId() {
		return this.kostPersZeiId;
	}

	public void setKostPersZeiId(Integer kostPersZeiId) {
		this.kostPersZeiId = kostPersZeiId;
	}

	public Integer getKostPersZeiImpId() {
		return this.kostPersZeiImpId;
	}

	public void setKostPersZeiImpId(Integer kostPersZeiImpId) {
		this.kostPersZeiImpId = kostPersZeiImpId;
	}

	public Double getKostSachFix() {
		return this.kostSachFix;
	}

	public void setKostSachFix(Double kostSachFix) {
		this.kostSachFix = kostSachFix;
	}

	public Double getKostSachVar() {
		return this.kostSachVar;
	}

	public void setKostSachVar(Double kostSachVar) {
		this.kostSachVar = kostSachVar;
	}

	public Integer getKostSachZeiId() {
		return this.kostSachZeiId;
	}

	public void setKostSachZeiId(Integer kostSachZeiId) {
		this.kostSachZeiId = kostSachZeiId;
	}

	public Integer getKostSachZeiImpId() {
		return this.kostSachZeiImpId;
	}

	public void setKostSachZeiImpId(Integer kostSachZeiImpId) {
		this.kostSachZeiImpId = kostSachZeiImpId;
	}

	public String getRevBeschr() {
		return this.revBeschr;
	}

	public void setRevBeschr(String revBeschr) {
		this.revBeschr = revBeschr;
	}

	public Date getRevDat() {
		return this.revDat;
	}

	public void setRevDat(Date revDat) {
		this.revDat = revDat;
	}

	public Integer getRevZobIdMit() {
		return this.revZobIdMit;
	}

	public void setRevZobIdMit(Integer revZobIdMit) {
		this.revZobIdMit = revZobIdMit;
	}

	public Date getRevDatNext() {
		return this.revDatNext;
	}

	public void setRevDatNext(Date revDatNext) {
		this.revDatNext = revDatNext;
	}

	public Integer getRevZobIdMitNext() {
		return this.revZobIdMitNext;
	}

	public void setRevZobIdMitNext(Integer revZobIdMitNext) {
		this.revZobIdMitNext = revZobIdMitNext;
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

	public String getGeloeschtDurch() {
		return this.geloeschtDurch;
	}

	public void setGeloeschtDurch(String geloeschtDurch) {
		this.geloeschtDurch = geloeschtDurch;
	}

	public String getErfasstDurch() {
		return this.erfasstDurch;
	}

	public void setErfasstDurch(String erfasstDurch) {
		this.erfasstDurch = erfasstDurch;
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

	public Short getZykId() {
		return this.zykId;
	}

	public void setZykId(Short zykId) {
		this.zykId = zykId;
	}

}