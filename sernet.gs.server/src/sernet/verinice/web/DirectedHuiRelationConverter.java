/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
package sernet.verinice.web;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import sernet.hui.common.connect.DirectedHuiRelation;
import sernet.hui.common.connect.HUITypeFactory;

/**
 * Convert between client and server-side representations of
 * {@link DirectedHuiRelation}
 */
@FacesConverter("sernet.verinice.web.DirectedHuiRelationConverter")
public class DirectedHuiRelationConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.isEmpty()) {
            return null;
        }
        char directionCode = value.charAt(0);
        boolean isForward = directionCode == 'f';
        String relationID = value.substring(1);
        return DirectedHuiRelation.getDirectedHuiRelation(
                HUITypeFactory.getInstance().getRelation(relationID), isForward);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        DirectedHuiRelation huiRelation = (DirectedHuiRelation) value;
        boolean isForward = huiRelation.isForward();
        String directionCode = isForward ? "f" : "b";
        String relationID = huiRelation.getHuiRelation().getId();
        return directionCode.concat(relationID);
    }

}
