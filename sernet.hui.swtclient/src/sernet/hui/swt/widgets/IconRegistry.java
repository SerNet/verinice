package sernet.hui.swt.widgets;

import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import sernet.hui.common.connect.Entity;
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
