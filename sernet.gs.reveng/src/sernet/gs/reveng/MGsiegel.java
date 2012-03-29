package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MGsiegel entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MGsiegel implements java.io.Serializable {

	// Fields

	private Short gruId;
	private Short siegelstufe;
	private String guid;
	private Date timestamp;
	private Set modZobjBsts = new HashSet(0);
	private Set NZielobjektsForSiegel = new HashSet(0);
	private Set NZielobjektsForSiegelItv = new HashSet(0);

	// Constructors

	/** default constructor */
	public MGsiegel() {
	}

	/** minimal constructor */
	public MGsiegel(Short gruId, Short siegelstufe, String guid, Date timestamp) {
		this.gruId = gruId;
		this.siegelstufe = siegelstufe;
		this.guid = guid;
		this.timestamp = timestamp;
	}

	/** full constructor */
	public MGsiegel(Short gruId, Short siegelstufe, String guid,
			Date timestamp, Set modZobjBsts, Set NZielobjektsForSiegel,
			Set NZielobjektsForSiegelItv) {
		this.gruId = gruId;
		this.siegelstufe = siegelstufe;
		this.guid = guid;
		this.timestamp = timestamp;
		this.modZobjBsts = modZobjBsts;
		this.NZielobjektsForSiegel = NZielobjektsForSiegel;
		this.NZielobjektsForSiegelItv = NZielobjektsForSiegelItv;
	}

	// Property accessors

	public Short getGruId() {
		return this.gruId;
	}

	public void setGruId(Short gruId) {
		this.gruId = gruId;
	}

	public Short getSiegelstufe() {
		return this.siegelstufe;
	}

	public void setSiegelstufe(Short siegelstufe) {
		this.siegelstufe = siegelstufe;
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

	public Set getModZobjBsts() {
		return this.modZobjBsts;
	}

	public void setModZobjBsts(Set modZobjBsts) {
		this.modZobjBsts = modZobjBsts;
	}

	public Set getNZielobjektsForSiegel() {
		return this.NZielobjektsForSiegel;
	}

	public void setNZielobjektsForSiegel(Set NZielobjektsForSiegel) {
		this.NZielobjektsForSiegel = NZielobjektsForSiegel;
	}

	public Set getNZielobjektsForSiegelItv() {
		return this.NZielobjektsForSiegelItv;
	}

	public void setNZielobjektsForSiegelItv(Set NZielobjektsForSiegelItv) {
		this.NZielobjektsForSiegelItv = NZielobjektsForSiegelItv;
	}

}