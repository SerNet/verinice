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
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.interfaces.CnATreeElementBuildException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.moditbp.elements.ITNetwork;
import sernet.verinice.model.moditbp.elements.ModITBPRequirement;
import sernet.verinice.model.moditbp.elements.ModITBPThreat;
import sernet.verinice.model.moditbp.elements.Module;

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
    
    private final static Set<String> moduleIdentifierPrefixes;
    
    private static String xmlRootDirectory = null;
    
    private static ITNetwork rootNetwork = null;
    
    private static NewITBPToVNA instance;
    
    static {
        moduleIdentifierPrefixes = new HashSet<>();
        moduleIdentifierPrefixes.addAll(Arrays.asList(new String[] {
                "ORP",
                "INF",
                "IND",
                "APP",
                "ISMS"
                
        }));
    }
    
    private NewITBPToVNA(String xmlRoot) {
        this.xmlRootDirectory = xmlRoot;
        try {
            rootNetwork = getRootITNetwork();
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
                       LOG.warn("Ignoring " +  xmlFile.getAbsolutePath() + "[" + document.getIdentifier() +"]. Seems to be Umsetzungshinweis");
                   }
               }
            }
        }
        LOG.error("Successfully parsed modules:\t" + modules.size());
        LOG.error("Successfully parsed threats:\t" + threats.size());
        
        transferModules(modules, threats, getRootITNetwork());
        LOG.debug("Transformation of elements complete");
        CnAElementHome.getInstance().update(getRootITNetwork());
        LOG.debug("ITNetwork updated");
    }
    
    private static void handleParsedXML(Set<DocumentType> documents) {
        for (DocumentType document : documents) {
            if (moduleIdentifierPrefixes.contains(getIdentifierPrexif(document.getIdentifier()))){
                transferModuleToVerinice(document);
            }
        }
    }
    
    private static void transferModules(Set<DocumentType> modules, Set<DocumentType> threats, ITNetwork rootNetwork) throws CommandException, CnATreeElementBuildException {

        for (DocumentType bsiModule : modules) {
            if (rootNetwork != null) {
                Module veriniceModule = (Module)
                        CnAElementFactory.getInstance().saveNew(
                                rootNetwork, Module.TYPE_ID, null, false);
                veriniceModule.setTitel(bsiModule.getFullTitle());
                veriniceModule.setIdentifier(bsiModule.getIdentifier());
                veriniceModule.setAbbreviation(bsiModule.getIdentifier());
                veriniceModule.setDescription(getDescriptionText(bsiModule.getFullTitle(), 
                        bsiModule.getDescription()));


                createRequirements(bsiModule, veriniceModule);

                for (SpecificThreatType threat : bsiModule.getThreatScenario().getSpecificThreats().getSpecificThreat()) {
                    ModITBPThreat veriniceThreat = (ModITBPThreat) CnAElementFactory.getInstance().saveNew(veriniceModule,
                            ModITBPThreat.TYPE_ID, null, false);
                    veriniceThreat.setTitel(threat.getHeadline());
                    veriniceThreat.setDescription(getDescriptionText(threat.getHeadline(), threat.getDescription()));

                }

                for (String threatIdentifier : bsiModule.getElementalThreats().getElementalThreat()) {
                    DocumentType bsiThreat = null;
                    for(DocumentType t : threats) {
                        if (threatIdentifier.equals(t.getIdentifier())) {
                            bsiThreat = t;
                            break;
                        }
                    }
                    ModITBPThreat veriniceThreat = (ModITBPThreat) CnAElementFactory.getInstance().saveNew(veriniceModule,
                            ModITBPThreat.TYPE_ID, null, false);
                    veriniceThreat.setTitel(bsiThreat.getFullTitle());
                    veriniceThreat.setIdentifier(bsiThreat.getIdentifier());
                    veriniceThreat.setAbbreviation("E");
                    //                    veriniceThreat.setDescription(bsiThreat.getDescription());
                    LOG.debug("Threat : \t" + veriniceThreat.getTitle()+ " created");
                }
                LOG.debug("Module : \t" + veriniceModule.getTitle()+ " created");
            }
        }
    }

    /**
     * @param bsiModule
     * @param veriniceModule
     * @throws CommandException
     * @throws CnATreeElementBuildException
     */
    private static void createRequirements(DocumentType bsiModule, Module veriniceModule) throws CommandException, CnATreeElementBuildException {
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
    private static ModITBPRequirement createRequirement(Module parent, 
            RequirementType bsiRequirement, String qualifier) 
                    throws CommandException, CnATreeElementBuildException {
        ModITBPRequirement vRequirement = (ModITBPRequirement) 
                CnAElementFactory.getInstance().saveNew(parent,
                        ModITBPRequirement.TYPE_ID, null, false);
        vRequirement.setTitel(bsiRequirement.getTitle());
        vRequirement.setAbbreviation(bsiRequirement.getIdentifier());
        vRequirement.setIdentifier(bsiRequirement.getIdentifier());
        vRequirement.setDescription(getDescriptionText(bsiRequirement.getTitle(),
                bsiRequirement.getDescription()));
        vRequirement.setQualifier(qualifier);
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
    
    private static ITNetwork getRootITNetwork() throws CommandException, CnATreeElementBuildException {
        if(rootNetwork == null) {
            rootNetwork = (ITNetwork)createNewElement(ITNetwork.TYPE_ID);
            rootNetwork.setTitel("IT-Grundschutz-Kompendium");
        } 
        return rootNetwork;
    }
    
    private static CnATreeElement createNewElement(String typeId) 
            throws CommandException, CnATreeElementBuildException {
        return CnAElementFactory.getInstance().saveNew(
                CnAElementFactory.getInstance().getModITBPModel(),
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
