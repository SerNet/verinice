package sernet.gs.reveng;

/**
 * NZobSb entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class NZobSb implements java.io.Serializable {

	// Fields

	private NZobSbId id;
	private Byte zsbUebertragung;
	private Byte zsbAngebunden;
	private Byte zsbVertraulich;
	private Byte zsbIntegritaet;
	private Byte zsbVerfuegbar;
	private Short zsbVertrSbkIdErm;
	private Short zsbVertrSbkId;
	private String zsbVertrBegr;
	private Short zsbVerfuSbkIdErm;
	private Short zsbVerfuSbkId;
	private String zsbVerfuBegr;
	private Short zsbIntegSbkIdErm;
	private Short zsbIntegSbkId;
	private String zsbIntegBegr;
	private Short zsbAutenSbkIdErm;
	private Short zsbAutenSbkId;
	private String zsbAutenBegr;
	private Short zsbRevisSbkIdErm;
	private Short zsbRevisSbkId;
	private String zsbRevisBegr;
	private Short zsbTransSbkIdErm;
	private Short zsbTransSbkId;
	private String zsbTransBegr;
	private Short zsbPersSbkId;
	private String zsbPersBegr;
	private Short zsbGesamtSbkId;
	private Short zsbPersDatenErm;
	private Short zsbPersDaten;
	private Short impNeu;
	private String guid;
	private String guidOrg;
	private Integer usn;

	// Constructors

	/** default constructor */
	public NZobSb() {
	}

	/** minimal constructor */
	public NZobSb(NZobSbId id, Byte zsbUebertragung, Byte zsbAngebunden,
			Byte zsbVertraulich, Byte zsbIntegritaet, Byte zsbVerfuegbar,
			String zsbVertrBegr, String zsbVerfuBegr, String zsbIntegBegr,
			String zsbAutenBegr, String zsbRevisBegr, String zsbTransBegr,
			String zsbPersBegr, String guid, Integer usn) {
		this.id = id;
		this.zsbUebertragung = zsbUebertragung;
		this.zsbAngebunden = zsbAngebunden;
		this.zsbVertraulich = zsbVertraulich;
		this.zsbIntegritaet = zsbIntegritaet;
		this.zsbVerfuegbar = zsbVerfuegbar;
		this.zsbVertrBegr = zsbVertrBegr;
		this.zsbVerfuBegr = zsbVerfuBegr;
		this.zsbIntegBegr = zsbIntegBegr;
		this.zsbAutenBegr = zsbAutenBegr;
		this.zsbRevisBegr = zsbRevisBegr;
		this.zsbTransBegr = zsbTransBegr;
		this.zsbPersBegr = zsbPersBegr;
		this.guid = guid;
		this.usn = usn;
	}

	/** full constructor */
	public NZobSb(NZobSbId id, Byte zsbUebertragung, Byte zsbAngebunden,
			Byte zsbVertraulich, Byte zsbIntegritaet, Byte zsbVerfuegbar,
			Short zsbVertrSbkIdErm, Short zsbVertrSbkId, String zsbVertrBegr,
			Short zsbVerfuSbkIdErm, Short zsbVerfuSbkId, String zsbVerfuBegr,
			Short zsbIntegSbkIdErm, Short zsbIntegSbkId, String zsbIntegBegr,
			Short zsbAutenSbkIdErm, Short zsbAutenSbkId, String zsbAutenBegr,
			Short zsbRevisSbkIdErm, Short zsbRevisSbkId, String zsbRevisBegr,
			Short zsbTransSbkIdErm, Short zsbTransSbkId, String zsbTransBegr,
			Short zsbPersSbkId, String zsbPersBegr, Short zsbGesamtSbkId,
			Short zsbPersDatenErm, Short zsbPersDaten, Short impNeu,
			String guid, String guidOrg, Integer usn) {
		this.id = id;
		this.zsbUebertragung = zsbUebertragung;
		this.zsbAngebunden = zsbAngebunden;
		this.zsbVertraulich = zsbVertraulich;
		this.zsbIntegritaet = zsbIntegritaet;
		this.zsbVerfuegbar = zsbVerfuegbar;
		this.zsbVertrSbkIdErm = zsbVertrSbkIdErm;
		this.zsbVertrSbkId = zsbVertrSbkId;
		this.zsbVertrBegr = zsbVertrBegr;
		this.zsbVerfuSbkIdErm = zsbVerfuSbkIdErm;
		this.zsbVerfuSbkId = zsbVerfuSbkId;
		this.zsbVerfuBegr = zsbVerfuBegr;
		this.zsbIntegSbkIdErm = zsbIntegSbkIdErm;
		this.zsbIntegSbkId = zsbIntegSbkId;
		this.zsbIntegBegr = zsbIntegBegr;
		this.zsbAutenSbkIdErm = zsbAutenSbkIdErm;
		this.zsbAutenSbkId = zsbAutenSbkId;
		this.zsbAutenBegr = zsbAutenBegr;
		this.zsbRevisSbkIdErm = zsbRevisSbkIdErm;
		this.zsbRevisSbkId = zsbRevisSbkId;
		this.zsbRevisBegr = zsbRevisBegr;
		this.zsbTransSbkIdErm = zsbTransSbkIdErm;
		this.zsbTransSbkId = zsbTransSbkId;
		this.zsbTransBegr = zsbTransBegr;
		this.zsbPersSbkId = zsbPersSbkId;
		this.zsbPersBegr = zsbPersBegr;
		this.zsbGesamtSbkId = zsbGesamtSbkId;
		this.zsbPersDatenErm = zsbPersDatenErm;
		this.zsbPersDaten = zsbPersDaten;
		this.impNeu = impNeu;
		this.guid = guid;
		this.guidOrg = guidOrg;
		this.usn = usn;
	}

	// Property accessors

	public NZobSbId getId() {
		return this.id;
	}

	public void setId(NZobSbId id) {
		this.id = id;
	}

	public Byte getZsbUebertragung() {
		return this.zsbUebertragung;
	}

	public void setZsbUebertragung(Byte zsbUebertragung) {
		this.zsbUebertragung = zsbUebertragung;
	}

	public Byte getZsbAngebunden() {
		return this.zsbAngebunden;
	}

	public void setZsbAngebunden(Byte zsbAngebunden) {
		this.zsbAngebunden = zsbAngebunden;
	}

	public Byte getZsbVertraulich() {
		return this.zsbVertraulich;
	}

	public void setZsbVertraulich(Byte zsbVertraulich) {
		this.zsbVertraulich = zsbVertraulich;
	}

	public Byte getZsbIntegritaet() {
		return this.zsbIntegritaet;
	}

	public void setZsbIntegritaet(Byte zsbIntegritaet) {
		this.zsbIntegritaet = zsbIntegritaet;
	}

	public Byte getZsbVerfuegbar() {
		return this.zsbVerfuegbar;
	}

	public void setZsbVerfuegbar(Byte zsbVerfuegbar) {
		this.zsbVerfuegbar = zsbVerfuegbar;
	}

	public Short getZsbVertrSbkIdErm() {
		return this.zsbVertrSbkIdErm;
	}

	public void setZsbVertrSbkIdErm(Short zsbVertrSbkIdErm) {
		this.zsbVertrSbkIdErm = zsbVertrSbkIdErm;
	}

	public Short getZsbVertrSbkId() {
		return this.zsbVertrSbkId;
	}

	public void setZsbVertrSbkId(Short zsbVertrSbkId) {
		this.zsbVertrSbkId = zsbVertrSbkId;
	}

	public String getZsbVertrBegr() {
		return this.zsbVertrBegr;
	}

	public void setZsbVertrBegr(String zsbVertrBegr) {
		this.zsbVertrBegr = zsbVertrBegr;
	}

	public Short getZsbVerfuSbkIdErm() {
		return this.zsbVerfuSbkIdErm;
	}

	public void setZsbVerfuSbkIdErm(Short zsbVerfuSbkIdErm) {
		this.zsbVerfuSbkIdErm = zsbVerfuSbkIdErm;
	}

	public Short getZsbVerfuSbkId() {
		return this.zsbVerfuSbkId;
	}

	public void setZsbVerfuSbkId(Short zsbVerfuSbkId) {
		this.zsbVerfuSbkId = zsbVerfuSbkId;
	}

	public String getZsbVerfuBegr() {
		return this.zsbVerfuBegr;
	}

	public void setZsbVerfuBegr(String zsbVerfuBegr) {
		this.zsbVerfuBegr = zsbVerfuBegr;
	}

	public Short getZsbIntegSbkIdErm() {
		return this.zsbIntegSbkIdErm;
	}

	public void setZsbIntegSbkIdErm(Short zsbIntegSbkIdErm) {
		this.zsbIntegSbkIdErm = zsbIntegSbkIdErm;
	}

	public Short getZsbIntegSbkId() {
		return this.zsbIntegSbkId;
	}

	public void setZsbIntegSbkId(Short zsbIntegSbkId) {
		this.zsbIntegSbkId = zsbIntegSbkId;
	}

	public String getZsbIntegBegr() {
		return this.zsbIntegBegr;
	}

	public void setZsbIntegBegr(String zsbIntegBegr) {
		this.zsbIntegBegr = zsbIntegBegr;
	}

	public Short getZsbAutenSbkIdErm() {
		return this.zsbAutenSbkIdErm;
	}

	public void setZsbAutenSbkIdErm(Short zsbAutenSbkIdErm) {
		this.zsbAutenSbkIdErm = zsbAutenSbkIdErm;
	}

	public Short getZsbAutenSbkId() {
		return this.zsbAutenSbkId;
	}

	public void setZsbAutenSbkId(Short zsbAutenSbkId) {
		this.zsbAutenSbkId = zsbAutenSbkId;
	}

	public String getZsbAutenBegr() {
		return this.zsbAutenBegr;
	}

	public void setZsbAutenBegr(String zsbAutenBegr) {
		this.zsbAutenBegr = zsbAutenBegr;
	}

	public Short getZsbRevisSbkIdErm() {
		return this.zsbRevisSbkIdErm;
	}

	public void setZsbRevisSbkIdErm(Short zsbRevisSbkIdErm) {
		this.zsbRevisSbkIdErm = zsbRevisSbkIdErm;
	}

	public Short getZsbRevisSbkId() {
		return this.zsbRevisSbkId;
	}

	public void setZsbRevisSbkId(Short zsbRevisSbkId) {
		this.zsbRevisSbkId = zsbRevisSbkId;
	}

	public String getZsbRevisBegr() {
		return this.zsbRevisBegr;
	}

	public void setZsbRevisBegr(String zsbRevisBegr) {
		this.zsbRevisBegr = zsbRevisBegr;
	}

	public Short getZsbTransSbkIdErm() {
		return this.zsbTransSbkIdErm;
	}

	public void setZsbTransSbkIdErm(Short zsbTransSbkIdErm) {
		this.zsbTransSbkIdErm = zsbTransSbkIdErm;
	}

	public Short getZsbTransSbkId() {
		return this.zsbTransSbkId;
	}

	public void setZsbTransSbkId(Short zsbTransSbkId) {
		this.zsbTransSbkId = zsbTransSbkId;
	}

	public String getZsbTransBegr() {
		return this.zsbTransBegr;
	}

	public void setZsbTransBegr(String zsbTransBegr) {
		this.zsbTransBegr = zsbTransBegr;
	}

	public Short getZsbPersSbkId() {
		return this.zsbPersSbkId;
	}

	public void setZsbPersSbkId(Short zsbPersSbkId) {
		this.zsbPersSbkId = zsbPersSbkId;
	}

	public String getZsbPersBegr() {
		return this.zsbPersBegr;
	}

	public void setZsbPersBegr(String zsbPersBegr) {
		this.zsbPersBegr = zsbPersBegr;
	}

	public Short getZsbGesamtSbkId() {
		return this.zsbGesamtSbkId;
	}

	public void setZsbGesamtSbkId(Short zsbGesamtSbkId) {
		this.zsbGesamtSbkId = zsbGesamtSbkId;
	}

	public Short getZsbPersDatenErm() {
		return this.zsbPersDatenErm;
	}

	public void setZsbPersDatenErm(Short zsbPersDatenErm) {
		this.zsbPersDatenErm = zsbPersDatenErm;
	}

	public Short getZsbPersDaten() {
		return this.zsbPersDaten;
	}

	public void setZsbPersDaten(Short zsbPersDaten) {
		this.zsbPersDaten = zsbPersDaten;
	}

	public Short getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Short impNeu) {
		this.impNeu = impNeu;
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

	public Integer getUsn() {
		return this.usn;
	}

	public void setUsn(Integer usn) {
		this.usn = usn;
	}

}