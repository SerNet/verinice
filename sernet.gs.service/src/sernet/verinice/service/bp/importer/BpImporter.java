package sernet.verinice.service.bp.importer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ITBP2VNA.generated.implementationhint.Document.Safeguards;
import ITBP2VNA.generated.module.Document;
import ITBP2VNA.generated.module.ElementalthreatRef;
import ITBP2VNA.generated.module.Requirement;
import ITBP2VNA.generated.module.RequirementRef;
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
import sernet.verinice.service.bp.importer.html.HtmlHelper;
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
 * this class transform the "New ITBP Compendium", first released in February of
 * 2018 to a vna-file, for the usage with verinice
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class BpImporter {

    private static final Logger LOG = Logger.getLogger(BpImporter.class);

    private static final Set<String> processIdentifierPrefixes;
    private static final Set<String> systemIdentifierPrefixes;
    private static final Map<String, String> implementationOrderByModuleIdentifier;

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

    private static final int MILLIS_PER_SECOND = 1000;

    private Map<String, BpThreat> addedThreats = new HashMap<>();
    private Map<String, BpRequirement> addedReqs = new HashMap<>();
    private Map<String, BpRequirementGroup> addedModules = new HashMap<>();

    static {
        processIdentifierPrefixes = new HashSet<>();
        processIdentifierPrefixes.addAll(Arrays.asList("CON", "DER", "ISMS", "OPS", "ORP"));
        systemIdentifierPrefixes = new HashSet<>();
        systemIdentifierPrefixes.addAll(Arrays.asList("APP", "IND", "INF", "NET", "SYS"));

        String propertyValueR1 = "bp_requirement_group_impl_seq_r1";
        String propertyValueR2 = "bp_requirement_group_impl_seq_r2";
        String propertyValueR3 = "bp_requirement_group_impl_seq_r3";

        Properties implementationOrder = new Properties();
        try (InputStream implementationOrderProperties = BpImporter.class
                .getResourceAsStream("implementation-order.properties")) {
            implementationOrder.load(implementationOrderProperties);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load implementation order from file", e);
        }

        Map<String, String> mapForImplementationOrder = new HashMap<>();

        for (Entry<Object, Object> entry : implementationOrder.entrySet()) {
            String moduleIdentifier = (String) entry.getKey();
            if (!moduleIdentifier.matches("[A-Z]{3,}\\.(\\d+\\.)*\\d+")) {
                throw new RuntimeException("Illegal module name: '" + moduleIdentifier + "'.");
            }
            String implementationOrderName = (String) entry.getValue();
            String implementationOrderPropertyValue;
            switch (implementationOrderName) {
            case "R1":
                implementationOrderPropertyValue = propertyValueR1;
                break;
            case "R2":
                implementationOrderPropertyValue = propertyValueR2;
                break;
            case "R3":
                implementationOrderPropertyValue = propertyValueR3;
                break;
            default:
                throw new RuntimeException("Illegal implementation order '"
                        + implementationOrderName + "' for module '" + moduleIdentifier + "'"
                        + ", allowed values: R1, R2, R3");
            }
            String existingMapping = mapForImplementationOrder.put(moduleIdentifier,
                    implementationOrderPropertyValue);
            if (existingMapping != null) {
                throw new RuntimeException(
                        "Found duplicate implementation order mapping for module '"
                                + moduleIdentifier + "'" + ".");
            }
        }

        implementationOrderByModuleIdentifier = Collections
                .unmodifiableMap(mapForImplementationOrder);
    }

    public BpImporter(String xmlRoot) {
        this.xmlRootDirectory = xmlRoot;
    }

    /**
     * main BSI-XML to vna transforming method
     * 
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
        }
        setupImportAndParseContent(modules, threats, implementationHints);
        LOG.debug("Successfully parsed modules:\t" + modules.size());
        LOG.debug("Successfully parsed threats:\t" + threats.size());
        LOG.debug("Successfully parsed implementation hints:\t" + implementationHints.size());

        long veryBeginning = System.currentTimeMillis();
        prepareITNetwork();
        long itnetworkReady = System.currentTimeMillis();
        LOG.debug("ITNetwork prepared, took :\t"
                + (itnetworkReady - veryBeginning) / MILLIS_PER_SECOND);
        generateElementalThreats(threats);
        long elementalThreatsReady = System.currentTimeMillis();
        LOG.debug("Elementalthreats ready, took :\t"
                + (elementalThreatsReady - itnetworkReady) / MILLIS_PER_SECOND);
        transferModules(modules);

        Set<String> moduleIdentifiersStrayImplementationOrder = new HashSet<>();
        moduleIdentifiersStrayImplementationOrder
                .addAll(implementationOrderByModuleIdentifier.keySet());
        moduleIdentifiersStrayImplementationOrder.removeAll(addedModules.keySet());
        if (!moduleIdentifiersStrayImplementationOrder.isEmpty()) {
            LOG.warn("The implementation order mapping file contains an entry for the module(s) "
                    + moduleIdentifiersStrayImplementationOrder
                    + ", but those modules do not exist in the import data.");
        }

        long modulesReady = System.currentTimeMillis();
        LOG.debug("Modules ready, took :\t"
                + (modulesReady - elementalThreatsReady) / MILLIS_PER_SECOND);
        LOG.debug("Transformation of elements complete");
        createSafeguards(implementationHints);
        long safeguardsReady = System.currentTimeMillis();
        LOG.debug(
                "Safeguards ready, took:\t" + (safeguardsReady - modulesReady) / MILLIS_PER_SECOND);
        updateElement(getRootItNetwork());
        LOG.debug("ItNetwork updated");
        LOG.debug("Import finished, took:\t"
                + (System.currentTimeMillis() - startImport) / MILLIS_PER_SECOND);
    }

    /**
     * 
     * prepares the transformation-process. especially finds the content
     * containing sub-directories (attention, structure is given by the BSI and
     * this is relying on that structure not changing)
     * 
     * When subdirectories are found, the parsing of the BSI-XML takes place,
     * the three Sets, passed as parameter, will be filled with the Java-Objects
     * representing the XML-Files
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
     * - {@link Document} into {@link Set} modules -
     * {@link ITBP2VNA.generated.threat.Document} into {@link Set} threats -
     * {@link ITBP2VNA.generated.implementationhint.Document} into {@link Set}
     * implementationHints
     * 
     * @param modules
     * @param threats
     * @param implementationHints
     * @param moduleDir
     * @param threatDir
     * @param implHintDir
     */
    private void parseBSIXml(Set<Document> modules, Set<ITBP2VNA.generated.threat.Document> threats,
            Set<ITBP2VNA.generated.implementationhint.Document> implementationHints, File moduleDir,
            File threatDir, File implHintDir) {
        for (File xmlFile : getXMLFiles(moduleDir)) {
            modules.add(ITBPParser.getInstance().parseModule(xmlFile));
        }
        for (File xmlFile : getXMLFiles(threatDir)) {
            threats.add(ITBPParser.getInstance().parseThreat(xmlFile));
        }
        for (File xmlFile : getXMLFiles(implHintDir)) {
            implementationHints.add(ITBPParser.getInstance().parseImplementationHint(xmlFile));
        }
    }

    /**
     * returns an array of {@link File} of a length of 4 the array contains the
     * subfolders of the BSI IT Baselineprotection represented in xml-Files
     * organized in the following structure:
     * 
     * 0 - module Subdirectory 1 - threat Subdirectory 2 - implementation
     * Subdirectory 3 - media Subdirectory
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
     * compares name of content-containing directory candidates to the
     * (BSI-given) names, and sets them as an element of an Array (which will be
     * returned)
     * 
     * @param moduleDir
     * @param threatDir
     * @param implHintDir
     * @param mediaDir
     * @param warningMoreThanOneDirectory
     * @param dirs
     * @param subDirectory
     */
    private void setSubDirectories(File moduleDir, File threatDir, File implHintDir, File mediaDir,
            final String warningMoreThanOneDirectory, File subDirectory, File[] dirs) {
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
    private CnATreeElement updateElement(CnATreeElement element) throws CreateBPElementException {
        try {
            UpdateElement<CnATreeElement> command = new UpdateElement<>(element, true,
                    ChangeLogEntry.STATION_ID);
            return getCommandService().executeCommand(command).getElement();
        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error creating root-it-network");
        }

    }

    /**
     * get all xmlFiles contained in a given Directory represented by a
     * {@link File}
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
            return Collections.emptyList();
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
            rootReqGroup = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID,
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
                safeguardRootGroup.setTitel(Messages.Root_Safeguard_Group_Name);
            }
        }

        if (rootThreatGroup != null) {
            rootThreatGroup.setTitel(Messages.Root_Threat_Group_Name);
            if (safeguardRootGroup != null) {
                createStructuredSubGroups(rootThreatGroup, safeguardRootGroup);
            }
        }
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
     * creates all Groups, necessary to represent the BSI-XML in a structured
     * way in verinice
     * 
     * @param rootThreatGroup
     * @param safeguardRootGroup
     * @throws CreateBPElementException
     */
    private void createStructuredSubGroups(BpThreatGroup rootThreatGroup,
            SafeguardGroup safeguardRootGroup) throws CreateBPElementException {
        elementalThreatGroup = (BpThreatGroup) createElement(BpThreatGroup.TYPE_ID, rootThreatGroup,
                Messages.Elemental_Threat_Group_Name);
        processSafeguardGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID,
                safeguardRootGroup, Messages.Process_Requirement_Group_Name);
        systemSafeguardGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID,
                safeguardRootGroup, Messages.System_Requirement_Group_Name);

        for (String name : systemIdentifierPrefixes) {
            createElement(BpRequirementGroup.TYPE_ID, systemReqGroup, name);
            createElement(SafeguardGroup.TYPE_ID, systemSafeguardGroup, name);
        }

        for (String name : processIdentifierPrefixes) {
            createElement(BpRequirementGroup.TYPE_ID, processReqGroup, name);
            createElement(SafeguardGroup.TYPE_ID, processSafeguardGroup, name);
        }
    }

    /**
     * takes the XML-Files (represented by
     * {@link ITBP2VNA.generated.implementationhint.Document} ) that are
     * representing the BSI Baseline-Protection-Compendium implementation-Hints
     * and transforms them into verinice-Objects {@link Safeguard}
     * 
     * @param implementationHints
     * @throws CreateBPElementException
     */
    private void createSafeguards(
            Set<ITBP2VNA.generated.implementationhint.Document> implementationHints)
            throws CreateBPElementException {

        Set<SafeguardGroup> subGroups = new HashSet<>(10);

        for (CnATreeElement child : systemSafeguardGroup.getChildren()) {
            if (SafeguardGroup.TYPE_ID.equals(child.getTypeId())) {
                subGroups.add((SafeguardGroup) child);
                updateElement(child);
            }
        }

        for (CnATreeElement child : processSafeguardGroup.getChildren()) {
            if (SafeguardGroup.TYPE_ID.equals(child.getTypeId())) {
                subGroups.add((SafeguardGroup) child);
                updateElement(child);
            }
        }

        updateElement(processSafeguardGroup);
        updateElement(systemSafeguardGroup);

        for (ITBP2VNA.generated.implementationhint.Document bsiSafeguard : implementationHints) {
            SafeguardGroup safeGuardParent = null;
            for (SafeguardGroup candidate : subGroups) {
                if (candidate.getTitle()
                        .startsWith(getIdentifierPrefix(bsiSafeguard.getIdentifier()))) {
                    safeGuardParent = candidate;
                    break;
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
     * determines a Module {@link BpRequirementGroup} that relates to a
     * {@link Safeguard} to create {@link CnALink} between the {@link Safeguard}
     * and the {@link BpRequirement} - children of the determined
     * {@link BpRequirementGroup}
     * 
     * @param safeguard
     * @return
     */
    private Set<Link> linkSafeguardToRequirements(Safeguard safeguard) {
        Set<Link> links = new HashSet<>();
        String groupIdentifier = getIdentifierPrefix(safeguard.getIdentifier());
        if (LOG.isDebugEnabled()) {
            LOG.debug("searching Requirement-Links for Safeguard:\t" + safeguard.getTitle()
                    + "\t with Identifier:\t" + safeguard.getIdentifier());
            LOG.debug("GroupIdentifier:\t" + groupIdentifier);
        }
        BpRequirementGroup parent = (BpRequirementGroup) getRequirementParentGroup(groupIdentifier,
                BpRequirementGroup.TYPE_ID, systemReqGroup, processReqGroup);
        String safeguardIdentifier = safeguard.getIdentifier();

        String comparableIdentifier = getRequirementIdentifierForSafeguardLink(safeguardIdentifier);
        for (CnATreeElement requirement : parent.getChildren()) {
            links.addAll(createSafeGuardToRequirementLinks(safeguard, comparableIdentifier,
                    requirement));
        }
        return links;
    }

    /**
     * to find relating requirements to link to, the identifier has to changed
     * from '$group.$x.M.$y' to '$group.$x.A.$y'
     */
    private String getRequirementIdentifierForSafeguardLink(String safeguardIdentifier) {
        StringTokenizer tokenizer = new StringTokenizer(safeguardIdentifier, ".");
        StringBuilder sb = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (!tokenizer.hasMoreTokens()) {
                sb.append(token.replace('M', 'A'));
            } else {
                sb.append(token).append('.');
            }
        }
        return sb.toString();
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
            if (LOG.isDebugEnabled()) {

                LOG.debug("Child is Requirement:\t" + requirement.getTitle() + " with identifier:\t"
                        + ((BpRequirement) requirement).getIdentifier());
            }
            if (((BpRequirement) requirement).getIdentifier().equals(comparableIdentifier)) {
                links.add(new Link((BpRequirement) requirement, safeguard,
                        BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, ""));
            }
        } else if (requirement instanceof BpRequirementGroup) {
            if (LOG.isDebugEnabled()) {

                LOG.debug("child is RequirementGroup :\t" + requirement.getTitle());
            }
            for (CnATreeElement child : requirement.getChildren()) {
                if (child instanceof BpRequirement) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("child is grandchild:\t" + child.getTitle()
                                + " with identifier:\t" + ((BpRequirement) child).getIdentifier());
                    }
                    if (((BpRequirement) child).getIdentifier().equals(comparableIdentifier)) {
                        links.add(new Link(((BpRequirement) child), safeguard,
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
    private CnATreeElement createElement(String typeId, CnATreeElement parent, String title)
            throws CreateBPElementException {
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

        if (rootNetwork == null) {
            LOG.error("Root-IT-Network not initialized. Ending import");
            return;
        }

        for (Document bsiModule : modules) {
            String groupIdentifier = getIdentifierPrefix(bsiModule.getIdentifier());

            BpRequirementGroup parent = (BpRequirementGroup) getRequirementParentGroup(
                    groupIdentifier, BpRequirementGroup.TYPE_ID, systemReqGroup, processReqGroup);

            BpRequirementGroup veriniceModule = null;

            if (!addedModules.containsKey(bsiModule.getIdentifier()) && parent != null) {
                veriniceModule = createModule(bsiModule, parent);
                linkElementalThreats(bsiModule);
                addedModules.put(bsiModule.getIdentifier(), veriniceModule);
            }
        }
    }

    /**
     * transform a single given {@link Document} into a
     * {@link BpRequirementGroup}
     * 
     * @param bsiModule
     * @param parent
     * @return
     * @throws CreateBPElementException
     */
    private BpRequirementGroup createModule(Document bsiModule, BpRequirementGroup parent)
            throws CreateBPElementException {
        BpRequirementGroup veriniceModule = null;
        if (parent != null) {
            String moduleIdentifier = bsiModule.getIdentifier();
            String moduleTitle = bsiModule.getTitle();
            veriniceModule = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID, parent,
                    moduleTitle);

            veriniceModule.setIdentifier(moduleIdentifier);
            veriniceModule
                    .setObjectBrowserDescription(HtmlHelper.getCompleteModuleXMLText(bsiModule));
            veriniceModule.setLastChange(getBSIDate(bsiModule.getLastChange()));
            String implementationOrder = implementationOrderByModuleIdentifier
                    .get(moduleIdentifier);
            if (implementationOrder != null) {
                veriniceModule.setImplementationOrder(implementationOrder);
            } else {
                LOG.warn("No implementation order specified for module '" + moduleIdentifier + "' ("
                        + moduleTitle + ")");
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Module : \t" + veriniceModule.getTitle() + " created");
            }
            createRequirementsForModule(bsiModule, veriniceModule);
        }
        return veriniceModule;
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
                BpThreat threat = getElementalThreatByIdentifier(threatIdentifier);

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
    private BpThreat getElementalThreatByIdentifier(String identifier) {
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
     * generate elemental-threats defined in a {@link Document} as instances of
     * {@link BpThreat} within the given structure
     * 
     * @param threats
     * @throws CreateBPElementException
     */
    private void generateElementalThreats(Set<ITBP2VNA.generated.threat.Document> threats)
            throws CreateBPElementException {
        for (ITBP2VNA.generated.threat.Document bsiThreat : threats) {
            if (!addedThreats.containsKey(bsiThreat.getIdentifier())) {
                BpThreat veriniceThreat = (BpThreat) createElement(BpThreat.TYPE_ID,
                        elementalThreatGroup, bsiThreat.getTitle());

                veriniceThreat.setIdentifier(bsiThreat.getIdentifier());
                String plainDescription = HtmlHelper.getAnyObjectDescription(
                        bsiThreat.getFullTitle(), 1, bsiThreat.getDescription());
                veriniceThreat.setConfidentiality(
                        Boolean.parseBoolean(bsiThreat.getCia().getConfidentiality()));
                veriniceThreat
                        .setIntegrity(Boolean.parseBoolean(bsiThreat.getCia().getIntegrity()));
                veriniceThreat.setAvailibility(
                        Boolean.parseBoolean(bsiThreat.getCia().getAvailability()));
                if (StringUtils.isNotEmpty(plainDescription)) {
                    veriniceThreat.setObjectBrowserDescription(plainDescription);
                }
                veriniceThreat = (BpThreat) updateElement(veriniceThreat);
                addedThreats.put(bsiThreat.getIdentifier(), veriniceThreat);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Threat : \t" + veriniceThreat.getTitle() + " created");
                }
            }
        }
    }

    /**
     * get the {@link BpRequirementGroup} (module) that contains a specific
     * {@link BpRequirement}
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
     * create Safeguards according to their level-definition ( BASIC, STANDARD,
     * HIGH) and links them to the {@link BpRequirement} defined in the
     * references module {@link BpRequirementGroup}
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
            SafeguardGroup parent) throws CreateBPElementException {

        final String defaultErrorMsg = "Could not create Safeguard:\t";

        List<Link> links = new ArrayList<>();
        Safeguards bsiModule = bsiSafeguardDocument.getSafeguards();
        List<ITBP2VNA.generated.implementationhint.Safeguard> safeGuards = bsiModule
                .getBasicSafeguards().getSafeguard();
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : safeGuards) {

            SafeguardGroup safeGuardParent = getSafeguardParent(parent,
                    bsiSafeguard.getIdentifier());

            Safeguard safeguard = createSafeguard(safeGuardParent, bsiSafeguard,
                    Messages.Qualifier_Basic, bsiSafeguardDocument.getLastChange().toString());
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
            } else {
                LOG.warn(defaultErrorMsg + bsiSafeguard.getTitle());
            }
        }
        safeGuards.clear();
        safeGuards = bsiModule.getStandardSafeguards().getSafeguard();
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : safeGuards) {
            SafeguardGroup safeGuardParent = getSafeguardParent(parent,
                    bsiSafeguard.getIdentifier());
            Safeguard safeguard = createSafeguard(safeGuardParent, bsiSafeguard,
                    Messages.Qualifier_Standard, bsiSafeguardDocument.getLastChange().toString());
            if (safeguard != null) {
                links.addAll(linkSafeguardToRequirements(safeguard));
            } else {
                LOG.warn(defaultErrorMsg + bsiSafeguard.getTitle());
            }
        }
        safeGuards.clear();
        safeGuards = bsiModule.getHighLevelSafeguards().getSafeguard();
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : safeGuards) {
            SafeguardGroup safeGuardParent = getSafeguardParent(parent,
                    bsiSafeguard.getIdentifier());
            Safeguard safeguard = createSafeguard(safeGuardParent, bsiSafeguard,
                    Messages.Qualifier_High, bsiSafeguardDocument.getLastChange().toString());
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
     * {@link ITBP2VNA.generated.implementationhint.Safeguard} to a
     * {@link Safeguard} and sets all possible properties
     * 
     * @param parent
     * @param bsiSafeguard
     * @param qualifier
     * @return
     * @throws CreateBPElementException
     */
    private Safeguard createSafeguard(SafeguardGroup parent,
            ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard, String qualifier,
            String lastChange) throws CreateBPElementException {
        if (parent != null) {
            Safeguard safeguard = (Safeguard) createElement(Safeguard.TYPE_ID, parent,
                    bsiSafeguard.getTitle());
            updateElement(safeguard);
            safeguard = setSafeguardProperties(bsiSafeguard, qualifier, lastChange, safeguard);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Safeguard : \t" + safeguard.getTitle() + "created ");
            }
            return (Safeguard) updateElement(safeguard);
        }
        return null;
    }

    /**
     * transforms all attributes from BSI-XML to {@link CnATreeElement} and sets
     * them for the {@link Safeguard} that is about to be created
     * 
     * @param bsiSafeguard
     * @param qualifier
     * @param lastChange
     * @param trueValue
     * @param safeguard
     * @throws CreateBPElementException
     */
    private Safeguard setSafeguardProperties(
            ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard, String qualifier,
            String lastChange, Safeguard safeguard) throws CreateBPElementException {
        safeguard.setIdentifier(bsiSafeguard.getIdentifier());

        String qualifierOptionValue = getSafeguardQualifierOptionValue(qualifier);

        safeguard.setQualifier(qualifierOptionValue);
        safeguard.setTitle(bsiSafeguard.getTitle());
        safeguard.setLastChange(getBSIDate(lastChange));
        safeguard.setIsAffectsConfidentiality(
                Boolean.parseBoolean(bsiSafeguard.getCia().getConfidentiality()));
        safeguard.setIsAffectsAvailability(
                Boolean.parseBoolean(bsiSafeguard.getCia().getAvailability()));
        safeguard.setIsAffectsIntegrity(Boolean.parseBoolean(bsiSafeguard.getCia().getIntegrity()));

        String descriptionTitle = extendTitleForObjectBrowser(safeguard.getTitle(),
                safeguard.getIdentifier(), new CIAWrapper(safeguard.isAffectsConfidentiality(),
                        safeguard.isAffectsIntegrity(), safeguard.isAffectsAvailability()));

        String plainDescription = HtmlHelper.getAnyObjectDescription(descriptionTitle, 1,
                bsiSafeguard.getDescription().getContent());
        if (StringUtils.isNotEmpty(plainDescription)) {
            safeguard.setObjectBrowserDescription(plainDescription);
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("No description found for:\t" + bsiSafeguard.getTitle());
        }

        return safeguard;
    }

    private String getSafeguardQualifierOptionValue(String qualifier) {
        if ("BASIC".equals(qualifier)) {
            return Safeguard.PROP_QUALIFIER_BASIC;
        } else if ("STANDARD".equals(qualifier)) {
            return Safeguard.PROP_QUALIFIER_STANDARD;
        } else if ("HIGH".equals(qualifier) || "HOCH".equals(qualifier)) {
            return Safeguard.PROP_QUALIFIER_HIGH;
        }
        return "";
    }

    private String getRequirementQualifierOptionValue(String qualifier) {
        if ("BASIC".equals(qualifier)) {
            return BpRequirement.PROP_QUALIFIER_BASIC;
        } else if ("STANDARD".equals(qualifier)) {
            return BpRequirement.PROP_QUALIFIER_STANDARD;
        } else if ("HIGH".equals(qualifier) || "HOCH".equals(qualifier)) {
            return BpRequirement.PROP_QUALIFIER_HIGH;
        }
        return "";
    }

    /**
     * {@link Safeguard} are related to {@link BpRequirement} which are related
     * to {@link BpRequirementGroup}. The {@link Safeguard} are structured the
     * same way as the {@link BpRequirementGroup} (modules), so there needs to
     * be a {@link SafeguardGroup} for every instance of
     * {@link BpRequirementGroup}
     * 
     * this method returns this {@link SafeguardGroup} for a given safeguard-
     * identifier or creates it, if not existent yet
     * 
     * @param rootGroup
     * @param identifier
     * @return
     * @throws CreateBPElementException
     */
    private SafeguardGroup getSafeguardParent(SafeguardGroup rootGroup, String identifier)
            throws CreateBPElementException {
        SafeguardGroup safeguardParent = null;
        if (identifier.contains(".M")) {
            String moduleIdentifier = identifier.substring(0, identifier.lastIndexOf(".M"));
            String subGroupIdentifier = identifier.substring(0, identifier.indexOf('.'));
            String moduleTitle = null;
            if (addedModules.containsKey(moduleIdentifier)) {
                moduleTitle = addedModules.get(moduleIdentifier).getTitle();
            } else {
                return null;
            }
            // safeguardparent is a module
            safeguardParent = (SafeguardGroup) getIBGroupByNameRecursive(rootGroup, moduleTitle);
            // moduleparent is a safeguardGroup like "APP", "DER", "INF, "CON",
            // ...
            SafeguardGroup safeguardRoot = (SafeguardGroup) processSafeguardGroup.getParent();
            SafeguardGroup moduleParent = (SafeguardGroup) getIBGroupByNameRecursive(
                    (IBpGroup) safeguardRoot, subGroupIdentifier);
            if (safeguardParent == null && moduleParent != null) {
                safeguardParent = (SafeguardGroup) getIBGroupByNameRecursive(
                        (IBpGroup) moduleParent, moduleTitle);
                if (safeguardParent == null) {
                    safeguardParent = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID,
                            moduleParent, moduleTitle);
                    safeguardParent.setIdentifier(moduleIdentifier);
                    safeguardParent = (SafeguardGroup) updateElement(safeguardParent);
                }
            }
        }
        return safeguardParent;

    }

    /**
     * searches for a {@link IBpGroup} which is a child of an given
     * {@link IBpGroup} and traverses the given subtree dynamically
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
                    matchingGroup = (IBpGroup) child;
                } else {
                    if (matchingGroup == null || !name.equals(matchingGroup.getTitle())) {
                        matchingGroup = getIBGroupByNameRecursive((IBpGroup) child, name);
                    }
                }
            }
        }
        return matchingGroup;
    }

    /**
     * creates all {@link BpRequirement} for a given {@link Document}, adding a
     * qualifier, given bei the list they are sorted into within the
     * {@link Document}
     * 
     * @param bsiModule
     * @param parent
     * @throws CreateBPElementException
     */
    private void createRequirementsForModule(Document bsiModule, BpRequirementGroup parent)
            throws CreateBPElementException {
        for (Requirement bsiRequirement : bsiModule.getRequirements().getBasicRequirements()
                .getRequirement()) {
            createRequirement(parent, bsiRequirement, Messages.Qualifier_Basic);
        }
        for (Requirement bsiRequirement : bsiModule.getRequirements().getStandardRequirements()
                .getRequirement()) {
            createRequirement(parent, bsiRequirement, Messages.Qualifier_Standard);
        }
        for (Requirement bsiRequirement : bsiModule.getRequirements().getHighLevelRequirements()
                .getRequirement()) {
            createRequirement(parent, bsiRequirement, Messages.Qualifier_High);
        }

    }

    /**
     * transforms a single given {@link Requirement} into a
     * {@link BpRequirement} and adds it to the caching-map addedReqs
     * 
     * @param parent
     * @param bsiRequirement
     * @param qualifier
     * @return
     * @throws CreateBPElementException
     */
    private BpRequirement createRequirement(BpRequirementGroup parent, Requirement bsiRequirement,
            String qualifier) throws CreateBPElementException {
        if (!addedReqs.containsKey(bsiRequirement.getIdentifier())) {
            BpRequirement veriniceRequirement = null;
            veriniceRequirement = (BpRequirement) createElement(BpRequirement.TYPE_ID, parent,
                    bsiRequirement.getTitle());
            veriniceRequirement.setIdentifier(bsiRequirement.getIdentifier());
            veriniceRequirement.setTitle(bsiRequirement.getTitle());
            veriniceRequirement.setLastChange(parent.getLastChange());
            veriniceRequirement.setIsAffectsConfidentiality(
                    Boolean.parseBoolean(bsiRequirement.getCia().getConfidentiality()));
            veriniceRequirement.setIsAffectsIntegrity(
                    Boolean.parseBoolean(bsiRequirement.getCia().getIntegrity()));
            veriniceRequirement.setIsAffectsAvailability(
                    Boolean.parseBoolean(bsiRequirement.getCia().getAvailability()));

            String title = extendTitleForObjectBrowser(bsiRequirement.getTitle(),
                    bsiRequirement.getIdentifier(),
                    new CIAWrapper(veriniceRequirement.IsAffectsConfidentiality(),
                            veriniceRequirement.IsAffectsIntegrity(),
                            veriniceRequirement.IsAffectsAvailability()));

            veriniceRequirement.setObjectBrowserDescription(HtmlHelper.getAnyElementDescription(
                    title.trim(), -1, -1, -1, bsiRequirement.getDescription().getAny()));

            String qualifierOptionValue = getRequirementQualifierOptionValue(qualifier);

            veriniceRequirement.setQualifier(qualifierOptionValue);
            addedReqs.put(bsiRequirement.getIdentifier(), veriniceRequirement);
            return (BpRequirement) updateElement(veriniceRequirement);
        } else {
            return addedReqs.get(bsiRequirement.getIdentifier());
        }
    }

    /**
     * creates the title for an {@link BpRequirement} / {@link Safeguard} to be
     * shown in the objectBrowser-Description
     * 
     * adds the identifier as a prefix
     * 
     * adds the responsible roles and affecting CIA as a suffix
     * 
     * @param bsiRequirement
     * @param veriniceRequirement
     * @return
     */
    private String extendTitleForObjectBrowser(String title, String identifier, CIAWrapper cia) {

        String titleBuilder = identifier + " " + title;
        StringBuilder ciaBuilder = new StringBuilder();
        ciaBuilder.append(cia.isConfidentiality() ? "C" : "");
        ciaBuilder.append(cia.isIntegrity() ? "I" : "");
        ciaBuilder.append(cia.isAvailability() ? "A" : "");
        ciaBuilder.append(ciaBuilder.length() > 0 ? ")" : "");
        String ciaTitle = ciaBuilder.toString();
        if (ciaTitle.length() > 0) {
            ciaTitle = "(" + ciaTitle;
        }

        titleBuilder = titleBuilder + " " + ciaTitle;
        return titleBuilder;
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
            throw new CreateBPElementException("Could not parse bsiDate:\t" + dateString);
        }
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
                CreateITNetwork command = new CreateITNetwork(model, true);
                command = getCommandService().executeCommand(command);
                rootNetwork = command.getNewElement();
                StringBuilder titleBuilder = new StringBuilder();
                titleBuilder.append(Messages.IT_Network_Name);
                titleBuilder.append("_");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String dateInISO = df.format(new Date());
                titleBuilder.append(dateInISO);
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
     * @param commandService
     *            the commandService to set
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
     * @param daoFactory
     *            the daoFactory to set
     */
    public void setDaoFactory(IDAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

}
