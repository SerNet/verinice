package sernet.gs.reveng;

import java.util.Date;

/**
 * MbDringlichkeitTxt entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbDringlichkeitTxt implements java.io.Serializable {

	// Fields

	private MbDringlichkeitTxtId id;
	private String name;
	private String beschreibung;
	private String guid;
	private Date timestamp;
	private Short impNeu;
	private String guidOrg;

	// Constructors

	/** default constructor */
	public MbDringlichkeitTxt() {
	}

	/** minimal constructor */
	public MbDringlichkeitTxt(MbDringlichkeitTxtId id) {
		this.id = id;
	}

	/** full constructor */
	public MbDringlichkeitTxt(MbDringlichkeitTxtId id, String name,
			String beschreibung, String guid, Date timestamp, Short impNeu,
			String guidOrg) {
		this.id = id;
		this.name = name;
		this.beschreibung = beschreibung;
		this.guid = guid;
		this.timestamp = timestamp;
		this.impNeu = impNeu;
		this.guidOrg = guidOrg;
	}

	// Property accessors

	public MbDringlichkeitTxtId getId() {
		return this.id;
	}

	public void setId(MbDringlichkeitTxtId id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBeschreibung() {
		return this.beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
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

}