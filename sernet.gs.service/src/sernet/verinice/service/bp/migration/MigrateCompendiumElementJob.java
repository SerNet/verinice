/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin.
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
 ******************************************************************************/
package sernet.verinice.service.bp.migration;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CopyCommand;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 * Base class for migrating requirements, safeguards and threats
 */
public abstract class MigrateCompendiumElementJob {

    private static final Logger log = Logger.getLogger(MigrateCompendiumElementJob.class);

    protected static final String HIBERNATE_DIALECT_ORACLE = "sernet.verinice.hibernate.Oracle10gNclobDialect";
    protected static final String HIBERNATE_DIALECT_DERBY = "sernet.verinice.hibernate.ByteArrayDerbyDialect";

    protected CnATreeElement element;
    protected CnATreeElement elementCompendium;
    protected VeriniceGraph veriniceGraph;
    protected String hibernateDialect;
    protected ICommandService commandService;
    private IBaseDao<CnALink, Serializable> linkDao;

    public MigrateCompendiumElementJob(CnATreeElement element, CnATreeElement elementCompendium) {
        this.element = element;
        this.elementCompendium = elementCompendium;
    }

    public void migrateModeling() throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Migrating " + elementCompendium.getTypeId() + ": "
                    + elementCompendium.getTitle() + " of element " + element.getTypeId() + ": "
                    + element.getTitle() + "...");
        }
        CnATreeElement elementCompendiumCopy = copyElement(getOrCreateGroup(), elementCompendium);
        createLink(elementCompendiumCopy, element, getLinkTypeId());
    }

    protected abstract String getLinkTypeId();

    /**
     * If required, overwrite this method in derived classes
     */
    protected String getIdentifier(CnATreeElement element) {
        if (element instanceof SafeguardGroup) {
            return ((SafeguardGroup) element).getIdentifier();
        }
        if (element instanceof Safeguard) {
            return ((Safeguard) element).getIdentifier();
        }
        if (element instanceof BpRequirementGroup) {
            return ((BpRequirementGroup) element).getIdentifier();
        }
        if (element instanceof BpRequirement) {
            return ((BpRequirement) element).getIdentifier();
        }
        return null;
    }

    protected CnATreeElement getOrCreateGroup() throws CommandException {
        CnATreeElement groupCompendium = elementCompendium.getParent();
        Set<CnATreeElement> children = veriniceGraph.getChildren(element);
        for (CnATreeElement child : children) {
            if (isSameElement(groupCompendium, child)) {
                return child;
            }
        }
        return copyElement(element, groupCompendium);
    }

    protected boolean isSameElement(CnATreeElement element, CnATreeElement element2) {
        if (element == null || getIdentifier(element) == null || element.getTypeId() == null) {
            return false;
        }
        return element.getTypeId().equals(element2.getTypeId())
                && getIdentifier(element).equals(getIdentifier(element2));
    }

    protected CnATreeElement copyElement(CnATreeElement group, CnATreeElement element)
            throws CommandException {
        String uuidNew = copyElementAndReturnUuid(group, element);
        CnATreeElement elementCopy = loadElement(uuidNew);
        addToGraph(group, elementCopy);
        return elementCopy;
    }

    private String copyElementAndReturnUuid(CnATreeElement group, CnATreeElement element)
            throws CommandException {
        List<String> uuidList = Collections.singletonList(element.getUuid());
        CopyCommand copyCommand = new CopyCommand(group.getUuid(), uuidList);
        copyCommand.setCopyChildren(false);
        copyCommand = commandService.executeCommand(copyCommand);
        return copyCommand.getNewElements().get(0);
    }

    private CnATreeElement loadElement(String uuid) throws CommandException {
        LoadElementByUuid<CnATreeElement> loadCommand = new LoadElementByUuid<>(uuid,
                RetrieveInfo.getPropertyInstance());
        loadCommand = commandService.executeCommand(loadCommand);
        return loadCommand.getElement();
    }

    private void addToGraph(CnATreeElement group, CnATreeElement element) throws CommandException {
        veriniceGraph.addVertex(element);
        veriniceGraph.addEdge(new Edge(group, element));
    }

    protected void createLinksWithRequirements(CnATreeElement compendiumElementCopy,
            String linkTypeId) {
        Set<CreateLinkData> createLinkDataSet = new HashSet<>();
        Set<CnATreeElement> linkedRequirementsOfSource = veriniceGraph
                .getLinkTargetsByElementType(elementCompendium, BpRequirement.TYPE_ID);
        Set<CnATreeElement> requirementsOfTargetElement = getRequirementOfTargetElement();
        for (CnATreeElement requirementSource : linkedRequirementsOfSource) {
            for (CnATreeElement requirement : requirementsOfTargetElement) {
                if (isSameElement(requirement, requirementSource)
                        && (!isLink(compendiumElementCopy, requirement, linkTypeId))) {
                    createLinkDataSet.add(new CreateLinkData(requirement.getDbId(),
                            compendiumElementCopy.getDbId(), linkTypeId));
                    veriniceGraph.addEdge(new Edge(compendiumElementCopy, requirement, linkTypeId));
                    break;
                }
            }
        }
        if (!createLinkDataSet.isEmpty()) {
            createLinksWithSql(createLinkDataSet);
        }
    }

    private boolean isLink(CnATreeElement source, CnATreeElement target, String linkTypeId) {
        return veriniceGraph.getLinkTargets(source, linkTypeId).contains(target);
    }

    protected Set<CnATreeElement> getRequirementOfTargetElement() {
        Set<CnATreeElement> requirements = new HashSet<>();
        Set<CnATreeElement> requirementGroups = veriniceGraph.getChildren(element,
                BpRequirementGroup.TYPE_ID);
        for (CnATreeElement requirementGroup : requirementGroups) {
            requirementGroup.setParentId(requirementGroup.getParent().getDbId());
            requirements.addAll(veriniceGraph.getChildren(requirementGroup));
        }
        return requirements;
    }

    protected void createLink(CnATreeElement elementCompendiumCopy, CnATreeElement element,
            String linkTypeId) {
        if (createLinkToElement()) {
            if (log.isDebugEnabled()) {
                log.debug("Creating link from " + elementCompendiumCopy + " to " + element);
            }
            createLinkBySql(elementCompendiumCopy.getDbId(), element.getDbId(), linkTypeId);
            veriniceGraph.addEdge(new Edge(elementCompendiumCopy, element, linkTypeId));
        }
    }

    protected void createLinkBySql(int dependantId, int dependencyId, String relationId) {
        String sql = generateSqlToCreateLink();
        if (log.isDebugEnabled()) {
            log.debug("Create links SQL: " + sql);
        }
        linkDao.executeCallback(session -> {
            Query query = session.createSQLQuery(sql);
            query.setInteger("dependantId", dependantId);
            query.setInteger("dependencyId", dependencyId);
            query.setString("linkType", relationId);
            query.setString("comment", "Created during modeling update to 1.17");
            return query.executeUpdate();
        });
    }

    private String generateSqlToCreateLink() {
        if (isDerby()) {
            return SqlHelper.generateDerbySqlToCreateLink();
        } else {
            return SqlHelper.generateDefaultSqlToCreateLink();
        }
    }

    private void createLinksWithSql(Set<CreateLinkData> createLinkDataSet) {
        String sql = generateSqlToCreateLinks(createLinkDataSet);
        if (log.isDebugEnabled()) {
            log.debug("Create links SQL: " + sql);
        }
        linkDao.executeCallback(session -> {
            Query query = session.createSQLQuery(sql);
            return query.executeUpdate();
        });
    }

    private String generateSqlToCreateLinks(Set<CreateLinkData> createLinkDataSet) {
        if (isOracle()) {
            return SqlHelper.generateOracleSqlToCreateLinks(createLinkDataSet);
        }
        if (isDerby()) {
            return SqlHelper.generateDerbySqlToCreateLinks(createLinkDataSet);
        }
        return SqlHelper.generateDefaultSqlToCreateLinks(createLinkDataSet);
    }

    protected boolean isDerby() {
        return HIBERNATE_DIALECT_DERBY.equals(getHibernateDialect());
    }

    protected boolean isOracle() {
        return HIBERNATE_DIALECT_ORACLE.equals(getHibernateDialect());
    }

    public String getHibernateDialect() {
        return hibernateDialect;
    }

    public void setHibernateDialect(String hibernateDialect) {
        this.hibernateDialect = hibernateDialect;
    }

    protected boolean createLinkToElement() {
        return true;
    }

    public void setVeriniceGraph(VeriniceGraph veriniceGraph) {
        this.veriniceGraph = veriniceGraph;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    public void setLinkDao(IBaseDao<CnALink, Serializable> linkDao) {
        this.linkDao = linkDao;
    }
}