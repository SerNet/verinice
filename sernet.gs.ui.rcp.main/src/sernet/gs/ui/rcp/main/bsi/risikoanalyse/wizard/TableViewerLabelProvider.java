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

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;

/**
 * Provides text or image for each column of a given element
 * in the TableViewer.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class TableViewerLabelProvider implements ITableLabelProvider {

	/**
	 * Provides an image in the first column of each item.
	 * 
	 * @param element the item representing the entire row
	 * @param columnIndex the zero-based index of the column
	 *        in which the label appears
	 * @return always the image for Gefaehrdungen
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		
		if (columnIndex == 0){
		  return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);
		}
		return null;
	}

	/**
	 * Provides the text for each column, except the first, per item.
	 * 
	 * @param element the item representing the entire row
	 * @param columnIndex the zero-based index of the column
	 *        in which the label appears
	 */
	public String getColumnText(Object element, int columnIndex) {
	    GefaehrdungsUmsetzung	 gef = (GefaehrdungsUmsetzung) element;
	    switch (columnIndex) {
	    case 0:
	        return null;
	    case 1:
	        return gef.getId();
	    case 2:
	        return gef.getTitle();
	    case 3:
	        return gef.getAlternativeText();
	    default:
	        break;
	    }
	    return "";
	}

	/**
	 * Not implemented - inherited by IBaseLabelProvider.
	 * 
	 * @param listener a label provider listener
	 */
	public void addListener(ILabelProviderListener listener) {}

	/**
	 * Not implemented - inherited by IBaseLabelProvider.
	 */
	public void dispose() {}

	/**
	 * Returns whether the label would be affected 
     * by a change to the given property of the given element.
     * 
     * @param element the item representing the entire row
     * @param property the property
	 * @return always false  
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * Not implemented - inherited by IBaseLabelProvider.
	 * 
	 * @param listener a label provider listener
	 */
	public void removeListener(ILabelProviderListener listener) {}
}
