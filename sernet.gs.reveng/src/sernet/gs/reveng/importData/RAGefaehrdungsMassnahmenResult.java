package sernet.gs.reveng.importData;

import sernet.gs.reveng.MbGefaehr;
import sernet.gs.reveng.MbGefaehrTxt;
import sernet.gs.reveng.MbMassn;
import sernet.gs.reveng.MbMassnTxt;
import sernet.gs.reveng.NZielobjekt;

public class RAGefaehrdungsMassnahmenResult {
/*
 * "select z.name, z.id.zobId," + 
			"	gtxt.name," + 
			"	rabtxt.kurz," + 
			"	g.id.gefId,   " + 
			"	mtxt.name," + 
			"	m.userdef, m.mskId, m.nr" + 
 */
	
	private NZielobjekt zielobjekt;
	private MbGefaehr gefaehrdung;
	private MbGefaehrTxt gefaehrdungTxt;
	private char risikobehandlungABCD;
	private MbMassn massnahme;
	private MbMassnTxt massnahmeTxt;
	
	
	
	public RAGefaehrdungsMassnahmenResult(NZielobjekt zielobjekt,
			MbGefaehr gefaehrdung, MbGefaehrTxt gefaehrdungTxt,
			char risikobehandlungABCD, MbMassn massnahme,
			MbMassnTxt massnahmeTxt) {
		super();
		this.zielobjekt = zielobjekt;
		this.gefaehrdung = gefaehrdung;
		this.gefaehrdungTxt = gefaehrdungTxt;
		this.risikobehandlungABCD = risikobehandlungABCD;
		this.massnahme = massnahme;
		this.massnahmeTxt = massnahmeTxt;
	}
	
	public NZielobjekt getZielobjekt() {
		return zielobjekt;
	}
	public void setZielobjekt(NZielobjekt zielobjekt) {
		this.zielobjekt = zielobjekt;
	}
	public MbGefaehr getGefaehrdung() {
		return gefaehrdung;
	}
	public void setGefaehrdung(MbGefaehr gefaehrdung) {
		this.gefaehrdung = gefaehrdung;
	}
	public MbGefaehrTxt getGefaehrdungTxt() {
		return gefaehrdungTxt;
	}
	public void setGefaehrdungTxt(MbGefaehrTxt gefaehrdungTxt) {
		this.gefaehrdungTxt = gefaehrdungTxt;
	}
	public char getRisikobehandlungABCD() {
		return risikobehandlungABCD;
	}
	public void setRisikobehandlungABCD(char risikobehandlungABCD) {
		this.risikobehandlungABCD = risikobehandlungABCD;
	}
	public MbMassn getMassnahme() {
		return massnahme;
	}
	public void setMassnahme(MbMassn massnahme) {
		this.massnahme = massnahme;
	}
	public MbMassnTxt getMassnahmeTxt() {
		return massnahmeTxt;
	}
	public void setMassnahmeTxt(MbMassnTxt massnahmeTxt) {
		this.massnahmeTxt = massnahmeTxt;
	}
	
	
	
}
