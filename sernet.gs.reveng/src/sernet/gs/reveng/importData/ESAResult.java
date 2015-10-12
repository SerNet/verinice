package sernet.gs.reveng.importData;

import java.util.Date;

public class ESAResult {
	String begruendung;
	byte einsatz;
	byte modellierung;
	String szenario;
	byte unj;
	String entscheidungDurch;
	String zmiName;
	Date entscheidungAm;
	Date entscheidungBis;

	public ESAResult(String begruendung, byte einsatz,
			byte modellierung, String szenario, byte unj,
			String entscheidungDurch, String zmiName, Date entscheidungAm, Date entscheidungBis) {
		super();
		this.begruendung = begruendung;
		this.einsatz = einsatz;
		this.modellierung = modellierung;
		this.szenario = szenario;
		this.unj = unj;
		this.entscheidungDurch = entscheidungDurch;
		this.zmiName = zmiName;
		this.entscheidungAm = entscheidungAm;
		this.entscheidungBis = entscheidungBis;
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

    public Date getEntscheidungAm() {
        return entscheidungAm;
    }

    public void setEntscheidungAm(Date entscheidungAm) {
        this.entscheidungAm = entscheidungAm;
    }

    public Date getEntscheidungBis() {
        return entscheidungBis;
    }

    public void setEntscheidungBis(Date entscheidungBis) {
        this.entscheidungBis = entscheidungBis;
    }
}