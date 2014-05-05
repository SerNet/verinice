package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MMetatyp entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MMetatyp implements java.io.Serializable {

	// Fields

	private Short mtyId;
	private String guid;
	private Date timestamp;
	private Set mbBausts = new HashSet(0);
	private Set mbZielobjSubtyps = new HashSet(0);
	private Set mbDringlichkeits = new HashSet(0);
	private Set mbStatuses = new HashSet(0);
	private Set mbZielobjTyps = new HashSet(0);

	// Constructors

	/** default constructor */
	public MMetatyp() {
	}

	/** minimal constructor */
	public MMetatyp(Short mtyId) {
		this.mtyId = mtyId;
	}

	/** full constructor */
	public MMetatyp(Short mtyId, String guid, Date timestamp, Set mbBausts,
			Set mbZielobjSubtyps, Set mbDringlichkeits, Set mbStatuses,
			Set mbZielobjTyps) {
		this.mtyId = mtyId;
		this.guid = guid;
		this.timestamp = timestamp;
		this.mbBausts = mbBausts;
		this.mbZielobjSubtyps = mbZielobjSubtyps;
		this.mbDringlichkeits = mbDringlichkeits;
		this.mbStatuses = mbStatuses;
		this.mbZielobjTyps = mbZielobjTyps;
	}

	// Property accessors

	public Short getMtyId() {
		return this.mtyId;
	}

	public void setMtyId(Short mtyId) {
		this.mtyId = mtyId;
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

	public Set getMbBausts() {
		return this.mbBausts;
	}

	public void setMbBausts(Set mbBausts) {
		this.mbBausts = mbBausts;
	}

	public Set getMbZielobjSubtyps() {
		return this.mbZielobjSubtyps;
	}

	public void setMbZielobjSubtyps(Set mbZielobjSubtyps) {
		this.mbZielobjSubtyps = mbZielobjSubtyps;
	}

	public Set getMbDringlichkeits() {
		return this.mbDringlichkeits;
	}

	public void setMbDringlichkeits(Set mbDringlichkeits) {
		this.mbDringlichkeits = mbDringlichkeits;
	}

	public Set getMbStatuses() {
		return this.mbStatuses;
	}

	public void setMbStatuses(Set mbStatuses) {
		this.mbStatuses = mbStatuses;
	}

	public Set getMbZielobjTyps() {
		return this.mbZielobjTyps;
	}

	public void setMbZielobjTyps(Set mbZielobjTyps) {
		this.mbZielobjTyps = mbZielobjTyps;
	}

}