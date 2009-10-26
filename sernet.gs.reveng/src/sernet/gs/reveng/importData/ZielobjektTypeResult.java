package sernet.gs.reveng.importData;

import sernet.gs.reveng.NZielobjekt;

public class ZielobjektTypeResult {
	public NZielobjekt zielobjekt;
	public String type;
	public String subtype;
	
	public ZielobjektTypeResult(NZielobjekt zielobjekt, String type, String subtype) {
		super();
		this.zielobjekt = zielobjekt;
		this.type = type;
		this.subtype = subtype;
	}
}
