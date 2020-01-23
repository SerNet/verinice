/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan
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
package sernet.verinice.rcp.bp;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.bp.ISecurityLevelProvider;
import sernet.verinice.model.bp.SecurityLevel;

/**
 * Decorates {@link ISecurityLevelProvider} elements with an overlay indicating
 * the security level that they implement.
 */
public class SecurityLevelDecorator extends LabelProvider implements ILightweightLabelDecorator {
    private static final String IMAGE_BASIC = "overlays/sec_level_b.png";
    private static final String IMAGE_STANDARD = "overlays/sec_level_s.png";
    private static final String IMAGE_HIGH = "overlays/sec_level_e.png";

    @Override
    public void decorate(Object element, IDecoration decoration) {
        if (element instanceof ISecurityLevelProvider) {
            ISecurityLevelProvider securable = (ISecurityLevelProvider) element;
            SecurityLevel level = securable.getSecurityLevel();
            if (level != null) {
                decoration.addOverlay(
                        ImageCache.getInstance().getImageDescriptor(getImageFilename(level)));
            }
        }
    }

    private String getImageFilename(SecurityLevel level) {
        switch (level) {
        case BASIC:
            return IMAGE_BASIC;
        case STANDARD:
            return IMAGE_STANDARD;
        case HIGH:
            return IMAGE_HIGH;
        default:
            throw new IllegalArgumentException("Security level not supported.");
        }
    }
}