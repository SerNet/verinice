package sernet.gs.reveng.importData;

import java.io.Serializable;

import sernet.gs.reveng.MUmsetzStatTxt;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbMassn;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstMass;

@SuppressWarnings("serial")
public class BausteineMassnahmenResult implements Serializable {
	public final MbBaust baustein;
	public final MbMassn massnahme;
	public final MUmsetzStatTxt umstxt;
	public final ModZobjBst zoBst;
	public final ModZobjBstMass obm;
	
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
