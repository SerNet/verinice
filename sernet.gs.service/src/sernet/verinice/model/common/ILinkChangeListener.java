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

import sernet.verinice.interfaces.IReevaluator;

/**
 * This contract is used by the {@link CnATreeElement} to receive value
 * reevaluation signals (@see
 * sernet.verinice.model.common.CnATreeElement.getLinkChangeListener()) normaly
 * in the case of a value change along the graph of linked
 * {@link CnATreeElement}. This contract works together with the
 * {@link IReevaluator}.
 *
 * @author Alexander Koderman <ak[at]sernet[dot]de>
 *
 */
public interface ILinkChangeListener {

	/**
	 * Determine protection level, either by going upwards through <code>CnALinks</code> for
	 * maximum protection level or by just sticking with the current one, depending
	 * on the description and thereby the rules set by the user.
	 *
	 * @param ta
	 */
	public void determineConfidentiality(CascadingTransaction ta) throws TransactionAbortedException ;

	/**
	 * Determine protection level, either by going upwards through <code>CnALinks</code> for
	 * maximum protection level or by just sticking with the current one, depending
	 * on the description and thereby the rules set by the user.
	 *
	 * @param ta
	 */
	public void determineIntegrity(CascadingTransaction ta) throws TransactionAbortedException ;

	/**
	 * Determine protection level, either by going upwards through <code>CnALinks</code> for
	 * maximum protection level or by just sticking with the current one, depending
	 * on the description and thereby the rules set by the user.
	 *
	 * @param ta
	 */
	public void determineAvailability(CascadingTransaction ta) throws TransactionAbortedException ;

	/**
	 * Determine an arbitrate value by going upwards through <code>CnALinks</code>.
	 */
	public void determineValue(CascadingTransaction ta) throws TransactionAbortedException ;
}
