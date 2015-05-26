package sernet.gs.reveng;

import java.util.Date;

/**
 * MUmsetzStatTxt entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MUmsetzStatTxt implements java.io.Serializable {

	// Fields

	private MUmsetzStatTxtId id;
	private String name;
	private String beschreibung;
	private String guid;
	private Date timestamp;
	private Short impNeu;

	// Constructors

	/** default constructor */
	public MUmsetzStatTxt() {
	}

	/** minimal constructor */
	public MUmsetzStatTxt(MUmsetzStatTxtId id, String guid, Date timestamp) {
		this.id = id;
		this.guid = guid;
		this.timestamp = timestamp;
	}

	/** full constructor */
	public MUmsetzStatTxt(MUmsetzStatTxtId id, String name,
			String beschreibung, String guid, Date timestamp, Short impNeu) {
		this.id = id;
		this.name = name;
		this.beschreibung = beschreibung;
		this.guid = guid;
		this.timestamp = timestamp;
		this.impNeu = impNeu;
	}

	// Property accessors

	public MUmsetzStatTxtId getId() {
		return this.id;
	}

	public void setId(MUmsetzStatTxtId id) {
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

}