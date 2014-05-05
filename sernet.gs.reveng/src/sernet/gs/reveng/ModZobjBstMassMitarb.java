package sernet.gs.reveng;

import java.util.Date;

/**
 * ModZobjBstMassMitarb entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class ModZobjBstMassMitarb implements java.io.Serializable {

	// Fields

	private ModZobjBstMassMitarbId id;
	private Integer mmtId;
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
	public ModZobjBstMassMitarb() {
	}

	/** minimal constructor */
	public ModZobjBstMassMitarb(ModZobjBstMassMitarbId id, Integer mmtId,
			Integer usn, String guid, Date timestamp) {
		this.id = id;
		this.mmtId = mmtId;
		this.usn = usn;
		this.guid = guid;
		this.timestamp = timestamp;
	}

	/** full constructor */
	public ModZobjBstMassMitarb(ModZobjBstMassMitarbId id, Integer mmtId,
			Short impNeu, Integer usn, String guid, String guidOrg,
			Date timestamp, Date loeschDatum, String erfasstDurch,
			String geloeschtDurch) {
		this.id = id;
		this.mmtId = mmtId;
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

	public ModZobjBstMassMitarbId getId() {
		return this.id;
	}

	public void setId(ModZobjBstMassMitarbId id) {
		this.id = id;
	}

	public Integer getMmtId() {
		return this.mmtId;
	}

	public void setMmtId(Integer mmtId) {
		this.mmtId = mmtId;
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