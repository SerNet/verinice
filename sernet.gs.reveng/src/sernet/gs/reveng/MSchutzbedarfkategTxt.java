package sernet.gs.reveng;

import java.util.Date;

/**
 * MSchutzbedarfkategTxt entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MSchutzbedarfkategTxt implements java.io.Serializable {

	// Fields

	private MSchutzbedarfkategTxtId id;
	private String name;
	private String beschreibung;
	private String guid;
	private Date timestamp;
	private Short impNeu;
	private Date changedOn;
	private String changedBy;
	private Integer notizId;

	// Constructors

	/** default constructor */
	public MSchutzbedarfkategTxt() {
	}

	/** minimal constructor */
	public MSchutzbedarfkategTxt(MSchutzbedarfkategTxtId id, String guid,
			Date timestamp) {
		this.id = id;
		this.guid = guid;
		this.timestamp = timestamp;
	}

	/** full constructor */
	public MSchutzbedarfkategTxt(MSchutzbedarfkategTxtId id, String name,
			String beschreibung, String guid, Date timestamp, Short impNeu,
			Date changedOn, String changedBy, Integer notizId) {
		this.id = id;
		this.name = name;
		this.beschreibung = beschreibung;
		this.guid = guid;
		this.timestamp = timestamp;
		this.impNeu = impNeu;
		this.changedOn = changedOn;
		this.changedBy = changedBy;
		this.notizId = notizId;
	}

	// Property accessors

	public MSchutzbedarfkategTxtId getId() {
		return this.id;
	}

	public void setId(MSchutzbedarfkategTxtId id) {
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

	public Integer getNotizId() {
		return this.notizId;
	}

	public void setNotizId(Integer notizId) {
		this.notizId = notizId;
	}

}