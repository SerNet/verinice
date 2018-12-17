/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.RelationNotDefinedException;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LinkTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(LinkTest.class);
    
    private static final int NUMBER_PER_GROUP = 1;

    private static final String[] IGNORED_TYPES = new String[]{
        SamtTopic.TYPE_ID,
        MassnahmenUmsetzung.TYPE_ID,
        Interview.TYPE_ID,
        Raum.TYPE_ID,
        Finding.TYPE_ID,
        BausteinUmsetzung.TYPE_ID,
        Anwendung.TYPE_ID,
        Gebaeude.TYPE_ID,
        Server.TYPE_ID,
        Evidence.TYPE_ID,
        FindingGroup.TYPE_ID,
        EvidenceGroup.TYPE_ID,        
    }; 
    
    static {
        Arrays.sort(IGNORED_TYPES);
    }
    
    @Resource(name="huiTypeFactory")
    private HUITypeFactory huiTypeFactory;

    private List<String> uuidList;
    private Map<String, CnATreeElement> elementMap;
    private Map<String, Set<CnALink>> linkMap;
    
    @Test
    public void testCreateLink() throws Exception {
        // create
        uuidList = new LinkedList<String>();
        Organization organization = createOrganization();
        uuidList.add(organization.getUuid());
        uuidList.addAll(createElementsInGroups(organization, NUMBER_PER_GROUP));
        
        linkMap = new Hashtable<String, Set<CnALink>>();
        elementMap = new Hashtable<String, CnATreeElement>();
        for (String uuid : uuidList) {
            RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
            ri.setParent(true);
            CnATreeElement element = elementDao.findByUuid(uuid, ri);
            checkElement(element);
            elementMap.put(element.getTypeId(), element);
        }
        
        Set<String> typeIdSet = elementMap.keySet();
        for (String typeId : typeIdSet) {
            createAllLinks(typeId);
        }
        
        
        // check
        Collection<CnATreeElement> elements = elementMap.values();      
        for (CnATreeElement element : elements) {
            element = checkLinksInElement(element);
        } 
        
        
        // remove
        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(organization);
        commandService.executeCommand(removeCommand);
        for (String uuid: uuidList) {
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid);
            command = commandService.executeCommand(command);
            CnATreeElement element = command.getElement();
            assertNull("Organization was not deleted.", element);
        } 
    }

    /**
     * Test if creating a {@link CnALink} with the wrong direction fails (gets detected by the validation of {@link CreateLink}
     * asserts that a {@link RelationNotDefinedException} is thrown by executing {@link CreateLink}-Command
     * @throws CommandException
     */
    @Test
    public void testNotValidLink() throws CommandException {
        Organization organization = createOrganization();
        IncidentScenario scenario = null;
        Asset asset = null;
        for(CnATreeElement child : organization.getChildren()) {
            if(IncidentScenarioGroup.TYPE_ID.equals(child.getTypeId()) ) {
                scenario = (IncidentScenario)createNewElement((Group<CnATreeElement>)child, IncidentScenario.class);
            } else if(AssetGroup.TYPE_ID.equals(child.getTypeId())) {
                asset = (Asset)createNewElement((Group<CnATreeElement>)child, Asset.class);
            }
        }

        for(HuiRelation relation : huiTypeFactory.getPossibleRelations(scenario.getEntityType().getId(), asset.getEntityType().getId())) {
            try {
                createLink(asset, scenario, relation.getId());
                Assert.fail("Expected exception was not thrown");
            } catch (CommandException e) {
                assertEquals(RelationNotDefinedException.class, e.getCause().getCause().getClass());
            }
        }


    }

    protected CnATreeElement checkLinksInElement(CnATreeElement element) {
        RetrieveInfo ri = new RetrieveInfo();
        ri.setLinksDown(true).setLinksUp(true);
        element = elementDao.findByUuid(element.getUuid(), ri);
        Set<CnALink> linkSet =linkMap.get(element.getTypeId());  
        if(linkSet!=null) {
            boolean found = false;
            Set<CnALink>linksDown = element.getLinksDown();
            Set<CnALink>linksUp = element.getLinksUp();
            for (CnALink link : linkSet) {
                assertTrue("Link not found, uuid: " + element.getUuid() + ", id: " + link.getRelationId(), linksDown.contains(link) || linksUp.contains(link));
            }
        } else {
            LOG.debug("No links created for: " + element.getTypeId());
        }
        return element;
    }

    protected void createAllLinks(String typeId) throws CommandException {
        CnATreeElement element = elementMap.get(typeId);
        EntityType entityType = huiTypeFactory.getEntityType(typeId);
        Set<HuiRelation> relations = entityType.getPossibleRelations();
        for (HuiRelation relation : relations) {
            String destinationTypeId = (relation.getFrom().equals(typeId)) ? relation.getTo(): relation.getFrom();
            CnATreeElement destination = elementMap.get(destinationTypeId);        
            assertTrue("No element found with destination id: " + destinationTypeId, 
                       destination!=null || isIgnoredType(destinationTypeId));
            if(destination!=null) {
                String linkType = relation.getId();
                CnALink link = createLink(element, destination, linkType);             
                addToLinkMap(element, link);
                LOG.debug("Link created from: " + element.getTypeId() + " to: " + destinationTypeId);  
            } else {
                LOG.debug("No Link created from: " + typeId + " to: " + destinationTypeId);
            }
        }
    }

    protected void addToLinkMap(CnATreeElement element, CnALink link) {
        Set<CnALink> linkSet = linkMap.get(element.getTypeId());
        if(linkSet==null) {
            linkSet = new HashSet<CnALink>();
            linkMap.put(element.getTypeId(), linkSet);
        }
        linkSet.add(link);
    }

    protected boolean isIgnoredType(String destinationTypeId) {
        return Arrays.binarySearch(IGNORED_TYPES, destinationTypeId)!=-1;
    }
}
