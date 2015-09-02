package sernet.gs.reveng.importData;

import java.io.Serializable;

import sernet.gs.reveng.MUmsetzStatTxt;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbMassn;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.ModZobjBstMassMitarb;
import sernet.gs.reveng.NmbNotiz;

public class NotizenMassnahmeResult implements Serializable {
	public MbBaust baustein;
	public MbMassn massnahme;
	public MUmsetzStatTxt umstxt;
	public ModZobjBst zoBst;
	public ModZobjBstMass obm;
	public NmbNotiz notiz;
	
	public NotizenMassnahmeResult(MbBaust baustein, MbMassn massnahme, 
			MUmsetzStatTxt umstxt, ModZobjBst zoBst, ModZobjBstMass obm, NmbNotiz notiz) {
		super();
		this.baustein = baustein;
		this.massnahme = massnahme;
		this.umstxt = umstxt;
		this.zoBst = zoBst;
		this.obm = obm;
		this.notiz = notiz;
	}
	
	
}
