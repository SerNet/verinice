package sernet.gs.reveng;

import java.util.Date;

/**
 * MbZielobjTypTxt entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbZielobjTypTxt implements java.io.Serializable {

	// Fields

	private MbZielobjTypTxtId id;
	private String name;
	private String name2;
	private String beschreibung;
	private String htmltext;
	private String guid;
	private Date timestamp;
	private Short impNeu;
	private String guidOrg;
	private String abstract_;
	private Date changedOn;
	private String changedBy;

	// Constructors

	/** default constructor */
	public MbZielobjTypTxt() {
	}

	/** minimal constructor */
	public MbZielobjTypTxt(MbZielobjTypTxtId id, String name, String guid,
			Date timestamp) {
		this.id = id;
		this.name = name;
		this.guid = guid;
		this.timestamp = timestamp;
	}

	/** full constructor */
	public MbZielobjTypTxt(MbZielobjTypTxtId id, String name, String name2,
			String beschreibung, String htmltext, String guid, Date timestamp,
			Short impNeu, String guidOrg, String abstract_, Date changedOn,
			String changedBy) {
		this.id = id;
		this.name = name;
		this.name2 = name2;
		this.beschreibung = beschreibung;
		this.htmltext = htmltext;
		this.guid = guid;
		this.timestamp = timestamp;
		this.impNeu = impNeu;
		this.guidOrg = guidOrg;
		this.abstract_ = abstract_;
		this.changedOn = changedOn;
		this.changedBy = changedBy;
	}

	// Property accessors

	public MbZielobjTypTxtId getId() {
		return this.id;
	}

	public void setId(MbZielobjTypTxtId id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName2() {
		return this.name2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}

	public String getBeschreibung() {
		return this.beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public String getHtmltext() {
		return this.htmltext;
	}

	public void setHtmltext(String htmltext) {
		this.htmltext = htmltext;
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

	public String getAbstract_() {
		return this.abstract_;
	}

	public void setAbstract_(String abstract_) {
		this.abstract_ = abstract_;
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

}