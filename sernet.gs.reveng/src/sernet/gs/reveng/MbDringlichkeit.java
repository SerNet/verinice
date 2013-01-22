package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MbDringlichkeit entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbDringlichkeit implements java.io.Serializable {

	// Fields

	private MbDringlichkeitId id;
	private SysImport sysImport;
	private MMetastatus mMetastatus;
	private MMetatyp mMetatyp;
	private Integer metaVers;
	private Integer obsoletVers;
	private String link;
	private Integer notizId;
	private String guid;
	private Date timestamp;
	private Date loeschDatum;
	private String erfasstDurch;
	private String geloeschtDurch;
	private Short impNeu;
	private String guidOrg;
	private Set nZielobjekts = new HashSet(0);

	// Constructors

	/** default constructor */
	public MbDringlichkeit() {
	}

	/** minimal constructor */
	public MbDringlichkeit(MbDringlichkeitId id, SysImport sysImport,
			MMetastatus mMetastatus, MMetatyp mMetatyp, Integer metaVers) {
		this.id = id;
		this.sysImport = sysImport;
		this.mMetastatus = mMetastatus;
		this.mMetatyp = mMetatyp;
		this.metaVers = metaVers;
	}

	/** full constructor */
	public MbDringlichkeit(MbDringlichkeitId id, SysImport sysImport,
			MMetastatus mMetastatus, MMetatyp mMetatyp, Integer metaVers,
			Integer obsoletVers, String link, Integer notizId, String guid,
			Date timestamp, Date loeschDatum, String erfasstDurch,
			String geloeschtDurch, Short impNeu, String guidOrg,
			Set nZielobjekts) {
		this.id = id;
		this.sysImport = sysImport;
		this.mMetastatus = mMetastatus;
		this.mMetatyp = mMetatyp;
		this.metaVers = metaVers;
		this.obsoletVers = obsoletVers;
		this.link = link;
		this.notizId = notizId;
		this.guid = guid;
		this.timestamp = (timestamp != null) ? (Date)timestamp.clone() : null;
		this.loeschDatum = (loeschDatum != null) ? (Date)loeschDatum.clone() : null;
		this.erfasstDurch = erfasstDurch;
		this.geloeschtDurch = geloeschtDurch;
		this.impNeu = impNeu;
		this.guidOrg = guidOrg;
		this.nZielobjekts = nZielobjekts;
	}

	// Property accessors

	public MbDringlichkeitId getId() {
		return this.id;
	}

	public void setId(MbDringlichkeitId id) {
		this.id = id;
	}

	public SysImport getSysImport() {
		return this.sysImport;
	}

	public void setSysImport(SysImport sysImport) {
		this.sysImport = sysImport;
	}

	public MMetastatus getMMetastatus() {
		return this.mMetastatus;
	}

	public void setMMetastatus(MMetastatus mMetastatus) {
		this.mMetastatus = mMetastatus;
	}

	public MMetatyp getMMetatyp() {
		return this.mMetatyp;
	}

	public void setMMetatyp(MMetatyp mMetatyp) {
		this.mMetatyp = mMetatyp;
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

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Integer getNotizId() {
		return this.notizId;
	}

	public void setNotizId(Integer notizId) {
		this.notizId = notizId;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
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

	public Set getNZielobjekts() {
		return this.nZielobjekts;
	}

	public void setNZielobjekts(Set nZielobjekts) {
		this.nZielobjekts = nZielobjekts;
	}

}