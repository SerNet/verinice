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
package sernet.gs.ui.rcp.main.bsi.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;
import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

/**
 * This class creates HTML code for verinice elements.
 * It's used by {@link BrowserView} to show detail information.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public abstract class HtmlWriter {

    private static final Logger LOG = Logger.getLogger(HtmlWriter.class);
    
    private static final String ISO_8859_1 = "iso-8859-1";
    private static final String UTF_8 = "utf-8";
  
    public static String getHtml(Object element) throws GSServiceException {
        String html = null;
        
        if (element instanceof Baustein) {
            Baustein bst = (Baustein) element;
            html = getHtmlFromStream(GSScraperUtil.getInstance().getModel().getBaustein(bst.getUrl(), bst.getStand()), bst.getEncoding());
        }

        if (element instanceof Gefaehrdung) {
            Gefaehrdung gef = (Gefaehrdung) element;
            html = getHtmlFromStream(GSScraperUtil.getInstance().getModel().getGefaehrdung(gef.getUrl(), gef.getStand()), gef.getEncoding());
        }

        if (element instanceof GefaehrdungsUmsetzung) {
            GefaehrdungsUmsetzung gefUms = (GefaehrdungsUmsetzung) element;
            if (gefUms.getUrl() == null || gefUms.getUrl().isEmpty() || gefUms.getUrl().equals("null")) { //$NON-NLS-1$
                html = toHtml(gefUms);
            } else {
                html = getHtmlFromStream(GSScraperUtil.getInstance().getModel().getGefaehrdung(gefUms.getUrl(),gefUms.getStand()), ISO_8859_1); //$NON-NLS-1$
                
            }
        }

        if (element instanceof BausteinUmsetzung) {
            BausteinUmsetzung bst = (BausteinUmsetzung) element;
            if (bst.getUrl() == null || bst.getUrl().isEmpty() || bst.getUrl().equals("null")){
            	html=toHtml(bst);
            }else {
            
            html = getHtmlFromStream(GSScraperUtil.getInstance().getModel().getBaustein(bst.getUrl(), bst.getStand()), bst.getEncoding());
        }
        }
        
        if (element instanceof Massnahme) {
            Massnahme mn = (Massnahme) element;
            html = GSScraperUtil.getInstance().getModel().getMassnahmeHtml(mn.getUrl(), mn.getStand());
        }
 
        if (element instanceof RisikoMassnahmenUmsetzung) {
            RisikoMassnahmenUmsetzung ums = (RisikoMassnahmenUmsetzung) element;
            RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(ums);
            if (ums.getRisikoMassnahme() != null) {
                html = toHtml(ums);
            } else {
                html = GSScraperUtil.getInstance().getModel().getMassnahmeHtml(ums.getUrl(), ums.getStand());
            }
        } else if (element instanceof MassnahmenUmsetzung) {
            MassnahmenUmsetzung mnu = (MassnahmenUmsetzung) element;
            if (mnu.getUrl() == null || mnu.getUrl().isEmpty() || mnu.getUrl().equals("null")){
            	html=toHtml(mnu);
            }else {
            html = GSScraperUtil.getInstance().getModel().getMassnahmeHtml(mnu.getUrl(), mnu.getStand());
        }
        }
 
        if (element instanceof TodoViewItem) {
            TodoViewItem item = (TodoViewItem) element;
            html = GSScraperUtil.getInstance().getModel().getMassnahmeHtml(item.getUrl(), item.getStand());
        }

        if (element instanceof IItem) {
            IItem item = (IItem) element;
            StringBuilder sb = new StringBuilder();
            writeHtml(sb, item.getName(), item.getDescription(), VeriniceCharset.CHARSET_UTF_8.name());
            html = sb.toString(); 
        }
        
        if (element instanceof IControl) {
            IControl control = (IControl) element;
            StringBuilder sb = new StringBuilder();
            writeHtml(sb, control.getTitle(), control.getDescription(), VeriniceCharset.CHARSET_UTF_8.name());
            html = sb.toString();         
        }
        
        if (element instanceof Threat) {
            Threat item = (Threat) element;
            StringBuilder sb = new StringBuilder();
            writeHtml(sb, item.getTitle(), item.getDescription(), VeriniceCharset.CHARSET_UTF_8.name());
            html = sb.toString();         
        }

        if (element instanceof Vulnerability) {
            Vulnerability item = (Vulnerability) element;
            StringBuilder sb = new StringBuilder();
            writeHtml(sb, item.getTitle(), item.getDescription(), VeriniceCharset.CHARSET_UTF_8.name());
            html = sb.toString();         
        }
        
        return html;
  
    }
    
    private static String toHtml(BausteinUmsetzung bstums){
    	StringBuilder buf =  new StringBuilder();
    	writeHtml(buf, bstums.getTitle(), bstums.getDescription(), ISO_8859_1);
    	return buf.toString();
    }
    
    private static String toHtml(MassnahmenUmsetzung mnums){
    	StringBuilder buf =  new StringBuilder();
    	writeHtml(buf, mnums.getTitle(), mnums.getDescription(), ISO_8859_1);
    	return buf.toString();
    }
    
    private static String toHtml(GefaehrdungsUmsetzung ums) {
        StringBuilder buf = new StringBuilder();
        writeHtml(buf, ums.getId() + " " + ums.getTitle(), ums.getDescription(), ISO_8859_1); //$NON-NLS-1$ //$NON-NLS-2$
        return buf.toString();
    }
    
    private static String toHtml(RisikoMassnahmenUmsetzung ums) {
        StringBuilder buf = new StringBuilder();
        RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(ums);
        writeHtml(buf, ums.getNumber() + " " + ums.getName(), ums.getDescription(), ISO_8859_1); //$NON-NLS-1$ //$NON-NLS-2$
        return buf.toString();
    }
    
    public static String getPage(String text) {
        StringBuilder sb = new StringBuilder();
        writeHtml(sb, null, text, VeriniceCharset.CHARSET_UTF_8.name());
        return sb.toString();
    }
    
    private static void writeHtml(StringBuilder buf, String headline, String bodytext, String encoding) {
        String cssDir = CnAWorkspace.getInstance().getWorkdir()+ File.separator + "html" + File.separator + "screen.css"; //$NON-NLS-1$ //$NON-NLS-2$
        buf.append("<html><head>"); //$NON-NLS-1$
        buf.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=").append(encoding).append("\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        buf.append("<link REL=\"stylesheet\" media=\"screen\" HREF=\"").append(cssDir).append("\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
        buf.append("</head><body><div id=\"content\">"); //$NON-NLS-1$
        if(headline!=null) {
            buf.append("<h1>"); //$NON-NLS-1$
            buf.append(headline);
            buf.append("</h1>"); //$NON-NLS-1$
        }
        buf.append("<p>"); //$NON-NLS-1$
        buf.append(bodytext.replaceAll("\\n", "<br/>")); //$NON-NLS-1$ //$NON-NLS-2$
        buf.append("</p></div></body></html>"); //$NON-NLS-1$
    }
    
    
   private static String getHtmlFromStream(InputStream is, String encoding) {
       final int utf8SkipWhitespaceChar = 160;
       try {
           if ( !(encoding.equalsIgnoreCase(ISO_8859_1) || encoding.equalsIgnoreCase(UTF_8)) ) { //$NON-NLS-1$ //$NON-NLS-2$
               encoding = UTF_8; //$NON-NLS-1$
           }
           
           InputStreamReader read = new InputStreamReader(is, encoding); //$NON-NLS-1$
           BufferedReader buffRead = new BufferedReader(read);
           StringBuilder b = new StringBuilder();
           String line;
           boolean skip = false;
           boolean skipComplete = false;
           String cssDir = CnAWorkspace.getInstance().getWorkdir()
                   + File.separator + "html" + File.separator + "screen.css"; //$NON-NLS-1$ //$NON-NLS-2$

           while ((line = buffRead.readLine()) != null) {
               if (!skipComplete) {
                   if (line.matches(".*div.*id=\"menuoben\".*") //$NON-NLS-1$
                           || line.matches(".*div.*class=\"standort\".*")){ //$NON-NLS-1$
                       skip = true;
                   } else if (line.matches(".*div.*id=\"content\".*")) { //$NON-NLS-1$
                       skip = false;
                       skipComplete = true;
                   }
               }


               // we strip away images et al to keep just the information we
               // need:
               line = line.replace("../../media/style/css/screen.css", cssDir); //$NON-NLS-1$
               line = line.replace("../../../screen.css", cssDir); //$NON-NLS-1$
               line = line.replace("../../screen.css", cssDir); //$NON-NLS-1$
               line = line.replace("../screen.css", cssDir); //$NON-NLS-1$
               line = line.replaceAll("<a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
               line = line.replaceAll("</a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
               line = line.replaceAll("<img.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
               line = line.replace((char) utf8SkipWhitespaceChar, ' '); // replace non-breaking spaces


               if (!skip) {
                   b.append(line);
               }
           }
           return b.toString();
       } catch (Exception e) {
           LOG.error("Error while geeting html from stream", e);
           return null;
       }
   }
}
