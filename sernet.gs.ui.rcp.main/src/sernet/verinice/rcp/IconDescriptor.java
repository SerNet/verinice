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

import java.io.File;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IconDescriptor {
    
    private ImageDescriptor imageDescriptor;

    private String path;

    /**
     * @param url
     */
    public IconDescriptor(URL url) {
        path = url.getPath();
        imageDescriptor = ImageDescriptor.createFromURL(url);
    }

    /**
     * @param file
     */
    public IconDescriptor(File file) {
        path=file.getPath();
        imageDescriptor = ImageDescriptor.createFromFile(null,file.getPath());
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public String getPath() {
        return path;
    }

    public void setImageDescriptor(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
