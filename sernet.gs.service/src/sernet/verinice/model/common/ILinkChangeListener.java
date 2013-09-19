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
package sernet.verinice.model.common;


public interface ILinkChangeListener {
	
	/**
	 * Determine protection level, either by going upwards through <code>CnALinks</code> for
	 * maximum protection level or by just sticking with the current one, depending
	 * on the description and tehreby the rules set by the user.
	 * 
	 * @param ta
	 */
	public void determineVertraulichkeit(CascadingTransaction ta) throws TransactionAbortedException ;
	
	/**
	 * Determine protection level, either by going upwards through <code>CnALinks</code> for
	 * maximum protection level or by just sticking with the current one, depending
	 * on the description and tehreby the rules set by the user.
	 * 
	 * @param ta
	 */
	public void determineIntegritaet(CascadingTransaction ta) throws TransactionAbortedException ;
	
	/**
	 * Determine protection level, either by going upwards through <code>CnALinks</code> for
	 * maximum protection level or by just sticking with the current one, depending
	 * on the description and tehreby the rules set by the user.
	 * 
	 * @param ta
	 */
	public void determineVerfuegbarkeit(CascadingTransaction ta) throws TransactionAbortedException ;
	
	
}
