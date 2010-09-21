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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;

import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public interface IRelationTable {
	
	public static final String COLUMN_IMG = "_img";
	public static final String COLUMN_TYPE = "_type";
	public static final String COLUMN_TYPE_IMG = "_type_img";
	public static final String COLUMN_TITLE = "_title";
    public static final String COLUMN_RISK_C = "_riskc";
    public static final String COLUMN_RISK_I = "_riski";
    public static final String COLUMN_RISK_A = "_riska";



	/**
	 * @return
	 */
	public CnATreeElement getInputElmt();

	/**
	 * @param inputElmt
	 */
	public void setInputElmt(CnATreeElement inputElmt);

	/**
	 * @param newLink 
	 * @param oldLink 
	 * 
	 */
	public void reload(CnALink oldLink, CnALink newLink);
	
	public void reloadAll();

}
