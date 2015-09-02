package sernet.gs.reveng.importData;
public class ESAResult {
	String begruendung;
	byte einsatz;
	byte modellierung;
	String szenario;
	byte unj;
	String entscheidungDurch;
	String zmiName;
	
	public ESAResult(String begruendung, byte einsatz,
			byte modellierung, String szenario, byte unj,
			String entscheidungDurch, String zmiName) {
		super();
		this.begruendung = begruendung;
		this.einsatz = einsatz;
		this.modellierung = modellierung;
		this.szenario = szenario;
		this.unj = unj;
		this.entscheidungDurch = entscheidungDurch;
		this.zmiName = zmiName;
	}

	public String getBegruendung() {
		return begruendung;
	}

	public void setBegruendung(String begruendung) {
		this.begruendung = begruendung;
	}

	public byte getEinsatz() {
		return einsatz;
	}

	public void setEinsatz(byte einsatz) {
		this.einsatz = einsatz;
	}

	public byte getModellierung() {
		return modellierung;
	}

	public void setModellierung(byte modellierung) {
		this.modellierung = modellierung;
	}

	public String getSzenario() {
		return szenario;
	}

	public void setSzenario(String szenario) {
		this.szenario = szenario;
	}

	public byte getUnj() {
		return unj;
	}

	public void setUnj(byte unj) {
		this.unj = unj;
	}

	public String getEntscheidungDurch() {
		return entscheidungDurch;
	}

	public void setEntscheidungDurch(String entscheidungDurch) {
		this.entscheidungDurch = entscheidungDurch;
	}

	public String getZmiName() {
		return zmiName;
	}

	public void setZmiName(String zmiName) {
		this.zmiName = zmiName;
	}
}