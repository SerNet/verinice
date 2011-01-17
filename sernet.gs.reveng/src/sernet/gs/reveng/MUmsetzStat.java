package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MUmsetzStat entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MUmsetzStat implements java.io.Serializable {

	// Fields

	private Short ustId;
	private String guid;
	private Date timestamp;
	private Set NZielobjektsForUstId = new HashSet(0);
	private Set modZobjBsts = new HashSet(0);
	private Set NZielobjektsForUstIdItv = new HashSet(0);

	// Constructors

	/** default constructor */
	public MUmsetzStat() {
	}

	/** minimal constructor */
	public MUmsetzStat(Short ustId, String guid, Date timestamp) {
		this.ustId = ustId;
		this.guid = guid;
		this.timestamp = timestamp;
	}

	/** full constructor */
	public MUmsetzStat(Short ustId, String guid, Date timestamp,
			Set NZielobjektsForUstId, Set modZobjBsts,
			Set NZielobjektsForUstIdItv) {
		this.ustId = ustId;
		this.guid = guid;
		this.timestamp = timestamp;
		this.NZielobjektsForUstId = NZielobjektsForUstId;
		this.modZobjBsts = modZobjBsts;
		this.NZielobjektsForUstIdItv = NZielobjektsForUstIdItv;
	}

	// Property accessors

	public Short getUstId() {
		return this.ustId;
	}

	public void setUstId(Short ustId) {
		this.ustId = ustId;
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

	public Set getNZielobjektsForUstId() {
		return this.NZielobjektsForUstId;
	}

	public void setNZielobjektsForUstId(Set NZielobjektsForUstId) {
		this.NZielobjektsForUstId = NZielobjektsForUstId;
	}

	public Set getModZobjBsts() {
		return this.modZobjBsts;
	}

	public void setModZobjBsts(Set modZobjBsts) {
		this.modZobjBsts = modZobjBsts;
	}

	public Set getNZielobjektsForUstIdItv() {
		return this.NZielobjektsForUstIdItv;
	}

	public void setNZielobjektsForUstIdItv(Set NZielobjektsForUstIdItv) {
		this.NZielobjektsForUstIdItv = NZielobjektsForUstIdItv;
	}

}