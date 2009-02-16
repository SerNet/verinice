package sernet.gs.reveng.importData;

import sernet.gs.reveng.MUmsetzStatTxt;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbMassn;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.ModZobjBstMassMitarb;

public class BausteineMassnahmenResult {
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
