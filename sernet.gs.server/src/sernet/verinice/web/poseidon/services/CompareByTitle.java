/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web.poseidon.services;

import java.util.Comparator;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.service.model.IObjectModelService;
import sernet.verinice.web.Messages;

public final class CompareByTitle implements Comparator<String> {

    static final String IMPLEMENTATION_STATUS_UNEDITED = "SingleSelectDummyValue";

    @Override
    public int compare(String o1, String o2) {
        return new NumericStringComparator().compare(getLabel(o1), getLabel(o2));
    }

    private String getLabel(String value) {

        if (MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET.equals(value)) {
            return Messages.getString(IMPLEMENTATION_STATUS_UNEDITED);
        }

        return getObjectService().getLabel(value);
    }

    private IObjectModelService getObjectService() {
        return (IObjectModelService) VeriniceContext.get(VeriniceContext.OBJECT_MODEL_SERVICE);
    }
}