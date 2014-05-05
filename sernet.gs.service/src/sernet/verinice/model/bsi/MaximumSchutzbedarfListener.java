/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bsi;

import java.io.Serializable;

import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * On a change event, iterates through all linked items, searching for the
 * maximum protection level to apply.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class MaximumSchutzbedarfListener implements ILinkChangeListener,
		Serializable {

	private CnATreeElement sbTarget;

	public MaximumSchutzbedarfListener(CnATreeElement item) {
		this.sbTarget = item;
	}

	public void determineIntegritaet(CascadingTransaction ta)
			throws TransactionAbortedException {
		if (hasBeenVisited(ta)){
			return;
		}
		ta.enter(sbTarget);

		// get protection level from upward links:
		int highestValue = 0;
		allLinks: for (CnALink link : sbTarget.getLinksUp()) {
			CnATreeElement upwardElmt = link.getDependant();
			if (upwardElmt.isSchutzbedarfProvider()) {
				// upwardElement might depend on maximum level itself, so
				// recurse up:
				upwardElmt.getLinkChangeListener().determineIntegritaet(ta);

				int value = upwardElmt.getSchutzbedarfProvider()
						.getIntegritaet();
				if (value > highestValue){
					highestValue = value;
				}
			}
		}
		
		// if we dont use the maximum principle, keep current level:
		if (!Schutzbedarf.isMaximumPrinzip(sbTarget.getSchutzbedarfProvider()
				.getIntegritaetDescription())){
			return;
		}
		sbTarget.getSchutzbedarfProvider().setIntegritaet(highestValue);
	}

	/**
	 * @param ta
	 * @return
	 */
	private boolean hasBeenVisited(CascadingTransaction ta) {
		if (ta.hasBeenVisited(sbTarget)) {
			return true; // we have already been down this path
		}
		return false;
	}

	public void determineVerfuegbarkeit(CascadingTransaction ta)
			throws TransactionAbortedException {
		if (hasBeenVisited(ta)){
			return;
		}
		ta.enter(sbTarget);


		// otherwise get protection level from upward links:
		int highestValue = 0;
		allLinks: for (CnALink link : sbTarget.getLinksUp()) {
			CnATreeElement upwardElmt = link.getDependant();
			if (upwardElmt.isSchutzbedarfProvider()) {

				// upwardElement might depend on maximum level itself, so
				// recurse up:
				upwardElmt.getLinkChangeListener().determineVerfuegbarkeit(ta);

				int value = upwardElmt.getSchutzbedarfProvider()
						.getVerfuegbarkeit();
				if (value > highestValue){
					highestValue = value;
				}
			}
		}

		// if we dont use the maximum principle, keep current level:
		if (!Schutzbedarf.isMaximumPrinzip(sbTarget.getSchutzbedarfProvider()
				.getVerfuegbarkeitDescription())){
			return;
		}
		sbTarget.getSchutzbedarfProvider().setVerfuegbarkeit(highestValue);
	}

	public void determineVertraulichkeit(CascadingTransaction ta)
			throws TransactionAbortedException {

		if (hasBeenVisited(ta)){
			return;
		}
		ta.enter(sbTarget);

		// otherwise get protection level from upward links:
		int highestValue = 0;
		allLinks: for (CnALink link : sbTarget.getLinksUp()) {
			CnATreeElement upwardElmt = link.getDependant();
			if (upwardElmt.isSchutzbedarfProvider()) {

				// upwardElement might depend on maximum level itself, so
				// recurse up:
				upwardElmt.getLinkChangeListener().determineVertraulichkeit(ta);

				int value = upwardElmt.getSchutzbedarfProvider()
						.getVertraulichkeit();
				if (value > highestValue){
					highestValue = value;
				}
			}
		}
		// if we dont use the maximum principle, keep current level:
		if (!Schutzbedarf.isMaximumPrinzip(sbTarget.getSchutzbedarfProvider()
				.getVertraulichkeitDescription())){
			return;
		}
		sbTarget.getSchutzbedarfProvider().setVertraulichkeit(highestValue);

	}

}
