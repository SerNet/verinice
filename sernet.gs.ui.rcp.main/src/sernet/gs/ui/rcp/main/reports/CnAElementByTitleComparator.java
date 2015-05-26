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
package sernet.gs.ui.rcp.main.reports;

import java.text.Collator;
import java.util.Comparator;

import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

public class CnAElementByTitleComparator implements
		Comparator<CnATreeElement> {

	public int compare(CnATreeElement o1, CnATreeElement o2) {
	    final int chapterFactor = 1000;
		if (o1 instanceof MassnahmenUmsetzung && o2 instanceof MassnahmenUmsetzung) {
			int[] kap1 = ((MassnahmenUmsetzung) o1).getKapitelValue();
			int[] kap2 = ((MassnahmenUmsetzung) o2).getKapitelValue();
			return (Integer.valueOf(kap1[0] * chapterFactor + kap1[1])
					.compareTo((kap2[0] * chapterFactor + kap2[1])));
		}
		return Collator.getInstance().compare(o1.getTitle(), o2.getTitle());
	}

}
