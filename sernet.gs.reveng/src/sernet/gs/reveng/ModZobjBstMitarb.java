package sernet.gs.reveng;

import java.util.Date;

/**
 * ModZobjBstMitarb entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class ModZobjBstMitarb implements java.io.Serializable {

	// Fields

	private ModZobjBstMitarbId id;
	private Short impNeu;
	private Integer usn;
	private String guid;
	private String guidOrg;
	private Date timestamp;
	private Date loeschDatum;
	private String erfasstDurch;
	private String geloeschtDurch;

	// Constructors

	/** default constructor */
	public ModZobjBstMitarb() {
	}

	/** minimal constructor */
	public ModZobjBstMitarb(ModZobjBstMitarbId id, Integer usn, String guid) {
		this.id = id;
		this.usn = usn;
		this.guid = guid;
	}

	/** full constructor */
	public ModZobjBstMitarb(ModZobjBstMitarbId id, Short impNeu, Integer usn,
			String guid, String guidOrg, Date timestamp, Date loeschDatum,
			String erfasstDurch, String geloeschtDurch) {
		this.id = id;
		this.impNeu = impNeu;
		this.usn = usn;
		this.guid = guid;
		this.guidOrg = guidOrg;
		this.timestamp = timestamp;
		this.loeschDatum = loeschDatum;
		this.erfasstDurch = erfasstDurch;
		this.geloeschtDurch = geloeschtDurch;
	}

	// Property accessors

	public ModZobjBstMitarbId getId() {
		return this.id;
	}

	public void setId(ModZobjBstMitarbId id) {
		this.id = id;
	}

	public Short getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Short impNeu) {
		this.impNeu = impNeu;
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

	public Date getLoeschDatum() {
		return this.loeschDatum;
	}

	public void setLoeschDatum(Date loeschDatum) {
		this.loeschDatum = loeschDatum;
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

}