package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * NmbNotiz entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class NmbNotiz implements java.io.Serializable {

	// Fields

	private NmbNotizId id;
	private String notizText;
	private String url;
	private Date timestamp;
	private String guid;
	private String guidOrg;
	private String createdBy;
	private Date changedOn;
	private String changedBy;
	private Set NZielobjekts = new HashSet(0);
	private Set mbBausts = new HashSet(0);
	private Set mbZielobjTyps = new HashSet(0);
	private Set modZobjBsts = new HashSet(0);
	private Set mbZielobjSubtyps = new HashSet(0);

	// Constructors

	/** default constructor */
	public NmbNotiz() {
	}

	/** minimal constructor */
	public NmbNotiz(NmbNotizId id, Date timestamp, String guid) {
		this.id = id;
		this.timestamp = timestamp;
		this.guid = guid;
	}

	/** full constructor */
	public NmbNotiz(NmbNotizId id, String notizText, String url,
			Date timestamp, String guid, String guidOrg, String createdBy,
			Date changedOn, String changedBy, Set NZielobjekts, Set mbBausts,
			Set mbZielobjTyps, Set modZobjBsts, Set mbZielobjSubtyps) {
		this.id = id;
		this.notizText = notizText;
		this.url = url;
		this.timestamp = timestamp;
		this.guid = guid;
		this.guidOrg = guidOrg;
		this.createdBy = createdBy;
		this.changedOn = changedOn;
		this.changedBy = changedBy;
		this.NZielobjekts = NZielobjekts;
		this.mbBausts = mbBausts;
		this.mbZielobjTyps = mbZielobjTyps;
		this.modZobjBsts = modZobjBsts;
		this.mbZielobjSubtyps = mbZielobjSubtyps;
	}

	// Property accessors

	public NmbNotizId getId() {
		return this.id;
	}

	public void setId(NmbNotizId id) {
		this.id = id;
	}

	public String getNotizText() {
		return this.notizText;
	}

	public void setNotizText(String notizText) {
		this.notizText = notizText;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
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

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
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

	public Set getNZielobjekts() {
		return this.NZielobjekts;
	}

	public void setNZielobjekts(Set NZielobjekts) {
		this.NZielobjekts = NZielobjekts;
	}

	public Set getMbBausts() {
		return this.mbBausts;
	}

	public void setMbBausts(Set mbBausts) {
		this.mbBausts = mbBausts;
	}

	public Set getMbZielobjTyps() {
		return this.mbZielobjTyps;
	}

	public void setMbZielobjTyps(Set mbZielobjTyps) {
		this.mbZielobjTyps = mbZielobjTyps;
	}

	public Set getModZobjBsts() {
		return this.modZobjBsts;
	}

	public void setModZobjBsts(Set modZobjBsts) {
		this.modZobjBsts = modZobjBsts;
	}

	public Set getMbZielobjSubtyps() {
		return this.mbZielobjSubtyps;
	}

	public void setMbZielobjSubtyps(Set mbZielobjSubtyps) {
		this.mbZielobjSubtyps = mbZielobjSubtyps;
	}

}