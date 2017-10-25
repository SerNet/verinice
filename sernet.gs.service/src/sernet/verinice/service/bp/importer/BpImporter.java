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
import java.util.Iterator;
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
 * this class transform the "New ITBP Compendium", first released in February of 2018 
 * to a vna-file, for the usage with verinice
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class BpImporter {

    private static final Logger LOG = Logger.getLogger(BpImporter.class);
    
    private static final Set<String> processIdentifierPrefixes;
    private static final Set<String> systemIdentifierPrefixes;
    
    private String xmlRootDirectory = null;
    
    private ItNetwork rootNetwork = null;
    
    private BpRequirementGroup processReqGroup = null;
    private BpRequirementGroup systemReqGroup = null;

    private SafeguardGroup processSafeguardGroup = null;
    private SafeguardGroup systemSafeguardGroup = null;

    private BpThreatGroup elementalThreatGroup = null;
    
    ICommandService commandService;
    IDAOFactory daoFactory;
    
    
    private static final String SUBDIRECTORY_MODULES = "bausteine";
    private static final String SUBDIRECTORY_MEDIA = "media";
    private static final String SUBDIRECTORY_THREATS = "elementare_gefaehrdungen_1";
    private static final String SUBDIRECTORY_IMPL_HINTS = "umsetzungshinweise";
    
    
    private static final String HTML_OPEN_TABLE = 
            "<table style=\"border-collapse: collapse;\">"; //$NON-NLS-1$
    private static final String HTML_CLOSE_TABLE = "</table>"; //$NON-NLS-1$
    private static final String HTML_OPEN_UL = "<ul>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_UL = "</ul>"; //$NON-NLS-1$
    private static final String HTML_OPEN_TR = "<tr>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_TR = "</tr>"; //$NON-NLS-1$
    private static final String HTML_OPEN_TD = 
            "<td style=\"border: 1px solid black\">"; //$NON-NLS-1$
    private static final String HTML_CLOSE_TD = "</td>"; //$NON-NLS-1$
    private static final String HTML_SPACE = "&nbsp;"; //$NON-NLS-1$
    private static final String HTML_OPEN_PARAGRAPH = "<p>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_PARAGRAPH = "</p>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_OPEN_PARAGRAPH = "</p><p>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_H1 = "</H1>"; //$NON-NLS-1$
    private static final String HTML_OPEN_H1 = "<H1>"; //$NON-NLS-1$
    private static final String HTML_OPEN_LIST_ITEM = "<li>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_LIST_ITEM = "</li>"; //$NON-NLS-1$
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
    
    /**
     * main BSI-XML to vna tranforming method 
     * @throws CreateBPElementException
     */
    public void run() throws CreateBPElementException {
        long startImport = System.currentTimeMillis();
        Set<Document> modules = new HashSet<>();
        Set<ITBP2VNA.generated.threat.Document> threats = new HashSet<>();
        Set<ITBP2VNA.generated.implementationhint.Document> implementationHints = new HashSet<>();
        if (xmlRootDirectory == null || xmlRootDirectory.length() == 0) {
            LOG.error("Wrong number of arguments, please provide root-Directory to XML-Archive");
            return;
        } else {
            setupImportAndParseContent(modules, threats, implementationHints);
            LOG.debug("Successfully parsed modules:\t" + modules.size());
            LOG.debug("Successfully parsed threats:\t" + threats.size());
            LOG.debug("Successfully parsed implementation hints:\t" + implementationHints.size());

            long veryBeginning = System.currentTimeMillis();
            prepareITNetwork();
            long itnetworkReady = System.currentTimeMillis();
            LOG.debug("ITNetwork prepared, took :\t" 
                    + (itnetworkReady - veryBeginning) / MILLIS_PER_SECOND) ;
            generateElementalThreads(threats);
            long elementalThreadsReady = System.currentTimeMillis();
            LOG.debug("Elementalthreats ready, took :\t" 
                    + (elementalThreadsReady - itnetworkReady) / MILLIS_PER_SECOND);
            transferModules(modules);
            long modulesReady = System.currentTimeMillis();
            LOG.debug("Modules ready, took :\t" 
                    + (modulesReady - elementalThreadsReady) / MILLIS_PER_SECOND);
            LOG.debug("Transformation of elements complete");
            createSafeguards(implementationHints);
            long safeguardsReady = System.currentTimeMillis();
            LOG.debug("Safeguards ready, took:\t" 
                    + (safeguardsReady - modulesReady) / MILLIS_PER_SECOND);
            updateElement(getRootItNetwork());
            LOG.debug("ItNetwork updated");
            LOG.debug("Import finished, took:\t" 
                    + (System.currentTimeMillis() - startImport) / MILLIS_PER_SECOND);
        }
    }


    /**
     * 
     * prepares the transformation-process. especially finds the content containing sub-directories 
     * (attention, structure is given by the BSI and this is relying on that structure not changing)
     *  
     * When subdirectories are found, the parsing of the BSI-XML takes place, the three
     * Sets, passed as parameter, will be filled with the Java-Objects representing the XML-Files
     * 
     * @param modules
     * @param threats
     * @param implementationHints
     */
    private void setupImportAndParseContent(Set<Document> modules, 
            Set<ITBP2VNA.generated.threat.Document> threats, 
            Set<ITBP2VNA.generated.implementationhint.Document> implementationHints) {
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
    private void parseBSIXml(Set<Document> modules, Set<ITBP2VNA.generated.threat.Document> threats,
            Set<ITBP2VNA.generated.implementationhint.Document> implementationHints, 
            File moduleDir, File threatDir, File implHintDir) {
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
            setSubDirectories(moduleDir, threatDir, implHintDir, mediaDir,
                    warningMoreThanOneDirectory, subDirectory, dirs);
        }
        return dirs;
        
    }


    /**
     * 
     * compares name of content-containing directory candidates to the (BSI-given) names,
     * and sets them as an element of an Array (which will be returned)
     * 
     * @param moduleDir
     * @param threatDir
     * @param implHintDir
     * @param mediaDir
     * @param warningMoreThanOneDirectory
     * @param dirs
     * @param subDirectory
     */
    private void setSubDirectories(File moduleDir, File threatDir, File implHintDir,
            File mediaDir, final String warningMoreThanOneDirectory,
            File subDirectory, File[] dirs) {
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
    
    /**
     * update a given {@link CnATreeElement} to write changes to db
     * 
     * @param element
     * @return
     * @throws CreateBPElementException
     */
    private CnATreeElement updateElement(CnATreeElement element) throws CreateBPElementException{
        try {
            UpdateElement<CnATreeElement> command = new UpdateElement<>(
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
    private List<File> getXMLFiles(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] directories = dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return FilenameUtils.isExtension(pathname.getName(), "xml");
                }
            });
            return Arrays.asList(directories);
        } else {
            return new ArrayList<>();
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
                    getRootItNetwork(), Messages.Root_Requirement_Group_Name);
        }

        systemReqGroup = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID, 
                rootReqGroup, Messages.System_Requirement_Group_Name);

        processReqGroup = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID, 
                rootReqGroup, Messages.Process_Requirement_Group_Name); 

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

        rootThreatGroup.setTitel(Messages.Root_Threat_Group_Name);


        createStructuredSubGroups(rootThreatGroup, safeguardRootGroup);

    }
    
    /**
     * gets the {@link BpRequirementGroup} which is child of the root-IT-Network
     * (root-Location of all {@link BpRequirement} in the Catalogue)
     * 
     * @return
     * @throws CreateBPElementException
     */
    private BpRequirementGroup getRootReqGroup() throws CreateBPElementException {
        for (CnATreeElement element : getRootItNetwork().getChildren()) {
            if (BpRequirementGroup.TYPE_ID.equals(element.getTypeId()) 
                    && Messages.Root_Requirement_Group_Name.equals(element.getTitle())) {
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
                rootThreatGroup, Messages.Elemental_Threat_Group_Name);
        processSafeguardGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID,
                safeguardRootGroup, Messages.Process_Requirement_Group_Name);
        systemSafeguardGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID, 
                safeguardRootGroup, Messages.System_Requirement_Group_Name);



        for (String name : systemIdentifierPrefixes) {
            createElement(BpRequirementGroup.TYPE_ID, systemReqGroup, name);
            createElement(SafeguardGroup.TYPE_ID, systemSafeguardGroup,  name);
        }


        for (String name : processIdentifierPrefixes) {
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
                subGroups.add((SafeguardGroup)child);
                updateElement(child);
            }
        }
        
        for (CnATreeElement child : processSafeguardGroup.getChildren()) {
            if (SafeguardGroup.TYPE_ID.equals(child.getTypeId())) {
                subGroups.add((SafeguardGroup)child);
                updateElement(child);
            }
        }
        
        updateElement(processSafeguardGroup);
        updateElement(systemSafeguardGroup);
        
        for (ITBP2VNA.generated.implementationhint.Document bsiSafeguard : implementationHints) {
            SafeguardGroup safeGuardParent = null;
            for (SafeguardGroup candidate : subGroups) {
                if (candidate.getTitle().
                        startsWith(getIdentifierPrefix(bsiSafeguard.getIdentifier()))) {
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
    
    /**
     * determines a Module {@link BpRequirementGroup} that relates to a {@link Safeguard}
     * to create {@link CnALink} between the {@link Safeguard} and the {@link BpRequirement} -
     * children of the determined {@link BpRequirementGroup}
     * 
     * @param safeguard
     * @return
     */
    private Set<Link> linkSafeguardToRequirements(Safeguard safeguard) {
        Set<Link> links = new HashSet<>();
        LOG.debug("searching Requirement-Links for Safeguard:\t" + 
                safeguard.getTitle() + "\t with Identifier:\t" + safeguard.getIdentifier());
        String groupIdentifier = getIdentifierPrefix(safeguard.getIdentifier());
        LOG.debug("GroupIdentifier:\t" + groupIdentifier);
        BpRequirementGroup parent = (BpRequirementGroup)getRequirementParentGroup(
                groupIdentifier, BpRequirementGroup.TYPE_ID, 
                systemReqGroup, processReqGroup);
        String safeguardIdentifier = safeguard.getIdentifier();
        String comparableIdentifier = safeguardIdentifier.replace('M', 'A');
        for (CnATreeElement requirement : parent.getChildren()) {
            links.addAll(createSafeGuardToRequirementLinks(safeguard,
                    comparableIdentifier, requirement));
        }
        return links;
    }


    /**
     * creates links (regarding to business-logic) between one {@link Safeguard} 
     * and the related {@link BpRequirement}
     * 
     * @param safeguard
     * @param links
     * @param comparableIdentifier
     * @param requirement
     */
    private Set<Link> createSafeGuardToRequirementLinks(Safeguard safeguard, 
            String comparableIdentifier, CnATreeElement requirement) {
        Set<Link> links = new HashSet<>();
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
        return links;
    }
    
    /**
     * simply creates a {@link CnATreeElement}
     * 
     * @param typeId
     * @param parent
     * @param title
     * @return
     * @throws CreateBPElementException
     */
    private CnATreeElement createElement(String typeId, 
            CnATreeElement parent, String title) throws CreateBPElementException {
        CreateElement<CnATreeElement> command = new CreateElement<>(parent, typeId, title);
        try {
            return getCommandService().executeCommand(command).getNewElement();
        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error creating BP-Element:\t" + title 
                    + " in container:\t" + parent.getTitle() + " of type " + typeId + " failed");
        }
    }
    
    /**
     * transfers the parsed {@link Document} object into {@link CnATreeElement}
     * (and calls related methods)
     * 
     * @param modules
     * @throws CreateBPElementException
     */
    private void transferModules(Set<Document> modules) throws CreateBPElementException {

        for (Document bsiModule : modules) {
            if (rootNetwork != null) {
                String groupIdentifier = getIdentifierPrefix(bsiModule.getIdentifier());
                
                BpRequirementGroup parent = (BpRequirementGroup)getRequirementParentGroup(
                        groupIdentifier, BpRequirementGroup.TYPE_ID, 
                        systemReqGroup, processReqGroup);
                
                BpRequirementGroup veriniceModule = null;
                
                if (! addedModules.containsKey(bsiModule.getIdentifier())) {
                    veriniceModule = createModule(bsiModule, parent);
                    linkElementalThreats(bsiModule);
                    addedModules.put(bsiModule.getIdentifier(), veriniceModule);
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
    private BpRequirementGroup createModule(Document bsiModule, 
            BpRequirementGroup parent) throws CreateBPElementException {
        BpRequirementGroup veriniceModule = null;
        if (parent != null) {
            veriniceModule = (BpRequirementGroup)createElement(BpRequirementGroup.TYPE_ID, 
                    parent, bsiModule.getFullTitle());

            veriniceModule.setIdentifier(bsiModule.getIdentifier());
            veriniceModule.setObjectBrowserDescription(getCompleteModuleXMLText(bsiModule));
            veriniceModule.setLastChange(getBSIDate(bsiModule.getLastChange())); 
            LOG.debug("Module : \t" + veriniceModule.getTitle() + " created");
            createRequirementsForModule(bsiModule, veriniceModule);
        }
        return veriniceModule;
    }
    
    
    /**
     * creates a formatted String like one of the following:
     * 
     * 1 Chaptertitle
     * 1.0 Chaptertitle
     * 1.2.3 Chaptertitle
     * 
     * to not create subchapters, set the int to -1
     * 
     * @param chapter
     * @param subChapter
     * @param subSubChapter
     * @param headline
     * @return
     */
    private String generateChapterHeader(int chapter, int subChapter,
            int subSubChapter, String headline) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(headline)) {
            sb.append(HTML_OPEN_H1);
        }

        if (chapter >= 1) {
            sb.append(chapter);
            if (subChapter >= 0) {
                sb.append('.');
                sb.append(subChapter);
                if (subSubChapter >= 0) {
                    sb.append('.');
                    sb.append(subSubChapter);
                }
            }
        }
        
        if (StringUtils.isNotEmpty(headline)) {
            sb.append(HTML_SPACE);
            sb.append(headline);
            sb.append(HTML_CLOSE_H1);
            sb.append(HTML_BR);
        }
        
        return sb.toString();
    }
    
    
    /**
     * The description (shown in the objectbrowser) for a module needs to show all (!) the content
     * which is defined in the related XML-File. It should be the equivalent
     * to the content that is shown in the printed (pdf) version of
     * the BSI-Baseline-Protection Compendium
     * 
     * this method gathers all that information and creates
     * a html-structure around it to format it in a pretty way
     * 
     * @param module
     * @return
     */
    private String getCompleteModuleXMLText(Document module) {
        StringBuilder descriptionBuilder = new StringBuilder();

        int chapter = 1;
        int subChapter = 0;
        
        chapter = getModuleIntroduction(module, descriptionBuilder, chapter);
        
        descriptionBuilder.append(getModuleSpecificThreats(module, chapter));
        
        chapter++;
        
        descriptionBuilder.append(getModuleReqIntro(module, chapter));
        
        subChapter = 1;
        
        descriptionBuilder.append(getModuleReqMain(module, chapter, subChapter));
        
        descriptionBuilder.append(ToHtmlTableTransformer.
                createCrossreferenceTable(module.getCrossreferences()));
        
        descriptionBuilder.append(getModuleDescriptionSuffix(module, chapter));
        
        return descriptionBuilder.toString();
        
        
    }

    /**
     * generates the part of the {@link BpRequirementGroup} (Module) 
     * object-browser-property that describes the {@link BpRequirement}
     * further Information (the literature notes) related to the module
     *  
     * @param module
     * @param descriptionBuilder
     * @param chapter
     */
    private String getModuleDescriptionSuffix(Document module, int chapter) {
        StringBuilder sb = new StringBuilder();
        int subChapter;
        sb.append(HTML_OPEN_PARAGRAPH);
        
        if (module.getBibliography() != null) {

            chapter++;
            sb.append(generateChapterHeader(chapter, -1, -1, Messages.Further_Information));
            
            subChapter = 1;
            sb.append(generateChapterHeader(chapter, subChapter, -1, Messages.Literature));
            for (BibItem bibItem :  module.getBibliography().getBibItem()) {
                StringBuilder bibBuilder = new StringBuilder();
                bibBuilder.append(HTML_OPEN_UL);
                bibBuilder.append(HTML_OPEN_LIST_ITEM);
                bibBuilder.append(bibItem.getShortHand()).append(" ");
                String descriptionText = bibItem.getDescription();
                descriptionText = descriptionText.replaceAll("<p>", "");
                descriptionText = descriptionText.replaceAll("</p>", "");
                descriptionText = descriptionText.replaceAll("<br />", "");
                bibBuilder.append(descriptionText);
                bibBuilder.append(HTML_CLOSE_LIST_ITEM);
                bibBuilder.append(HTML_CLOSE_UL);
                sb.append(bibBuilder.toString());
            }
        }
        return sb.toString();
    }

    /**
     * generates the part of the {@link BpRequirementGroup} (Module) 
     * object-browser-property that describes the {@link BpRequirement}
     * defined within that module
     * 
     * @param module
     * @param descriptionBuilder
     * @param chapter
     * @param subChapter
     */
    private String getModuleReqMain(Document module, int chapter, int subChapter) {
        StringBuilder sb = new StringBuilder();
        sb.append(generateChapterHeader(chapter, subChapter++, -1, Messages.Basic_Requirements));
        sb.append(HTML_OPEN_PARAGRAPH);
        sb.append(Messages.Basic_Requirements_Intro);
        sb.append(HTML_CLOSE_PARAGRAPH);
        sb.append(getModuleRequirementDescription(module.getRequirements()
                .getBasicRequirements().getRequirement()));
        
        sb.append(HTML_CLOSE_PARAGRAPH);
        
        sb.append(generateChapterHeader(chapter, subChapter++, -1, Messages.Standard_Requirements));
        sb.append(HTML_OPEN_PARAGRAPH);
        sb.append(Messages.Standard_Requirements_Intro);
        sb.append(HTML_CLOSE_PARAGRAPH);
        sb.append(getModuleRequirementDescription(module.getRequirements()
                .getStandardRequirements().getRequirement()));        
        
        sb.append(HTML_CLOSE_OPEN_PARAGRAPH);
        
        sb.append(generateChapterHeader(chapter, subChapter++, -1, Messages.High_Requirements));

        sb.append(HTML_OPEN_PARAGRAPH);
        sb.append(Messages.High_Requirements_Intro);
        sb.append(HTML_CLOSE_PARAGRAPH);
        
        sb.append(getModuleRequirementDescription(module.getRequirements()
                .getHighLevelRequirements().getRequirement()));        
        
        sb.append(HTML_CLOSE_PARAGRAPH);
        return sb.toString();
    }

    /**
     * get Introduction of Requirements-Description of description of
     * {@link BpRequirementGroup} (Module)
     * 
     * @param module
     * @param descriptionBuilder
     * @param chapter
     */
    private String getModuleReqIntro(Document module, int chapter) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN_PARAGRAPH);
        
        sb.append(generateChapterHeader(chapter, -1, -1, Messages.Requirements));
        
        sb.append(module.getRequirements().getDescription());
        
        sb.append(HTML_OPEN_TABLE);
        sb.append(HTML_OPEN_TR);
        sb.append(HTML_OPEN_TD);
        sb.append(Messages.Main_Responsible);
        sb.append(HTML_CLOSE_TD);
        sb.append(HTML_OPEN_TD);
        sb.append(module.getRequirements().getMainResponsibleRole());
        sb.append(HTML_CLOSE_TD);
        sb.append(HTML_CLOSE_TR);
        sb.append(HTML_OPEN_TR);
        sb.append(HTML_OPEN_TD);
        sb.append(Messages.Further_Responsibles);
        sb.append(HTML_CLOSE_TD);
        sb.append(HTML_OPEN_TD);
        
        List<String> roles = null;
        if (module.getRequirements().getFurtherResponsibleRoles() != null) {
            roles = module.getRequirements().getFurtherResponsibleRoles().getRole();
        }
        if (roles != null && !roles.isEmpty()) {
            Iterator<String> iter = roles.iterator();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            
        }
        sb.append(HTML_CLOSE_TD);
        sb.append(HTML_CLOSE_TR);        
        sb.append(HTML_CLOSE_TABLE);
        
        sb.append(HTML_CLOSE_PARAGRAPH);
        return sb.toString();
    }

    /**
     * 
     * creates the specific-threats describing part of the 
     * {@link BpRequirementGroup} (Module) description
     * 
     * @param module
     * @param descriptionBuilder
     * @param chapter
     */
    private String getModuleSpecificThreats(Document module, int chapter) {
        int subChapter;
        StringBuilder sb = new StringBuilder();
        sb.append(generateChapterHeader(chapter, -1, -1, Messages.Threat_Situation));
        sb.append(module.getThreatScenario().getDescription());
        
        SpecificThreats specificThreats = module.getThreatScenario().getSpecificThreats();
        
        subChapter = 1;
        for (SpecificThreat specificThreat : specificThreats.getSpecificThreat()) {
            sb.append(HTML_OPEN_PARAGRAPH);
            sb.append(generateChapterHeader(chapter, 
                    subChapter++, -1, specificThreat.getHeadline()));
            sb.append(specificThreat.getDescription());
            sb.append(HTML_CLOSE_PARAGRAPH);
        }
        return sb.toString();
    }

    /**
     * creates the intro of a {@link BpRequirementGroup} (Module) description
     * 
     * @param module
     * @param descriptionBuilder
     * @param chapter
     * @return
     */
    private int getModuleIntroduction(Document module, 
            StringBuilder descriptionBuilder, int chapter) {
        descriptionBuilder.append(HTML_OPEN_H1);
        descriptionBuilder.append(module.getFullTitle());
        descriptionBuilder.append(HTML_CLOSE_H1);
        
        descriptionBuilder.append(generateChapterHeader(chapter, -1, -1, Messages.Description));
        descriptionBuilder.append(getModuleDescriptionStart(module, chapter++));
        return chapter;
    }


    /**
     * returns the description (three parts (Introduction, Purpose, Differentiation))
     * of a module {@link BpRequirementGroup}
     * 
     * @param module
     * @param descriptionBuilder
     */
    private String getModuleDescriptionStart(Document module, int chapter) {
        StringBuilder sb = new StringBuilder();
        
        
        List<Object> introduction = module.getDescription().getIntroduction();
        
        int subChapter = 1;
        
        for (Object o : introduction) {
            if (o instanceof Element) {
                Element node = (Element)o;
                if ("introduction".equals(node.getNodeName())){
                    sb.append(generateChapterHeader(chapter, subChapter++,
                            -1, Messages.Introduction));
                    sb.append(HTML_OPEN_PARAGRAPH);
                    sb.append(node.getTextContent());
                    sb.append(HTML_CLOSE_PARAGRAPH);
                } else if ("purpose".equals(node.getNodeName())) {
                    sb.append(generateChapterHeader(chapter, subChapter++, -1, Messages.Purpose));
                    sb.append(HTML_OPEN_PARAGRAPH);
                    sb.append(node.getTextContent());
                    sb.append(HTML_CLOSE_PARAGRAPH);
                } else if ("differentiation".equals(node.getNodeName())) {
                    sb.append(generateChapterHeader(chapter, subChapter++,
                            -1, Messages.Differentiation));
                    sb.append(HTML_OPEN_PARAGRAPH);
                    sb.append(node.getTextContent());
                    sb.append(HTML_CLOSE_PARAGRAPH);
                }
            }
        }
        
        return sb.toString();
    }
    
    
    /**
     * returns the description of a {@link BpRequirement}
     * HTML-Formatted
     *  
     * @param requirements
     * @return
     */
    public String getModuleRequirementDescription(List<Requirement> requirements) {
        StringBuilder sb = new StringBuilder();
        
        for (Requirement requirement : requirements) {
            String title = getRequirementDescriptionStart(requirement);
            title += " ";
            title += getRequirementResponsibleDescription(requirement);
            title += " ";
            title += getRequirementCIA(requirement);
            sb.append(HTML_OPEN_H1);
            sb.append(title);
            sb.append(HTML_CLOSE_H1);
            sb.append(HTML_OPEN_PARAGRAPH);
            sb.append(getAnyElementDescription("", -1, -1 ,-1,
                    requirement.getDescription().getAny()));
            sb.append(HTML_CLOSE_PARAGRAPH);
        }
        
        return sb.toString();
    }


    /**
     * returns the description-prefix of a {@link BpRequirement}
     * HTML-Formatted
     * 
     * @param requirement
     */
    private String getRequirementDescriptionStart(Requirement requirement) {
        StringBuilder sb = new StringBuilder();
        sb.append(requirement.getIdentifier());
        sb.append(" ");
        sb.append(requirement.getTitle());
        return sb.toString();
    }


    /**
     * returns a string that is the suffix to a {@link BpRequirement} title,
     * that contains all responsible roles
     * 
     * @param requirement
     */
    private String getRequirementResponsibleDescription(Requirement requirement) {
        StringBuilder sb = new StringBuilder();
        
        if (requirement.getResponsibleRoles() != null 
                && !requirement.getResponsibleRoles().getRole().isEmpty()) {
            sb.append("[");
            Iterator<String> iter = requirement.getResponsibleRoles().getRole().iterator();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) {
                    sb.append(",");                    
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }


    /**
     * returns a string that is the suffix to a {@link BpRequirement} title,
     * that contains which protection categories are affected by this requirement
     * 
     * C for Confidentiality
     * I for Integrity
     * A for Availabiltiy
     * 
     * @param requirement
     */
    private String getRequirementCIA(Requirement requirement) {
        StringBuilder sb = new StringBuilder();
        
        String confidentiality = (Boolean.parseBoolean(requirement.getCia().
                getConfidentiality())) ? "C" : "";
        String integrity = (Boolean.parseBoolean(requirement.getCia().
                getIntegrity())) ? "I" : "";
        String availitbility = (Boolean.parseBoolean(requirement.getCia().
                getAvailability())) ? "A" : "";
        
        String cia = confidentiality + integrity + availitbility;

        if (StringUtils.isNotEmpty(cia)) {
            cia = "(" + cia + ")";
            sb.append(cia);
        }
        
        return sb.toString();
    }


    /**
     * create links between {@link BpThreat} and related {@link BpRequirement}
     * like they are defined in the given {@link Document} 
     * 
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
                    Link link = new Link(requirement, threat, 
                            BpRequirement.REL_BP_REQUIREMENT_BP_THREAT, "");
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
     * returns a {@link BpThreat} defined by its identifier
     * 
     * @param identifier
     * @return
     */
    private BpThreat getElementalThreadByIdentifier(String identifier) {
        if (addedThreats.containsKey(identifier)) {
            return addedThreats.get(identifier);
        } else {
            LOG.error("Could not find threat with id:\t" + identifier);
            return null;
        }
    }
    
    /**
     * returns a {@link BpRequirement} defined by its identifier
     * 
     * @param identifier
     * @return
     */
    private BpRequirement getRequirementByIdentifier(String identifier) {
        if (addedReqs.containsKey(identifier)) {
            return addedReqs.get(identifier);
        } else {
            LOG.error("Could not find requirement with id:\t" + identifier);
            return null;            
        }
    }
    
    
    /**
     * generate elemental-threats definied in a {@link Document} as instances of {@link BpThreat}
     * within the given structure
     * 
     * @param threats
     * @throws CreateBPElementException
     */
    private void generateElementalThreads(
            Set<ITBP2VNA.generated.threat.Document> threats) 
                    throws CreateBPElementException {
        for (ITBP2VNA.generated.threat.Document bsiThreat : threats) {
            if (! addedThreats.containsKey(bsiThreat.getIdentifier())) {
                BpThreat veriniceThreat = (BpThreat) createElement(BpThreat.TYPE_ID, 
                        elementalThreatGroup, bsiThreat.getFullTitle());
                veriniceThreat.setTitel(bsiThreat.getFullTitle());
                
                veriniceThreat.setIdentifier(bsiThreat.getIdentifier());
                String plainDescription = getAnyObjectDescription(
                        bsiThreat.getFullTitle(), 1, bsiThreat.getDescription());
                veriniceThreat.setConfidentiality(
                        Boolean.parseBoolean(bsiThreat.getCia().getConfidentiality()));
                veriniceThreat.setIntegrity(
                        Boolean.parseBoolean(bsiThreat.getCia().getIntegrity()));
                veriniceThreat.setAvailibility(
                        Boolean.parseBoolean(bsiThreat.getCia().getAvailability()));
                
                if (plainDescription != null && plainDescription.length() > 0) {
                    veriniceThreat.setObjectBrowserDescription(plainDescription);
                }
                veriniceThreat = (BpThreat) updateElement(veriniceThreat);
                addedThreats.put(bsiThreat.getIdentifier(), veriniceThreat);
                
                LOG.debug("Threat : \t" + veriniceThreat.getTitle() + " created");
            }
        }
    }
    

    /**
     * get the {@link BpRequirementGroup} (module) that contains a specific {@link BpRequirement}
     * 
     * @param groupIdentifier
     * @param typeId
     * @param systemGroup
     * @param processGroup
     * @return
     */
    private CnATreeElement getRequirementParentGroup(String groupIdentifier, String typeId,
            CnATreeElement systemGroup, CnATreeElement processGroup) {
        CnATreeElement group = null;
        
        if (systemIdentifierPrefixes.contains(groupIdentifier)) {
            
            for (CnATreeElement reqGroup : systemGroup.getChildren()) {
                if (reqGroup.getTypeId().equals(typeId) 
                        && reqGroup.getTitle().equals(groupIdentifier)) {
                    group = reqGroup;
                    break;
                }
            }
            
        } else if (processIdentifierPrefixes.contains(groupIdentifier)) {
            for (CnATreeElement reqGroup : processGroup.getChildren()) {
                if (reqGroup.getTypeId().equals(typeId) 
                        && reqGroup.getTitle().equals(groupIdentifier)) {
                    group = reqGroup;
                    break;
                }
            }
            
        }
        return group;
    }
    
    
    
    private String getIdentifierPrefix(String id) {
        if (id != null && id.length() >= 3 && id.contains(".")) {
            return id.substring(0, id.indexOf('.'));
        } else {
            return id;
        }
    }

    /**
     * 
     * create Safeguards according to their level-definition ( BASIC, STANDARD, HIGH)
     * and links them to the {@link BpRequirement} defined in the
     *  references module {@link BpRequirementGroup} 
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
    private void createSafeguardsForModule(
            ITBP2VNA.generated.implementationhint.Document bsiSafeguardDocument,
            SafeguardGroup parent) 
            throws CreateBPElementException {
        
        final String defaultErrorMsg = "Could not create Safeguard:\t";
        
        List<Link> links = new ArrayList<>();
        Safeguards bsiModule = bsiSafeguardDocument.getSafeguards();
        List<ITBP2VNA.generated.implementationhint.Safeguard> safeGuards = 
                bsiModule.getBasicSafeguards().getSafeguard();
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : safeGuards) {
            
            SafeguardGroup safeGuardParent = getSafeguardParent(parent,
                    bsiSafeguard.getIdentifier());
            
            Safeguard safeguard = createSafeguard(safeGuardParent,
                    bsiSafeguard, Messages.Qualifier_Basic,
                    bsiSafeguardDocument.getLastChange().toString());
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
            } else {
                LOG.warn(defaultErrorMsg + bsiSafeguard.getTitle());
            }
        }
        safeGuards.clear();
        safeGuards = bsiModule.getStandardSafeguards().getSafeguard();
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : safeGuards) {
            SafeguardGroup safeGuardParent = getSafeguardParent(
                    parent, bsiSafeguard.getIdentifier());
            Safeguard safeguard = createSafeguard(
                    safeGuardParent, bsiSafeguard, Messages.Qualifier_Standard,
                    bsiSafeguardDocument.getLastChange().toString());
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
            } else {
                LOG.warn(defaultErrorMsg + bsiSafeguard.getTitle());
            }        
        }
        safeGuards.clear();
        safeGuards = bsiModule.getHighLevelSafeguards().getSafeguard();
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : safeGuards) {
            SafeguardGroup safeGuardParent = getSafeguardParent(
                    parent, bsiSafeguard.getIdentifier());
            Safeguard safeguard = createSafeguard(
                    safeGuardParent, bsiSafeguard, Messages.Qualifier_High,
                    bsiSafeguardDocument.getLastChange().toString());
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
            } else {
                LOG.warn(defaultErrorMsg + bsiSafeguard.getTitle());
            }
        }
        
        
        
        CreateMultipleLinks linkCommand = new CreateMultipleLinks(links);
        try {
            getCommandService().executeCommand(linkCommand);
        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error creating links for safeguards");
        }
    }
    
    
    /**
     * transform a single 
     * {@link ITBP2VNA.generated.implementationhint.Safeguard} 
     * to a {@link Safeguard}
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
            safeguard = setSafeguardProperties(bsiSafeguard, 
                    qualifier, lastChange, trueValue, safeguard);
            
            LOG.debug("Safeguard : \t"  + safeguard.getTitle() + "created ");

            return (Safeguard) updateElement(safeguard);
        }
        return null;
    }


    /**
     * transforms all attributes from BSI-XML to {@link CnATreeElement} and sets them for 
     * the {@link Safeguard} that is about to be created
     * 
     * @param bsiSafeguard
     * @param qualifier
     * @param lastChange
     * @param trueValue
     * @param safeguard
     * @throws CreateBPElementException
     */
    private Safeguard setSafeguardProperties(
            ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard, 
            String qualifier, String lastChange, final String trueValue,
            Safeguard safeguard) throws CreateBPElementException {
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
                bsiSafeguard.getCia().getConfidentiality()) ? true : false);
        safeguard.setIsAffectsAvailability(trueValue.equals(
                bsiSafeguard.getCia().getAvailability()) ? true : false);
        safeguard.setIsAffectsIntegrity(trueValue.equals(
                bsiSafeguard.getCia().getIntegrity()) ? true : false);
        
        if (bsiSafeguard.getResponsibleRoles() != null) {
            for (String role : bsiSafeguard.getResponsibleRoles().getRole()) {
                safeguard.addResponsibleRole(role);
            }
        }
        return safeguard;
    }


    /**
     * {@link Safeguard} are related to {@link BpRequirement} which are related
     * to {@link BpRequirementGroup}. The {@link Safeguard} are structured the same way
     * as the {@link BpRequirementGroup} (modules), so there needs to be
     * a {@link SafeguardGroup} for every instance of {@link BpRequirementGroup}
     * 
     * this method returns this {@link SafeguardGroup} for a given 
     * safeguard- identifier or creates it, if not existant yet
     * 
     * @param rootGroup
     * @param identifier
     * @return
     * @throws CreateBPElementException
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
        SafeguardGroup safeguardRoot = (SafeguardGroup) processSafeguardGroup.getParent();
        SafeguardGroup moduleParent = (SafeguardGroup)getIBGroupByNameRecursive(
                (IBpGroup) safeguardRoot, subGroupIdentifier);
        if (safeguardParent == null) {
            if (moduleParent != null) {
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
    
    /**
     * searches for a {@link IBpGroup} which is a child
     * of an given {@link IBpGroup} and traverses
     * the given subtree dynamically
     * 
     * @param rootGroup
     * @param name
     * @return null, if no matching group found
     */
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
    private void createRequirementsForModule(Document bsiModule,
            BpRequirementGroup parent) throws CreateBPElementException {
        for (Requirement bsiRequirement : bsiModule.getRequirements()
                .getBasicRequirements().getRequirement()) {
            createRequirement(parent, bsiRequirement, Messages.Qualifier_Basic);
        }
        for (Requirement bsiRequirement : bsiModule.getRequirements()
                .getStandardRequirements().getRequirement()) {
            createRequirement(parent, bsiRequirement, Messages.Qualifier_Standard);
        }
        for (Requirement bsiRequirement : bsiModule.getRequirements()
                .getHighLevelRequirements().getRequirement()) {
            createRequirement(parent, bsiRequirement, Messages.Qualifier_High);
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
        if (!addedReqs.containsKey(bsiRequirement.getIdentifier())) {
            BpRequirement veriniceRequirement = null; 
            veriniceRequirement = (BpRequirement) createElement(
                    BpRequirement.TYPE_ID, parent, bsiRequirement.getTitle());
            veriniceRequirement.setIdentifier(bsiRequirement.getIdentifier());
            veriniceRequirement.setObjectBrowserDescription(getAnyElementDescription(
                    bsiRequirement.getTitle(), -1, -1 ,-1 ,
                    bsiRequirement.getDescription().getAny()));
            veriniceRequirement.setTitle(bsiRequirement.getTitle());
            veriniceRequirement.setLastChange(parent.getLastChange());
            veriniceRequirement.setIsAffectsConfidentiality(trueValue.equals(
                    bsiRequirement.getCia().getConfidentiality()) ? true : false);
            veriniceRequirement.setIsAffectsIntegrity(trueValue.equals(
                    bsiRequirement.getCia().getIntegrity()) ? true : false);
            veriniceRequirement.setIsAffectsAvailability(trueValue.equals(
                    bsiRequirement.getCia().getAvailability()) ? true : false);
            
            
            if (bsiRequirement.getResponsibleRoles() != null) {

                for (String role : bsiRequirement.getResponsibleRoles().getRole()) {
                    veriniceRequirement.addResponsibleRole(role);
                }

            }
            veriniceRequirement.setQualifier(qualifier);
            addedReqs.put(bsiRequirement.getIdentifier(), veriniceRequirement);
            return (BpRequirement) updateElement(veriniceRequirement);
        } else {
            return addedReqs.get(bsiRequirement.getIdentifier());
        }
    }
    
    
    /**
     * transforms date defined in BSI-XML (pattern yyyy-MM-dd) to instance of
     * {@link Date}
     * 
     * @param dateString
     * @return
     * @throws CreateBPElementException
     */
    private Date getBSIDate(String dateString) throws CreateBPElementException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            throw new CreateBPElementException(
                    "Could not parse bsiDate:\t" + dateString);
        }
    }

    
    /**
     * transforms mixed HTML/XML-Content 
     * (given by a {@link List} of {@link Element} 
     * to a html-formatted String
     * 
     * @param title
     * @param anyElements
     * @return
     */
    private String getAnyElementDescription(String title, int chapter, int subChapter,
            int subSubChapter, List<Element> anyElements) {
        StringBuilder sb = new StringBuilder();
        if (! "".equals(title)) {
            sb.append(generateChapterHeader(chapter, subChapter,
                    subSubChapter, title));
            sb.append(HTML_OPEN_PARAGRAPH);
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
     * searches for a text-element in a tree given by an {@link Element}
     * and returns it
     * 
     * @param sb
     * @param element
     */
    private String extractContentFromObject(Object element) {
        StringBuilder sb = new StringBuilder();
        if (element instanceof Element) {
            sb.append(unwrapText((Element)element));
        } else if (element instanceof String) {
            sb.append((String)element);
        } 
        return sb.toString();
    }
    
    /**
     * transforms mixed HTML/XML-Content 
     * (given by a {@link List} of {@link Object} 
     * to a html-formatted String
     * 
     * @param title
     * @param headlineLevel
     * @param anyObjects
     * @return
     */
    private String getAnyObjectDescription(String title, 
            int headlineLevel, List<Object> anyObjects) {
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
            sb.append(HTML_OPEN_PARAGRAPH);
            for (Object o : anyObjects) {
                sb.append(extractContentFromObject(o));            
            }
            sb.append(HTML_CLOSE_PARAGRAPH);
        } else {
            LOG.debug("No Description found for :\t" + title);
        }
        
        return sb.toString();
    }
    
    
    /**
     * creates a html-formatted text (including nested tags) 
     * that is given by a {@link Node}
     * 
     * blacklists known BSI-XML-Structure defining strings to not 
     * appear as html-tags in the output
     * 
     * @param element
     * @return
     */
    private String unwrapText(Node element) {
        Set<String> blacklist = new HashSet<>();
        blacklist.add("introduction");
        blacklist.add("purpose");
        blacklist.add("differentiation");
        blacklist.add("description");
        blacklist.add("");
        blacklist.add("#text");
        StringBuilder sb = new StringBuilder();
        Stack<String> htmlElements = new Stack<>();
        Node node = element;
        String htmlFormatElement = StringUtils.
                isNotEmpty(node.getNodeName()) ? node.getNodeName() : "";
        if (!blacklist.contains(htmlFormatElement)) {
            sb.append("<").append(htmlFormatElement).append(">");   
            htmlElements.push(htmlFormatElement);
        }
        if (node instanceof Text) {
            sb.append(node.getNodeValue());
        } else {
            for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                sb.append(unwrapText(element.getChildNodes().item(i)));
            }
        }
        while (! htmlElements.isEmpty()) {
            sb.append("</").append(htmlElements.pop()).append(">");
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

            if (rootNetwork == null && model != null) {
                CreateITNetwork command = new CreateITNetwork(model, ItNetwork.class, true);
                command = getCommandService().executeCommand(command);
                rootNetwork = command.getNewElement();
                StringBuilder titleBuilder = new StringBuilder();
                titleBuilder.append(Messages.IT_Network_Name);
                titleBuilder.append(" (");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateInISO = df.format(new Date());
                titleBuilder.append(dateInISO).append(" )");
                rootNetwork.setTitel(titleBuilder.toString());
                updateElement(rootNetwork);
            } 
        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error while loading BPModel");
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
