package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MMetastatus entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MMetastatus implements java.io.Serializable {

	// Fields

	private Short mstId;
	private String guid;
	private Date timestamp;
	private Set mbSchichts = new HashSet(0);
	private Set mbZielobjTyps = new HashSet(0);
	private Set mbStatuses = new HashSet(0);
	private Set mbBausts = new HashSet(0);
	private Set mbZielobjSubtyps = new HashSet(0);
	private Set mbDringlichkeits = new HashSet(0);

	// Constructors

	/** default constructor */
	public MMetastatus() {
	}

	/** minimal constructor */
	public MMetastatus(Short mstId) {
		this.mstId = mstId;
	}

	/** full constructor */
	public MMetastatus(Short mstId, String guid, Date timestamp,
			Set mbSchichts, Set mbZielobjTyps, Set mbStatuses, Set mbBausts,
			Set mbZielobjSubtyps, Set mbDringlichkeits) {
		this.mstId = mstId;
		this.guid = guid;
		this.timestamp = timestamp;
		this.mbSchichts = mbSchichts;
		this.mbZielobjTyps = mbZielobjTyps;
		this.mbStatuses = mbStatuses;
		this.mbBausts = mbBausts;
		this.mbZielobjSubtyps = mbZielobjSubtyps;
		this.mbDringlichkeits = mbDringlichkeits;
	}

	// Property accessors

	public Short getMstId() {
		return this.mstId;
	}

	public void setMstId(Short mstId) {
		this.mstId = mstId;
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

	public Set getMbSchichts() {
		return this.mbSchichts;
	}

	public void setMbSchichts(Set mbSchichts) {
		this.mbSchichts = mbSchichts;
	}

	public Set getMbZielobjTyps() {
		return this.mbZielobjTyps;
	}

	public void setMbZielobjTyps(Set mbZielobjTyps) {
		this.mbZielobjTyps = mbZielobjTyps;
	}

	public Set getMbStatuses() {
		return this.mbStatuses;
	}

	public void setMbStatuses(Set mbStatuses) {
		this.mbStatuses = mbStatuses;
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

}