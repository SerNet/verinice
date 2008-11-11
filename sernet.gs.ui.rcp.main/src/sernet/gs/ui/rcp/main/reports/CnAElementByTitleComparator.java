package sernet.gs.ui.rcp.main.reports;

import java.text.Collator;
import java.util.Comparator;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.NumericStringComparator;

public class CnAElementByTitleComparator implements
		Comparator<CnATreeElement> {

	NumericStringComparator comparator = new NumericStringComparator();
	
	@Override
	public int compare(CnATreeElement o1, CnATreeElement o2) {
		if (o1 instanceof MassnahmenUmsetzung && o2 instanceof MassnahmenUmsetzung) {
			int[] kap1 = ((MassnahmenUmsetzung) o1).getKapitelValue();
			int[] kap2 = ((MassnahmenUmsetzung) o2).getKapitelValue();
			return (new Integer(kap1[0] * 1000 + kap1[1])
					.compareTo((kap2[0] * 1000 + kap2[1])));
		}
		return Collator.getInstance().compare(o1.getTitel(), o2.getTitel());
	}

}
