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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.osgi.util.NLS;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;
import sernet.gs.service.Retriever;
import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bpm.TodoViewItem;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;
import sernet.verinice.model.licensemanagement.NoLicenseAssignedException;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.rcp.account.LicenseMgmtPage;
import sernet.verinice.service.parser.GSScraperUtil;

/**
 * This class creates HTML code for verinice elements.
 * It's used by {@link BrowserView} to show detail information.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class HtmlWriter {

    private static final Logger LOG = Logger.getLogger(HtmlWriter.class);
    
    private static final String ISO_8859_1 = "iso-8859-1";
    private static final String UTF_8 = "utf-8";
    private static final String NULL_STRING = "null";
    
    private HtmlWriter() {
    }

    /**
     * Gets the HTML-Content to display considering a given object
     * which used to be an instanceof {@link CnATreeElement}
     * @param element
     * @return
     * @throws GSServiceException
     */
    public static String getHtml(Object element) throws GSServiceException {
        
        String html = handleRequestDynamic(element);
        
        if (StringUtils.isEmpty(html)){
            html = handleRequestStatic(element);
        }
        
        if (element instanceof IBpElement || element instanceof IBpGroup) {
            StringBuilder buf = new StringBuilder();
            writeHtml(buf, "", html, VeriniceCharset.CHARSET_UTF_8.name());
            html = buf.toString();
        }
        
        return html;
  
    }
    
    /**
     * Tries to determine get HTML-text for {@link BrowserView}
     *  via dynamic SNCA-approach 
     * ( xml-Attribute showInObjectBrowser equals true on huiproperty)
     * 
     * @param element to recieve html-text for
     * 
     * @return (html) content of showInObjectBrowser-annotated property or
     * empty String if no property is found or element is not instanceof
     * {@link CnATreeElement}
     */
    private static String handleRequestDynamic(Object element) {
        if (!(element instanceof CnATreeElement)){
            return StringUtils.EMPTY;
        }

        CnATreeElement cnaTreeElement = (CnATreeElement) element;
        EntityType entityType = cnaTreeElement.getEntityType();
        if (entityType == null) {
            return StringUtils.EMPTY;
        }

        StringBuilder sb = new StringBuilder();
        List<PropertyType> htmlProperties = entityType.getObjectBrowserPropertyTypes();
        Iterator<PropertyType> iterator = htmlProperties.iterator();
        while (iterator.hasNext()) {
            sb.append(buildObjectBrowserContent(cnaTreeElement, iterator.next()));
            if (iterator.hasNext()) {
                sb.append("<br><br>");
            }
        }
        return sb.toString();
    }

    /**
     * Builds the content of the {@link BrowserView} considering the 
     * selected {@link CnATreeElement} in the object-tree and the 
     * {@link PropertyType} (optionally) configured in the SNCA.xml 
     * (if not configured by xml-Attribute, it is hard-coded in this class) 
     * 
     * @param sb
     * @param cnaTreeElement
     * @param iterator
     * @return
     */
    private static String buildObjectBrowserContent(
                CnATreeElement cnaTreeElement, PropertyType propertyType) {
        StringBuilder sb = new StringBuilder();
        cnaTreeElement = Retriever.checkRetrieveElement(cnaTreeElement);
        PropertyList propertyList = cnaTreeElement.getEntity().getProperties(
                propertyType.getId());
        try {
            for (Property property : propertyList.getProperties()) {
                sb.append((property.isLimitedLicense()) 
                        ? getLicenseRestrictedContent(property)  
                                : getProperty(cnaTreeElement, propertyType));
            }
        } catch (LicenseManagementException e){
            LOG.error("Error while validating license", e);
        }
        return sb.toString();
    }
    
    private static String getProperty(CnATreeElement cnATreeElement, 
            PropertyType propertyType){
        String value = "";
        if (RisikoMassnahmenUmsetzung.HIBERNATE_TYPE_ID.equals(
                cnATreeElement.getObjectType())){
            value = getOwnRiskSafeguardText(cnATreeElement);
        }
        if (StringUtils.isEmpty(value)){
            value = cnATreeElement.getPropertyValue(propertyType.getId());
        }
        return value;
    }

    /**
     * @param cnATreeElement
     */
    private static String getOwnRiskSafeguardText(CnATreeElement cnATreeElement) {
        RisikoMassnahmenUmsetzung ums = (RisikoMassnahmenUmsetzung) cnATreeElement;
        RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(ums);
        if (ums.getRisikoMassnahme() != null) {
            return toHtml(ums);
        } 
        return "";
    }
    
    /**
     * Prepares the 
     * decryption of the value of a license restricted property 
     * 
     * Throws a {@link LicenseManagementException} if the 
     * propertyvalue could not be decrypted
     * 
     * @param sb
     * @param property
     * @throws LicenseManagementException
     */
    private static String getLicenseRestrictedContent(Property property)
            throws LicenseManagementException {
        StringBuilder sb = new StringBuilder();
        LicenseMessageInfos infos = getLicenseMgmtService().
                getLicenseMessageInfos(ServiceFactory.
                        lookupAuthService().getUsername(), 
                        property.getLicenseContentId(), "",  null);
        if (infos.isNoLicenseAvailable()){
            return Messages.BrowserView_No_License_assigned;
        } else { // license exists, so set label
            infos.setAccountWizardLabel(LicenseMgmtPage.
                    getLicenseLabelString(infos.getLicenseId()));
        }
        if (infos.getValidUntil().isBefore(LocalDate.now())){
            return NLS.bind(Messages.BrowserView_License_Not_Valid_Anymore, 
                    new Object[]{
                            infos.getAccountWizardLabel()
                    });
        }
        else if (infos.isInvalidSoon()){
            LocalDate dateNow = LocalDate.now();
            LocalDate validUntil = infos.getValidUntil();
            long daysValid = 
                    ChronoUnit.DAYS.
                    between(dateNow, validUntil);
            String msg = NLS.bind(Messages.
                    BrowserView_License_Ends_Soon,
                    new Object[]{
                            infos.getAccountWizardLabel(),
                            daysValid});
            sb.append(msg);
        }

        sb.append(getLicenseRestrictedPropertyValue(property));
        return sb.toString();
    }

    /**
     * Hands over the license restricted property value to the 
     * {@link LicenseManagementService) to decrypt it
     * 
     * @param sb
     * @param property
     */
    private static String getLicenseRestrictedPropertyValue(Property property) {
        String encryptedContentId = property.getLicenseContentId();
        String cypherText = property.getPropertyValue();
        String currentUser = ServiceFactory.lookupAuthService().getUsername();
        try {
            return getLicenseMgmtService().decryptRestrictedProperty(
                    encryptedContentId, cypherText, currentUser );
        } catch (NoLicenseAssignedException e){
            String msg  = "User has no license assigned for this content";
            LOG.error(msg, e);
        } catch (LicenseManagementException e){
            String msg = "Something went wrong decrypting license restricted information";
            LOG.error(msg, e);
        }
        return Messages.BrowserView_No_License_assigned;
    }

    /**
     * Get HTML-text for {@link BrowserView} the hardcoded (old-style) way
     * should only be used, if DynamicRequest does return empty result
     * 
     * @param element to recieve html-text for
     * 
     * @return (html) content of specified property of given element
     *
     * @throws GSServiceException
     */
    private static String handleRequestStatic(Object element) 
                throws GSServiceException {
        if (element instanceof Baustein) {
            Baustein bst = (Baustein) element;
            return getHtmlFromStream(GSScraperUtil.getInstance().getModel().
                    getBaustein(bst.getUrl(), bst.getStand()), bst.getEncoding());
        }

        if (element instanceof OwnGefaehrdung) {
            OwnGefaehrdung ownGefaehrdung = (OwnGefaehrdung) element;
            if (ownGefaehrdung.getUrl() == null 
                    || ownGefaehrdung.getUrl().isEmpty() 
                    || ownGefaehrdung.getUrl().equals(NULL_STRING)) { // $NON-NLS-1$
                return toHtml(ownGefaehrdung);
            } else {
                return getHtmlFromStream(GSScraperUtil.getInstance().
                        getModel().getGefaehrdung(ownGefaehrdung.getUrl(),
                                ownGefaehrdung.getStand()), UTF_8);
            }
        } else if (element instanceof Gefaehrdung) {
            Gefaehrdung gef = (Gefaehrdung) element;
            return getHtmlFromStream(GSScraperUtil.getInstance().getModel().
                    getGefaehrdung(gef.getUrl(), gef.getStand()), gef.getEncoding());
        }

        if (element instanceof GefaehrdungsUmsetzung) {
            GefaehrdungsUmsetzung gefUms = (GefaehrdungsUmsetzung) element;
            if (gefUms.getUrl() == null || gefUms.getUrl().isEmpty() 
                    || gefUms.getUrl().equals(NULL_STRING)) { // $NON-NLS-1$
                return toHtml(gefUms);
            } else {
                return getHtmlFromStream(GSScraperUtil.getInstance().
                        getModel().getGefaehrdung(gefUms.getUrl(),
                                gefUms.getStand()), UTF_8); //$NON-NLS-1$
                
            }
        }


        if (element instanceof BausteinUmsetzung) {
            BausteinUmsetzung bst = (BausteinUmsetzung) element;
            if (bst.getUrl() == null || bst.getUrl().isEmpty() 
                    || bst.getUrl().equals(NULL_STRING)) {
            	return toHtml(bst);
            } else {
            return getHtmlFromStream(GSScraperUtil.getInstance().getModel().
                    getBaustein(bst.getUrl(), bst.getStand()),
                    bst.getEncoding());
        }
        }
        
        if (element instanceof Massnahme) {
            Massnahme mn = (Massnahme) element;
            return GSScraperUtil.getInstance().getModel().
                    getMassnahmeHtml(mn.getUrl(), mn.getStand());
        }

        if (element instanceof RisikoMassnahmenUmsetzung) {
            RisikoMassnahmenUmsetzung ums = (RisikoMassnahmenUmsetzung) element;
            RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(ums);
            if (ums.getRisikoMassnahme() != null) {
                return toHtml(ums);
            } else {
                return GSScraperUtil.getInstance().getModel().
                        getMassnahmeHtml(ums.getUrl(), ums.getStand());
            }
        } else if (element instanceof MassnahmenUmsetzung) {
            MassnahmenUmsetzung mnu = (MassnahmenUmsetzung) element;
            if (mnu.getUrl() == null || mnu.getUrl().isEmpty() 
                    || mnu.getUrl().equals(NULL_STRING)) {
                return toHtml(mnu);
            } else {
                return GSScraperUtil.getInstance().getModel().
                        getMassnahmeHtml(mnu.getUrl(), mnu.getStand());
            }
        }
 
        if (element instanceof TodoViewItem) {
            TodoViewItem item = (TodoViewItem) element;
            return GSScraperUtil.getInstance().getModel().
                    getMassnahmeHtml(item.getUrl(), item.getStand());
        }

        if (element instanceof IItem) {
            IItem item = (IItem) element;
            StringBuilder sb = new StringBuilder();
            writeHtml(sb, item.getName(), item.getDescription(), 
                    VeriniceCharset.CHARSET_UTF_8.name());
            return sb.toString(); 
        }
        
        if (element instanceof Control){
            StringBuilder sb = new StringBuilder();
            Control control = (Control)element;
            PropertyType titleProperty = HUITypeFactory.getInstance().
                    getPropertyType(control.getEntityType().getId(),
                            Control.PROP_NAME);
            PropertyType descriptionProperty = HUITypeFactory.getInstance().
                    getPropertyType(control.getEntityType().getId(),
                            Control.PROP_DESC);
            writeHtml(sb, buildObjectBrowserContent(control,
                    titleProperty),
                    buildObjectBrowserContent(control, 
                            descriptionProperty),
                    VeriniceCharset.CHARSET_UTF_8.name());
            return sb.toString();
        }
        
        if (element instanceof SamtTopic){
            StringBuilder sb = new StringBuilder();
            SamtTopic samtTopic = (SamtTopic)element;
            PropertyType titleProperty = HUITypeFactory.getInstance().
                    getPropertyType(samtTopic.getEntityType().getId(),
                            SamtTopic.PROP_NAME);
            PropertyType descriptionProperty = HUITypeFactory.getInstance().
                    getPropertyType(samtTopic.getEntityType().getId(),
                            SamtTopic.PROP_DESC);
            writeHtml(sb, buildObjectBrowserContent(samtTopic,
                    titleProperty),
                    buildObjectBrowserContent(samtTopic,
                            descriptionProperty),
                    VeriniceCharset.CHARSET_UTF_8.name());
            return sb.toString();
        }
        
        if (element instanceof Threat) {
            Threat item = (Threat) element;
            StringBuilder sb = new StringBuilder();
            PropertyType titleProperty = HUITypeFactory.getInstance().
                    getPropertyType(item.getEntityType().getId(),
                            Threat.PROP_NAME);
            PropertyType descriptionProperty = HUITypeFactory.getInstance().
                    getPropertyType(item.getEntityType().getId(),
                            Threat.PROP_DESCRIPTION);            
            writeHtml(sb, 
                    buildObjectBrowserContent(item, titleProperty), 
                    buildObjectBrowserContent(item, descriptionProperty),
                    VeriniceCharset.CHARSET_UTF_8.name());
            return sb.toString();         
        }

        if (element instanceof Vulnerability) {
            Vulnerability item = (Vulnerability) element;
            StringBuilder sb = new StringBuilder();
            PropertyType titleProperty = HUITypeFactory.getInstance().
                    getPropertyType(item.getEntityType().getId(),
                            Vulnerability.PROP_NAME);
            PropertyType descriptionProperty = HUITypeFactory.getInstance().
                    getPropertyType(item.getEntityType().getId(),
                            Vulnerability.PROP_DESC);
            writeHtml(sb, 
                    buildObjectBrowserContent(item, titleProperty), 
                    buildObjectBrowserContent(item, descriptionProperty),
                    VeriniceCharset.CHARSET_UTF_8.name());
            return sb.toString();         
        }
        
        return "";
    }
    
    private static String toHtml(BausteinUmsetzung bstums){
    	StringBuilder buf =  new StringBuilder();
    	PropertyType titleProperty = HUITypeFactory.getInstance().
    	        getPropertyType(bstums.getEntityType().getId(),
    	                BausteinUmsetzung.P_NAME);
    	PropertyType descriptionProperty = HUITypeFactory.getInstance().
    	        getPropertyType(bstums.getEntityType().getId(), 
    	                BausteinUmsetzung.P_BAUSTEIN_BESCHREIBUNG);
    	writeHtml(buf, buildObjectBrowserContent(bstums, titleProperty),
    	        buildObjectBrowserContent(bstums, descriptionProperty),
    	        ISO_8859_1);
    	return buf.toString();
    }
    
    private static String toHtml(MassnahmenUmsetzung mnums){
    	StringBuilder buf =  new StringBuilder();
    	PropertyType titleProperty = HUITypeFactory.getInstance().
    	        getPropertyType(mnums.getEntityType().
    	                getId(), MassnahmenUmsetzung.P_NAME);
    	PropertyType descriptionProperty = HUITypeFactory.getInstance().
    	        getPropertyType(mnums.getEntityType().
    	                getId(), MassnahmenUmsetzung.P_BESCHREIBUNG);
    	writeHtml(buf, buildObjectBrowserContent(mnums, titleProperty),
    	        buildObjectBrowserContent(mnums, descriptionProperty),
    	        ISO_8859_1);
    	return buf.toString();
    }

    private static String toHtml(GefaehrdungsUmsetzung ums) {
        StringBuilder buf = new StringBuilder();
        PropertyType propertyType = HUITypeFactory.getInstance().getPropertyType(
                ums.getEntityType().getId(), GefaehrdungsUmsetzung.PROP_ID);
        StringBuilder titleBuilder = new StringBuilder().append(
                buildObjectBrowserContent(ums, propertyType));
        titleBuilder.append(" ");
        propertyType = HUITypeFactory.getInstance().getPropertyType(
                ums.getEntityType().getId(), GefaehrdungsUmsetzung.PROP_TITEL);
        titleBuilder.append(buildObjectBrowserContent(ums, propertyType));
        propertyType = HUITypeFactory.getInstance().getPropertyType(
                ums.getEntityType().getId(), 
                GefaehrdungsUmsetzung.PROP_DESCRIPTION);
        writeHtml(buf, titleBuilder.toString(), 
                buildObjectBrowserContent(ums, propertyType),
                ISO_8859_1); //$NON-NLS-1$ //$NON-NLS-2$
        return buf.toString();
    }

    private static String toHtml(OwnGefaehrdung gef) {
        StringBuilder buf = new StringBuilder();
        writeHtml(buf, gef.getId() + " " + gef.getTitel(), 
                gef.getBeschreibung(), ISO_8859_1); //$NON-NLS-1$ //$NON-NLS-2$
        return removeUnsupportedHtmlPattern(buf.toString());
    }
    
    private static String toHtml(RisikoMassnahmenUmsetzung ums) {
        StringBuilder buf = new StringBuilder();
        writeHtml(buf, ums.getNumber() + " " + ums.getName(),
                ums.getDescription(), ISO_8859_1); //$NON-NLS-1$ //$NON-NLS-2$
        return buf.toString();
    }
    
    public static String getPage(String text) {
        StringBuilder sb = new StringBuilder();
        writeHtml(sb, null, text, VeriniceCharset.CHARSET_UTF_8.name());
        return sb.toString();
    }
    
    private static void writeHtml(StringBuilder buf, String headline, String bodytext, String encoding) {
        
        String cssFile = "screen.css";
        
        String cssDir = CnAWorkspace.getInstance().getWorkdir()+ 
                File.separator + "html" + File.separator + cssFile; //$NON-NLS-1$ //$NON-NLS-2$
        buf.append("<html><head>"); //$NON-NLS-1$
        buf.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=").
        append(encoding).append("\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        buf.append("<link REL=\"stylesheet\" media=\"screen\" HREF=\"").append(cssDir).
        append("\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
        buf.append("</head><body><div id=\"content\">"); //$NON-NLS-1$
        if(headline!=null) {
            buf.append("<h1>"); //$NON-NLS-1$
            buf.append(headline);
            buf.append("</h1>"); //$NON-NLS-1$
        }
        buf.append("<p>"); //$NON-NLS-1$
        if(bodytext!=null) {
            buf.append(bodytext.replaceAll("\\n", "<br/>")); //$NON-NLS-1$ //$NON-NLS-2$
        }
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
               line = convertCss(line, cssDir);
               line = removeUnsupportedHtmlPattern(line);
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

   private static String removeUnsupportedHtmlPattern(String line) {
       line = line.replaceAll("<a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
       line = line.replaceAll("</a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
       line = line.replaceAll("<img.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
       line = line.replaceAll("<em>", ""); //$NON-NLS-1$ //$NON-NLS-2$
       line = line.replaceAll("</em>", ""); //$NON-NLS-1$ //$NON-NLS-2$
       return line;
   }

   private static String convertCss(String line, String cssDir) {
       line = line.replace("../../media/style/css/screen.css", cssDir); //$NON-NLS-1$
       line = line.replace("../../../screen.css", cssDir); //$NON-NLS-1$
       line = line.replace("../../screen.css", cssDir); //$NON-NLS-1$
       line = line.replace("../screen.css", cssDir); //$NON-NLS-1$
       return line;
   }
   
   private static ILicenseManagementService getLicenseMgmtService(){
       return ServiceFactory.lookupLicenseManagementService();
   }
   
}