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
package sernet.hui.swt.widgets;

import org.eclipse.swt.widgets.Composite;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyType;

public interface IHuiControlFactory {

    IHuiControl createControl(Entity entity, PropertyType fieldType, boolean editable,
            Composite parent, boolean focus, boolean showValidationHint,
            boolean useValidationGuiHints);
}
