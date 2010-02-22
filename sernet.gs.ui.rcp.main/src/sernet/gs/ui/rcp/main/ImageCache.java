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

import sernet.verinice.iso27k.model.Asset;
import sernet.verinice.iso27k.model.Audit;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.Document;
import sernet.verinice.iso27k.model.Evidence;
import sernet.verinice.iso27k.model.Exception;
import sernet.verinice.iso27k.model.Finding;
import sernet.verinice.iso27k.model.IISO27kGroup;
import sernet.verinice.iso27k.model.Incident;
import sernet.verinice.iso27k.model.IncidentScenario;
import sernet.verinice.iso27k.model.Interview;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.model.PersonIso;
import sernet.verinice.iso27k.model.Requirement;
import sernet.verinice.iso27k.model.Response;
import sernet.verinice.iso27k.model.Threat;
import sernet.verinice.iso27k.model.Vulnerability;

/**
 * URLs and ImageDescriptors for all images used by the application.
 * 
 * @author koderman@sernet.de
 *
 */
public class ImageCache {

	public static final String UNKNOWN = "generic_element.gif";
	
	public static final String UNKNOW_NEW = "generic_element_new.png";

	public static final String UNKNOWN_GROUP = "generic_elements.gif";

	public static final String UNKNOWN_GROUP_NEW = "generic_elements_new.png";
	
	public static final String PRODUCT_LG = "product_lg.gif";

	public static final String GEBAEUDE = "building.png";

	public static final String RAUM= "door_open.png";

	public static final String PERSON = "user_suit.png";

	public static final String SERVER = "server_database.png";

	public static final String TELEFON = "phone.png";

	public static final String NETWORK = "drive_network.png";

	public static final String SHIELD = "shield.png";
	
	public static final String TOOL= "24-tool-a.png";

	public static final String WRENCH= "wrench.png";
	
	public static final String EXPLORER = "tree_explorer.gif";

	public static final String ARROW_IN = "arrow_in.png";
	public static final String ARROW_OUT = "arrow_out.png";

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

	public static final String STUFE_NONE = "stufe_none.png";
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
	public static final String VIEW_ISMVIEW = "sweetie-verinice/png/16-arrow-branch-bgr.png";
	public static final String VIEW_DSMODEL = "shield.png";
	public static final String VIEW_TODO = "24-em-check.png";
	public static final String VIEW_AUDIT = "24-zoom.png";
	public static final String VIEW_NOTE = "note.png";

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

	public static final String LINK_DOWN = "link_down.png";
	public static final String LINK_UP = "link_up.png";

	public static final String NOTE = "note.png";

	public static final String NOTE_NEW = "note-new.png";

	public static final String DELETE = "delete.png";
	
	public static final String EDIT = "edit.png";

	public static final String SAVE = "save.png";
	
	public static final String ATTACH = "attach.png";
	
	public static final String VIEW = "view.png";
	
	public static final String MIME_ARCHIVE = "mime-archive.png";
	public static final String MIME_AUDIO = "mime-audio.png";
	public static final String MIME_DOCUMENT = "mime-document.png";
	public static final String MIME_HTML = "mime-html.png";
	public static final String MIME_PDF = "mime-pdf.png";
	public static final String MIME_PRESENTATION = "mime-presentation.png";
	public static final String MIME_SPREADSHEET = "mime-spreadsheet.png";
	public static final String MIME_TEXT = "mime-text.png";
	public static final String MIME_UNKNOWN = "mime-unknown.png";
	public static final String MIME_VIDEO = "mime-video.png";
	public static final String MIME_XML = "mime-xml.png";
	public static final String MIME_IMAGE = "mime-image.png";
	
	public static final String LINKED = "linked.gif";
	
	public static final String UNLINKED = "unlinked.gif";

	/* ************************************************************************************************** */
	// ISO 27000 icons - ISM Tree View:
	public static final String ISO27K_ASSET 			= "sweetie-verinice/png/16-asset-grey.png";
	public static final String ISO27K_THREAT 			= "sweetie-verinice/png/16-lightening.png";
	public static final String ISO27K_VULNERABILITY 	= "sweetie-verinice/png/16-shield-blue-broken.png";
	public static final String ISO27K_INCIDENT_SCENARIO = "sweetie-verinice/png/16-arrow-branch-bgr.png";
	public static final String ISO27K_INCIDENT 			= "sweetie-verinice/png/16-arrow-incident-red.png";
	public static final String ISO27K_REQUIREMENT 		= "sweetie-verinice/png/16-paper-gavel-alt.png";
	public static final String ISO27K_EXCEPTION 		= "sweetie-verinice/png/16-paper-excerpt-yellow.png";
	public static final String ISO27K_AUDIT 			= "sweetie-verinice/png/16-clipboard-audit.png";
	public static final String ISO27K_INTERVIEW 		= "sweetie-verinice/png/16-clipboard-comment.png";
	public static final String ISO27K_IMRPOVEMENT_NOTE 	= "sweetie-verinice/png/16-clipboard-report-bar.png";
	public static final String ISO27K_EVIDENCE 			= "sweetie-verinice/png/16-clipboard-eye.png";
	public static final String ISO27K_RESPONSE 			= "sweetie-verinice/png/16-paper-arrow-green.png";
	public static final String ISO27K_DOCUMENT 			= "mime-document.png";
	public static final String ISO27K_RECORD 			= "mime-text.png";
	public static final String ISO27K_SCOPE 			= "tree_explorer.gif";
	public static final String ISO27K_PERSON 			= "user_suit.png";
	public static final String ISO27K_FOLDER 			= "folder.png";

	public static final String ISO27K_CONTROL			= "stufe_none.png";
	public static final String ISO27K_CONTROL_NO		= "16-em-cross.png";
	public static final String ISO27K_CONTROL_YES		= "16-em-check.png";
	
	// ISO 27k icons - other:
	public static final String ISO27K_RISK = "sweetie-verinice/png/16-paper-calculate-percent.png";
	/* ************************************************************************************************** */
	
	
	private static ImageCache instance;

	private static URL imagePath;

	
	private final Map<ImageDescriptor, Image> imageMap = new HashMap<ImageDescriptor, Image>();

	// for ISO27k elements: map of <element type> : <icon name> 
	private HashMap<String, String> iso27kIconMap;
	
	private ImageCache() {
		iso27kIconMap = new HashMap<String, String>();
		
		// fill type map for iso27k icons:
		iso27kIconMap.put(Organization.TYPE_ID, ImageCache.ISO27K_SCOPE);
		iso27kIconMap.put(Asset.TYPE_ID, ImageCache.ISO27K_ASSET);
		iso27kIconMap.put(Threat.TYPE_ID, ImageCache.ISO27K_THREAT);
		iso27kIconMap.put(Vulnerability.TYPE_ID,
											ImageCache.ISO27K_VULNERABILITY);
		iso27kIconMap.put(IncidentScenario.TYPE_ID,
											ImageCache.ISO27K_INCIDENT_SCENARIO);
		iso27kIconMap.put(Incident.TYPE_ID, ImageCache.ISO27K_INCIDENT);
		iso27kIconMap.put(Requirement.TYPE_ID, ImageCache.ISO27K_REQUIREMENT);
		iso27kIconMap.put(Exception.TYPE_ID, ImageCache.ISO27K_EXCEPTION);
		iso27kIconMap.put(Audit.TYPE_ID, ImageCache.ISO27K_AUDIT);
		iso27kIconMap.put(Interview.TYPE_ID, ImageCache.ISO27K_INTERVIEW);
		iso27kIconMap.put(Finding.TYPE_ID, ImageCache.ISO27K_IMRPOVEMENT_NOTE);
		iso27kIconMap.put(Evidence.TYPE_ID, ImageCache.ISO27K_EVIDENCE);
		iso27kIconMap.put(Document.TYPE_ID, ImageCache.ISO27K_DOCUMENT);
		iso27kIconMap.put(PersonIso.TYPE_ID, ImageCache.ISO27K_PERSON);
		iso27kIconMap.put(Control.TYPE_ID,   ImageCache.ISO27K_CONTROL);
		iso27kIconMap.put(Response.TYPE_ID,   ImageCache.ISO27K_RESPONSE);
	}

	public static ImageCache getInstance() {
		if (instance == null) {
			instance = new ImageCache();
			imagePath = Activator.getDefault().getBundle().getEntry("icons/");
		}
		return instance;
	}
	
	public Image getISO27kTypeImage(String typeId) {
		String imageUrl = iso27kIconMap.get(typeId);
		if (typeId == null || imageUrl == null)
			return getImage(ImageCache.UNKNOWN);
		
		return getImage(imageUrl);
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
