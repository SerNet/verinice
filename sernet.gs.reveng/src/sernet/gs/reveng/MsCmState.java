package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MsCmState entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MsCmState implements java.io.Serializable {

	// Fields

	private Short cmStaId;
	private String guid;
	private Date timestamp;
	private Set mbZielobjSubtyps = new HashSet(0);
	private Set NZielobjekts = new HashSet(0);
	private Set mbZielobjTyps = new HashSet(0);
	private Set mbBausts = new HashSet(0);

	// Constructors

	/** default constructor */
	public MsCmState() {
	}

	/** minimal constructor */
	public MsCmState(Short cmStaId) {
		this.cmStaId = cmStaId;
	}

	/** full constructor */
	public MsCmState(Short cmStaId, String guid, Date timestamp,
			Set mbZielobjSubtyps, Set NZielobjekts, Set mbZielobjTyps,
			Set mbBausts) {
		this.cmStaId = cmStaId;
		this.guid = guid;
		this.timestamp = timestamp;
		this.mbZielobjSubtyps = mbZielobjSubtyps;
		this.NZielobjekts = NZielobjekts;
		this.mbZielobjTyps = mbZielobjTyps;
		this.mbBausts = mbBausts;
	}

	// Property accessors

	public Short getCmStaId() {
		return this.cmStaId;
	}

	public void setCmStaId(Short cmStaId) {
		this.cmStaId = cmStaId;
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

	public Set getMbZielobjSubtyps() {
		return this.mbZielobjSubtyps;
	}

	public void setMbZielobjSubtyps(Set mbZielobjSubtyps) {
		this.mbZielobjSubtyps = mbZielobjSubtyps;
	}

	public Set getNZielobjekts() {
		return this.NZielobjekts;
	}

	public void setNZielobjekts(Set NZielobjekts) {
		this.NZielobjekts = NZielobjekts;
	}

	public Set getMbZielobjTyps() {
		return this.mbZielobjTyps;
	}

	public void setMbZielobjTyps(Set mbZielobjTyps) {
		this.mbZielobjTyps = mbZielobjTyps;
	}

	public Set getMbBausts() {
		return this.mbBausts;
	}

	public void setMbBausts(Set mbBausts) {
		this.mbBausts = mbBausts;
	}

}