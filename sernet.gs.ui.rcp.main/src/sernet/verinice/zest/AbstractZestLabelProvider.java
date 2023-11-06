
/*******************************************************************************
 * Copyright (c) 2023 Urs Zeidler <uz@sernet.de>.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Urs Zeidler <uz@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.zest;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.zest.core.widgets.ZestStyles;

public abstract class AbstractZestLabelProvider extends LabelProvider
implements IEntityStyleProvider, IConnectionStyleProvider {

    @Override
    public int getConnectionStyle(Object rel) {
        return ZestStyles.CONNECTIONS_DIRECTED;
    }

    @Override
    public Color getColor(Object rel) {
        return null;
    }

    @Override
    public Color getHighlightColor(Object rel) {
        return null;
    }

    @Override
    public int getLineWidth(Object rel) {
        return 0;
    }

    @Override
    public Color getNodeHighlightColor(Object entity) {
        return null;
    }

    @Override
    public Color getBorderColor(Object entity) {
        return null;
    }

    @Override
    public Color getBorderHighlightColor(Object entity) {
        return null;
    }

    @Override
    public int getBorderWidth(Object entity) {
        return 0;
    }

    @Override
    public Color getBackgroundColour(Object entity) {
        return null;
    }

    @Override
    public Color getForegroundColour(Object entity) {
        return null;
    }

    @Override
    public IFigure getTooltip(Object entity) {
        return null;
    }

    @Override
    public boolean fisheyeNode(Object entity) {
        return false;
    }

}
