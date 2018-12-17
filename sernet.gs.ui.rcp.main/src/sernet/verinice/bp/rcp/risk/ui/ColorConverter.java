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
package sernet.verinice.bp.rcp.risk.ui;

import org.eclipse.swt.graphics.RGB;

import sernet.verinice.model.bp.risk.Risk.Color;

public final class ColorConverter {

    public static RGB toRGB(Color color) {
        if (color == null) {
            return null;
        }
        return new RGB(color.red, color.green, color.blue);
    }

    public static Color toRiskColor(RGB rgb) {
        if (rgb == null) {
            return null;
        }
        return new Color(rgb.red, rgb.green, rgb.blue);
    }

    private ColorConverter() {

    }

}
