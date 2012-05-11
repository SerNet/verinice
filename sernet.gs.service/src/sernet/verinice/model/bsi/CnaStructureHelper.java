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

import sernet.gs.model.Baustein;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;

/**
 * Helper class to verify basic business logic rules for main tree,
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public final class CnaStructureHelper {

	public static boolean canContain(Object obj) {
		if (obj instanceof BausteinUmsetzung)
			return true;
		if (obj instanceof Baustein)
			return true;
		if (obj instanceof LinkKategorie)
			return true;
		if (obj instanceof FinishedRiskAnalysis)
			return true;
		return false;
	}
}
