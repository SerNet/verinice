/*******************************************************************************
 * Copyright (c) 2012 Julia Haas.
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
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.service.ServerInitializer;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**Decorator for new user-defined Controls
 * @author Julia Haas <jh[at]sernet[dot]de>
 *
 */
public class OwnControlDecorator extends LabelProvider implements ILightweightLabelDecorator{

    public static final String IMAGE_PATH = "overlays/owned_ovr.gif";

    public void decorate(Object o, IDecoration decoration) {
        if (o instanceof MassnahmenUmsetzung ) {
            ServerInitializer.inheritVeriniceContextState();
            MassnahmenUmsetzung massnahme = (MassnahmenUmsetzung) o;
            if (massnahme.getUrl() == null || massnahme.getUrl().isEmpty() || massnahme.getUrl().equals("null")) {
                decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_PATH));
            }

        }

    }
}
