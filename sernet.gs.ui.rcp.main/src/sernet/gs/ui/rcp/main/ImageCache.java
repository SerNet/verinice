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
package sernet.gs.ui.rcp.main;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.Exception;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.samt.SamtTopic;

/**
 * URLs and ImageDescriptors for all images used by the application.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class ImageCache {

    // FIXME ak exception accessing read only element as normal user
    // FIXME ak secureDAO not used on all elements, ie bausteinumsetzung
    // FIXME ak exception sving element, entity exists in session with separate instance...
    
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
    public static final String VIEW_TASK = "tasks.gif";

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
    
    public static final String LOAD_MORE = "load-more.gif";

	/* ************************************************************************************************** */
	// ISO 27000 icons - ISM Tree View:
	public static final String ISO27K_ASSET 			= "sweetie-verinice/png/16-asset-grey.png";
	public static final String ISO27K_ASSETGROUP        = "sweetie-verinice/png/assetgroup.png";
    public static final String ISO27K_THREAT 			= "sweetie-verinice/png/16-lightening.png";
	public static final String ISO27K_VULNERABILITY 	= "sweetie-verinice/png/16-shield-blue-broken.png";
//	public static final String ISO27K_INCIDENT_SCENARIO = "sweetie-verinice/png/16-arrow-branch-bgr.png";
	public static final String ISO27K_INCIDENT_SCENARIO = "dialog-warning.png";
	public static final String ISO27K_INCIDENT 			= "sweetie-verinice/png/16-arrow-incident-red.png";
	public static final String ISO27K_REQUIREMENT 		= "sweetie-verinice/png/16-paper-gavel-alt.png";
	public static final String ISO27K_EXCEPTION 		= "sweetie-verinice/png/16-paper-excerpt-yellow.png";
	public static final String ISO27K_AUDIT 			= "sweetie-verinice/png/16-clipboard-audit.png";
	public static final String ISO27K_AUDITGROUP        = "sweetie-verinice/png/auditgroup.png";
    public static final String ISO27K_INTERVIEW 		= "sweetie-verinice/png/16-clipboard-comment.png";
	public static final String ISO27K_IMRPOVEMENT_NOTE 	= "sweetie-verinice/png/16-clipboard-report-bar.png";
	public static final String ISO27K_EVIDENCE 			= "sweetie-verinice/png/16-clipboard-eye.png";
	public static final String ISO27K_RESPONSE 			= "sweetie-verinice/png/16-paper-arrow-green.png";
	public static final String ISO27K_DOCUMENT 			= "mime-document.png";
	public static final String ISO27K_RECORD 			= "mime-text.png";
	public static final String ISO27K_SCOPE 			= "tree_explorer.gif";
	public static final String ISO27K_PERSON 			= "user_suit.png";
	public static final String ISO27K_FOLDER 			= "folder.png";
	public static final String ISO27K_PROCESS 			= "sweetie-verinice/png/16-paper-workplan.png";
	public static final String ISO27K_IMPORT            = "import.gif";

	public static final String ISO27K_CONTROL			= "stufe_none.png";
	public static final String ISO27K_CONTROLGROUP      = "controlgroup.png";
    public static final String ISO27K_CONTROL_NO		= "16-em-cross.png";
	public static final String ISO27K_CONTROL_YES		= "16-em-check.png";
	
	public static final String ISA_TOPIC		        = "isa-topic.png";
	
	// ISO 27k icons - other:
	public static final String ISO27K_RISK = "sweetie-verinice/png/16-paper-calculate-percent.png";
	/* ************************************************************************************************** */

	public static final String NOALIENS = "noaliens.png";
	
	
	
	private static ImageCache instance;

	private static URL imagePath;

	
	private final Map<ImageDescriptor, Image> imageMap = new HashMap<ImageDescriptor, Image>();

	// for BSI elements: map of <element type> : <icon name> 
	private static final HashMap<String, String> BSI_ICON_MAP;
	
	// for ISO27k elements: map of <element type> : <icon name> 
	private static final HashMap<String, String> ISO27K_ICON_MAP;

	
	static {
		ISO27K_ICON_MAP = new HashMap<String, String>();
		
		// fill type map for iso27k icons:
		ISO27K_ICON_MAP.put(Organization.TYPE_ID, ImageCache.ISO27K_SCOPE);
		ISO27K_ICON_MAP.put(Asset.TYPE_ID, ImageCache.ISO27K_ASSET);
		ISO27K_ICON_MAP.put(AssetGroup.TYPE_ID, ImageCache.ISO27K_ASSETGROUP);
        ISO27K_ICON_MAP.put(Threat.TYPE_ID, ImageCache.ISO27K_THREAT);
		ISO27K_ICON_MAP.put(Vulnerability.TYPE_ID, ImageCache.ISO27K_VULNERABILITY);
		ISO27K_ICON_MAP.put(IncidentScenario.TYPE_ID,ImageCache.ISO27K_INCIDENT_SCENARIO);
		ISO27K_ICON_MAP.put(Incident.TYPE_ID, ImageCache.ISO27K_INCIDENT);
		ISO27K_ICON_MAP.put(Requirement.TYPE_ID, ImageCache.ISO27K_REQUIREMENT);
		ISO27K_ICON_MAP.put(Exception.TYPE_ID, ImageCache.ISO27K_EXCEPTION);
		ISO27K_ICON_MAP.put(Audit.TYPE_ID, ImageCache.ISO27K_AUDIT);
		ISO27K_ICON_MAP.put(AuditGroup.TYPE_ID, ImageCache.ISO27K_AUDITGROUP);
        ISO27K_ICON_MAP.put(Interview.TYPE_ID, ImageCache.ISO27K_INTERVIEW);
		ISO27K_ICON_MAP.put(Finding.TYPE_ID, ImageCache.ISO27K_IMRPOVEMENT_NOTE);
		ISO27K_ICON_MAP.put(Evidence.TYPE_ID, ImageCache.ISO27K_EVIDENCE);
		ISO27K_ICON_MAP.put(Document.TYPE_ID, ImageCache.ISO27K_DOCUMENT);
		ISO27K_ICON_MAP.put(PersonIso.TYPE_ID, ImageCache.ISO27K_PERSON);
		ISO27K_ICON_MAP.put(Control.TYPE_ID,   ImageCache.ISO27K_CONTROL);
		ISO27K_ICON_MAP.put(ControlGroup.TYPE_ID,   ImageCache.ISO27K_CONTROLGROUP);
        ISO27K_ICON_MAP.put(Response.TYPE_ID,   ImageCache.ISO27K_RESPONSE);
		ISO27K_ICON_MAP.put(sernet.verinice.model.iso27k.Process.TYPE_ID,   ImageCache.ISO27K_PROCESS);
		ISO27K_ICON_MAP.put(Record.TYPE_ID,   ImageCache.ISO27K_RECORD);
		ISO27K_ICON_MAP.put(SamtTopic.TYPE_ID,   ImageCache.ISO27K_CONTROL);
		ISO27K_ICON_MAP.put(ImportIsoGroup.TYPE_ID, ImageCache.ISO27K_IMPORT);
		
		BSI_ICON_MAP = new HashMap<String, String>();
		
		// fill type map for BSI icons:
		BSI_ICON_MAP.put(Anwendung.TYPE_ID, ImageCache.ANWENDUNG);
		BSI_ICON_MAP.put(Gebaeude.TYPE_ID, ImageCache.GEBAEUDE);
		BSI_ICON_MAP.put(Client.TYPE_ID, ImageCache.CLIENT);
		BSI_ICON_MAP.put(Server.TYPE_ID, ImageCache.SERVER);
		BSI_ICON_MAP.put(SonstIT.TYPE_ID, ImageCache.SONSTIT);
		BSI_ICON_MAP.put(TelefonKomponente.TYPE_ID, ImageCache.TELEFON);
		BSI_ICON_MAP.put(Person.TYPE_ID, ImageCache.PERSON);
		BSI_ICON_MAP.put(NetzKomponente.TYPE_ID, ImageCache.NETWORK);
		BSI_ICON_MAP.put(Raum.TYPE_ID, ImageCache.RAUM);
		BSI_ICON_MAP.put(AnwendungenKategorie.TYPE_ID, ImageCache.ANWENDUNG);
		BSI_ICON_MAP.put(GebaeudeKategorie.TYPE_ID, ImageCache.GEBAEUDE);
		BSI_ICON_MAP.put(ClientsKategorie.TYPE_ID, ImageCache.CLIENT);
		BSI_ICON_MAP.put(ServerKategorie.TYPE_ID, ImageCache.SERVER);
		BSI_ICON_MAP.put(SonstigeITKategorie.TYPE_ID, ImageCache.SONSTIT);
		BSI_ICON_MAP.put(TKKategorie.TYPE_ID, ImageCache.TELEFON);
		BSI_ICON_MAP.put(PersonenKategorie.TYPE_ID, ImageCache.PERSON);
		BSI_ICON_MAP.put(NKKategorie.TYPE_ID, ImageCache.NETWORK);
		BSI_ICON_MAP.put(RaeumeKategorie.TYPE_ID, ImageCache.RAUM);
		BSI_ICON_MAP.put(BausteinUmsetzung.TYPE_ID, ImageCache.BAUSTEIN_UMSETZUNG);
		BSI_ICON_MAP.put(MassnahmenUmsetzung.TYPE_ID, ImageCache.ISO27K_CONTROL);
	}
	
	private ImageCache() {
		
	}
	


	public static ImageCache getInstance() {
		if (instance == null) {
			instance = new ImageCache();
			imagePath = Activator.getDefault().getBundle().getEntry("icons/");
		}
		return instance;
	}
	
	public Image getBSITypeImage(String typeId) {
		Image image = getImage(ImageCache.UNKNOWN);
		if (typeId != null && BSI_ICON_MAP.get(typeId) != null) {
			image = getImage(BSI_ICON_MAP.get(typeId));
		}
		return image;
	}
	
	public Image getISO27kTypeImage(String typeId) {
		Image image = getImage(ImageCache.UNKNOWN);
		if (typeId != null && ISO27K_ICON_MAP.get(typeId) != null) {
			image = getImage(ISO27K_ICON_MAP.get(typeId));
		}
		return image;
	}

	public Image getObjectTypeImage(String typeId) {
		Image image = getISO27kTypeImage(typeId);
		if (image == getImage(ImageCache.UNKNOWN))
			image = getBSITypeImage(typeId);
		return image;
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

	/**
	 * @param implemented
	 * @return
	 */
	public Image getControlImplementationImage(String implemented) {
		if (IControl.IMPLEMENTED_NOTEDITED.equals(implemented))
			return getImage(ImageCache.MASSNAHMEN_UMSETZUNG_UNBEARBEITET);
		
		if (IControl.IMPLEMENTED_NA.equals(implemented))
			return getImage(ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
		
		if (IControl.IMPLEMENTED_NO.equals(implemented))
			return getImage(ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
		
		if (IControl.IMPLEMENTED_PARTLY.equals(implemented))
			return getImage(ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
		
		if (IControl.IMPLEMENTED_YES.equals(implemented))
			return getImage(ImageCache.MASSNAHMEN_UMSETZUNG_JA);
		
		// else simply return notedited:
		return getImage(ImageCache.MASSNAHMEN_UMSETZUNG_UNBEARBEITET);
	}
	
}
