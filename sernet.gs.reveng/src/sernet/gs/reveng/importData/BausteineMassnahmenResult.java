package sernet.gs.reveng.importData;

import java.io.Serializable;

import sernet.gs.reveng.MUmsetzStatTxt;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbMassn;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstMass;

@SuppressWarnings("serial")
public class BausteineMassnahmenResult implements Serializable {
	public MbBaust baustein;
	public MbMassn massnahme;
	public MUmsetzStatTxt umstxt;
	public ModZobjBst zoBst;
	public ModZobjBstMass obm;
	
	public BausteineMassnahmenResult(MbBaust baustein, MbMassn massnahme, 
			MUmsetzStatTxt umstxt, ModZobjBst zoBst, ModZobjBstMass obm) {
		super();
		this.baustein = baustein;
		this.massnahme = massnahme;
		this.umstxt = umstxt;
		this.zoBst = zoBst;
		this.obm = obm;
	}
	
	
}
