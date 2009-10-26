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
package sernet.gs.ui.rcp.main;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * URLs and ImageDescriptors for all images used by the application.
 * 
 * @author koderman@sernet.de
 *
 */
public class ImageCache {

	public static final String UNKNOWN = "generic_elements.gif";

	public static final String PRODUCT_LG = "product_lg.gif";

	public static final String GEBAEUDE = "building.png";

	public static final String RAUM= "door_open.png";

	public static final String PERSON = "user_suit.png";

	public static final String SERVER = "server_database.png";

	public static final String TELEFON = "phone.png";

	public static final String NETWORK = "drive_network.png";

	public static final String SHIELD = "shield.png";
	
	public static final String TOOL= "24-tool-a.png";

	public static final String EXPLORER = "tree_explorer.gif";

	public static final String BAUSTEIN_UMSETZUNG = "16-cube-blue.png";
	
	public static final String BAUSTEIN_UMSETZUNG_A = "baustein_a.png";
	public static final String BAUSTEIN_UMSETZUNG_B = "baustein_b.png";
	public static final String BAUSTEIN_UMSETZUNG_C = "baustein_c.png";
	
	public static final String MASSNAHMEN_UMSETZUNG_UNBEARBEITET = "exclamation.png";
	public static final String MASSNAHMEN_UMSETZUNG_NEIN = "16-em-cross.png";
	public static final String MASSNAHMEN_UMSETZUNG_JA = "16-em-check.png";
	public static final String MASSNAHMEN_UMSETZUNG_ENTBEHRLICH = "progress_rem.gif";
	public static final String MASSNAHMEN_UMSETZUNG_TEILWEISE = "16-clock.png";
	
	public static final String RISIKO_MASSNAHMEN_UMSETZUNG = "16-message-warn.png";
	
	public static final String BAUSTEIN = "16-cube-blue.png";

	public static final String STUFE_A = "stufe_a.png";
	public static final String STUFE_B = "stufe_b.png";
	public static final String STUFE_C = "stufe_c.png";
	public static final String STUFE_Z = "stufe_z.png";
	public static final String STUFE_W = "stufe_w.png";

	public static final String ANWENDUNG = "application_osx.png";
	
	public static final String CLIENT= "computer.png";

	public static final String FILTER= "filter_tsk.gif";

	public static final String REPORT = "report.png";

	public static final String DBCONNECT = "database_connect.png";
	
	public static final String DBCLOSE = "database_delete.png";

	public static final String VIEW_BROWSER = "tag.png";
	public static final String VIEW_MASSNAHMEN = "de.png";
	public static final String VIEW_BSIMODEL = "tree_explorer.gif";
	public static final String VIEW_DSMODEL = "shield.png";
	public static final String VIEW_TODO = "24-em-check.png";
	public static final String VIEW_AUDIT = "24-zoom.png";

	public static final String EXPANDALL   = "expandall.gif";
	public static final String COLLAPSEALL = "collapseall.gif";
	
	public static final String OPEN_EDIT = "edtsrclkup_co.gif";

	public static final String CASCADE = "application_cascade.png";
	public static final String SECURITY = "16-security-lock.png";

	public static final String KONSOLIDATOR = "konsolidator.png";

	public static final String GEFAEHRDUNG = "dialog-warning.png";

	public static final String SONSTIT = "sonstit.png";

	public static final String LINKS = "link.png";

	public static final String CHART_PIE = "chart_pie.png";
	public static final String CHART_BAR = "chart_bar.png";
	public static final String CHART_CURVE = "chart_curve.png";

	public static final String VIEW_DOCUMENT = "script.png";

	public static final String AUTOBAUSTEIN = "autobaustein.png";

	public static final String RELOAD = "arrow_refresh.png";

	


	
	
	
	private static ImageCache instance;

	private static URL imagePath;

	
	private final Map<ImageDescriptor, Image> imageMap = new HashMap<ImageDescriptor, Image>();

	public static ImageCache getInstance() {
		if (instance == null) {
			instance = new ImageCache();
			imagePath = Activator.getDefault().getBundle().getEntry("icons/");
		}
		return instance;
	}
	
	public ImageDescriptor getImageDescriptor(String url) {
		ImageDescriptor descriptor;
		try {
			descriptor = ImageDescriptor.createFromURL(new URL(imagePath, url));
		} catch (MalformedURLException e) {
			descriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		return descriptor;
	
	}
	
	public Image getImage(String url) {
		ImageDescriptor descriptor;
		try {
			descriptor = ImageDescriptor.createFromURL(new URL(imagePath, url));
		} catch (MalformedURLException e) {
			descriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		return getImage(descriptor);
	}

	public Image getImage(ImageDescriptor id) {
		if (id == null)
			return null;
		Image image = imageMap.get(id);
		if (image == null) {
			image = id.createImage();
			imageMap.put(id, image);
		}
		return image;
	}
	
	public void dispose() {
		for (Image image : imageMap.values()) {
			image.dispose();
		}
		imageMap.clear();
	}
	
}
