/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search.tables;

import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.column.IconColumn;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class TableImageProvider {


    public static Image getImagePath(VeriniceSearchResultRow row) {

        ImageCache imgCache = ImageCache.getInstance();

        // check if custom image is set
        String imagePath = row.getValueFromResultString(IconColumn.ICON_PROPERTY_NAME);
        if (!imagePath.isEmpty()){
            return imgCache.getImage(imagePath);
        }

        String typeId = row.getParent().getEntityTypeId();

        // retrieve default images
        if(imgCache.isBSITypeElement(typeId)){
            return ImageCache.getInstance().getBSITypeImage(typeId);
        }

        if(imgCache.isISO27kTypeElement(typeId)){
            return imgCache.getISO27kTypeImage(typeId);
        }


        return null;
    }

}
