/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.swt.widgets;

import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import sernet.hui.common.connect.EntityType;

/**
 * Register icons for entity types that will be used
 * in list displays, table viewers etc.
 * 
 * @author koderman@sernet.de
 *
 */
public class IconRegistry {
	private static HashMap<String, IconProvider> icons = new HashMap<String, IconProvider>();
	private static Image nullImage = new Image (Display.getDefault(), 1, 1);
	
	public static IconProvider getIconProvider(String hui) {
		IconProvider icon = icons.get(hui);
		if (icon == null)
			icon = new IconProvider() {
				public Image getIcon() {
					return nullImage;
				}
			
		};
		return icon;
	}
	
	public static void registerIconProvider(EntityType hui, IconProvider icon) {
		icons.put(hui.getId(), icon);
	}
	

}
