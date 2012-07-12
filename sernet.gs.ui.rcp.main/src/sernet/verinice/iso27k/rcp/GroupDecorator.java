/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.IISO27Scope;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * Decorates {@link IISO27kGroup}s but not
 *  <ul>
 *  <li>{@link IISO27Scope}s</li>
 *  <li>{@link Audit}s</li>
 *  <li>{@link Asset}s</li>
 *  </ul>
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GroupDecorator extends LabelProvider implements ILightweightLabelDecorator {
    
    public static final String IMAGE_PATH = "overlays/folder_decorator.png";

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
     */
    @Override
    public void decorate(Object o, IDecoration decoration) {
        if(o instanceof IISO27kGroup 
           && !(o instanceof IISO27Scope)
           && !(o instanceof Asset)) {
            decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_PATH));
        }
    }
}
