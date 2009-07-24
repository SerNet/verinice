/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.common.model.CascadingTransaction;
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

	public void integritaetChanged(CascadingTransaction ta) {
		
		
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
		sbTarget.getSchutzbedarfProvider().setIntegritaet(highestValue, ta);
	}

	

	public void verfuegbarkeitChanged(CascadingTransaction ta) {
		
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
		sbTarget.getSchutzbedarfProvider().setVerfuegbarkeit(highestValue, ta);
	}

	public void vertraulichkeitChanged(CascadingTransaction ta) {
		
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
		sbTarget.getSchutzbedarfProvider().setVertraulichkeit(highestValue, ta);
	}
	
}
