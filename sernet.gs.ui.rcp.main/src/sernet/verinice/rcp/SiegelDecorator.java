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
package sernet.verinice.rcp;


import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BausteinUmsetzung;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class SiegelDecorator extends LabelProvider implements ILightweightLabelDecorator {

    public static final String IMAGE_A = "overlays/siegel-a.png";
    public static final String IMAGE_B = "overlays/siegel-b.png";
    public static final String IMAGE_C = "overlays/siegel-c.png";

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
     */
    @Override
    public void decorate(Object o, IDecoration decoration) {
        if(o instanceof BausteinUmsetzung) {
            Activator.inheritVeriniceContextState();
            BausteinUmsetzung baustein = (BausteinUmsetzung) o;        
            baustein = (BausteinUmsetzung) Retriever.retrieveElement(baustein,RetrieveInfo.getChildrenInstance().setChildrenProperties(true));
            switch (baustein.getErreichteSiegelStufe()) {
                case 'A':
                    decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_A));
                    break;
                case 'B':
                    decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_B));
                    break;
                case 'C':
                    decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_C));
                    break;
                default:
                    break;
            }
            
        }
    }

}
