/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.common;

import java.io.Serializable;

/**
 * Convenient class to implement a {@link ILinkChangeListener} to spare the
 * override of methods.
 * 
 * @author Urs Zeidler uz[at]sernet.de
 *
 */
public abstract class AbstractLinkChangeListener implements ILinkChangeListener, Serializable {

    private static final long serialVersionUID = -7008559663534040013L;

    /* (non-Javadoc)
     * @see sernet.verinice.model.common.ILinkChangeListener#determineValue(sernet.verinice.model.common.CascadingTransaction)
     */
    @Override
    public void determineValue(CascadingTransaction ta) throws TransactionAbortedException {
    }

    @Override
    public void determineConfidentiality(CascadingTransaction ta)
            throws TransactionAbortedException {
    }

    @Override
    public void determineIntegrity(CascadingTransaction ta) throws TransactionAbortedException {
    }

    @Override
    public void determineAvailability(CascadingTransaction ta) throws TransactionAbortedException {
    }

}
