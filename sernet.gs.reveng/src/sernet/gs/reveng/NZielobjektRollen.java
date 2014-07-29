package sernet.gs.reveng;

import java.util.Date;

/**
 * NZielobjektRollen entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class NZielobjektRollen implements java.io.Serializable {

	// Fields

	private NZielobjektRollenId id;
	private Date loeschDatum;
	private Integer usn;
	private String guid;
	private String guidOrg;
	private Date timestamp;
	private String erfasstDurch;
	private String geloeschtDurch;
	private Short impNeu;

	// Constructors

	/** default constructor */
	public NZielobjektRollen() {
	}

	/** minimal constructor */
	public NZielobjektRollen(NZielobjektRollenId id, Integer usn, String guid) {
		this.id = id;
		this.usn = usn;
		this.guid = guid;
	}

	/** full constructor */
	public NZielobjektRollen(NZielobjektRollenId id, Date loeschDatum,
			Integer usn, String guid, String guidOrg, Date timestamp,
			String erfasstDurch, String geloeschtDurch, Short impNeu) {
		this.id = id;
		this.loeschDatum = loeschDatum;
		this.usn = usn;
		this.guid = guid;
		this.guidOrg = guidOrg;
		this.timestamp = timestamp;
		this.erfasstDurch = erfasstDurch;
		this.geloeschtDurch = geloeschtDurch;
		this.impNeu = impNeu;
	}

	// Property accessors

	public NZielobjektRollenId getId() {
		return this.id;
	}

	public void setId(NZielobjektRollenId id) {
		this.id = id;
	}

	public Date getLoeschDatum() {
		return this.loeschDatum;
	}

	public void setLoeschDatum(Date loeschDatum) {
		this.loeschDatum = loeschDatum;
	}

	public Integer getUsn() {
		return this.usn;
	}

	public void setUsn(Integer usn) {
		this.usn = usn;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getGuidOrg() {
		return this.guidOrg;
	}

	public void setGuidOrg(String guidOrg) {
		this.guidOrg = guidOrg;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getErfasstDurch() {
		return this.erfasstDurch;
	}

	public void setErfasstDurch(String erfasstDurch) {
		this.erfasstDurch = erfasstDurch;
	}

	public String getGeloeschtDurch() {
		return this.geloeschtDurch;
	}

	public void setGeloeschtDurch(String geloeschtDurch) {
		this.geloeschtDurch = geloeschtDurch;
	}

	public Short getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Short impNeu) {
		this.impNeu = impNeu;
	}

}