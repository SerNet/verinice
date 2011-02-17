package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * NZielobjekt entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class NZielobjekt implements java.io.Serializable {

	// Fields

	private NZielobjektId id;
	private MsCmState msCmState;
	private MbStatus mbStatus;
	private MbDringlichkeit mbDringlichkeit;
	private MYesno MYesnoByGefOk;
	private MYesno MYesnoByGefOkItv;
	private MGsiegel MGsiegelBySiegel;
	private MGsiegel MGsiegelBySiegelItv;
	private NmbNotiz nmbNotiz;
	private MbZielobjSubtyp mbZielobjSubtyp;
	private MUmsetzStat MUmsetzStatByUstIdItv;
	private MUmsetzStat MUmsetzStatByUstId;
	private Integer orgImpId;
	private String name;
	private String nameOrg;
	private Integer nameSame;
	private String kuerzel;
	private String beschreibung;
	private String anwBeschrInf;
	private String anwInf1Beschr;
	private String anwInf2Beschr;
	private String itvAuditor;
	private String vertragsgr;
	private String untersuchungsg;
	private String projektierung;
	private String verteiler;
	private String sichtung;
	private String telefon;
	private String email;
	private String abteilung;
	private Integer anzahl;
	private Date erteiltAm;
	private Date gueltigBis;
	private Short impNeu;
	private Date erfasstAm;
	private String erfasstDurch;
	private Integer usn;
	private String guid;
	private String guidOrg;
	private Date loeschDatum;
	private Byte exportiert;
	private String geloeschtDurch;
	private Date changedOn;
	private String changedBy;
	private Date cmTimestamp;
	private String cmUsername;
	private Integer cmImpId;
	private Integer cmVerId1;
	private Short cmVerId2;
	private Short raFarbe;
	private Short raFarbeItv;
	private String kuerzelOrg;
	private Integer kuerzelSame;
	private Short setDefault;
	private Set mbBausts = new HashSet(0);
	private Set modZobjBstsForFkZbZ2 = new HashSet(0);
	private Set modZobjBstsForFkZbZ = new HashSet(0);

	// Constructors

	/** default constructor */
	public NZielobjekt() {
	}

	/** minimal constructor */
	public NZielobjekt(NZielobjektId id, MYesno MYesnoByGefOk,
			MYesno MYesnoByGefOkItv, MGsiegel MGsiegelBySiegel,
			MGsiegel MGsiegelBySiegelItv, NmbNotiz nmbNotiz,
			MbZielobjSubtyp mbZielobjSubtyp, MUmsetzStat MUmsetzStatByUstIdItv,
			MUmsetzStat MUmsetzStatByUstId, Integer orgImpId, String name,
			String nameOrg, Integer nameSame, String kuerzel, Integer usn,
			String guid, Byte exportiert, Short raFarbe, Short raFarbeItv,
			Short setDefault) {
		this.id = id;
		this.MYesnoByGefOk = MYesnoByGefOk;
		this.MYesnoByGefOkItv = MYesnoByGefOkItv;
		this.MGsiegelBySiegel = MGsiegelBySiegel;
		this.MGsiegelBySiegelItv = MGsiegelBySiegelItv;
		this.nmbNotiz = nmbNotiz;
		this.mbZielobjSubtyp = mbZielobjSubtyp;
		this.MUmsetzStatByUstIdItv = MUmsetzStatByUstIdItv;
		this.MUmsetzStatByUstId = MUmsetzStatByUstId;
		this.orgImpId = orgImpId;
		this.name = name;
		this.nameOrg = nameOrg;
		this.nameSame = nameSame;
		this.kuerzel = kuerzel;
		this.usn = usn;
		this.guid = guid;
		this.exportiert = exportiert;
		this.raFarbe = raFarbe;
		this.raFarbeItv = raFarbeItv;
		this.setDefault = setDefault;
	}

	/** full constructor */
	public NZielobjekt(NZielobjektId id, MsCmState msCmState,
			MbStatus mbStatus, MbDringlichkeit mbDringlichkeit,
			MYesno MYesnoByGefOk, MYesno MYesnoByGefOkItv,
			MGsiegel MGsiegelBySiegel, MGsiegel MGsiegelBySiegelItv,
			NmbNotiz nmbNotiz, MbZielobjSubtyp mbZielobjSubtyp,
			MUmsetzStat MUmsetzStatByUstIdItv, MUmsetzStat MUmsetzStatByUstId,
			Integer orgImpId, String name, String nameOrg, Integer nameSame,
			String kuerzel, String beschreibung, String anwBeschrInf,
			String anwInf1Beschr, String anwInf2Beschr, String itvAuditor,
			String vertragsgr, String untersuchungsg, String projektierung,
			String verteiler, String sichtung, String telefon, String email,
			String abteilung, Integer anzahl, Date erteiltAm, Date gueltigBis,
			Short impNeu, Date erfasstAm, String erfasstDurch, Integer usn,
			String guid, String guidOrg, Date loeschDatum, Byte exportiert,
			String geloeschtDurch, Date changedOn, String changedBy,
			Date cmTimestamp, String cmUsername, Integer cmImpId,
			Integer cmVerId1, Short cmVerId2, Short raFarbe, Short raFarbeItv,
			String kuerzelOrg, Integer kuerzelSame, Short setDefault,
			Set mbBausts, Set modZobjBstsForFkZbZ2, Set modZobjBstsForFkZbZ) {
		this.id = id;
		this.msCmState = msCmState;
		this.mbStatus = mbStatus;
		this.mbDringlichkeit = mbDringlichkeit;
		this.MYesnoByGefOk = MYesnoByGefOk;
		this.MYesnoByGefOkItv = MYesnoByGefOkItv;
		this.MGsiegelBySiegel = MGsiegelBySiegel;
		this.MGsiegelBySiegelItv = MGsiegelBySiegelItv;
		this.nmbNotiz = nmbNotiz;
		this.mbZielobjSubtyp = mbZielobjSubtyp;
		this.MUmsetzStatByUstIdItv = MUmsetzStatByUstIdItv;
		this.MUmsetzStatByUstId = MUmsetzStatByUstId;
		this.orgImpId = orgImpId;
		this.name = name;
		this.nameOrg = nameOrg;
		this.nameSame = nameSame;
		this.kuerzel = kuerzel;
		this.beschreibung = beschreibung;
		this.anwBeschrInf = anwBeschrInf;
		this.anwInf1Beschr = anwInf1Beschr;
		this.anwInf2Beschr = anwInf2Beschr;
		this.itvAuditor = itvAuditor;
		this.vertragsgr = vertragsgr;
		this.untersuchungsg = untersuchungsg;
		this.projektierung = projektierung;
		this.verteiler = verteiler;
		this.sichtung = sichtung;
		this.telefon = telefon;
		this.email = email;
		this.abteilung = abteilung;
		this.anzahl = anzahl;
		this.erteiltAm = erteiltAm;
		this.gueltigBis = gueltigBis;
		this.impNeu = impNeu;
		this.erfasstAm = erfasstAm;
		this.erfasstDurch = erfasstDurch;
		this.usn = usn;
		this.guid = guid;
		this.guidOrg = guidOrg;
		this.loeschDatum = loeschDatum;
		this.exportiert = exportiert;
		this.geloeschtDurch = geloeschtDurch;
		this.changedOn = changedOn;
		this.changedBy = changedBy;
		this.cmTimestamp = cmTimestamp;
		this.cmUsername = cmUsername;
		this.cmImpId = cmImpId;
		this.cmVerId1 = cmVerId1;
		this.cmVerId2 = cmVerId2;
		this.raFarbe = raFarbe;
		this.raFarbeItv = raFarbeItv;
		this.kuerzelOrg = kuerzelOrg;
		this.kuerzelSame = kuerzelSame;
		this.setDefault = setDefault;
		this.mbBausts = mbBausts;
		this.modZobjBstsForFkZbZ2 = modZobjBstsForFkZbZ2;
		this.modZobjBstsForFkZbZ = modZobjBstsForFkZbZ;
	}

	// Property accessors

	public NZielobjektId getId() {
		return this.id;
	}

	public void setId(NZielobjektId id) {
		this.id = id;
	}

	public MsCmState getMsCmState() {
		return this.msCmState;
	}

	public void setMsCmState(MsCmState msCmState) {
		this.msCmState = msCmState;
	}

	public MbStatus getMbStatus() {
		return this.mbStatus;
	}

	public void setMbStatus(MbStatus mbStatus) {
		this.mbStatus = mbStatus;
	}

	public MbDringlichkeit getMbDringlichkeit() {
		return this.mbDringlichkeit;
	}

	public void setMbDringlichkeit(MbDringlichkeit mbDringlichkeit) {
		this.mbDringlichkeit = mbDringlichkeit;
	}

	public MYesno getMYesnoByGefOk() {
		return this.MYesnoByGefOk;
	}

	public void setMYesnoByGefOk(MYesno MYesnoByGefOk) {
		this.MYesnoByGefOk = MYesnoByGefOk;
	}

	public MYesno getMYesnoByGefOkItv() {
		return this.MYesnoByGefOkItv;
	}

	public void setMYesnoByGefOkItv(MYesno MYesnoByGefOkItv) {
		this.MYesnoByGefOkItv = MYesnoByGefOkItv;
	}

	public MGsiegel getMGsiegelBySiegel() {
		return this.MGsiegelBySiegel;
	}

	public void setMGsiegelBySiegel(MGsiegel MGsiegelBySiegel) {
		this.MGsiegelBySiegel = MGsiegelBySiegel;
	}

	public MGsiegel getMGsiegelBySiegelItv() {
		return this.MGsiegelBySiegelItv;
	}

	public void setMGsiegelBySiegelItv(MGsiegel MGsiegelBySiegelItv) {
		this.MGsiegelBySiegelItv = MGsiegelBySiegelItv;
	}

	public NmbNotiz getNmbNotiz() {
		return this.nmbNotiz;
	}

	public void setNmbNotiz(NmbNotiz nmbNotiz) {
		this.nmbNotiz = nmbNotiz;
	}

	public MbZielobjSubtyp getMbZielobjSubtyp() {
		return this.mbZielobjSubtyp;
	}

	public void setMbZielobjSubtyp(MbZielobjSubtyp mbZielobjSubtyp) {
		this.mbZielobjSubtyp = mbZielobjSubtyp;
	}

	public MUmsetzStat getMUmsetzStatByUstIdItv() {
		return this.MUmsetzStatByUstIdItv;
	}

	public void setMUmsetzStatByUstIdItv(MUmsetzStat MUmsetzStatByUstIdItv) {
		this.MUmsetzStatByUstIdItv = MUmsetzStatByUstIdItv;
	}

	public MUmsetzStat getMUmsetzStatByUstId() {
		return this.MUmsetzStatByUstId;
	}

	public void setMUmsetzStatByUstId(MUmsetzStat MUmsetzStatByUstId) {
		this.MUmsetzStatByUstId = MUmsetzStatByUstId;
	}

	public Integer getOrgImpId() {
		return this.orgImpId;
	}

	public void setOrgImpId(Integer orgImpId) {
		this.orgImpId = orgImpId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameOrg() {
		return this.nameOrg;
	}

	public void setNameOrg(String nameOrg) {
		this.nameOrg = nameOrg;
	}

	public Integer getNameSame() {
		return this.nameSame;
	}

	public void setNameSame(Integer nameSame) {
		this.nameSame = nameSame;
	}

	public String getKuerzel() {
		return this.kuerzel;
	}

	public void setKuerzel(String kuerzel) {
		this.kuerzel = kuerzel;
	}

	public String getBeschreibung() {
		return this.beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public String getAnwBeschrInf() {
		return this.anwBeschrInf;
	}

	public void setAnwBeschrInf(String anwBeschrInf) {
		this.anwBeschrInf = anwBeschrInf;
	}

	public String getAnwInf1Beschr() {
		return this.anwInf1Beschr;
	}

	public void setAnwInf1Beschr(String anwInf1Beschr) {
		this.anwInf1Beschr = anwInf1Beschr;
	}

	public String getAnwInf2Beschr() {
		return this.anwInf2Beschr;
	}

	public void setAnwInf2Beschr(String anwInf2Beschr) {
		this.anwInf2Beschr = anwInf2Beschr;
	}

	public String getItvAuditor() {
		return this.itvAuditor;
	}

	public void setItvAuditor(String itvAuditor) {
		this.itvAuditor = itvAuditor;
	}

	public String getVertragsgr() {
		return this.vertragsgr;
	}

	public void setVertragsgr(String vertragsgr) {
		this.vertragsgr = vertragsgr;
	}

	public String getUntersuchungsg() {
		return this.untersuchungsg;
	}

	public void setUntersuchungsg(String untersuchungsg) {
		this.untersuchungsg = untersuchungsg;
	}

	public String getProjektierung() {
		return this.projektierung;
	}

	public void setProjektierung(String projektierung) {
		this.projektierung = projektierung;
	}

	public String getVerteiler() {
		return this.verteiler;
	}

	public void setVerteiler(String verteiler) {
		this.verteiler = verteiler;
	}

	public String getSichtung() {
		return this.sichtung;
	}

	public void setSichtung(String sichtung) {
		this.sichtung = sichtung;
	}

	public String getTelefon() {
		return this.telefon;
	}

	public void setTelefon(String telefon) {
		this.telefon = telefon;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAbteilung() {
		return this.abteilung;
	}

	public void setAbteilung(String abteilung) {
		this.abteilung = abteilung;
	}

	public Integer getAnzahl() {
		return this.anzahl;
	}

	public void setAnzahl(Integer anzahl) {
		this.anzahl = anzahl;
	}

	public Date getErteiltAm() {
		return this.erteiltAm;
	}

	public void setErteiltAm(Date erteiltAm) {
		this.erteiltAm = erteiltAm;
	}

	public Date getGueltigBis() {
		return this.gueltigBis;
	}

	public void setGueltigBis(Date gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	public Short getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Short impNeu) {
		this.impNeu = impNeu;
	}

	public Date getErfasstAm() {
		return this.erfasstAm;
	}

	public void setErfasstAm(Date erfasstAm) {
		this.erfasstAm = erfasstAm;
	}

	public String getErfasstDurch() {
		return this.erfasstDurch;
	}

	public void setErfasstDurch(String erfasstDurch) {
		this.erfasstDurch = erfasstDurch;
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

	public Date getLoeschDatum() {
		return this.loeschDatum;
	}

	public void setLoeschDatum(Date loeschDatum) {
		this.loeschDatum = loeschDatum;
	}

	public Byte getExportiert() {
		return this.exportiert;
	}

	public void setExportiert(Byte exportiert) {
		this.exportiert = exportiert;
	}

	public String getGeloeschtDurch() {
		return this.geloeschtDurch;
	}

	public void setGeloeschtDurch(String geloeschtDurch) {
		this.geloeschtDurch = geloeschtDurch;
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

	public Date getCmTimestamp() {
		return this.cmTimestamp;
	}

	public void setCmTimestamp(Date cmTimestamp) {
		this.cmTimestamp = cmTimestamp;
	}

	public String getCmUsername() {
		return this.cmUsername;
	}

	public void setCmUsername(String cmUsername) {
		this.cmUsername = cmUsername;
	}

	public Integer getCmImpId() {
		return this.cmImpId;
	}

	public void setCmImpId(Integer cmImpId) {
		this.cmImpId = cmImpId;
	}

	public Integer getCmVerId1() {
		return this.cmVerId1;
	}

	public void setCmVerId1(Integer cmVerId1) {
		this.cmVerId1 = cmVerId1;
	}

	public Short getCmVerId2() {
		return this.cmVerId2;
	}

	public void setCmVerId2(Short cmVerId2) {
		this.cmVerId2 = cmVerId2;
	}

	public Short getRaFarbe() {
		return this.raFarbe;
	}

	public void setRaFarbe(Short raFarbe) {
		this.raFarbe = raFarbe;
	}

	public Short getRaFarbeItv() {
		return this.raFarbeItv;
	}

	public void setRaFarbeItv(Short raFarbeItv) {
		this.raFarbeItv = raFarbeItv;
	}

	public String getKuerzelOrg() {
		return this.kuerzelOrg;
	}

	public void setKuerzelOrg(String kuerzelOrg) {
		this.kuerzelOrg = kuerzelOrg;
	}

	public Integer getKuerzelSame() {
		return this.kuerzelSame;
	}

	public void setKuerzelSame(Integer kuerzelSame) {
		this.kuerzelSame = kuerzelSame;
	}

	public Short getSetDefault() {
		return this.setDefault;
	}

	public void setSetDefault(Short setDefault) {
		this.setDefault = setDefault;
	}

	public Set getMbBausts() {
		return this.mbBausts;
	}

	public void setMbBausts(Set mbBausts) {
		this.mbBausts = mbBausts;
	}

	public Set getModZobjBstsForFkZbZ2() {
		return this.modZobjBstsForFkZbZ2;
	}

	public void setModZobjBstsForFkZbZ2(Set modZobjBstsForFkZbZ2) {
		this.modZobjBstsForFkZbZ2 = modZobjBstsForFkZbZ2;
	}

	public Set getModZobjBstsForFkZbZ() {
		return this.modZobjBstsForFkZbZ;
	}

	public void setModZobjBstsForFkZbZ(Set modZobjBstsForFkZbZ) {
		this.modZobjBstsForFkZbZ = modZobjBstsForFkZbZ;
	}

}