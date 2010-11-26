package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MbStatus entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbStatus implements java.io.Serializable {

	// Fields

	private MbStatusId id;
	private SysImport sysImport;
	private MMetastatus MMetastatus;
	private MMetatyp MMetatyp;
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
	private Set NZielobjekts = new HashSet(0);

	// Constructors

	/** default constructor */
	public MbStatus() {
	}

	/** minimal constructor */
	public MbStatus(MbStatusId id, SysImport sysImport,
			MMetastatus MMetastatus, Integer metaVers) {
		this.id = id;
		this.sysImport = sysImport;
		this.MMetastatus = MMetastatus;
		this.metaVers = metaVers;
	}

	/** full constructor */
	public MbStatus(MbStatusId id, SysImport sysImport,
			MMetastatus MMetastatus, MMetatyp MMetatyp, Integer metaVers,
			Integer obsoletVers, String link, Integer notizId, String guid,
			Date timestamp, Date loeschDatum, String erfasstDurch,
			String geloeschtDurch, Short impNeu, String guidOrg,
			Set NZielobjekts) {
		this.id = id;
		this.sysImport = sysImport;
		this.MMetastatus = MMetastatus;
		this.MMetatyp = MMetatyp;
		this.metaVers = metaVers;
		this.obsoletVers = obsoletVers;
		this.link = link;
		this.notizId = notizId;
		this.guid = guid;
		this.timestamp = timestamp;
		this.loeschDatum = loeschDatum;
		this.erfasstDurch = erfasstDurch;
		this.geloeschtDurch = geloeschtDurch;
		this.impNeu = impNeu;
		this.guidOrg = guidOrg;
		this.NZielobjekts = NZielobjekts;
	}

	// Property accessors

	public MbStatusId getId() {
		return this.id;
	}

	public void setId(MbStatusId id) {
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

	public MMetatyp getMMetatyp() {
		return this.MMetatyp;
	}

	public void setMMetatyp(MMetatyp MMetatyp) {
		this.MMetatyp = MMetatyp;
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
		return this.NZielobjekts;
	}

	public void setNZielobjekts(Set NZielobjekts) {
		this.NZielobjekts = NZielobjekts;
	}

}