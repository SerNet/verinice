/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;

/**
 * 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IconCellProvider extends ImageCellProvider {

    private static final Logger LOG = Logger.getLogger(IconCellProvider.class);
    
    private int column;
    
    private static final int COLUMN_AMOUNT = 20;
    
    /**
     * @param thumbnailSize
     */
    protected IconCellProvider(int column) {
        super(COLUMN_AMOUNT);
        this.column = column;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.ImageCellProvider#getImage(java.lang.Object)
     */
    @Override
    protected Image getImage(Object element) {
        Image image = null;
        if (element instanceof IconDescriptor[]) {
            IconDescriptor[] iconDescriptors = (IconDescriptor[]) element;
            if(iconDescriptors[column]!=null) {
                image = getImage((iconDescriptors[column]).getImageDescriptor());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Image: " + iconDescriptors[column].getPath());
                }
            }
        }
        return image;
    }

    public Image getImage(ImageDescriptor id) {
        if (id == null){
            return null;
        }
        Image image = id.createImage(false);
        if (image == null) {
            image = getImage(ImageCache.UNKNOWN);
        } 

        return image;
    }

}
