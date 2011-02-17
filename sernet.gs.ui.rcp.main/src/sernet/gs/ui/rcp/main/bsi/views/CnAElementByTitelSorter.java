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

package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

class CnAElementByTitelSorter extends ViewerSorter {
		NumericStringComparator numComp = new NumericStringComparator();
		
		@Override
		public int category(Object element) {
			return element instanceof BausteinUmsetzung ? 0 : 1;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof MassnahmenUmsetzung
					&& e2 instanceof MassnahmenUmsetzung) {
				// sort chapters correctly by converting 2.45, 2.221, 3.42
				// to 2045, 2221, 3024
				int[] kap1 = ((MassnahmenUmsetzung) e1).getKapitelValue();
				int[] kap2 = ((MassnahmenUmsetzung) e2).getKapitelValue();
				return (Integer.valueOf(kap1[0] * 1000 + kap1[1])
						.compareTo((kap2[0] * 1000 + kap2[1])));
			}

			if (e1 instanceof BausteinUmsetzung

			&& e2 instanceof BausteinUmsetzung) {
				// sort chapters correctly by converting 2.45, 2.221, 3.42
				// to 2045, 2221, 3024
				int[] kap1 = ((BausteinUmsetzung) e1).getKapitelValue();
				int[] kap2 = ((BausteinUmsetzung) e2).getKapitelValue();
				return (Integer.valueOf(kap1[0] * 1000 + kap1[1])
						.compareTo((kap2[0] * 1000 + kap2[1])));
			}
			
			if (e1 instanceof IBSIStrukturElement
					&& e2 instanceof IBSIStrukturElement) {
//				String k1 = ((IBSIStrukturElement)e1).getKuerzel();
//				String k2 = ((IBSIStrukturElement)e2).getKuerzel();
				String k1 = ((IBSIStrukturElement)e1).getKuerzel() + ((CnATreeElement)e1).getTitle();
				String k2 = ((IBSIStrukturElement)e2).getKuerzel() + ((CnATreeElement)e2).getTitle();
				return numComp.compare(k1, k2);
			}

			return super.compare(viewer, e1, e2);
		}

	}
