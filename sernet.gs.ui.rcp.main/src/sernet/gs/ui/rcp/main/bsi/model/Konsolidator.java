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

import java.util.List;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * The Konsolidator copys values from one object to another,
 * filling in values that the user has already entered. 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class Konsolidator {

	/**
	 * Copy values for all Massnahmen from one BausteinUmsetzung to another.
	 * 
	 * @param source
	 * @param target
	 */
	public static void konsolidiereMassnahmen(BausteinUmsetzung source,
			BausteinUmsetzung target) {
		if (!source.getKapitel().equals(target.getKapitel()))
			return;
		
		for (MassnahmenUmsetzung mn: target.getMassnahmenUmsetzungen()) {
			MassnahmenUmsetzung sourceMn = source.getMassnahmenUmsetzung(mn.getUrl());
			if (sourceMn != null) {
				mn.getEntity().copyEntity(sourceMn.getEntity());
			}
		}
	}

	/**
	 * Copy values from one BausteinUmsetzung to another.
	 * @param source
	 * @param target
	 */
	public static void konsolidiereBaustein(BausteinUmsetzung source, 
			BausteinUmsetzung target) {
		target.getEntity().copyEntity(source.getEntity());
	}

}
