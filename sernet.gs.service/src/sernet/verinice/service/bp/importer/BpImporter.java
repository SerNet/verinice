package sernet.verinice.service.bp.importer;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import ITBP2VNA.generated.implementationhint.Document.Safeguards;
import ITBP2VNA.generated.module.BibItem;
import ITBP2VNA.generated.module.Description;
import ITBP2VNA.generated.module.Document;
import ITBP2VNA.generated.module.Document.ThreatScenario.SpecificThreats;
import ITBP2VNA.generated.module.ElementalthreatRef;
import ITBP2VNA.generated.module.Requirement;
import ITBP2VNA.generated.module.RequirementRef;
import ITBP2VNA.generated.module.SpecificThreat;
import sernet.verinice.interfaces.CnATreeElementBuildException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Link;
import sernet.verinice.service.bp.LoadBpModel;
import sernet.verinice.service.bp.exceptions.CreateBPElementException;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.CreateITNetwork;
import sernet.verinice.service.commands.CreateMultipleLinks;
import sernet.verinice.service.commands.UpdateElement;

/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/

/**
 * this tool transform the "New ITBP Compendium", first released in February of 2018 
 * to a vna-file, for the usage with verinice
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class BpImporter {

    private static final Logger LOG = Logger.getLogger(BpImporter.class);
    
    /**
     *  ATTENTION:
     *  this property defines if specific threats will appear as own objects and being
     *  referenced (via {@link CnALink} ) from other objects. It is still 
     *  not finally decided, if these threats should be part of the catalogue or not
     *  
     *  To not delete the already existing code, to import those elements, it can be controlled
     *  via this boolean, in which way the threats should be considered 
     *  
     *  (false means, the specific threats just appear within the object-browser-description
     *  of the {@link BpRequirementGroup} instances (modules))
     */

    private static final Set<String> processIdentifierPrefixes;
    private static final Set<String> systemIdentifierPrefixes;
    
    private String xmlRootDirectory = null;
    
    private ItNetwork rootNetwork = null;
    
    private BpRequirementGroup processReqGroup = null;
    private BpRequirementGroup systemReqGroup = null;

    private SafeguardGroup processSafeguardGroup = null;
    private SafeguardGroup systemSafeguardGroup = null;

    private BpThreatGroup processThreatGroup = null;
    private BpThreatGroup systemThreatGroup = null;
    private BpThreatGroup elementalThreatGroup = null;
    
    ICommandService commandService;
    IDAOFactory daoFactory;
    
    
    private static final String SUBDIRECTORY_MODULES = "bausteine";
    private static final String SUBDIRECTORY_MEDIA = "media";
    private static final String SUBDIRECTORY_THREATS = "elementare_gefaehrdungen_1";
    private static final String SUBDIRECTORY_IMPL_HINTS = "umsetzungshinweise";
    
    
    private static final String HTML_OPEN_TABLE="<table>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_TABLE="</table>"; //$NON-NLS-1$
    private static final String HTML_OPEN_UL="<ul>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_UL="</ul>"; //$NON-NLS-1$
    private static final String HTML_OPEN_TR="<tr>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_TR="</tr>"; //$NON-NLS-1$
    private static final String HTML_OPEN_TD="<td>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_TD="</td>"; //$NON-NLS-1$
    
    private static final String HTML_OPEN_PARAGRAPH="<p>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_PARAGRAPH="</p>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_OPEN_PARAGRAPH = "</p><p>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_H1 = "</H1>"; //$NON-NLS-1$
    private static final String HTML_OPEN_H1 = "<H1>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_H2 = "</H2>"; //$NON-NLS-1$
    private static final String HTML_OPEN_H2 = "<H2>"; //$NON-NLS-1$
    private static final String HTML_OPEN_LIST_ITEM = "<li>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_LIST_ITEM = "</li>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_TABLE_DOWN_ROW = "</td></tr>"; //$NON-NLS-1$
    private static final String HTML_BR = "<br>"; //$NON-NLS-1$
    
    private static final int MILLIS_PER_SECOND = 1000;
    
    
    private Map<String, BpThreat> addedThreats = new HashMap<>();
    private Map<String, BpRequirement> addedReqs = new HashMap<>();
    private Map<String, BpRequirementGroup> addedModules = new HashMap<>();
    
    static {
        processIdentifierPrefixes = new HashSet<>();
        processIdentifierPrefixes.addAll(Arrays.asList(new String[] {
                "CON",
                "DER",
                "ISMS",
                "OPS",
                "ORP"
                
        }));
        systemIdentifierPrefixes = new HashSet<>();
        systemIdentifierPrefixes.addAll(Arrays.asList(new String[] {
                "APP",
                "IND",
                "INF",
                "NET",
                "SYS"
        }));
    }
    
    public BpImporter(String xmlRoot) {
        this.xmlRootDirectory = xmlRoot;
    }
    
    
    public void run() throws CreateBPElementException {
        long startImport = System.currentTimeMillis();
        Set<Document> modules = new HashSet<>();
        Set<ITBP2VNA.generated.threat.Document> threats = new HashSet<>();
        Set<ITBP2VNA.generated.implementationhint.Document> implementationHints = new HashSet<>();
        if (xmlRootDirectory == null || xmlRootDirectory.length() == 0) {
           LOG.error("Wrong number of arguments, please provide root-Directory to XML-Archive");
           return;
        } else
            setupImportProcess(modules, threats, implementationHints);
        LOG.debug("Successfully parsed modules:\t" + modules.size());
        LOG.debug("Successfully parsed threats:\t" + threats.size());
        LOG.debug("Successfully parsed implementation hints:\t" + implementationHints.size());
        
        long veryBeginning = System.currentTimeMillis();
        prepareITNetwork();
        long itnetworkReady = System.currentTimeMillis();
        LOG.debug("ITNetwork prepared, took :\t" + (itnetworkReady - veryBeginning) / MILLIS_PER_SECOND) ;
        generateElementalThreads(threats);
        long elementalThreadsReady = System.currentTimeMillis();
        LOG.debug("Elementalthreats ready, took :\t" + (elementalThreadsReady - itnetworkReady) / MILLIS_PER_SECOND );
        transferModules(modules);
        long modulesReady = System.currentTimeMillis();
        LOG.debug("Modules ready, took :\t" + (modulesReady - elementalThreadsReady) / MILLIS_PER_SECOND );
        LOG.debug("Transformation of elements complete");
        createSafeguards(implementationHints);
        long safeguardsReady = System.currentTimeMillis();
        LOG.debug("Safeguards ready, took:\t" + (safeguardsReady - modulesReady )/ MILLIS_PER_SECOND );
        updateElement(getRootItNetwork());
        LOG.debug("ItNetwork updated");
        LOG.debug("Import finished, took:\t" + (System.currentTimeMillis() - startImport )/ MILLIS_PER_SECOND );
    }


    /**
     * 
     * 
     * @param modules
     * @param threats
     * @param implementationHints
     */
    private void setupImportProcess(Set<Document> modules, Set<ITBP2VNA.generated.threat.Document> threats, Set<ITBP2VNA.generated.implementationhint.Document> implementationHints) {
        File rootDir = new File(xmlRootDirectory);
        if (rootDir.exists() && rootDir.isDirectory()) {
            File[] directories = rootDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            File[] subDirectories = determineSubdirectories(directories);
            File moduleDir = subDirectories[0];
            File threatDir = subDirectories[1];
            File implHintDir = subDirectories[2];
            File mediaDir = subDirectories[3];

            parseBSIXml(modules, threats, implementationHints, moduleDir, threatDir, implHintDir);

        }
    }


    /**
     * parses XML-Files in given Subdirectories of BSI-XML to
     * 
     * - {@link Document} into {@link Set} modules
     * - {@link ITBP2VNA.generated.threat.Document} into {@link Set} threats
     * - {@link ITBP2VNA.generated.implementationhint.Document} into {@link Set} implementationHints
     * 
     * @param modules
     * @param threats
     * @param implementationHints
     * @param moduleDir
     * @param threatDir
     * @param implHintDir
     */
    private void parseBSIXml(Set<Document> modules, Set<ITBP2VNA.generated.threat.Document> threats, Set<ITBP2VNA.generated.implementationhint.Document> implementationHints, File moduleDir, File threatDir, File implHintDir) {
        for (File xmlFile : getXMLFiles(moduleDir)) {
            modules.add(ITBPParser.getInstance().parseModule(xmlFile));
        }
        for (File xmlFile : getXMLFiles(threatDir)) {
            threats.add(ITBPParser.getInstance().parseThread(xmlFile));
        }
        for (File xmlFile : getXMLFiles(implHintDir)) {
            implementationHints.add(ITBPParser.getInstance().parseImplementationHint(xmlFile));
        }
    }
    
    /**
     * returns an array of {@link File} of a length of 4
     * the array contains the subfolders of the BSI IT Baselineprotection
     * represented in xml-Files organized in the following structure:
     * 
     * 0 - module Subdirectory
     * 1 - threat Subdirectory
     * 2 - implementation Subdirectory
     * 3 - media Subdirectory
     * 
     * @param directories
     * @return
     */
    private File[] determineSubdirectories(File[] directories) {
        File moduleDir = null;
        File threatDir = null;
        File implHintDir = null;
        File mediaDir = null;
        
        final String warningMoreThanOneDirectory = "more than one directory named:\t"; 

        File[] dirs = new File[4];
        for (File subDirectory : directories) {
            if (SUBDIRECTORY_IMPL_HINTS.equals(subDirectory.getName())) {
                if (implHintDir != null) {
                    LOG.warn(warningMoreThanOneDirectory + SUBDIRECTORY_IMPL_HINTS);
                }
                dirs[2] = subDirectory;
                
            } else if (SUBDIRECTORY_MEDIA.equals(subDirectory.getName())) {
                if (mediaDir != null) {
                    LOG.warn(warningMoreThanOneDirectory + SUBDIRECTORY_MEDIA);
                }
                dirs[3] = subDirectory;                        
            } else if (SUBDIRECTORY_MODULES.equals(subDirectory.getName())) {
                if (moduleDir != null) {
                    LOG.warn(warningMoreThanOneDirectory + SUBDIRECTORY_MODULES);
                }
                dirs[0] = subDirectory;
                
            } else if (SUBDIRECTORY_THREATS.equals(subDirectory.getName())) {
                if (threatDir != null) {
                    LOG.warn(warningMoreThanOneDirectory + SUBDIRECTORY_THREATS);
                }
                dirs[1] = subDirectory;
            }
                
        }
        return dirs;
        
    }
    
    /**
     * update a given {@link CnATreeElement} to write changes to db
     * 
     * @param element
     * @return
     * @throws CreateBPElementException
     */
    private CnATreeElement updateElement(CnATreeElement element) throws CreateBPElementException{
        try {
            UpdateElement<CnATreeElement> command = new UpdateElement<CnATreeElement>(
                    element, true, ChangeLogEntry.STATION_ID);
            return getCommandService().executeCommand(command).getElement();
        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error creating root-it-network");
        }
        
    }

    /**
     * get all xmlFiles contained in a given Directory represented by a {@link File}
     * 
     * @param dir
     * @return
     */
    private List<File> getXMLFiles(File dir){
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] directories = dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return FilenameUtils.isExtension(pathname.getName(), "xml");
                }
            });
            return Arrays.asList(directories);
        } else {
            return new ArrayList<File>();
        }
    }
    
    
    /**
     * creates an {@link ItNetwork} and its substructure to prepare it for 
     * transforming the bsi-data (xml) into verinice Objects
     * 
     * @throws CreateBPElementException
     */
    private void prepareITNetwork() throws CreateBPElementException {

        BpRequirementGroup rootReqGroup = getRootReqGroup();
        if (rootReqGroup == null) {
            rootReqGroup =  (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID, 
                    getRootItNetwork(), Messages.ROOT_REQUIREMENT_GROUP_NAME);
        }

        systemReqGroup = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID, 
                rootReqGroup, Messages.SYSTEM_REQUIREMENT_GROUP_NAME);

        processReqGroup = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID, 
                rootReqGroup, Messages.PROCESS_REQUIREMENT_GROUP_NAME); 

        BpThreatGroup rootThreatGroup = null;
        SafeguardGroup safeguardRootGroup = null;
        for (CnATreeElement child : getRootItNetwork().getChildren()) {
            if (BpThreatGroup.TYPE_ID.equals(child.getTypeId())) {
                if (rootThreatGroup != null) {
                    LOG.warn("Found more than one root-Threat-Group");
                }
                rootThreatGroup = (BpThreatGroup) child;
            } else if (SafeguardGroup.TYPE_ID.equals(child.getTypeId())) {
                if (safeguardRootGroup != null) {
                    LOG.warn("Found more than one root-Requirement-Group");
                }
                safeguardRootGroup = (SafeguardGroup) child;                
            }
        } 

        rootThreatGroup.setTitel(Messages.ROOT_THREAT_GROUP_NAME);


        createStructuredSubGroups(rootThreatGroup, safeguardRootGroup);

    }
    
    private BpRequirementGroup getRootReqGroup() throws CreateBPElementException {
        for (CnATreeElement element : getRootItNetwork().getChildren()) {
            if ( BpRequirementGroup.TYPE_ID.equals(element.getTypeId()) &&  
                    Messages.ROOT_REQUIREMENT_GROUP_NAME.equals(element.getTitle()) ) {
                return (BpRequirementGroup) element;
            }
        }
        return null;
    }


    /**
     * 
     * creates all Groups, necessary to represent the BSI-XML in a structured way in verinice
     * 
     * @param rootThreatGroup
     * @param safeguardRootGroup
     * @throws CreateBPElementException
     */
    private void createStructuredSubGroups(BpThreatGroup rootThreatGroup, 
            SafeguardGroup safeguardRootGroup) throws CreateBPElementException {
        elementalThreatGroup = (BpThreatGroup) createElement(BpThreatGroup.TYPE_ID, 
                rootThreatGroup, Messages.ELEMENTAL_THREAT_GROUP_NAME);
        processSafeguardGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID,
                safeguardRootGroup, Messages.PROCESS_REQUIREMENT_GROUP_NAME);
        systemSafeguardGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID, 
                safeguardRootGroup, Messages.SYSTEM_REQUIREMENT_GROUP_NAME);



        for (String name : systemIdentifierPrefixes ) {
            createElement(BpRequirementGroup.TYPE_ID, systemReqGroup, name);
            createElement(SafeguardGroup.TYPE_ID, systemSafeguardGroup,  name);
        }


        for (String name : processIdentifierPrefixes ) {
            createElement(BpRequirementGroup.TYPE_ID, processReqGroup, name);
            createElement(SafeguardGroup.TYPE_ID, processSafeguardGroup, name);
        }
    }  

    /**
     * takes the XML-Files (represented by
     *  {@link ITBP2VNA.generated.implementationhint.Document} ) that 
     * are representing the BSI Baseline-Protection-Compendium 
     * implementation-Hints and transforms them into 
     * verinice-Objects {@link Safeguard}
     * 
     * @param implementationHints
     * @throws CreateBPElementException
     */
    private void createSafeguards(Set<ITBP2VNA.generated.implementationhint.Document> 
        implementationHints) throws CreateBPElementException {
        
        Set<SafeguardGroup> subGroups = new HashSet<>(10);
        
        for (CnATreeElement child : systemSafeguardGroup.getChildren()) {
            if (SafeguardGroup.TYPE_ID.equals(child.getTypeId())) {
                subGroups.add((SafeguardGroup)child );
            }
        }
        
        for (CnATreeElement child : processSafeguardGroup.getChildren()) {
            if (SafeguardGroup.TYPE_ID.equals(child.getTypeId())) {
                subGroups.add((SafeguardGroup)child );
            }
        }
        
        for (ITBP2VNA.generated.implementationhint.Document bsiSafeguard : implementationHints) {
            SafeguardGroup safeGuardParent = null;
            for (SafeguardGroup candidate : subGroups) {
                if (candidate.getTitle().startsWith(getIdentifierPrefix(bsiSafeguard.getIdentifier()))) {
                    safeGuardParent = candidate;
                }
            }

            if (safeGuardParent != null) {
                createSafeguardsForModule(bsiSafeguard, safeGuardParent);
            } else {
                LOG.warn("Could not determine parent for :\t" + bsiSafeguard.getTitle());
            }
        }
        
        
    }
    
    private Set<Link> linkSafeguardToRequirements(Safeguard safeguard) {
        Set<Link> links = new HashSet<>();
        LOG.debug("searching Requirement-Links for Safeguard:\t" + 
                safeguard.getTitle() + "\t with Identifier:\t" + safeguard.getIdentifier());
        String groupIdentifier = getIdentifierPrefix(safeguard.getIdentifier());
        LOG.debug("GroupIdentifier:\t" + groupIdentifier);
        BpRequirementGroup parent = getRequirementParentGroup(groupIdentifier);
        LOG.debug("Parent:\t" + parent.getTitle());
        String safeguardIdentifier = safeguard.getIdentifier();
        String comparableIdentifier = safeguardIdentifier.replace('M', 'A');
        for (CnATreeElement requirement : parent.getChildren()) {
            if (requirement instanceof BpRequirement) {
                LOG.debug("Child is Requirement:\t" + requirement.getTitle() 
                    + " with identifier:\t" + ((BpRequirement)requirement).getIdentifier());
                if (((BpRequirement)requirement).getIdentifier().equals(comparableIdentifier)){
                    links.add(new Link((BpRequirement)requirement, safeguard, 
                            BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, ""));
                }
            } else if (requirement instanceof BpRequirementGroup) {
                LOG.debug("child is RequirementGroup :\t" + requirement.getTitle());
                for (CnATreeElement child : requirement.getChildren()) {
                    if (child instanceof BpRequirement) {
                        LOG.debug("child is grandchild:\t" + child.getTitle() + " with identifier:\t" 
                                + ((BpRequirement)child).getIdentifier());
                        if (((BpRequirement)child).getIdentifier().equals(comparableIdentifier)){
                            links.add(new Link(((BpRequirement)child), safeguard, 
                                    BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, ""));
                        }
                    }
                }
            }
        }
        return links;
    }
    
    private CnATreeElement createElement(String typeId, CnATreeElement parent, String title) throws CreateBPElementException {
        CreateElement<CnATreeElement> command = new CreateElement<>(parent, typeId, title);
        try {
            return getCommandService().executeCommand(command).getNewElement();
        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error creating BP-Element:\t" + title 
                    + " in container:\t" + parent.getTitle() + " of type " + typeId + " failed");
        }
    }
    
    private void transferModules(Set<Document> modules) throws CreateBPElementException {

        for (Document bsiModule : modules) {
            if (rootNetwork != null) {
                String groupIdentifier = getIdentifierPrefix(bsiModule.getIdentifier());
                
                BpRequirementGroup parent = getRequirementParentGroup(groupIdentifier);
                
                BpRequirementGroup veriniceModule = null;
                
                if (! addedModules.containsKey(bsiModule.getIdentifier())) {
                    veriniceModule = createModule(bsiModule, parent);
                    linkElementalThreats(bsiModule);
                    addedModules.put(bsiModule.getIdentifier(), veriniceModule);
                } else {
                    veriniceModule = addedModules.get(bsiModule.getIdentifier());
                }
                
            }
        }
    }


    /**
     * transform a single given {@link Document} into a {@link BpRequirementGroup}
     * 
     * @param bsiModule
     * @param parent
     * @return
     * @throws CreateBPElementException
     */
    private BpRequirementGroup createModule(Document bsiModule, BpRequirementGroup parent) throws CreateBPElementException {
        BpRequirementGroup veriniceModule = null;
        if (parent != null) {
            veriniceModule = (BpRequirementGroup)createElement(BpRequirementGroup.TYPE_ID, 
                    parent, bsiModule.getFullTitle());

            veriniceModule.setIdentifier(bsiModule.getIdentifier());
//                veriniceModule.setObjectBrowserDescription(getModuleDescriptionText(bsiModule.getFullTitle(), 
//                        bsiModule.getDescription()));
            veriniceModule.setObjectBrowserDescription(getCompleteModuleXMLText(bsiModule));
            veriniceModule.setLastChange(getBSIDate(bsiModule.getLastChange()));
//                veriniceModule.setMainResponsibleRole(bsiModule.X );
            LOG.debug("Module : \t" + veriniceModule.getTitle()+ " created");
            createRequirementsForModule(bsiModule, veriniceModule);
        }
        return veriniceModule;
    }
    
    private String getCompleteModuleXMLText(Document module) {
        StringBuilder descriptionBuilder = new StringBuilder();
        
        descriptionBuilder.append(getModuleDescriptionStart(module));
        
        descriptionBuilder.append(HTML_CLOSE_OPEN_PARAGRAPH);
        
        for (Object o : module.getThreatScenario().getDescription()) {
            if (o instanceof ITBP2VNA.generated.module.Description) {
                ITBP2VNA.generated.module.Description desc = (ITBP2VNA.generated.module.Description) o;
                descriptionBuilder.append(getAnyElementDescription("", 0, desc.getAny()));
            } 
                
        }
        
        
        descriptionBuilder.append(HTML_BR);
        
        SpecificThreats specificThreats = module.getThreatScenario().getSpecificThreats();
        
        for (SpecificThreat specificThreat : specificThreats.getSpecificThreat()) {
            descriptionBuilder.append(getAnyElementDescription(specificThreat.getHeadline(),
                    1, specificThreat.getDescription().getAny()));
            descriptionBuilder.append(HTML_CLOSE_OPEN_PARAGRAPH);
        }
        
        for (Object o : module.getRequirements().getDescription()) {
            if (o instanceof Description) {
                descriptionBuilder.append(HTML_OPEN_PARAGRAPH);
                descriptionBuilder.append("<b>");
                Description desc = (Description)o;
                descriptionBuilder.append(getAnyElementDescription("", 0, desc.getAny()));
                descriptionBuilder.append("</b>");
                descriptionBuilder.append(HTML_CLOSE_PARAGRAPH);
            }
        }
//        descriptionBuilder.append(getAnyObjectDescription("", 1, module.getRequirements().getDescription()));
        
        descriptionBuilder.append(HTML_CLOSE_OPEN_PARAGRAPH);
        
        descriptionBuilder.append(Messages.MAIN_RESPONSIBLE +  ":\t" + 
                module.getRequirements().getMainResponsibleRole());
        
        descriptionBuilder.append(HTML_CLOSE_OPEN_PARAGRAPH);

        List<String> roles = null;
        if (module.getRequirements().getFurtherResponsibleRoles() != null) {
            roles = module.getRequirements().getFurtherResponsibleRoles().getRole();
        }
        
        if (roles != null && !roles.isEmpty()) {
            descriptionBuilder.append(HTML_CLOSE_OPEN_PARAGRAPH);
            descriptionBuilder.append(Messages.FURTHER_RESPONSIBLES + ":");
            descriptionBuilder.append(HTML_OPEN_UL);

            for (String role : roles) {
                descriptionBuilder.append(HTML_OPEN_LIST_ITEM)
                    .append(role).append(HTML_CLOSE_LIST_ITEM);    
            }

            descriptionBuilder.append(HTML_CLOSE_OPEN_PARAGRAPH);
        }
        
        descriptionBuilder.append("<H1><b>" + Messages.BASIC_REQUIREMENTS + "</b></H1>");
        descriptionBuilder.append(getModuleRequirementDescription(module.getRequirements()
                .getBasicRequirements().getRequirement()));
        
        descriptionBuilder.append(HTML_CLOSE_OPEN_PARAGRAPH);
        
        descriptionBuilder.append(Messages.STANDARD_REQUIREMENTS);
        descriptionBuilder.append(getModuleRequirementDescription(module.getRequirements()
                .getStandardRequirements().getRequirement()));        
        
        descriptionBuilder.append(HTML_CLOSE_OPEN_PARAGRAPH);
        
        descriptionBuilder.append(Messages.HIGH_REQUIREMENTS);
        descriptionBuilder.append(getModuleRequirementDescription(module.getRequirements()
                .getHighLevelRequirements().getRequirement()));        
        
        descriptionBuilder.append(HTML_CLOSE_OPEN_PARAGRAPH);
        
        descriptionBuilder.append(Messages.ELEMENTAL_THREAT_GROUP_NAME + HTML_OPEN_UL);
        for(String threat : module.getElementalThreats().getElementalThreat()){
            descriptionBuilder.append(HTML_OPEN_LIST_ITEM)
                .append(threat).append(HTML_CLOSE_LIST_ITEM);
        }
        descriptionBuilder.append(HTML_CLOSE_UL);
        
        descriptionBuilder.append(HTML_CLOSE_PARAGRAPH);
        
        descriptionBuilder.append(ToHtmlTableTransformer.createCrossreferenceTable(module.getCrossreferences()));
        
        descriptionBuilder.append(HTML_OPEN_PARAGRAPH);
        
        if (module.getBibliography() != null) {

            descriptionBuilder.append(HTML_OPEN_H1).append("Literatur").append(HTML_CLOSE_H1);
            descriptionBuilder.append(HTML_OPEN_UL);



            for(BibItem bibItem :  module.getBibliography().getBibItem() ) {
                descriptionBuilder.append(HTML_OPEN_LIST_ITEM);
                descriptionBuilder.append(getAnyElementDescription(bibItem.getTitle(), 1, bibItem.getDescription().getAny()));
                descriptionBuilder.append(bibItem.getShortHand()).append(HTML_BR);
                descriptionBuilder.append(HTML_CLOSE_LIST_ITEM);
            }

            descriptionBuilder.append(HTML_CLOSE_UL);
            descriptionBuilder.append(HTML_CLOSE_PARAGRAPH);
        }
        
        return descriptionBuilder.toString();
        
        
    }


    /**
     * @param module
     * @param descriptionBuilder
     */
    private String getModuleDescriptionStart(Document module) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN_H1);
        sb.append(module.getFullTitle());
        sb.append(HTML_CLOSE_H1);
        sb.append(HTML_OPEN_PARAGRAPH);
        
        
        sb.append(getAnyObjectDescription("", 0, 
                module.getDescription().getIntroduction()));
        sb.append(getAnyObjectDescription("", 0, 
                module.getDescription().getPurpose()));
        sb.append(getAnyObjectDescription("", 0, 
                module.getDescription().getDifferentiation()));
        
        sb.append(HTML_CLOSE_OPEN_PARAGRAPH);
        
        return sb.toString();
    }
    
    public String getModuleRequirementDescription(List<Requirement> requirements) {
        StringBuilder sb = new StringBuilder();
        
        for (Requirement requirement : requirements) {
            sb.append(getRequirementDescriptionStart(requirement));
            
            sb.append(getRequirementResponsibleDescription(requirement));
            
            sb.append(getRequirementCIATable(requirement));
            
            sb.append(HTML_OPEN_PARAGRAPH);
            sb.append(getAnyElementDescription("", 0, requirement.getDescription().getAny()));
            sb.append(HTML_CLOSE_PARAGRAPH);
        }
        
        return sb.toString();
    }


    /**
     * @param sb
     * @param requirement
     */
    private String getRequirementDescriptionStart(Requirement requirement) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN_PARAGRAPH);
        sb.append(HTML_OPEN_H2);
        sb.append(requirement.getIdentifier());
        sb.append(" ");
        sb.append(requirement.getTitle());
        sb.append(HTML_CLOSE_H2);
        sb.append(HTML_CLOSE_PARAGRAPH);
        return sb.toString();
    }


    /**
     * @param sb
     * @param requirement
     */
    private String getRequirementResponsibleDescription(Requirement requirement) {
        StringBuilder sb = new StringBuilder();
        if(requirement.getResponsibleRoles() != null &&
                requirement.getResponsibleRoles().getRole().isEmpty()) {
            sb.append(Messages.RESPONSIBLES).append(":").append(HTML_OPEN_UL);
            for (String role : requirement.getResponsibleRoles().getRole()) {
                sb.append(HTML_OPEN_LIST_ITEM).append(role).append(HTML_CLOSE_LIST_ITEM);
            }
            sb.append(HTML_CLOSE_UL);
        }
        return sb.toString();
    }


    /**
     * @param sb
     * @param requirement
     */
    private String getRequirementCIATable(Requirement requirement) {
        StringBuilder sb = new StringBuilder();
        String confidentiality = (Boolean.parseBoolean(requirement.getCia().getConfidentiality())) ? "X" : "-";
        String integrity = (Boolean.parseBoolean(requirement.getCia().getIntegrity())) ? "X" : "-";
        String availitbility = (Boolean.parseBoolean(requirement.getCia().getAvailability())) ? "X" : "-";
        
        sb.append(HTML_OPEN_TABLE);
        sb.append(HTML_OPEN_TR).append(HTML_OPEN_TD);
        sb.append(Messages.CIA_AFFECTS);
        sb.append(HTML_CLOSE_TD).append(HTML_OPEN_TD);
        sb.append(Messages.CIA_AFFECTS_YES);
        sb.append(" / ");
        sb.append(Messages.CIA_AFFECTS_NO);
        sb.append(HTML_CLOSE_TD).append(HTML_CLOSE_TR);
        sb.append(HTML_OPEN_TR).append(HTML_OPEN_TD);
        sb.append(Messages.CIA_AFFECTS_CONFIDENTIALITY);
        sb.append(HTML_CLOSE_TD).append(HTML_OPEN_TD);
        sb.append(confidentiality).append(HTML_CLOSE_TABLE_DOWN_ROW);
        sb.append(HTML_OPEN_TR).append(HTML_OPEN_TD);
        sb.append(Messages.CIA_AFFECTS_INTEGRITY).append(HTML_CLOSE_TD).append(HTML_OPEN_TD);
        sb.append(integrity).append(HTML_CLOSE_TD).append(HTML_CLOSE_TABLE_DOWN_ROW);
        sb.append(HTML_OPEN_TR).append(HTML_OPEN_TD);
        sb.append(Messages.CIA_AFFECTS_AVAILABILITY);
        sb.append(HTML_CLOSE_TD).append(HTML_OPEN_TD);
        sb.append(availitbility)
            .append(HTML_CLOSE_TD)
            .append(HTML_CLOSE_TABLE_DOWN_ROW);
        sb.append(HTML_CLOSE_TABLE);
        return sb.toString();
    }


    /**
     * @param bsiModule
     * @throws CreateBPElementException
     */
    private void linkElementalThreats(Document bsiModule) throws CreateBPElementException {
        List<Link> linkList = new ArrayList<>();
        for (RequirementRef reqRef : bsiModule.getCrossreferences().getRequirementRef()) {
            String reqIdentifier = reqRef.getIdentifier();
            BpRequirement requirement = getRequirementByIdentifier(reqIdentifier);
            for (ElementalthreatRef elementalThreatReference : reqRef.getElementalthreatRef()) {
                String isReferenced = elementalThreatReference.getIsReferenced();
                String threatIdentifier = elementalThreatReference.getIdentifier();
                BpThreat threat = getElementalThreadByIdentifier(threatIdentifier);
                
                if (Boolean.parseBoolean(isReferenced)) {
                    Link link = new Link(requirement, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT, "");
                    linkList.add(link);
                }
            }
        }
        CreateMultipleLinks linkCommand = new CreateMultipleLinks(linkList);
        try {
            getCommandService().executeCommand(linkCommand);
        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error creating links");
        }
    }

    
    private BpThreat getElementalThreadByIdentifier(String identifier) {
        if (addedThreats.containsKey(identifier)) {
            return addedThreats.get(identifier);
        } else {
            LOG.error("Could not find threat with id:\t" + identifier);
            return null;
        }
    }
    
    private BpRequirement getRequirementByIdentifier(String identifier) {
        if (addedReqs.containsKey(identifier)) {
            return addedReqs.get(identifier);
        } else {
            LOG.error("Could not find requirement with id:\t" + identifier);
            return null;            
        }
    }
    
    private BpRequirementGroup getModuleByIdentifier(String identifier) {
        if(addedModules.containsKey(identifier) ) {
            return addedModules.get(identifier);
        } else {
            LOG.error("Could not find module with id:\t" + identifier);
            return null;            
        }
    }
    
    private void generateElementalThreads(Set<ITBP2VNA.generated.threat.Document> threats) throws CreateBPElementException {
        for (ITBP2VNA.generated.threat.Document bsiThreat : threats) {
            if (! addedThreats.containsKey(bsiThreat.getIdentifier())) {
                BpThreat veriniceThreat = (BpThreat) createElement(BpThreat.TYPE_ID, 
                        elementalThreatGroup, bsiThreat.getFullTitle() );
                veriniceThreat.setTitel(bsiThreat.getFullTitle());
                
                veriniceThreat.setIdentifier(bsiThreat.getIdentifier());
                String plainDescription = getAnyObjectDescription(bsiThreat.getFullTitle(), 1, bsiThreat.getDescription());
                veriniceThreat.setConfidentiality(Boolean.parseBoolean(bsiThreat.getCia().getConfidentiality()));
                veriniceThreat.setIntegrity(Boolean.parseBoolean(bsiThreat.getCia().getIntegrity()));
                veriniceThreat.setAvailibility(Boolean.parseBoolean(bsiThreat.getCia().getAvailability()));
                
                if (plainDescription != null && plainDescription.length() > 0) {
                    veriniceThreat.setObjectBrowserDescription(plainDescription);
                }
                veriniceThreat = (BpThreat) updateElement(veriniceThreat);
                addedThreats.put(bsiThreat.getIdentifier(), veriniceThreat);
                
                LOG.debug("Threat : \t" + veriniceThreat.getTitle()+ " created");
            }
        }
    }
    
    private String getElementalThreatDescription(String title, String description) {
        StringBuilder sb = new StringBuilder();
        if (! "".equals(title)) {
            sb.append(HTML_OPEN_H1).append(title).append(HTML_CLOSE_H1);
        }
        sb.append(description);
        return sb.toString();
    }
    
    /**
     * @param groupIdentifier
     */
    private BpRequirementGroup getRequirementParentGroup(String groupIdentifier) {
        BpRequirementGroup group = null;
        
        if (systemIdentifierPrefixes.contains(groupIdentifier)) {
            
            for (CnATreeElement reqGroup : systemReqGroup.getChildren()) {
                if (reqGroup.getTypeId().equals(BpRequirementGroup.TYPE_ID) 
                        && reqGroup.getTitle().equals(groupIdentifier)) {
                    group = (BpRequirementGroup) reqGroup;
                    break;
                }
            }
            
        } else if (processIdentifierPrefixes.contains(groupIdentifier)) {
            for (CnATreeElement reqGroup : processReqGroup.getChildren()) {
                if (reqGroup.getTypeId().equals(BpRequirementGroup.TYPE_ID) 
                        && reqGroup.getTitle().equals(groupIdentifier)) {
                    group = (BpRequirementGroup) reqGroup;
                    break;
                }
            }
            
        }
        return group;
    }
    
    private String getIdentifierPrefix(String id) {
        if (id != null && id.length() >= 3 && id.contains(".")) {
            return id.substring(0, id.indexOf('.'));
        } else return id;
    }

    /**
     * 
     * create Safeguards according to their level-definition ( BASIC, STANDARD, HIGH)
     * and links them to the {@link BpRequirement} defined in the references module {@link BpRequirementGroup} 
     * 
     * @param bsiModule
     * @param parent
     * @throws CommandException
     * @throws CnATreeElementBuildException
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private void createSafeguardsForModule(ITBP2VNA.generated.implementationhint.Document bsiSafeguardDocument,
            SafeguardGroup parent) 
            throws CreateBPElementException {
        List<Link> links = new ArrayList<>();
        Safeguards bsiModule = bsiSafeguardDocument.getSafeguards();
        List<ITBP2VNA.generated.implementationhint.Safeguard> safeGuards = 
                bsiModule.getBasicSafeguards().getSafeguard();
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : safeGuards) {
            Safeguard safeguard = createSafeguard(parent, bsiSafeguard, Messages.QUALIFIER_BASIC,
                    bsiSafeguardDocument.getLastChange().toString());
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
                links.addAll(linkSafeguardToElementalThreat(
                        bsiSafeguard.getIdentifier(), safeguard));
            } else {
                LOG.warn("Could not create Safeguard:\t" + bsiSafeguard.getTitle());
            }
        }
        safeGuards.clear();
        safeGuards = bsiModule.getStandardSafeguards().getSafeguard();
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : safeGuards) {
            Safeguard safeguard = createSafeguard(parent, bsiSafeguard, Messages.QUALIFIER_STANDARD,
                    bsiSafeguardDocument.getLastChange().toString());
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
                links.addAll(linkSafeguardToElementalThreat(
                        bsiSafeguard.getIdentifier(), safeguard));
            } else {
                LOG.warn("Could not create Safeguard:\t" + bsiSafeguard.getTitle());
            }        
        }
        safeGuards.clear();
        safeGuards = bsiModule.getHighLevelSafeguards().getSafeguard();
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : safeGuards) {
            Safeguard safeguard = createSafeguard(parent, bsiSafeguard, Messages.QUALIFIER_HIGH,
                    bsiSafeguardDocument.getLastChange().toString());
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
                links.addAll(linkSafeguardToElementalThreat(
                        bsiSafeguard.getIdentifier(), safeguard));
            } else {
                LOG.warn("Could not create Safeguard:\t" + bsiSafeguard.getTitle());
            }
        }
        
        
        
        CreateMultipleLinks linkCommand = new CreateMultipleLinks(links);
        try {
            getCommandService().executeCommand(linkCommand);
        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error creating links for safeguards");
        }
    }
    
    private Set<Link> linkSafeguardToElementalThreat(String bsiSafeguardIdentifier, 
            Safeguard vSafeguard){
        Set<Link> links = new HashSet<>();
        
        String firstPart = bsiSafeguardIdentifier.substring(0, 
                bsiSafeguardIdentifier.lastIndexOf('.'));
        String secondPart = bsiSafeguardIdentifier.substring(
                bsiSafeguardIdentifier.lastIndexOf('.'));
        secondPart = secondPart.replace('M', 'A');
        StringBuilder sb = new StringBuilder();
        sb.append(firstPart).append(secondPart);
        BpRequirement requirement = getRequirementByIdentifier(sb.toString());
        if ( requirement != null) {
            for (CnALink link : requirement.getLinksDown()) {
                if (link.getDependency() instanceof BpThreat) {
                    links.add(new Link(vSafeguard, link.getDependency(), 
                            Safeguard.REL_BP_SAFEGUARD_BP_THREAT, ""));
                } 
            }
        }
        
        return links;
        
        
    }
    
    /**
     * transform a single {@link ITBP2VNA.generated.implementationhint.Safeguard} to a {@link Safeguard}
     * and sets all possible properties
     * 
     * @param parent
     * @param bsiSafeguard
     * @param qualifier
     * @return
     * @throws CreateBPElementException
     */
    @SuppressWarnings("unused")
    private Safeguard createSafeguard(SafeguardGroup parent, 
            ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard, 
            String qualifier, String lastChange) 
                    throws CreateBPElementException {
        final String trueValue = "true";
        if( parent != null) {
            Safeguard safeguard = (Safeguard) createElement(Safeguard.TYPE_ID, 
                    parent, bsiSafeguard.getTitle());
            safeguard.setAbbreviation(bsiSafeguard.getIdentifier());
            safeguard.setIdentifier(bsiSafeguard.getIdentifier());
            String plainDescription = getAnyObjectDescription(bsiSafeguard.getTitle(), 1,
                    bsiSafeguard.getDescription().getContent());
            String htmlDescription = plainDescription;
            if (plainDescription != null && plainDescription.length() > 0) {
                safeguard.setObjectBrowserDescription(htmlDescription);
            } else {
                LOG.debug("No description found for:\t" + bsiSafeguard.getTitle());
            }
            safeguard.setQualifier(qualifier);
            safeguard.setTitle(bsiSafeguard.getTitle());
            safeguard.setLastChange(getBSIDate(lastChange));
            safeguard.setIsAffectsConfidentiality(trueValue.equals(
                    bsiSafeguard.getCia().getConfidentiality())? true : false);
            safeguard.setIsAffectsAvailability(trueValue.equals(
                    bsiSafeguard.getCia().getAvailability())? true : false);
            safeguard.setIsAffectsIntegrity(trueValue.equals(
                    bsiSafeguard.getCia().getIntegrity())? true : false);
            
            if (bsiSafeguard.getResponsibleRoles() != null) {
                for ( String role : bsiSafeguard.getResponsibleRoles().getRole()) {
                    safeguard.addResponsibleRole(role);
                }
            }
            
            LOG.debug("Safeguard : \t"  + safeguard.getTitle() + "created ");

            return (Safeguard) updateElement(safeguard);
        }
        return null;
    }


    /**
     * @param plainDescription
     * @return
     */


    private SafeguardGroup getSafeguardParent(SafeguardGroup rootGroup,
            String identifier) throws CreateBPElementException {
        String moduleIdentifier = identifier.substring(0, identifier.lastIndexOf(".M"));
        String subGroupIdentifier = identifier.substring(0, identifier.indexOf('.'));
        String moduleTitle = null;
        if (addedModules.containsKey(moduleIdentifier)) {
            moduleTitle = addedModules.get(moduleIdentifier).getTitle();
        } else {
            return null;
        }
        // safeguardparent is a module
        SafeguardGroup safeguardParent = 
                (SafeguardGroup)getIBGroupByNameRecursive(rootGroup, moduleTitle);
        // moduleparent is a safeguardGroup like "APP", "DER", "INF, "CON", ...
        SafeguardGroup safeguardRoot =(SafeguardGroup) processSafeguardGroup.getParent();
        SafeguardGroup moduleParent = (SafeguardGroup)getIBGroupByNameRecursive(
                (IBpGroup) safeguardRoot, subGroupIdentifier);
        if (safeguardParent == null) {
            if(moduleParent != null) {
                safeguardParent = (SafeguardGroup)getIBGroupByNameRecursive(
                        (IBpGroup) moduleParent, moduleTitle);
                if (safeguardParent == null) {
                    safeguardParent = (SafeguardGroup)createElement(
                            SafeguardGroup.TYPE_ID, moduleParent, moduleTitle);
                }
            } else {
                safeguardParent = null;
            }
        } 
        
        return safeguardParent;
        
    }
    
    private IBpGroup getIBGroupByNameRecursive(IBpGroup rootGroup, String name) {
        IBpGroup matchingGroup = null;
        CnATreeElement element = (CnATreeElement) rootGroup;
        for (CnATreeElement child : element.getChildren()) {
            if (child instanceof IBpGroup) {
                if (name.equals(child.getTitle())) {
                    matchingGroup = (IBpGroup)child;
                } else {
                    if (matchingGroup == null || !name.equals(matchingGroup.getTitle())) {
                        matchingGroup = getIBGroupByNameRecursive((IBpGroup)child, name);
                    }
                }
            }
        }
        return matchingGroup;
    }
    
    
    /**
     * creates all {@link BpRequirement} for a given {@link Document}, 
     * adding a qualifier, given bei the list they are sorted 
     * into within the {@link Document} 
     * 
     * @param bsiModule
     * @param parent
     * @throws CreateBPElementException
     */
    private void createRequirementsForModule(Document bsiModule, BpRequirementGroup parent) throws CreateBPElementException {
        for (Requirement bsiRequirement : bsiModule.getRequirements()
                .getBasicRequirements().getRequirement()) {
            createRequirement(parent, bsiRequirement, Messages.QUALIFIER_BASIC);
        }
        for (Requirement bsiRequirement : bsiModule.getRequirements()
                .getStandardRequirements().getRequirement()) {
            createRequirement(parent, bsiRequirement, Messages.QUALIFIER_STANDARD);
        }
        for (Requirement bsiRequirement : bsiModule.getRequirements()
                .getHighLevelRequirements().getRequirement()) {
            createRequirement(parent, bsiRequirement, Messages.QUALIFIER_HIGH);
        }
        
    }

    /**
     * transforms a single given {@link Requirement} into a {@link BpRequirement}
     * and adds it to the caching-map addedReqs
     * 
     * @param parent
     * @param bsiRequirement
     * @param qualifier
     * @return
     * @throws CreateBPElementException
     */
    private BpRequirement createRequirement(BpRequirementGroup parent,
            Requirement bsiRequirement, String qualifier) throws CreateBPElementException {
        final String trueValue = "true";
        if(!addedReqs.containsKey(bsiRequirement.getIdentifier())){
            BpRequirement vRequirement = null; 
            vRequirement = (BpRequirement) createElement(BpRequirement.TYPE_ID, parent, bsiRequirement.getTitle());
            vRequirement.setIdentifier(bsiRequirement.getIdentifier());
            vRequirement.setObjectBrowserDescription(getAnyElementDescription(
                    bsiRequirement.getTitle(), 1, bsiRequirement.getDescription().getAny()));
            vRequirement.setTitle(bsiRequirement.getTitle());
            vRequirement.setLastChange(parent.getLastChange());
            vRequirement.setIsAffectsConfidentiality(trueValue.equals(
                    bsiRequirement.getCia().getConfidentiality()) ? true : false);
            vRequirement.setIsAffectsIntegrity(trueValue.equals(
                    bsiRequirement.getCia().getIntegrity()) ? true : false);
            vRequirement.setIsAffectsAvailability(trueValue.equals(
                    bsiRequirement.getCia().getAvailability()) ? true : false);
            
            
            if ( bsiRequirement.getResponsibleRoles() != null ) {

                for ( String role : bsiRequirement.getResponsibleRoles().getRole()) {
                    vRequirement.addResponsibleRole(role);
                }

            }
            vRequirement.setQualifier(qualifier);
            addedReqs.put(bsiRequirement.getIdentifier(), vRequirement);
            return (BpRequirement) updateElement(vRequirement);
        } else {
            return addedReqs.get(bsiRequirement.getIdentifier());
        }
    }
    
    private Date getBSIDate(String dateString) throws CreateBPElementException{
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            throw new CreateBPElementException(
                    "Could not parse bsiDate:\t" + dateString);
        }
    }

    
    /**
     * experimental, tries to deal with the HTML/XML-Mixture
     * @param title
     * @param anyElements
     * @return
     */
    private String getAnyElementDescription(String title, int headlineLvl, List<Element> anyElements) {
        StringBuilder sb = new StringBuilder();
        if (! "".equals(title)) {
            if (headlineLvl > 0) {
                sb.append("<H");
                sb.append(String.valueOf(headlineLvl));
                sb.append(">");
                sb.append(title).append("</H");
                sb.append(String.valueOf(headlineLvl));
                sb.append(">");
                sb.append(HTML_OPEN_PARAGRAPH);
            } else {
                sb.append(HTML_BR).append(title).append(HTML_BR);
            }
        }
        for (Object element : anyElements) {
            sb.append(extractContentFromObject(element));
        }
        
        if (! "".equals(title)) {
            sb.append(HTML_CLOSE_PARAGRAPH);
        }
        
        return sb.toString();
    }


    /**
     * @param sb
     * @param element
     */
    private String extractContentFromObject(Object element) {
        StringBuilder sb = new StringBuilder();
        if (element instanceof Element) {
            sb.append(unwrapText((Element)element));
        } else if(element instanceof String) {
            sb.append((String)element);
        } 
        return sb.toString();
    }
    
    private String getAnyObjectDescription(String title, int headlineLevel, List<Object> anyObjects) {
        StringBuilder sb = new StringBuilder();
        if (! "".equals(title)) {
            if (headlineLevel > 0) {
                sb.append("<H");
                sb.append(String.valueOf(headlineLevel));
                sb.append(">").append(title).append("</H");
                sb.append(String.valueOf(headlineLevel));
                sb.append(">");
            } else {
                sb.append(HTML_BR).append(title).append(HTML_BR);
            }
        }
        if (anyObjects != null) {
            for (Object o : anyObjects) {
                sb.append(extractContentFromObject(o));            
            }
        } else {
            LOG.debug("No Description found for :\t" + title);
        }
        
        return sb.toString();
    }
    
    private String unwrapText(Node element) {
        Set<String> blacklist = new HashSet<>();
        blacklist.add("introduction");
        blacklist.add("purpose");
        blacklist.add("differentiation");
        blacklist.add("");
        blacklist.add("#text");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Stack<String> htmlElements = new Stack<>();
            Node node = element.getChildNodes().item(i);
            String htmlFormatElement = StringUtils.isNotEmpty(node.getNodeName()) ? node.getNodeName() : "";
            if( !blacklist.contains(htmlFormatElement)) {
                sb.append("<").append(htmlFormatElement).append(">");   
                htmlElements.push(htmlFormatElement);
            }
            if (node instanceof Text) {
                sb.append(node.getNodeValue());
            } else {
                sb.append(unwrapText(node));
            }
            while (! htmlElements.isEmpty()) {
                sb.append("</").append(htmlElements.pop()).append(">");
            }
        }
        return sb.toString();
    }
    
    /**
     * extracts the Decription of a  {@link Document} and transforms it into 
     * a single html-formated string which can be shown within the Object-Browser
     * when the target verinice-Object {@link BpRequirementGroup} will be selected
     * 
     * @param title
     * @param description
     * @return
     */
    private String getModuleDescriptionText(String title, Document.Description description) {
        StringBuilder sb = new StringBuilder();
        if (! "".equals(title)) {
            sb.append(HTML_OPEN_H1).append(title).append(HTML_CLOSE_H1);
        }
        if (description != null) {
            String introduction = getAnyObjectDescription("", 0, description.getIntroduction()); 
            String purpose = getAnyObjectDescription("", 0, description.getPurpose());
            String differentiation = getAnyObjectDescription("", 0, description.getDifferentiation());
            sb.append((introduction != null) ? introduction : "<p>No Introduction</p>");
            sb.append((purpose != null) ? purpose : "<p>No Purpose</p>");
            sb.append((differentiation != null) ? differentiation : "<p>No Differentiation</p>");
            if (introduction == null || introduction.length() == 0) {
                LOG.error("No introduction in description found for :\t" + title);
            }
            if (purpose == null || purpose.length() == 0) {
                LOG.error("No purpose in description found for :\t" + title);
            }
            if (differentiation == null || differentiation.length() == 0) {
                LOG.error("No differentiation in description found for :\t" + title);
            }
        }
        return sb.toString();
    }
    
    /**
     * get the root {@link ItNetwork} and creates it at the first call
     * 
     * @return {@link ItNetwork}
     * @throws CreateBPElementException
     */
    private ItNetwork getRootItNetwork() throws CreateBPElementException {
        try { 
            LoadBpModel modelLoader = new LoadBpModel();
            modelLoader = getCommandService().executeCommand(modelLoader);
            BpModel model = modelLoader.getModel();

            if(rootNetwork == null && model != null) {
                CreateITNetwork command = new CreateITNetwork(model, ItNetwork.class, true);
                command = getCommandService().executeCommand(command);
                rootNetwork = command.getNewElement();
                StringBuilder titleBuilder = new StringBuilder();
                titleBuilder.append(Messages.IT_NETWORK_NAME);
                titleBuilder.append(" (");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateInISO = df.format(new Date());
                titleBuilder.append(dateInISO).append(" )");
                rootNetwork.setTitel(titleBuilder.toString());
                updateElement(rootNetwork);
            } 
        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error while loading BPModel"); // TODO : internationalize
        }
        return rootNetwork;
    }
    
    /**
     * @return the commandService
     */
    public ICommandService getCommandService() {
        return commandService;
    }

    /**
     * @param commandService the commandService to set
     */
    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    /**
     * @return the daoFactory
     */
    public IDAOFactory getDaoFactory() {
        return daoFactory;
    }

    /**
     * @param daoFactory the daoFactory to set
     */
    public void setDaoFactory(IDAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

}
