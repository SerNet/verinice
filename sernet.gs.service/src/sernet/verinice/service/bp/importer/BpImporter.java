package sernet.verinice.service.bp.importer;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import ITBP2VNA.generated.implementationhint.Document.Safeguards;
import ITBP2VNA.generated.module.Document;
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

    private final static Logger LOG = Logger.getLogger(BpImporter.class);
    
    private final static Set<String> processIdentifierPrefixes;
    private final static Set<String> systemIdentifierPrefixes;
    
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
    
    private final static String rootRequirementGroupName = "Bausteine/Anforderungen (GS-Kompendium";
    private final static String processRequirementGroupname = "Prozess-Bausteine";
    private final static String systemRequirementGroupname = "System-Bausteine";
    
    private final static String rootThreatGroupName = "Gefährdungen";
    private final static String elementalThreatGroupName = "Elementare Gefährdungen";
    private final static String specificThreatGroupName = "Spezifische Gefährdungen";
    private final static String specificProcessThreatGroupName = "Zu Prozess-Bausteinen";
    private final static String specificSystemThreatGroupName = "Zu System-Bausteinen";
    
    private final static String SUBDIRECTORY_MODULES = "bausteine";
    private final static String SUBDIRECTORY_MEDIA = "media";
    private final static String SUBDIRECTORY_THREATS = "elementare_gefaehrdungen_1";
    private final static String SUBDIRECTORY_IMPL_HINTS = "umsetzungshinweise";
    
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
    };
    
    
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
        LOG.debug("ITNetwork prepared, took :\t" + String.valueOf((itnetworkReady - veryBeginning) / 1000) );
        generateElementalThreads(threats);
        long elementalThreadsReady = System.currentTimeMillis();
        LOG.debug("Elementalthreats ready, took :\t" + String.valueOf((elementalThreadsReady - itnetworkReady) / 1000) );
        transferModules(modules);
        long modulesReady = System.currentTimeMillis();
        LOG.debug("Modules ready, took :\t" + String.valueOf((modulesReady - elementalThreadsReady) / 1000) );
        LOG.debug("Transformation of elements complete");
        createSafeguards(implementationHints);
        long safeguardsReady = System.currentTimeMillis();
        LOG.debug("Safeguards ready, took:\t" + String.valueOf((safeguardsReady - modulesReady )/1000) );
        updateElement(getRootItNetwork());
        LOG.debug("ItNetwork updated");
        LOG.debug("Import finished, took:\t" + String.valueOf((System.currentTimeMillis() - startImport )/1000) );
    }


    /**
     * 
     * 
     * @param modules
     * @param threats
     * @param implementationHints
     */
    private void setupImportProcess(Set<Document> modules, Set<ITBP2VNA.generated.threat.Document> threats, Set<ITBP2VNA.generated.implementationhint.Document> implementationHints) {
        {
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

        File[] dirs = new File[4];
        for (File subDirectory : directories) {
            if (SUBDIRECTORY_IMPL_HINTS.equals(subDirectory.getName())) {
                if (implHintDir != null) {
                    LOG.warn("more than one directory named:\t" + SUBDIRECTORY_IMPL_HINTS);
                }
                dirs[2] = subDirectory;
                
            } else if (SUBDIRECTORY_MEDIA.equals(subDirectory.getName())) {
                if (mediaDir != null) {
                    LOG.warn("more than one directory named:\t" + SUBDIRECTORY_MEDIA);
                }
                dirs[3] = subDirectory;                        
            } else if (SUBDIRECTORY_MODULES.equals(subDirectory.getName())) {
                if (moduleDir != null) {
                    LOG.warn("more than one directory named:\t" + SUBDIRECTORY_MODULES);
                }
                dirs[0] = subDirectory;
                
            } else if (SUBDIRECTORY_THREATS.equals(subDirectory.getName())) {
                if (threatDir != null) {
                    LOG.warn("more than one directory named:\t" + SUBDIRECTORY_THREATS);
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
            UpdateElement<CnATreeElement> command = new UpdateElement<CnATreeElement>(element, true, ChangeLogEntry.STATION_ID);
            return (CnATreeElement) getCommandService().executeCommand(command).getElement();
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
        File[] directories = dir.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File pathname) {
                return FilenameUtils.isExtension(pathname.getName(), "xml");
            }
        });
        return Arrays.asList(directories);
    }
    
    
    /**
     * creates an {@link ItNetwork} and its substructure to prepare it for 
     * transforming the bsi-data (xml) into verinice Objects
     * 
     * @throws CreateBPElementException
     */
    private void prepareITNetwork() throws CreateBPElementException {

        BpRequirementGroup rootReqGroup = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID, getRootItNetwork(), rootRequirementGroupName);

        systemReqGroup = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID, rootReqGroup, systemRequirementGroupname);

        processReqGroup = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID, rootReqGroup, processRequirementGroupname); 

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

        rootThreatGroup.setTitel(rootThreatGroupName);


        createStructuredSubGroups(rootThreatGroup, safeguardRootGroup);

    }


    /**
     * 
     * creates all Groups, necessary to represent the BSI-XML in a structured way in verinice
     * 
     * @param rootThreatGroup
     * @param safeguardRootGroup
     * @throws CreateBPElementException
     */
    private void createStructuredSubGroups(BpThreatGroup rootThreatGroup, SafeguardGroup safeguardRootGroup) throws CreateBPElementException {
        elementalThreatGroup = (BpThreatGroup) createElement(BpThreatGroup.TYPE_ID, rootThreatGroup, elementalThreatGroupName);
        BpThreatGroup specificThreatGroup = (BpThreatGroup) createElement(BpThreatGroup.TYPE_ID, rootThreatGroup, specificThreatGroupName);
        processThreatGroup = (BpThreatGroup) createElement(BpThreatGroup.TYPE_ID, specificThreatGroup, specificProcessThreatGroupName);
        systemThreatGroup = (BpThreatGroup) createElement(BpThreatGroup.TYPE_ID, specificThreatGroup, specificSystemThreatGroupName);
        processSafeguardGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID, safeguardRootGroup, processRequirementGroupname);
        systemSafeguardGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID, safeguardRootGroup, systemRequirementGroupname);



        for (String name : systemIdentifierPrefixes ) {
            createElement(BpRequirementGroup.TYPE_ID, systemReqGroup, name);
            createElement(BpThreatGroup.TYPE_ID, systemThreatGroup, name);
            createElement(SafeguardGroup.TYPE_ID, systemSafeguardGroup,  name);
        }


        for (String name : processIdentifierPrefixes ) {
            createElement(BpRequirementGroup.TYPE_ID, processReqGroup, name);
            createElement(BpThreatGroup.TYPE_ID, processThreatGroup, name);
            createElement(SafeguardGroup.TYPE_ID, processSafeguardGroup, name);
        }
    }  

    /**
     * takes the XML-Files (represented by {@link ITBP2VNA.generated.implementationhint.Document} ) that 
     * are representing the BSI Baseline-Protection-Compendium implementation-Hints
     * and transforms them into verinice-Objects {@link Safeguard}
     * 
     * @param implementationHints
     * @throws CreateBPElementException
     */
    private void createSafeguards(Set<ITBP2VNA.generated.implementationhint.Document> implementationHints) throws CreateBPElementException {
        
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
            SafeguardGroup parent = null;
            for (SafeguardGroup candidate : subGroups) {
                if (candidate.getTitle().startsWith(getIdentifierPrefix(bsiSafeguard.getIdentifier()))) {
                    parent = candidate;
                }
            }

            if (parent != null) {
                createSafeguardsForModule(bsiSafeguard, parent);
            } else {
                LOG.warn("Could not determine parent for :\t" + bsiSafeguard.getTitle());
            }
        }
        
        
    }
    
    private Set<Link> linkSafeguardToRequirements(Safeguard safeguard) throws CreateBPElementException{
        Set<Link> links = new HashSet<>();
        LOG.debug("searching Requirement-Links for Safeguard:\t" + safeguard.getTitle() + "\t with Identifier:\t" + safeguard.getIdentifier());
        String groupIdentifier = getIdentifierPrefix(safeguard.getIdentifier());
        LOG.debug("GroupIdentifier:\t" + groupIdentifier);
        BpRequirementGroup parent = getRequirementParentGroup(groupIdentifier);
        LOG.debug("Parent:\t" + parent.getTitle());
        String safeguardIdentifier = safeguard.getIdentifier();
        String comparableIdentifier = safeguardIdentifier.replace('M', 'A');
        for (CnATreeElement requirement : parent.getChildren()) {
            if (requirement instanceof BpRequirement) {
                LOG.debug("Child is Requirement:\t" + requirement.getTitle() + " with identifier:\t" + ((BpRequirement)requirement).getIdentifier());
                if (((BpRequirement)requirement).getIdentifier().equals(comparableIdentifier)){
                    links.add(new Link((BpRequirement)requirement, safeguard));
                }
            } else if (requirement instanceof BpRequirementGroup) {
                LOG.debug("child is RequirementGroup :\t" + requirement.getTitle());
                for (CnATreeElement child : requirement.getChildren()) {
                    if (child instanceof BpRequirement) {
                        LOG.debug("child is grandchild:\t" + child.getTitle() + " with identifier:\t" + ((BpRequirement)child).getIdentifier());
                        if (((BpRequirement)child).getIdentifier().equals(comparableIdentifier)){
                            links.add(new Link(((BpRequirement)child), safeguard));
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
            throw new CreateBPElementException(e, "Error creating BP-Element:\t" + title + " in container:\t" + parent.getTitle() + " of type " + typeId + " failed");
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
                } else {
                    veriniceModule = addedModules.get(bsiModule.getIdentifier());
                }
                
                createSpecificThreats(bsiModule, groupIdentifier, veriniceModule);
                
                linkElementalThreats(bsiModule);
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
            veriniceModule = (BpRequirementGroup)createElement(BpRequirementGroup.TYPE_ID, parent, bsiModule.getFullTitle());

            veriniceModule.setIdentifier(bsiModule.getIdentifier());
            veriniceModule.setDescription(getModuleDescriptionText(bsiModule.getFullTitle(), 
                    bsiModule.getDescription()));
            LOG.debug("Module : \t" + veriniceModule.getTitle()+ " created");
            addedModules.put(bsiModule.getIdentifier(), veriniceModule);
            createRequirementsForModule(bsiModule, veriniceModule);
        }
        return veriniceModule;
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
                    Link link = new Link(requirement, threat);
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


    /**
     * @param bsiModule
     * @param groupIdentifier
     * @param veriniceModule
     * @throws CreateBPElementException
     */
    private void createSpecificThreats(Document bsiModule, String groupIdentifier, BpRequirementGroup veriniceModule) throws CreateBPElementException {
        List<Link> linkList = new ArrayList<>();
        for (SpecificThreat threat : bsiModule.getThreatScenario().getSpecificThreats().getSpecificThreat()) {

            BpThreatGroup tParent = getSpecificThreatParentGroup(groupIdentifier, veriniceModule.getTitle());

            String title = bsiModule.getIdentifier() + " " + threat.getHeadline();
            
            if (! addedThreats.containsKey(title) && tParent != null) {
                
                BpThreat veriniceThreat = (BpThreat) createElement(BpThreat.TYPE_ID, tParent, title);
                
                veriniceThreat.setDescription(getAnyElementDescription(threat.getHeadline(),
                        threat.getDescription().getAny()));
//                Link link = new Link(veriniceModule, veriniceThreat);
//                linkList.add(link);
                addedThreats.put(title, veriniceThreat);
            } 

        }
        CreateMultipleLinks linkCommand = new CreateMultipleLinks(linkList);
        try {
            getCommandService().executeCommand(linkCommand);
        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error creating links to specific threads");
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
                veriniceThreat.setAbbreviation(bsiThreat.getCia().toString());
                veriniceThreat.setDescription(getSafeguardDescription(bsiThreat.getFullTitle(), bsiThreat.getDescription()));
                veriniceThreat = (BpThreat) updateElement(veriniceThreat);
                addedThreats.put(bsiThreat.getIdentifier(), veriniceThreat);
                
                LOG.debug("Threat : \t" + veriniceThreat.getTitle()+ " created");
            }
        }
    }
    
    private String getElementalThreatDescription(String title, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("<H1>").append(title).append("</H1>");
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
                if (reqGroup.getTypeId().equals(BpRequirementGroup.TYPE_ID) && reqGroup.getTitle().equals(groupIdentifier)) {
                    group = (BpRequirementGroup) reqGroup;
                    break;
                }
            }
            
        } else if (processIdentifierPrefixes.contains(groupIdentifier)) {
            for (CnATreeElement reqGroup : processReqGroup.getChildren()) {
                if (reqGroup.getTypeId().equals(BpRequirementGroup.TYPE_ID) && reqGroup.getTitle().equals(groupIdentifier)) {
                    group = (BpRequirementGroup) reqGroup;
                    break;
                }
            }
            
        }
        return group;
    }
    
    private BpThreatGroup getSpecificThreatParentGroup(String groupIdentifier,
            String moduleTitle) throws CreateBPElementException {
        BpThreatGroup group = null;
        BpThreatGroup threatParentGroup = null;
        
        if (systemIdentifierPrefixes.contains(groupIdentifier)) {
            
            for (CnATreeElement threatGroup : systemThreatGroup.getChildren()) {
                if (threatGroup.getTypeId().equals(BpThreatGroup.TYPE_ID) && threatGroup.getTitle().equals(groupIdentifier)) {
                    group = (BpThreatGroup) threatGroup;
                    break;
                }
            }
            
        } else if (processIdentifierPrefixes.contains(groupIdentifier)) {
            for (CnATreeElement threatGroup : processThreatGroup.getChildren()) {
                if (threatGroup.getTypeId().equals(BpThreatGroup.TYPE_ID) && threatGroup.getTitle().equals(groupIdentifier)) {
                    group = (BpThreatGroup) threatGroup;
                    
                    break;
                }
            }
            
        }
        threatParentGroup = (BpThreatGroup) getIBGroupByNameRecursive(group, moduleTitle);
        if ( threatParentGroup == null ) {
            threatParentGroup = (BpThreatGroup) createElement(BpThreatGroup.TYPE_ID, group, moduleTitle);
        }
        return threatParentGroup;
    }
    
    private String getIdentifierPrefix(String id) {
        if (id != null && id.length() >= 3 && id.contains(".")) {
            return id.substring(0, id.indexOf("."));
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
    private void createSafeguardsForModule(ITBP2VNA.generated.implementationhint.Document bsiSafeguardDocument, SafeguardGroup parent) 
            throws CreateBPElementException {
        List<Link> links = new ArrayList<>();
        Safeguards bsiModule = bsiSafeguardDocument.getSafeguards();
        String moduleIdentifier = bsiSafeguardDocument.getIdentifier();
        
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : bsiModule.getBasicSafeguards().getSafeguard()) {
            Safeguard safeguard = createSafeguard(parent, bsiSafeguard, "BASIC");
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
                links.addAll(linkSafeguardToElementalThreat(moduleIdentifier, safeguard));
            } else {
                LOG.warn("Could not create Safeguard:\t" + bsiSafeguard.getTitle());
            }
        }
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : bsiModule.getStandardSafeguards().getSafeguard()) {
            Safeguard safeguard = createSafeguard(parent, bsiSafeguard, "STANDARD");
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
                links.addAll(linkSafeguardToElementalThreat(moduleIdentifier, safeguard));
            } else {
                LOG.warn("Could not create Safeguard:\t" + bsiSafeguard.getTitle());
            }        }
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : bsiModule.getHighLevelSafeguards().getSafeguard()) {
            Safeguard safeguard = createSafeguard(parent, bsiSafeguard, "HIGH");
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
                links.addAll(linkSafeguardToElementalThreat(moduleIdentifier, safeguard));
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
    
    private Set<Link> linkSafeguardToElementalThreat(String bsiModuleIdentifier, Safeguard vSafeguard){
        Set<Link> links = new HashSet<>();
        
        BpRequirementGroup module = getModuleByIdentifier(bsiModuleIdentifier);
        if ( module != null) {
            for (CnALink link : module.getLinksDown()) {
                if (link.getDependency() instanceof BpThreat) {
                    links.add(new Link(vSafeguard, link.getDependency()));
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
            ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard, String qualifier) 
                    throws CreateBPElementException {
        parent = getSafeguardParent(parent, bsiSafeguard.getIdentifier());
        if( parent != null) {
            Safeguard safeguard = (Safeguard) createElement(Safeguard.TYPE_ID, parent, bsiSafeguard.getTitle());
            safeguard.setAbbreviation(bsiSafeguard.getIdentifier());
            safeguard.setIdentifier(bsiSafeguard.getIdentifier());
            String description = getSafeguardDescription(bsiSafeguard.getTitle(),
                    bsiSafeguard.getDescription().getContent());
            if (description != null && description.length() > 0) {
                safeguard.setDescription(description);
            } else {
                LOG.error("No description found for:\t" + bsiSafeguard.getTitle());
            }
            safeguard.setQualifier(qualifier);
            safeguard.setTitle(bsiSafeguard.getTitle());
            LOG.debug("Safeguard : \t"  + safeguard.getTitle() + "created ");

            return (Safeguard) updateElement(safeguard);
        }
        return null;
    }

    private SafeguardGroup getSafeguardParent(SafeguardGroup rootGroup,
            String identifier) throws CreateBPElementException {
        String moduleIdentifier = identifier.substring(0, identifier.lastIndexOf(".M"));
        String subGroupIdentifier = identifier.substring(0, identifier.indexOf("."));
        String moduleTitle = null;
        if (addedModules.containsKey(moduleIdentifier)) {
            moduleTitle = addedModules.get(moduleIdentifier).getTitle();
        } else {
            return null;
        }
        // safeguardparent is a module
        SafeguardGroup safeguardParent = (SafeguardGroup)getIBGroupByNameRecursive(rootGroup, moduleTitle);
        // moduleparent is a safeguardGroup like "APP", "DER", "INF, "CON", ...
        SafeguardGroup safeguardRoot =(SafeguardGroup) processSafeguardGroup.getParent();
        SafeguardGroup moduleParent = (SafeguardGroup)getIBGroupByNameRecursive((IBpGroup) safeguardRoot, subGroupIdentifier);
        if (safeguardParent == null) {
            if(moduleParent != null) {
                safeguardParent = (SafeguardGroup)getIBGroupByNameRecursive((IBpGroup) moduleParent, moduleTitle);
                if (safeguardParent == null) {
                    safeguardParent = (SafeguardGroup)createElement(SafeguardGroup.TYPE_ID, moduleParent, moduleTitle);
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
        for (Requirement bsiRequirement : bsiModule.getRequirements().getBasicRequirements().getRequirement()) {
            createRequirement(parent, bsiRequirement, "BASIC");
        }
        for (Requirement bsiRequirement : bsiModule.getRequirements().getStandardRequirements().getRequirement()) {
            createRequirement(parent, bsiRequirement, "STANDARD");
        }
        for (Requirement bsiRequirement : bsiModule.getRequirements().getHighLevelRequirements().getRequirement()) {
            createRequirement(parent, bsiRequirement, "HIGH");
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
        if(!addedReqs.containsKey(bsiRequirement.getIdentifier())){
            BpRequirement vRequirement = null; 
            vRequirement = (BpRequirement) createElement(BpRequirement.TYPE_ID, parent, bsiRequirement.getTitle());
            vRequirement.setIdentifier(bsiRequirement.getIdentifier());
            vRequirement.setDescription(getAnyElementDescription(bsiRequirement.getTitle(), bsiRequirement.getDescription().getAny()));
            vRequirement.setTitle(bsiRequirement.getTitle());
            //        bsiRequirement.getResponsibleRoles(); // TODO
            //        bsiRequirement.getCia(); // TODO
            vRequirement.setQualifier(qualifier);
            addedReqs.put(bsiRequirement.getIdentifier(), vRequirement);
            return (BpRequirement) updateElement(vRequirement);
        } else {
            return addedReqs.get(bsiRequirement.getIdentifier());
        }
    }

    
    /**
     * experimental, tries to deal with the HTML/XML-Mixture
     * @param title
     * @param anyElements
     * @return
     */
    private String getAnyElementDescription(String title, List<Element> anyElements) {
        StringBuilder sb = new StringBuilder();
        sb.append("<H1>").append(title).append("</H1>");
        for (Object element : anyElements) {
            sb.append(extractContentFromElement(element));
        }
        
        return sb.toString();
    }


    /**
     * @param sb
     * @param element
     */
    private String extractContentFromElement(Object element) {
        StringBuilder sb = new StringBuilder();
        if (element instanceof Element) {
            sb.append(unwrapText((Element)element));
        } else if(element instanceof String) {
            sb.append((String)element);
        }
        return sb.toString();
    }
    
    private String getSafeguardDescription(String title, List<Object> anyElements) {
        StringBuilder sb = new StringBuilder();
        sb.append("<H1>").append(title).append("</H1>");
        if (anyElements != null) {
            for (Object o : anyElements) {
                sb.append(extractContentFromElement(o));            
            }
        } else {
            LOG.error("No Description found for :\t" + String.valueOf(title));
//            throw new NoDescriptionFoundException("No Description found for :\t" + String.valueOf(title)));
        }
        
        return sb.toString();
    }
    
    private String unwrapText(Node element) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node node = element.getChildNodes().item(i);
            if (node instanceof Text) {
                String htmlFormatElement = node.getParentNode().getNodeName();
                sb.append("<").append(htmlFormatElement).append(">");
                sb.append(node.getNodeValue());
                sb.append("</").append(htmlFormatElement).append(">");
            } else {
                sb.append(unwrapText(node));
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
        sb.append("<H1>").append(title).append("</H1>");
        if(description != null) {
            String introduction = getSafeguardDescription("", description.getIntroduction()); 
            String purpose = getSafeguardDescription("", description.getPurpose());
            String differentiation = getSafeguardDescription("", description.getDifferentiation());
            sb.append((introduction != null) ? introduction : "<p>No Introduction</p>");
            sb.append((purpose != null) ? purpose : "<p>No Purpose</p>");
            sb.append((differentiation != null) ? differentiation : "<p>No Differentiation</p>");
            if (introduction != null && introduction.length() > 0) {
                LOG.error("No introduction in description found for :\t" + title);
            }
            if (purpose != null && purpose.length() > 0) {
                LOG.error("No purpose in description found for :\t" + title);
            }
            if (differentiation != null && differentiation.length() > 0) {
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
            rootNetwork.setTitel("IT-Grundschutz-Kompendium");
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
