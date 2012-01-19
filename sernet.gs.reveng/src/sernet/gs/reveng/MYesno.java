package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MYesno entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MYesno implements java.io.Serializable {

	// Fields

	private Short yesId;
	private String guid;
	private Date timestamp;
	private Set NZielobjektsForGefOkItv = new HashSet(0);
	private Set NZielobjektsForGefOk = new HashSet(0);

	// Constructors

	/** default constructor */
	public MYesno() {
	}

	/** minimal constructor */
	public MYesno(Short yesId) {
		this.yesId = yesId;
	}

	/** full constructor */
	public MYesno(Short yesId, String guid, Date timestamp,
			Set NZielobjektsForGefOkItv, Set NZielobjektsForGefOk) {
		this.yesId = yesId;
		this.guid = guid;
		this.timestamp = timestamp;
		this.NZielobjektsForGefOkItv = NZielobjektsForGefOkItv;
		this.NZielobjektsForGefOk = NZielobjektsForGefOk;
	}

	// Property accessors

	public Short getYesId() {
		return this.yesId;
	}

	public void setYesId(Short yesId) {
		this.yesId = yesId;
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

	public Set getNZielobjektsForGefOkItv() {
		return this.NZielobjektsForGefOkItv;
	}

	public void setNZielobjektsForGefOkItv(Set NZielobjektsForGefOkItv) {
		this.NZielobjektsForGefOkItv = NZielobjektsForGefOkItv;
	}

	public Set getNZielobjektsForGefOk() {
		return this.NZielobjektsForGefOk;
	}

	public void setNZielobjektsForGefOk(Set NZielobjektsForGefOk) {
		this.NZielobjektsForGefOk = NZielobjektsForGefOk;
	}

}