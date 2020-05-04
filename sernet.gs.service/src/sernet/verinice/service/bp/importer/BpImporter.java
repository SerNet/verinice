package sernet.verinice.service.bp.importer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ITBP2VNA.generated.implementationhint.Document.Safeguards;
import ITBP2VNA.generated.module.Cia;
import ITBP2VNA.generated.module.Document;
import ITBP2VNA.generated.module.Document.Crossreferences;
import ITBP2VNA.generated.module.ElementalthreatRef;
import ITBP2VNA.generated.module.Requirement;
import ITBP2VNA.generated.module.RequirementRef;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.SecurityLevel;
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

    private static final List<String> processIdentifierPrefixes = Collections
            .unmodifiableList(Arrays.asList("CON", "DER", "ISMS", "OPS", "ORP"));
    private static final List<String> systemIdentifierPrefixes = Collections
            .unmodifiableList(Arrays.asList("APP", "IND", "INF", "NET", "SYS"));

    private static final Pattern MODULE_IDENTIFIER = Pattern.compile("([A-Z]+)\\.(\\d+\\.)*\\d+");

    private String xmlRootDirectory = null;

    private BpRequirementGroup processReqGroup = null;
    private BpRequirementGroup systemReqGroup = null;

    private SafeguardGroup processSafeguardGroup = null;
    private SafeguardGroup systemSafeguardGroup = null;

    private BpThreatGroup elementalThreatGroup = null;

    ICommandService commandService;
    IDAOFactory daoFactory;

    private static final String SUBDIRECTORY_MODULES = "bausteine";
    private static final String SUBDIRECTORY_THREATS = "elementare_gefaehrdungen_1";
    private static final String SUBDIRECTORY_IMPL_HINTS = "umsetzungshinweise";

    private static final String BP_REQUIREMENT_IMPLEMENTATION_ORDER_R1 = "bp_requirement_group_impl_seq_r1";
    private static final String BP_REQUIREMENT_IMPLEMENTATION_ORDER_R2 = "bp_requirement_group_impl_seq_r2";
    private static final String BP_REQUIREMENT_IMPLEMENTATION_ORDER_R3 = "bp_requirement_group_impl_seq_r3";

    private static final int MILLIS_PER_SECOND = 1000;

    private Map<String, BpThreat> addedThreats = new HashMap<>();
    private Map<String, BpRequirement> addedReqs = new HashMap<>();
    private Map<String, BpRequirementGroup> addedModules = new HashMap<>();

    public BpImporter(String xmlRoot) {
        this.xmlRootDirectory = xmlRoot;
    }

    /**
     * main BSI-XML to vna transforming method
     *
     */
    public void run() throws CreateBPElementException {
        long startImport = System.currentTimeMillis();

        if (xmlRootDirectory == null || xmlRootDirectory.length() == 0) {
            LOG.error("Wrong number of arguments, please provide root-Directory to XML-Archive");
            return;
        }
        File rootDir = new File(xmlRootDirectory);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            LOG.error(rootDir + "is not a valid import directory");
            return;
        }

        ImportData importData = parseContent(rootDir);
        ImportMetadata importMetadata = parseMetadataFiles(rootDir);

        LOG.debug("Successfully parsed modules:\t" + importData.modules.size());
        LOG.debug("Successfully parsed threats:\t" + importData.threats.size());
        LOG.debug("Successfully parsed implementation hints:\t"
                + importData.implementationHints.size());

        long veryBeginning = System.currentTimeMillis();
        ItNetwork rootNetwork = prepareITNetwork(importMetadata);
        long itnetworkReady = System.currentTimeMillis();
        LOG.debug("ITNetwork prepared, took :\t"
                + (itnetworkReady - veryBeginning) / MILLIS_PER_SECOND);
        generateElementalThreats(importData.threats, importMetadata);
        long elementalThreatsReady = System.currentTimeMillis();
        LOG.debug("Elementalthreats ready, took :\t"
                + (elementalThreatsReady - itnetworkReady) / MILLIS_PER_SECOND);

        transferModules(importData, importMetadata);

        Set<String> moduleIdentifiersStrayImplementationOrder = new HashSet<>();
        moduleIdentifiersStrayImplementationOrder
                .addAll(importMetadata.implementationOrderByModuleIdentifier.keySet());
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
        createSafeguards(importData.implementationHints, importMetadata);
        long safeguardsReady = System.currentTimeMillis();
        LOG.debug(
                "Safeguards ready, took:\t" + (safeguardsReady - modulesReady) / MILLIS_PER_SECOND);
        updateElement(rootNetwork);
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
     */
    private static ImportData parseContent(File rootDir) {

        File moduleDir = rootDir.toPath().resolve(SUBDIRECTORY_MODULES).toFile();
        File threatDir = rootDir.toPath().resolve(SUBDIRECTORY_THREATS).toFile();
        File implHintDir = rootDir.toPath().resolve(SUBDIRECTORY_IMPL_HINTS).toFile();

        ITBPParser itbpParser = ITBPParser.getInstance();
        Set<Document> modules = getXMLFiles(moduleDir).stream().map(itbpParser::parseModule)
                .collect(Collectors.toSet());
        Set<ITBP2VNA.generated.threat.Document> threats = getXMLFiles(threatDir).stream()
                .map(itbpParser::parseThreat).collect(Collectors.toSet());
        Set<ITBP2VNA.generated.implementationhint.Document> implementationHints = getXMLFiles(
                implHintDir).stream().map(itbpParser::parseImplementationHint)
                        .collect(Collectors.toSet());

        return new ImportData(modules, threats, implementationHints);

    }

    /**
     * Read additional metadata required for the import from the import
     * directory
     */
    private static ImportMetadata parseMetadataFiles(File rootDirectory) {
        Map<String, String> implementationOrderByModuleIdentifier = readImplementationOrder(
                rootDirectory.toPath().resolve("implementation-order.properties"));

        String release = null;

        // read additional metatada from the properties file at the
        // given location. Currently, there is a single optional property
        // denoting the compendium release version, i.e. 2019-0
        Path metadataProperties = rootDirectory.toPath().resolve("metadata.properties");
        if (metadataProperties.toFile().exists()) {
            Properties metadata = loadProperties(metadataProperties);

            release = metadata.getProperty("release");
            Matcher releaseMatcher = Pattern.compile("(\\d{4})-\\d+").matcher(release);
            if (!releaseMatcher.matches()) {
                throw new IllegalArgumentException("Invalid release: " + release);
            }
            String year = releaseMatcher.group(1);
            if (Integer.parseInt(year) != ZonedDateTime.now().getYear()) {
                LOG.warn("Release " + release
                        + " does not match the current year, continuing anyway");
            }

            LOG.info("Importing release " + release);

        } else {
            LOG.warn("metadata.properties file not found, using defaults");
        }

        return new ImportMetadata(implementationOrderByModuleIdentifier, release);
    }

    /**
     * Reads the modules' implementation order from the properties file at the
     * given location. The file contains a mapping from module IDs to
     * implementation orders, i.e. <code>ISMS.1=R1</code>.
     */
    private static Map<String, String> readImplementationOrder(Path implementationOrderProperties) {
        Properties implementationOrder = loadProperties(implementationOrderProperties);

        Map<String, String> result = new HashMap<>();
        for (Entry<Object, Object> entry : implementationOrder.entrySet()) {
            String moduleIdentifier = (String) entry.getKey();
            validateModuleIdentifier(moduleIdentifier);

            String implementationOrderName = (String) entry.getValue();
            String implementationOrderPropertyValue;
            switch (implementationOrderName) {
            case "R1":
                implementationOrderPropertyValue = BP_REQUIREMENT_IMPLEMENTATION_ORDER_R1;
                break;
            case "R2":
                implementationOrderPropertyValue = BP_REQUIREMENT_IMPLEMENTATION_ORDER_R2;
                break;
            case "R3":
                implementationOrderPropertyValue = BP_REQUIREMENT_IMPLEMENTATION_ORDER_R3;
                break;
            default:
                throw new RuntimeException("Illegal implementation order '"
                        + implementationOrderName + "' for module '" + moduleIdentifier + "'"
                        + ", allowed values: R1, R2, R3");
            }
            String existingMapping = result.put(moduleIdentifier, implementationOrderPropertyValue);
            if (existingMapping != null) {
                throw new RuntimeException(
                        "Found duplicate implementation order mapping for module '"
                                + moduleIdentifier + "'" + ".");
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static void validateModuleIdentifier(String moduleIdentifier) {
        Matcher matcher = MODULE_IDENTIFIER.matcher(moduleIdentifier);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal module name: '" + moduleIdentifier + "'.");
        }
        String prefix = matcher.group(1);
        if (!systemIdentifierPrefixes.contains(prefix)
                && !processIdentifierPrefixes.contains(prefix)) {
            throw new IllegalArgumentException("Illegal system identifier prefix '" + prefix
                    + "' used in module identifier '" + moduleIdentifier + "'.");
        }
    }

    private static Properties loadProperties(Path propertiesFile) {
        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(propertiesFile)) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load additional information from properties files", e);
        }
        return properties;
    }

    /**
     * update a given {@link CnATreeElement} to write changes to db
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
     */
    private static List<File> getXMLFiles(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] directories = dir.listFiles(
                    (FileFilter) pathname -> FilenameUtils.isExtension(pathname.getName(), "xml"));
            return Arrays.asList(directories);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * creates an {@link ItNetwork} and its substructure to prepare it for
     * transforming the bsi-data (xml) into verinice Objects
     */
    private ItNetwork prepareITNetwork(ImportMetadata importMetadata)
            throws CreateBPElementException {

        ItNetwork rootNetwork = createRootItNetwork(importMetadata);

        BpRequirementGroup rootReqGroup = (BpRequirementGroup) createElement(
                BpRequirementGroup.TYPE_ID, rootNetwork, Messages.Root_Requirement_Group_Name,
                BpRequirementGroup.PROP_RELEASE, importMetadata.release);

        systemReqGroup = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID,
                rootReqGroup, Messages.System_Requirement_Group_Name,
                BpRequirementGroup.PROP_RELEASE, importMetadata.release);

        processReqGroup = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID,
                rootReqGroup, Messages.Process_Requirement_Group_Name,
                BpRequirementGroup.PROP_RELEASE, importMetadata.release);

        BpThreatGroup rootThreatGroup = (BpThreatGroup) createElement(BpThreatGroup.TYPE_ID,
                rootNetwork, Messages.Root_Threat_Group_Name, BpThreatGroup.PROP_RELEASE,
                importMetadata.release);

        SafeguardGroup safeguardRootGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID,
                rootNetwork, Messages.Root_Safeguard_Group_Name, SafeguardGroup.PROP_RELEASE,
                importMetadata.release);

        createStructuredSubGroups(rootThreatGroup, safeguardRootGroup, importMetadata);

        return rootNetwork;
    }

    /**
     * creates all Groups, necessary to represent the BSI-XML in a structured
     * way in verinice
     */
    private void createStructuredSubGroups(BpThreatGroup rootThreatGroup,
            SafeguardGroup safeguardRootGroup, ImportMetadata importMetadata)
            throws CreateBPElementException {
        elementalThreatGroup = (BpThreatGroup) createElement(BpThreatGroup.TYPE_ID, rootThreatGroup,
                Messages.Elemental_Threat_Group_Name, BpThreatGroup.PROP_RELEASE,
                importMetadata.release);
        processSafeguardGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID,
                safeguardRootGroup, Messages.Process_Requirement_Group_Name,
                SafeguardGroup.PROP_RELEASE, importMetadata.release);
        systemSafeguardGroup = (SafeguardGroup) createElement(SafeguardGroup.TYPE_ID,
                safeguardRootGroup, Messages.System_Requirement_Group_Name,
                SafeguardGroup.PROP_RELEASE, importMetadata.release);

        for (String name : systemIdentifierPrefixes) {
            createElement(BpRequirementGroup.TYPE_ID, systemReqGroup, name,
                    BpRequirementGroup.PROP_RELEASE, importMetadata.release);
            createElement(SafeguardGroup.TYPE_ID, systemSafeguardGroup, name,
                    SafeguardGroup.PROP_RELEASE, importMetadata.release);
        }

        for (String name : processIdentifierPrefixes) {
            createElement(BpRequirementGroup.TYPE_ID, processReqGroup, name,
                    BpRequirementGroup.PROP_RELEASE, importMetadata.release);
            createElement(SafeguardGroup.TYPE_ID, processSafeguardGroup, name,
                    SafeguardGroup.PROP_RELEASE, importMetadata.release);
        }
    }

    /**
     * takes the XML-Files (represented by
     * {@link ITBP2VNA.generated.implementationhint.Document} ) that are
     * representing the BSI Baseline-Protection-Compendium implementation-Hints
     * and transforms them into verinice-Objects {@link Safeguard}
     */
    private void createSafeguards(
            Set<ITBP2VNA.generated.implementationhint.Document> implementationHints,
            ImportMetadata importMetadata) throws CreateBPElementException {

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
                createSafeguardsForModule(bsiSafeguard, safeGuardParent, importMetadata);
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
    private static String getRequirementIdentifierForSafeguardLink(String safeguardIdentifier) {
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
     */
    private CnATreeElement createElement(String typeId, CnATreeElement parent, String title,
            String releasePropertyId, String release) throws CreateBPElementException {
        CreateElement<CnATreeElement> command = new CreateElement<CnATreeElement>(parent, typeId,
                title) {
            private static final long serialVersionUID = 8513409513498160007L;

            @Override
            protected CnATreeElement saveElement() {
                if (release != null) {
                    element.setSimpleProperty(releasePropertyId, release);
                }
                return super.saveElement();
            }
        };
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
     */
    private void transferModules(ImportData importData, ImportMetadata importMetadata)
            throws CreateBPElementException {

        Map<String, ITBP2VNA.generated.implementationhint.Document> implementationHintsByIdentifier = importData.implementationHints
                .stream()
                .collect(Collectors.toMap(
                        ITBP2VNA.generated.implementationhint.Document::getIdentifier,
                        Function.identity()));
        for (Document bsiModule : importData.modules) {
            String moduleIdentifier = bsiModule.getIdentifier();
            String groupIdentifier = getIdentifierPrefix(moduleIdentifier);

            BpRequirementGroup parent = (BpRequirementGroup) getRequirementParentGroup(
                    groupIdentifier, BpRequirementGroup.TYPE_ID, systemReqGroup, processReqGroup);

            BpRequirementGroup veriniceModule = null;

            if (!addedModules.containsKey(moduleIdentifier) && parent != null) {
                veriniceModule = createModule(bsiModule, parent,
                        implementationHintsByIdentifier.get(moduleIdentifier), importMetadata);
                linkElementalThreats(bsiModule);
                addedModules.put(moduleIdentifier, veriniceModule);
            }
        }
    }

    /**
     * transform a single given {@link Document} into a
     * {@link BpRequirementGroup}
     */
    private BpRequirementGroup createModule(Document bsiModule, BpRequirementGroup parent,
            ITBP2VNA.generated.implementationhint.Document implementationHint,
            ImportMetadata importMetadata) throws CreateBPElementException {
        BpRequirementGroup veriniceModule = null;
        if (parent != null) {
            String moduleIdentifier = bsiModule.getIdentifier();
            String moduleTitle = bsiModule.getTitle();
            veriniceModule = (BpRequirementGroup) createElement(BpRequirementGroup.TYPE_ID, parent,
                    moduleTitle, BpRequirementGroup.PROP_RELEASE, importMetadata.release);

            veriniceModule.setIdentifier(moduleIdentifier);
            veriniceModule
                    .setObjectBrowserDescription(HtmlHelper.getCompleteModuleXMLText(bsiModule));
            veriniceModule.setLastChange(getBSIDate(bsiModule.getLastChange()));
            String implementationOrder = importMetadata.implementationOrderByModuleIdentifier
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
            createRequirementsForModule(bsiModule, veriniceModule, implementationHint,
                    importMetadata);
        }
        return veriniceModule;
    }

    /**
     * create links between {@link BpThreat} and related {@link BpRequirement}
     * like they are defined in the given {@link Document}
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
     */
    private void generateElementalThreats(Set<ITBP2VNA.generated.threat.Document> threats,
            ImportMetadata importMetadata) throws CreateBPElementException {
        for (ITBP2VNA.generated.threat.Document bsiThreat : threats) {
            if (!addedThreats.containsKey(bsiThreat.getIdentifier())) {
                BpThreat veriniceThreat = (BpThreat) createElement(BpThreat.TYPE_ID,
                        elementalThreatGroup, bsiThreat.getTitle(), BpThreat.PROP_RELEASE,
                        importMetadata.release);

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

    private static String getIdentifierPrefix(String id) {
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
     */
    private void createSafeguardsForModule(
            ITBP2VNA.generated.implementationhint.Document bsiSafeguardDocument,
            SafeguardGroup parent, ImportMetadata importMetadata) throws CreateBPElementException {

        final String defaultErrorMsg = "Could not create Safeguard:\t";

        List<Link> links = new ArrayList<>();
        Safeguards bsiModule = bsiSafeguardDocument.getSafeguards();
        List<ITBP2VNA.generated.implementationhint.Safeguard> safeGuards = bsiModule
                .getBasicSafeguards().getSafeguard();
        for (ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard : safeGuards) {

            SafeguardGroup safeGuardParent = getSafeguardParent(parent,
                    bsiSafeguard.getIdentifier(), importMetadata);

            Safeguard safeguard = createSafeguard(safeGuardParent, bsiSafeguard,
                    Messages.Qualifier_Basic, bsiSafeguardDocument.getLastChange().toString(),
                    importMetadata);
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
                    bsiSafeguard.getIdentifier(), importMetadata);
            Safeguard safeguard = createSafeguard(safeGuardParent, bsiSafeguard,
                    Messages.Qualifier_Standard, bsiSafeguardDocument.getLastChange().toString(),
                    importMetadata);
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
                    bsiSafeguard.getIdentifier(), importMetadata);
            Safeguard safeguard = createSafeguard(safeGuardParent, bsiSafeguard,
                    Messages.Qualifier_High, bsiSafeguardDocument.getLastChange().toString(),
                    importMetadata);
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
     */
    private Safeguard createSafeguard(SafeguardGroup parent,
            ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard, String qualifier,
            String lastChange, ImportMetadata importMetadata) throws CreateBPElementException {
        if (parent != null) {
            Safeguard safeguard = (Safeguard) createElement(Safeguard.TYPE_ID, parent,
                    bsiSafeguard.getTitle(), Safeguard.PROP_RELEASE, importMetadata.release);
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
     */
    private Safeguard setSafeguardProperties(
            ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard, String qualifier,
            String lastChange, Safeguard safeguard) throws CreateBPElementException {
        safeguard.setIdentifier(bsiSafeguard.getIdentifier());

        SecurityLevel securityLevel = getSecurityLevelFromQualifier(qualifier);

        safeguard.setSecurityLevel(securityLevel);
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

    private SecurityLevel getSecurityLevelFromQualifier(String qualifier) {
        if ("BASIC".equals(qualifier)) {
            return SecurityLevel.BASIC;
        } else if ("STANDARD".equals(qualifier)) {
            return SecurityLevel.STANDARD;
        } else if ("HIGH".equals(qualifier) || "HOCH".equals(qualifier)) {
            return SecurityLevel.HIGH;
        }
        return null;
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
     */
    private SafeguardGroup getSafeguardParent(SafeguardGroup rootGroup, String identifier,
            ImportMetadata importMetadata) throws CreateBPElementException {
        SafeguardGroup safeguardParent = null;
        if (identifier.contains(".M")) {
            String moduleIdentifier = identifier.substring(0, identifier.lastIndexOf(".M"));
            String subGroupIdentifier = identifier.substring(0, identifier.indexOf('.'));
            String moduleTitle = null;
            if (addedModules.containsKey(moduleIdentifier)) {
                moduleTitle = addedModules.get(moduleIdentifier).getTitle();
            } else {
                LOG.warn("Cannot find parent for safeguard " + identifier + ", module "
                        + moduleIdentifier + " does not exist.");
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
                            moduleParent, moduleTitle, SafeguardGroup.PROP_RELEASE,
                            importMetadata.release);
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
     */
    private void createRequirementsForModule(Document bsiModule, BpRequirementGroup parent,
            ITBP2VNA.generated.implementationhint.Document implementationHint,
            ImportMetadata importMetadata) throws CreateBPElementException {

        Optional<Map<String, ITBP2VNA.generated.implementationhint.Safeguard>> safeguardsByCorrespondingRequirementIdentifier = Optional
                .ofNullable(implementationHint)
                .map(value -> Stream
                        .concat(Stream.concat(
                                value.getSafeguards().getBasicSafeguards().getSafeguard().stream(),
                                value.getSafeguards().getStandardSafeguards().getSafeguard()
                                        .stream()),
                                value.getSafeguards().getHighLevelSafeguards().getSafeguard()
                                        .stream())
                        .collect(Collectors
                                .toMap(safeguard -> getRequirementIdentifierForSafeguardLink(
                                        safeguard.getIdentifier()), Function.identity())));

        for (Requirement bsiRequirement : bsiModule.getRequirements().getBasicRequirements()
                .getRequirement()) {
            createRequirement(parent, bsiRequirement,
                    safeguardsByCorrespondingRequirementIdentifier
                            .map(map -> map.get(bsiRequirement.getIdentifier())).orElse(null),
                    Messages.Qualifier_Basic, bsiModule.getCrossreferences(), importMetadata);
        }
        for (Requirement bsiRequirement : bsiModule.getRequirements().getStandardRequirements()
                .getRequirement()) {
            createRequirement(parent, bsiRequirement,
                    safeguardsByCorrespondingRequirementIdentifier
                            .map(map -> map.get(bsiRequirement.getIdentifier())).orElse(null),
                    Messages.Qualifier_Standard, bsiModule.getCrossreferences(), importMetadata);
        }
        for (Requirement bsiRequirement : bsiModule.getRequirements().getHighLevelRequirements()
                .getRequirement()) {
            createRequirement(parent, bsiRequirement,
                    safeguardsByCorrespondingRequirementIdentifier
                            .map(map -> map.get(bsiRequirement.getIdentifier())).orElse(null),
                    Messages.Qualifier_High, bsiModule.getCrossreferences(), importMetadata);
        }

    }

    /**
     * transforms a single given {@link Requirement} into a
     * {@link BpRequirement} and adds it to the caching-map addedReqs
     */
    private BpRequirement createRequirement(BpRequirementGroup parent, Requirement bsiRequirement,
            ITBP2VNA.generated.implementationhint.Safeguard bsiSafeguard, String qualifier,
            Crossreferences crossreferences, ImportMetadata importMetadata)
            throws CreateBPElementException {
        if (!addedReqs.containsKey(bsiRequirement.getIdentifier())) {
            BpRequirement veriniceRequirement = null;
            veriniceRequirement = (BpRequirement) createElement(BpRequirement.TYPE_ID, parent,
                    bsiRequirement.getTitle(), BpRequirement.PROP_RELEASE, importMetadata.release);
            veriniceRequirement.setIdentifier(bsiRequirement.getIdentifier());
            veriniceRequirement.setTitle(bsiRequirement.getTitle());
            veriniceRequirement.setLastChange(parent.getLastChange());
            Cia cia = ImportUtils.getCiaForRequirement(bsiRequirement, crossreferences);

            veriniceRequirement
                    .setIsAffectsConfidentiality(Boolean.parseBoolean(cia.getConfidentiality()));
            veriniceRequirement.setIsAffectsIntegrity(Boolean.parseBoolean(cia.getIntegrity()));
            veriniceRequirement
                    .setIsAffectsAvailability(Boolean.parseBoolean(cia.getAvailability()));

            String title = extendTitleForObjectBrowser(bsiRequirement.getTitle(),
                    bsiRequirement.getIdentifier(),
                    new CIAWrapper(veriniceRequirement.IsAffectsConfidentiality(),
                            veriniceRequirement.IsAffectsIntegrity(),
                            veriniceRequirement.IsAffectsAvailability()));

            String requirementDescription = HtmlHelper.getAnyElementDescription(title.trim(), -1,
                    -1, -1, bsiRequirement.getDescription().getAny());
            if (bsiSafeguard != null) {
                String safeguardDescription = HtmlHelper.getAnyObjectDescription(
                        Messages.Implementation_Hint, 3,
                        bsiSafeguard.getDescription().getContent());
                requirementDescription = String.join("", requirementDescription,
                        safeguardDescription);
            }

            veriniceRequirement.setObjectBrowserDescription(requirementDescription);

            SecurityLevel level = getSecurityLevelFromQualifier(qualifier);

            veriniceRequirement.setSecurityLevel(level);

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
     * create the root {@link ItNetwork}
     */
    private ItNetwork createRootItNetwork(ImportMetadata importMetadata)
            throws CreateBPElementException {
        try {
            LoadBpModel modelLoader = new LoadBpModel();
            modelLoader = getCommandService().executeCommand(modelLoader);
            BpModel model = modelLoader.getModel();
            Objects.requireNonNull(model, "Unable to load base protection model");
            CreateITNetwork command = new CreateITNetwork(model, false);
            command = getCommandService().executeCommand(command);
            ItNetwork rootNetwork = command.getNewElement();
            StringBuilder titleBuilder = new StringBuilder();
            titleBuilder.append(Messages.IT_Network_Name);
            if (importMetadata.release != null) {
                titleBuilder.append("_");
                titleBuilder.append(importMetadata.release);
            }
            titleBuilder.append("_");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String dateInISO = df.format(new Date());
            titleBuilder.append(dateInISO);
            rootNetwork.setTitel(titleBuilder.toString());
            updateElement(rootNetwork);
            return rootNetwork;

        } catch (CommandException e) {
            throw new CreateBPElementException(e, "Error while loading BPModel");
        }
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

    private static class ImportData {
        private final Set<Document> modules;
        private final Set<ITBP2VNA.generated.threat.Document> threats;
        private final Set<ITBP2VNA.generated.implementationhint.Document> implementationHints;

        ImportData(Set<Document> modules, Set<ITBP2VNA.generated.threat.Document> threats,
                Set<ITBP2VNA.generated.implementationhint.Document> implementationHints) {
            this.modules = modules;
            this.threats = threats;
            this.implementationHints = implementationHints;
        }
    }

    private static class ImportMetadata {

        private final Map<String, String> implementationOrderByModuleIdentifier;
        private final String release;

        ImportMetadata(Map<String, String> implementationOrderByModuleIdentifier, String release) {
            this.implementationOrderByModuleIdentifier = implementationOrderByModuleIdentifier;
            this.release = release;
        }
    }
}