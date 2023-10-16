/*******************************************************************************
 * Copyright (c) 2020 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.model.bp;

import java.util.Collection;

import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.validation.CnAValidation;

/**
 * Default implementation that does nothing
 */
public class DefaultBpModelListener implements IBpModelListener {

    @Override
    public void databaseChildRemoved(CnATreeElement child) {
        // do nothing
    }

    @Override
    public void childAdded(CnATreeElement category, CnATreeElement child) {
        // do nothing
    }

    @Override
    public void databaseChildAdded(CnATreeElement child) {
        // do nothing
    }

    @Override
    public void modelRefresh(Object object) {
        // do nothing
    }

    @Override
    public void childRemoved(CnATreeElement category, CnATreeElement child) {
        // do nothing
    }

    @Override
    public void childChanged(CnATreeElement child) {
        // do nothing
    }

    @Override
    public void databaseChildChanged(CnATreeElement child) {
        // do nothing
    }

    @Override
    public void databaseChildRemoved(ChangeLogEntry entry) {
        // do nothing
    }

    @Override
    public void linkChanged(CnALink old, CnALink link, Object source) {
        // do nothing
    }

    @Override
    public void linkRemoved(CnALink link) {
        // do nothing
    }

    @Override
    public void linksAdded(Collection<CnALink> links) {
        // do nothing
    }

    @Override
    public void modelReload(ISO27KModel newModel) {
        // do nothing
    }

    @Override
    public void validationAdded(Integer scopeId) {
        // do nothing
    }

    @Override
    public void validationRemoved(Integer scopeId) {
        // do nothing
    }

    @Override
    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation) {
        // do nothing
    }

    @Override
    public void modelReload(BpModel newModel) {
        // do nothing
    }

}
