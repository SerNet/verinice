/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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

import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public interface IRelationTable {
	
	String COLUMN_IMG = "_img";
	String COLUMN_TYPE = "_type";
	String COLUMN_TYPE_IMG = "_type_img";
	String COLUMN_TITLE = "_title";
	String COLUMN_COMMENT= "_comment";
    String COLUMN_RISK_C = "_riskc";
    String COLUMN_RISK_I = "_riski";
    String COLUMN_RISK_A = "_riska";



	/**
	 * @return
	 */
	CnATreeElement getInputElmt();

	/**
	 * @param inputElmt
	 */
	void setInputElmt(CnATreeElement inputElmt);

	/**
	 * @param newLink 
	 * @param oldLink 
	 * 
	 */
	void reload(CnALink oldLink, CnALink newLink);
	
	void reloadAll();

}
