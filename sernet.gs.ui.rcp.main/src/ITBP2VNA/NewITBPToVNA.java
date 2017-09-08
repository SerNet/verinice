package ITBP2VNA;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ITBP2VNA.generated.DescriptionType;
import ITBP2VNA.generated.DocumentType;
import ITBP2VNA.generated.RequirementType;
import ITBP2VNA.generated.SpecificThreatType;
import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.interfaces.CnATreeElementBuildException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.common.CnATreeElement;

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
public class NewITBPToVNA {

    private final static Logger LOG = Logger.getLogger(NewITBPToVNA.class);
    
    private final static Set<String> processIdentifierPrefixes;
    private final static Set<String> systemIdentifierPrefixes;
    
    private static String xmlRootDirectory = null;
    
    private static ItNetwork rootNetwork = null;
    private static BpRequirementGroup processReqGroup = null;
    private static BpRequirementGroup systemReqGroup = null;

    private static BpThreatGroup processThreatGroup = null;
    private static BpThreatGroup systemThreatGroup = null;
    private static BpThreatGroup elementalThreatGroup = null;
    
    
    
    private static NewITBPToVNA instance;
    
    private final static String rootRequirementGroupName = "31 Bausteine/Anforderungen (GS-Kompendium";
    private final static String processRequirementGroupname = "Prozess-Bausteine";
    private final static String systemRequirementGroupname = "System-Bausteine";
    
    private final static String rootThreatGroupName = "41 Gefährdungen";
    private final static String elementalThreatGroupName = "Elementare Gefährdungen";
    private final static String specificThreatGroupName = "Spezifische Gefährdungen";
    private final static String specificProcessThreatGroupName = "Zu Prozess-Bausteinen";
    private final static String specificSystemThreatGroupName = "Zu System-Bausteinen";
    
    private static Set<String> addedThreats = new HashSet<>();
    private static Set<String> addedReqs = new HashSet<>();
    
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
    
    private NewITBPToVNA(String xmlRoot) {
        this.xmlRootDirectory = xmlRoot;
        try {
            rootNetwork = getRootItNetwork();
        } catch (CommandException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CnATreeElementBuildException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            run();
        } catch (CommandException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CnATreeElementBuildException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    public static void run() throws CommandException, CnATreeElementBuildException {
        Set<DocumentType> modules = new HashSet<>();
        Set<DocumentType> threats = new HashSet<>();
        if (xmlRootDirectory == null || xmlRootDirectory.length() == 0) {
           LOG.error("Wrong number of arguments, please provide root-Directory to XML-Archive");
           return;
        } else {
            File rootDir = new File(xmlRootDirectory);
            if (rootDir.exists() && rootDir.isDirectory()) {
               Collection<File> xmlFiles = FileUtils.listFiles(rootDir, new String[] {"xml"}, true);  
               ITBPParser parser = new ITBPParser();
               for (File xmlFile : xmlFiles) {
                   DocumentType document = parser.parseFile(xmlFile);
                   if (document.getCrossreferences() != null) {
                       modules.add(document);
                   } else if (document.getIdentifier().startsWith("G ")) {
                       threats.add(document);
                   } else {
                       LOG.debug("Ignoring " +  xmlFile.getAbsolutePath() + "[" + document.getIdentifier() +"]. Seems to be Umsetzungshinweis");
                   }
               }
            }
        }
        LOG.error("Successfully parsed modules:\t" + modules.size());
        LOG.error("Successfully parsed threats:\t" + threats.size());
        
        prepareITNetwork();
        CnAElementHome.getInstance().update(getRootItNetwork());
        transferModules(modules, threats, getRootItNetwork());
        LOG.debug("Transformation of elements complete");
        CnAElementHome.getInstance().update(getRootItNetwork());
        LOG.debug("ItNetwork updated");
    }
    
    
    private static void prepareITNetwork() throws CommandException, CnATreeElementBuildException {

        BpRequirementGroup rootReqGroup = (BpRequirementGroup) createNewElement(BpRequirementGroup.TYPE_ID, getRootItNetwork());
        rootReqGroup.setTitel(rootRequirementGroupName);;
        
        systemReqGroup =  (BpRequirementGroup) createNewElement(BpRequirementGroup.TYPE_ID, rootReqGroup);
        processReqGroup =  (BpRequirementGroup) createNewElement(BpRequirementGroup.TYPE_ID, rootReqGroup);
        
        
        systemReqGroup.setTitel(systemRequirementGroupname);
        processReqGroup.setTitel(processRequirementGroupname);
        
        
        BpThreatGroup rootThreatGroup = (BpThreatGroup) createNewElement(BpThreatGroup.TYPE_ID, getRootItNetwork());
        
        rootThreatGroup.setTitel(rootThreatGroupName);

        
        elementalThreatGroup = (BpThreatGroup) createNewElement(BpThreatGroup.TYPE_ID, rootThreatGroup);
        elementalThreatGroup.setTitel(elementalThreatGroupName);
        BpThreatGroup specificThreatGroup = (BpThreatGroup) createNewElement(BpThreatGroup.TYPE_ID, rootThreatGroup);
        specificThreatGroup.setTitel(specificThreatGroupName);
        
        processThreatGroup = (BpThreatGroup) createNewElement(BpThreatGroup.TYPE_ID, specificThreatGroup);
        processThreatGroup.setTitel(specificProcessThreatGroupName);
        systemThreatGroup = (BpThreatGroup) createNewElement(BpThreatGroup.TYPE_ID, specificThreatGroup);
        systemThreatGroup.setTitel(specificSystemThreatGroupName);
        
        for (String name : systemIdentifierPrefixes ) {
            BpRequirementGroup group = (BpRequirementGroup) createNewElement(BpRequirementGroup.TYPE_ID, systemReqGroup);
            group.setTitel(name);
            
            BpThreatGroup tGroup = (BpThreatGroup) createNewElement(BpThreatGroup.TYPE_ID, systemThreatGroup);
            tGroup.setTitel(name);
        }
        
        
        for (String name : processIdentifierPrefixes ) {
            BpRequirementGroup group = (BpRequirementGroup) createNewElement(BpRequirementGroup.TYPE_ID, processReqGroup);
            group.setTitel(name);
            
            BpThreatGroup tGroup = (BpThreatGroup) createNewElement(BpThreatGroup.TYPE_ID, processThreatGroup);
            tGroup.setTitel(name);
        }
        
            
    }
    
    private static void transferModules(Set<DocumentType> modules, Set<DocumentType> threats, ItNetwork rootNetwork) throws CommandException, CnATreeElementBuildException {

        for (DocumentType bsiModule : modules) {
            if (rootNetwork != null) {
                String groupIdentifier = getIdentifierPrefix(bsiModule.getIdentifier());
                
                BpRequirementGroup parent = getRequirementParentGroup(groupIdentifier);
                
                if (! addedReqs.contains(bsiModule.getFullTitle())) {


                    BpRequirement veriniceModule = (BpRequirement)
                            CnAElementFactory.getInstance().saveNew(
                                    parent, BpRequirement.TYPE_ID, null, false);
                    veriniceModule.setTitle(bsiModule.getFullTitle());
                    veriniceModule.setIdentifier(bsiModule.getIdentifier());
                    veriniceModule.setAbbreviation(bsiModule.getIdentifier());
                    veriniceModule.setDescription(getDescriptionText(bsiModule.getFullTitle(), 
                            bsiModule.getDescription()));
                    LOG.debug("Module : \t" + veriniceModule.getTitle()+ " created");
                    addedReqs.add(bsiModule.getFullTitle());
                }


//                createRequirements(bsiModule, veriniceModule);

                
                for (SpecificThreatType threat : bsiModule.getThreatScenario().getSpecificThreats().getSpecificThreat()) {



                    BpThreatGroup tParent = getSpecificThreatParentGroup(groupIdentifier);

                    String title = bsiModule.getIdentifier() + " " + threat.getHeadline();
                    
                    if (! addedThreats.contains(title)) {
                        
                        BpThreat veriniceThreat = (BpThreat) CnAElementFactory.getInstance().saveNew(tParent,
                                BpThreat.TYPE_ID, null, false);
                        veriniceThreat.setTitel(title);
                        veriniceThreat.setDescription(getDescriptionText(threat.getHeadline(), threat.getDescription()));
                        addedThreats.add(title);
                    }

                }

                for (String threatIdentifier : bsiModule.getElementalThreats().getElementalThreat()) {
                    DocumentType bsiThreat = null;
                    for(DocumentType t : threats) {
                        if (threatIdentifier.equals(t.getIdentifier())) {
                            bsiThreat = t;
                            break;
                        }
                    }
                    if (! addedThreats.contains(bsiThreat.getFullTitle())) {
                        BpThreat veriniceThreat = (BpThreat) CnAElementFactory.getInstance().saveNew(elementalThreatGroup,
                                BpThreat.TYPE_ID, null, false);
                        veriniceThreat.setTitel(bsiThreat.getFullTitle());
                        veriniceThreat.setIdentifier(bsiThreat.getIdentifier());
                        veriniceThreat.setAbbreviation("E");
                        //                    veriniceThreat.setDescription(bsiThreat.getDescription());
                        addedThreats.add(bsiThreat.getFullTitle());
                        LOG.debug("Threat : \t" + veriniceThreat.getTitle()+ " created");
                    }
                }
            }
        }
    }
    
    private static Set<String> getAllChildrenTitle(CnATreeElement element){
        Set<String> title = new HashSet<>();
        
        element = Retriever.checkRetrieveChildren(element);
        
        if ( element != null && element.getChildren() != null) {

            for (CnATreeElement child : element.getChildren()) {
                if (child instanceof IBpGroup) {
                    title.addAll(getAllChildrenTitle(child));
                } else {
                    title.add(element.getTitle());
                }
            }
        }
        return title;
    }

    /**
     * @param groupIdentifier
     */
    private static BpRequirementGroup getRequirementParentGroup(String groupIdentifier) {
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
    
    private static BpThreatGroup getSpecificThreatParentGroup(String groupIdentifier) {
        BpThreatGroup group = null;
        
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
        return group;
    }
    
    private static String getIdentifierPrefix(String id) {
        if (id != null && id.length() >= 3 && id.contains(".")) {
            return id.substring(0, id.indexOf("."));
        } else return id;
    }

    /**
     * @param bsiModule
     * @param veriniceModule
     * @throws CommandException
     * @throws CnATreeElementBuildException
     */
    private static void createRequirements(DocumentType bsiModule, BpRequirement veriniceModule) throws CommandException, CnATreeElementBuildException {
        for (RequirementType requirement : bsiModule.getRequirements().getBasicRequirements().getRequirement()) {
            createRequirement(veriniceModule, requirement, "BASIC");
        }
        for (RequirementType requirement : bsiModule.getRequirements().getStandardRequirements().getRequirement()) {
            createRequirement(veriniceModule, requirement, "STANDARD");
        }
        for (RequirementType requirement : bsiModule.getRequirements().getHighLevelRequirements().getRequirement()) {
            createRequirement(veriniceModule, requirement, "HIGH");
        }
    }

    @SuppressWarnings("unused")
    private static BpRequirement createRequirement(BpRequirement parent, 
            RequirementType bsiRequirement, String qualifier) 
                    throws CommandException, CnATreeElementBuildException {
        BpRequirement vRequirement = (BpRequirement) 
                CnAElementFactory.getInstance().saveNew(parent,
                        BpRequirement.TYPE_ID, null, false);
        vRequirement.setTitel(bsiRequirement.getTitle());
        vRequirement.setAbbreviation(bsiRequirement.getIdentifier());
        vRequirement.setIdentifier(bsiRequirement.getIdentifier());
        vRequirement.setDescription(getDescriptionText(bsiRequirement.getTitle(),
                bsiRequirement.getDescription()));
//        vRequirement.setQualifier(qualifier);
        LOG.debug("Requierement : \t"  + vRequirement.getTitle() + "created ");
        return vRequirement;
    }
    
    private static String getDescriptionText(String title, DescriptionType description) {
        StringBuilder sb = new StringBuilder();
        sb.append("<H1>").append(title).append("</H1>");
        if(description != null) {
            String introduction = (description.getIntroduction() != null ) 
                    ? description.getIntroduction().getP() : null;
            String purpose = (description.getPurpose() != null ) 
                    ? description.getPurpose().getP() : null;
            String differentiation = (description.getDifferentiation() != null ) 
                    ? description.getDifferentiation().getP() : null;
            sb.append((introduction != null) ? introduction : "<p>No Introduction</p>");
            sb.append((purpose != null) ? purpose : "<p>No Purpose</p>");
            sb.append((differentiation != null) ? differentiation : "<p>No differentiation</p>");
        }
        return sb.toString();
    }
    
    private static ItNetwork getRootItNetwork() throws CommandException, CnATreeElementBuildException {
        if(rootNetwork == null) {
            rootNetwork = (ItNetwork)createNewElement(ItNetwork.TYPE_ID, CnAElementFactory.getInstance().getBpModel());
            rootNetwork.setTitel("IT-Grundschutz-Kompendium");
        } 
        return rootNetwork;
    }
    
    private static CnATreeElement createNewElement(String typeId, CnATreeElement container) 
            throws CommandException, CnATreeElementBuildException {
        LOG.debug("creating instanceof " + typeId);
        return CnAElementFactory.getInstance().saveNew(
                container,
                typeId, null, false);
    }
    
    private static void transferModuleToVerinice(DocumentType moduleDocument) {
        
    }
    
    private static String getIdentifierPrexif(String identifier) {
        if (identifier.contains(".")) {
            return identifier.substring(0, identifier.lastIndexOf("."));
        }
        return identifier;
    }
    
    public static NewITBPToVNA getInstance(String xmlRootDir) {
        if (instance == null) {
            instance = new NewITBPToVNA(xmlRootDir);
        }
        return instance;
    }
    
    
    

}
