package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MbSchicht entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbSchicht implements java.io.Serializable {

	// Fields

	private MbSchichtId id;
	private SysImport sysImport;
	private MMetastatus MMetastatus;
	private String link;
	private Integer metaVers;
	private Integer obsoletVers;
	private Date loeschDatum;
	private String guid;
	private Date timestamp;
	private String erfasstDurch;
	private String geloeschtDurch;
	private Short impNeu;
	private Short mtyId;
	private String guidOrg;
	private Set mbBausts = new HashSet(0);

	// Constructors

	/** default constructor */
	public MbSchicht() {
	}

	/** minimal constructor */
	public MbSchicht(MbSchichtId id, SysImport sysImport,
			MMetastatus MMetastatus, Integer metaVers, String guid,
			Date timestamp) {
		this.id = id;
		this.sysImport = sysImport;
		this.MMetastatus = MMetastatus;
		this.metaVers = metaVers;
		this.guid = guid;
		this.timestamp = timestamp;
	}

	/** full constructor */
	public MbSchicht(MbSchichtId id, SysImport sysImport,
			MMetastatus MMetastatus, String link, Integer metaVers,
			Integer obsoletVers, Date loeschDatum, String guid, Date timestamp,
			String erfasstDurch, String geloeschtDurch, Short impNeu,
			Short mtyId, String guidOrg, Set mbBausts) {
		this.id = id;
		this.sysImport = sysImport;
		this.MMetastatus = MMetastatus;
		this.link = link;
		this.metaVers = metaVers;
		this.obsoletVers = obsoletVers;
		this.loeschDatum = loeschDatum;
		this.guid = guid;
		this.timestamp = timestamp;
		this.erfasstDurch = erfasstDurch;
		this.geloeschtDurch = geloeschtDurch;
		this.impNeu = impNeu;
		this.mtyId = mtyId;
		this.guidOrg = guidOrg;
		this.mbBausts = mbBausts;
	}

	// Property accessors

	public MbSchichtId getId() {
		return this.id;
	}

	public void setId(MbSchichtId id) {
		this.id = id;
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

	public Short getMtyId() {
		return this.mtyId;
	}

	public void setMtyId(Short mtyId) {
		this.mtyId = mtyId;
	}

	public String getGuidOrg() {
		return this.guidOrg;
	}

	public void setGuidOrg(String guidOrg) {
		this.guidOrg = guidOrg;
	}

	public Set getMbBausts() {
		return this.mbBausts;
	}

	public void setMbBausts(Set mbBausts) {
		this.mbBausts = mbBausts;
	}

}