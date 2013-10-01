/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import sernet.verinice.model.bsi.risikoanalyse.IGefaehrdungsBaumElement;

/**
 * Provides an image and text for each item in the TreeViewer.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class GefaehrdungTreeViewerLabelProvider implements ILabelProvider {

    /**
     * Returns the image of the element.
     * 
     * @param element
     *            the element which's image is requested
     * @return the image of the element
     */
    public Image getImage(Object element) {
        try {
            IGefaehrdungsBaumElement iGefaehrdungsBaumElement = (IGefaehrdungsBaumElement) element;
            return GefaehrdungsElementImageProvider.getImage(element);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the text of the element
     * 
     * @param element
     *            the element which's text is requested
     * @return the text of the element
     */
    public String getText(Object element) {
        try {
            IGefaehrdungsBaumElement iGefaehrdungsBaumElement = (IGefaehrdungsBaumElement) element;
            return iGefaehrdungsBaumElement.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Not used. Must be implemented due to IBaseLabelProvider.
     * 
     * @param listener
     *            a label provider listener
     */
    public void addListener(ILabelProviderListener listener) {
    }

    /**
     * Not used. Must be implemented due to IBaseLabelProvider.
     */
    public void dispose() {
    }

    /**
     * Returns whether the label would be affected by a change to the given
     * property of the given element.
     * 
     * @param element
     *            the element
     * @param property
     *            the property
     * @return always false
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * Not used. Must be implemented due to IBaseLabelProvider.
     * 
     * @param listener
     *            a label provider listener
     */
    public void removeListener(ILabelProviderListener listener) {
    }
}
