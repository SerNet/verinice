/****************************************************************************
 *                                                                          *
 *  Project: CnATool              						
 * ------------------------------------------------------------------------ *
 *                                                                          *
 * The Contents of this file are made available subject to                  *
 * the terms of GNU Lesser General Public License Version 2.1.              *
 *                                                                          * 
 * GNU Lesser General Public License Version 2.1                            *
 * ======================================================================== *
 * Copyright 2007 Alexander Koderman                                        *
 *                                                                          *
 * This library is free software; you can redistribute it and/or            *
 * modify it under the terms of the GNU Lesser General Public               *
 * License version 2.1, as published by the Free Software Foundation.       *
 *                                                                          *
 * This library is distributed in the hope that it will be useful,          *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 * Lesser General Public License for more details.                          *
 *                                                                          *
 * You should have received a copy of the GNU Lesser General Public         *
 * License along with this library; if not, write to the Free Software      *
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,                    *
 * MA  02111-1307  USA                                                      *
 *                                                                          *
 ****************************************************************************/

package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.NumericStringComparator;

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
				return (new Integer(kap1[0] * 1000 + kap1[1])
						.compareTo((kap2[0] * 1000 + kap2[1])));
			}

			if (e1 instanceof BausteinUmsetzung

			&& e2 instanceof BausteinUmsetzung) {
				// sort chapters correctly by converting 2.45, 2.221, 3.42
				// to 2045, 2221, 3024
				int[] kap1 = ((BausteinUmsetzung) e1).getKapitelValue();
				int[] kap2 = ((BausteinUmsetzung) e2).getKapitelValue();
				return (new Integer(kap1[0] * 1000 + kap1[1])
						.compareTo((kap2[0] * 1000 + kap2[1])));
			}
			
			if (e1 instanceof IBSIStrukturElement
					&& e2 instanceof IBSIStrukturElement) {
//				String k1 = ((IBSIStrukturElement)e1).getKuerzel();
//				String k2 = ((IBSIStrukturElement)e2).getKuerzel();
				String k1 = ((IBSIStrukturElement)e1).getKuerzel() + ((CnATreeElement)e1).getTitel();
				String k2 = ((IBSIStrukturElement)e2).getKuerzel() + ((CnATreeElement)e2).getTitel();
				return numComp.compare(k1, k2);
			}

			return super.compare(viewer, e1, e2);
		}

	}