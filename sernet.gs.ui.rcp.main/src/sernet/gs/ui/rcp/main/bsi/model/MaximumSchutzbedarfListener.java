package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;

/**
 * On a change event, iterates through all linked items, searching
 * for the maximum protection level to apply.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MaximumSchutzbedarfListener implements ILinkChangeListener, Serializable {

	private CnATreeElement sbTarget;
	
	
	
	public MaximumSchutzbedarfListener(CnATreeElement item) {
		this.sbTarget = item;
	}

	public void integritaetChanged() {
		if (!Schutzbedarf.isMaximumPrinzip(sbTarget.getSchutzbedarfProvider()
				.getIntegritaetDescription()))
			return;
		
		int highestValue = 0;
		allLinks: for(CnALink link : sbTarget.getLinksUp()) {
			CnATreeElement elmt = link.getDependant();
			if (elmt.isSchutzbedarfProvider()) {
				int value = elmt.getSchutzbedarfProvider().getIntegritaet();
				if (value > highestValue)
					highestValue = value;
				if (highestValue == Schutzbedarf.SEHRHOCH)
					break allLinks;
			}
		}
		sbTarget.getSchutzbedarfProvider().setIntegritaet(highestValue);
	}

	

	public void verfuegbarkeitChanged() {
		if (!Schutzbedarf.isMaximumPrinzip(sbTarget.getSchutzbedarfProvider()
				.getVerfuegbarkeitDescription()))
			return;
		
		int highestValue = 0;
		allLinks: for(CnALink link : sbTarget.getLinksUp()) {
			CnATreeElement elmt = link.getDependant();
			if (elmt.isSchutzbedarfProvider()) {
				int value = elmt.getSchutzbedarfProvider().getVerfuegbarkeit();
				if (value > highestValue)
					highestValue = value;
				if (highestValue == Schutzbedarf.SEHRHOCH)
					break allLinks;
			}
		}
		sbTarget.getSchutzbedarfProvider().setVerfuegbarkeit(highestValue);
	}

	public void vertraulichkeitChanged() {
		if (!Schutzbedarf.isMaximumPrinzip(sbTarget.getSchutzbedarfProvider()
				.getVertraulichkeitDescription()))
			return;
		
		int highestValue = 0;
		allLinks: for(CnALink link : sbTarget.getLinksUp()) {
			CnATreeElement elmt = link.getDependant();
			if (elmt.isSchutzbedarfProvider()) {
				int value = elmt.getSchutzbedarfProvider().getVertraulichkeit();
				if (value > highestValue)
					highestValue = value;
				if (highestValue == Schutzbedarf.SEHRHOCH)
					break allLinks;
			}
		}
		sbTarget.getSchutzbedarfProvider().setVertraulichkeit(highestValue);
	}
	
}
