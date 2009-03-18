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
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import sernet.gs.ui.rcp.main.bsi.model.CnaStructureHelper;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class FinishedRiskAnalysis extends CnATreeElement  {

	public static final String TYPE_ID = "riskanalysis";
	
	
	public FinishedRiskAnalysis(CnATreeElement cnaElement) {
		super(cnaElement);
	}
	
	FinishedRiskAnalysis() {
	}
	

	@Override
	public String getTitel() {
		return "Risikoanalyse";
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof GefaehrdungsUmsetzung)
			return true;
		return CnaStructureHelper.canContain(obj);
	}


}
