package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MbMassn entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbMassn implements java.io.Serializable {

	// Fields

	private MbMassnId id;
	private MbMassn mbMassn;
	private Integer mskId;
	private Integer mskImpId;
	private Integer nr;
	private String nrAlt;
	private Integer notizId;
	private String link;
	private Short metaNeu;
	private Integer metaVers;
	private Integer obsoletVers;
	private Date loeschDatum;
	private Short userdef;
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
	private Short cmStaId;
	private Set mbMassns = new HashSet(0);

	// Constructors

	/** default constructor */
	public MbMassn() {
	}

	/** minimal constructor */
	public MbMassn(MbMassnId id, Integer mskId, Integer mskImpId, Integer nr,
			Short metaNeu, Integer metaVers, String guid, Date timestamp,
			Integer usn) {
		this.id = id;
		this.mskId = mskId;
		this.mskImpId = mskImpId;
		this.nr = nr;
		this.metaNeu = metaNeu;
		this.metaVers = metaVers;
		this.guid = guid;
		this.timestamp = timestamp;
		this.usn = usn;
	}

	/** full constructor */
	public MbMassn(MbMassnId id, MbMassn mbMassn, Integer mskId,
			Integer mskImpId, Integer nr, String nrAlt, Integer notizId,
			String link, Short metaNeu, Integer metaVers, Integer obsoletVers,
			Date loeschDatum, Short userdef, String guid, Date timestamp,
			Integer usn, String erfasstDurch, String geloeschtDurch,
			Short impNeu, String guidOrg, Date changedOn, String changedBy,
			Date cmTimestamp, String cmUsername, Integer cmImpId,
			Integer cmVerId1, Short cmVerId2, Short cmStaId, Set mbMassns) {
		this.id = id;
		this.mbMassn = mbMassn;
		this.mskId = mskId;
		this.mskImpId = mskImpId;
		this.nr = nr;
		this.nrAlt = nrAlt;
		this.notizId = notizId;
		this.link = link;
		this.metaNeu = metaNeu;
		this.metaVers = metaVers;
		this.obsoletVers = obsoletVers;
		this.loeschDatum = loeschDatum;
		this.userdef = userdef;
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
		this.cmStaId = cmStaId;
		this.mbMassns = mbMassns;
	}

	// Property accessors

	public MbMassnId getId() {
		return this.id;
	}

	public void setId(MbMassnId id) {
		this.id = id;
	}

	public MbMassn getMbMassn() {
		return this.mbMassn;
	}

	public void setMbMassn(MbMassn mbMassn) {
		this.mbMassn = mbMassn;
	}

	public Integer getMskId() {
		return this.mskId;
	}

	public void setMskId(Integer mskId) {
		this.mskId = mskId;
	}

	public Integer getMskImpId() {
		return this.mskImpId;
	}

	public void setMskImpId(Integer mskImpId) {
		this.mskImpId = mskImpId;
	}

	public Integer getNr() {
		return this.nr;
	}

	public void setNr(Integer nr) {
		this.nr = nr;
	}

	public String getNrAlt() {
		return this.nrAlt;
	}

	public void setNrAlt(String nrAlt) {
		this.nrAlt = nrAlt;
	}

	public Integer getNotizId() {
		return this.notizId;
	}

	public void setNotizId(Integer notizId) {
		this.notizId = notizId;
	}

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Short getMetaNeu() {
		return this.metaNeu;
	}

	public void setMetaNeu(Short metaNeu) {
		this.metaNeu = metaNeu;
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

	public Short getUserdef() {
		return this.userdef;
	}

	public void setUserdef(Short userdef) {
		this.userdef = userdef;
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

	public Short getCmStaId() {
		return this.cmStaId;
	}

	public void setCmStaId(Short cmStaId) {
		this.cmStaId = cmStaId;
	}

	public Set getMbMassns() {
		return this.mbMassns;
	}

	public void setMbMassns(Set mbMassns) {
		this.mbMassns = mbMassns;
	}

}