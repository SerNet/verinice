/*******************************************************************************
 * Copyright (c) 2018 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.dataprotection.migration;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Lists;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IElementEntityDao;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnALink.Id;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Process;

/**
 * This command migrates one or more iso organizations from the old data privacy
 * to the new one. Each control linked to a process which can be found in the
 * migration table of the {@link TomMapper} will be migrated. This means it
 * removes all old links from type RELATIONS, and add the link
 * REL_PROCESS_CONTROL_OBJECTIVES and add the toms properties to the control.
 */
public class MigrateDataProtectionCommand extends GraphCommand {

    private static final Logger LOG = Logger.getLogger(MigrateDataProtectionCommand.class);

    private static final long serialVersionUID = 1L;
    private static final String[] TYPE_IDS = new String[] { Process.TYPE_ID, Control.TYPE_ID };
    public static final String REL_PROCESS_CONTROL_OBJECTIVES = "rel_process_control_objectives";
    public static final List<String> RELATIONS = Lists.newArrayList(
            "rel_process_control_Zutrittskontrolle", "rel_process_control_Zugangskontrolle",
            "rel_process_control_Zugriffskontrolle", "rel_process_control_Weitergabekontrolle",
            "rel_process_control_Eingabekontrolle", "rel_process_control_Auftragskontrolle",
            "rel_process_control_Verfügbarkeitskontrolle",
            "rel_process_control_Trennungskontrolle");
    private transient IBaseDao<CnALink, Serializable> linkDao;
    private List<String> missedControlNames;
    private List<String> affectedControlsNames;
    private List<String> affectedProcessNames;
    private int numberOfDeletedLinks;
    private int numberOfCreatedLinks;
    private int affectedNumberOfControls;
    private int missedNumberOfControls;

    private Set<CnATreeElement> processes;

    public MigrateDataProtectionCommand(Integer... scopeIds) {
        initalizeCommand(scopeIds);
    }

    public MigrateDataProtectionCommand(Set<CnATreeElement> selectedElementSet) {
        Set<Integer> set = new HashSet<>();
        for (CnATreeElement cnATreeElement : selectedElementSet) {
            set.add(cnATreeElement.getDbId());
        }
        initalizeCommand(set.toArray(new Integer[selectedElementSet.size()]));
    }

    /**
     * @param scopeIds
     */
    protected void initalizeCommand(Integer... scopeIds) {
        GraphElementLoader loader = new GraphElementLoader();
        loader.setTypeIds(TYPE_IDS);
        if (scopeIds.length > 0) {
            loader.setScopeIds(scopeIds);
        }
        addLoader(loader);
        for (String relation : RELATIONS) {
            addRelationId(relation);
        }

        numberOfCreatedLinks = 0;
        missedNumberOfControls = 0;
    }

    @Override
    public void executeWithGraph() {
        linkDao = getDaoFactory().getDAO(CnALink.class);
        VeriniceGraph processGraph = getGraph();
        processes = processGraph.getElements(Process.TYPE_ID);
        Set<CnATreeElement> controls = processGraph.getElements(Control.TYPE_ID);
        Set<Integer> scopeIds = collectAffectedScopeIds();

        affectedControlsNames = new ArrayList<>(controls.size());
        missedControlNames = new ArrayList<>(controls.size());
        affectedProcessNames = new ArrayList<>(processes.size());
        Map<CnATreeElement, Set<CnATreeElement>> dsLinks2create = new HashMap<>(controls.size());
        Set<CnATreeElement> controls2Update = new HashSet<>(controls.size());
        Set<CnALink.Id> linkData = new HashSet<>(
                controls.size() * RELATIONS.size() * processes.size());
        Set<CnATreeElement> missedControls = new HashSet<>(controls.size());

        for (CnATreeElement control : controls) {
            if (!isDsGvoControl(control, processGraph)) {
                continue;
            }
            Set<PropertyType> toms = getTOMS(control);
            if (toms != null && !toms.isEmpty()) {
                updateControlWithToms(control, toms, controls2Update);
                Set<CnATreeElement> affectedProcesses = collectLinksToRemove(control, linkData);
                dsLinks2create.put(control, affectedProcesses);
                affectedControlsNames.add(control.getTitle());
            } else {
                missedControlNames.add(control.getTitle());
                missedControls.add(control);
            }
        }
        numberOfDeletedLinks = linkData.size();
        missedNumberOfControls = missedControls.size();
        affectedNumberOfControls = dsLinks2create.size();
        for (CnATreeElement cnATreeElement : processes) {
            affectedProcessNames.add(cnATreeElement.getTitle());
        }
        persitData(linkData, missedControls, scopeIds, controls2Update, dsLinks2create);
        getGraphService().setRelationIds(null);
    }

    /**
     * Check if the control is linked to a process, only then the control is
     * relevant for the migration because we get only those relevant links back
     * from the graph service.
     *
     */
    private boolean isDsGvoControl(CnATreeElement control, VeriniceGraph processGraph) {
        Set<CnATreeElement> linkTargets = processGraph.getLinkTargetsByElementType(control,
                Process.TYPE_ID);
        return !linkTargets.isEmpty();
    }

    /**
     * Collects the scopes of the affected processes.
     */
    private Set<Integer> collectAffectedScopeIds() {
        Set<Integer> scopeIds = new HashSet<>(processes.size());
        for (CnATreeElement process : processes) {
            scopeIds.add(process.getScopeId());
        }
        return scopeIds;
    }

    /**
     * For performance reasons we write the data in big chunks to the database
     * and don't use commands.
     */
    private void persitData(Set<Id> linkData, Set<CnATreeElement> missedControls,
            final Set<Integer> scopeIds, Set<CnATreeElement> controls2Update,
            Map<CnATreeElement, Set<CnATreeElement>> dsLinks2create) {
        try {

            IElementEntityDao elementEntityDao = getDaoFactory().getElementEntityDao();
            for (CnATreeElement cnATreeElement : controls2Update) {
                elementEntityDao.mergeEntityOfElement(cnATreeElement, false);
            }

            IBaseDao<CnATreeElement, Serializable> baseDao = getDaoFactory()
                    .getDAO(CnATreeElement.class);
            baseDao.executeCallback(new HibernateCallback() {

                @Override
                public Object doInHibernate(Session session)
                        throws HibernateException, SQLException {
                    for (Integer sId : scopeIds) {
                        Query query = session.createQuery(
                                "update CnATreeElement c set c.sourceId='DP' where c.scopeId=:sId and c.sourceId='BDSG' or c.sourceId='BDSG_VRL'");
                        query.setParameter("sId", sId);
                        query.executeUpdate();
                    }
                    return null;
                }
            });

            changeLinks(linkData, missedControls, dsLinks2create);
        } catch (Exception e) {
            LOG.error("Error while delete persit migratioin data.", e);
            throw new RuntimeCommandException(e);
        }
    }

    /**
     * Creates the new links of type 'rel_process_control_objectives' for each
     * process to control in dsLinks2create. Update all remaining links
     * description and delete all old links.
     */
    private void changeLinks(Set<Id> linkData, Set<CnATreeElement> missedControls,
            Map<CnATreeElement, Set<CnATreeElement>> dsLinks2create) {
        for (Entry<CnATreeElement, Set<CnATreeElement>> entry : dsLinks2create.entrySet()) {
            for (CnATreeElement process : entry.getValue()) {
                CnALink link = new CnALink(process, entry.getKey(), REL_PROCESS_CONTROL_OBJECTIVES,
                        null);
                linkDao.merge(link, false);
                numberOfCreatedLinks++;
            }
        }

        updateMissedLinksDescription(missedControls);
        deleteOldLinks(linkData);
    }

    /**
     * Set the description 'check' to the old, not removed links, because the
     * control is not migrated.
     */
    private void updateMissedLinksDescription(Set<CnATreeElement> missedControls) {
        VeriniceGraph processGraph = getGraph();
        LOG.info("update old link descriptions");
        final Set<CnALink.Id> ids = new HashSet<>(missedControls.size() * RELATIONS.size());
        for (CnATreeElement control : missedControls) {
            Set<Edge> allRelations = processGraph.getEdgesByElementType(control, Process.TYPE_ID);
            for (Edge edge : allRelations) {
                ids.add(new CnALink.Id(edge.getSource().getDbId(), edge.getTarget().getDbId(),
                        edge.getType()));
            }
        }
        linkDao.executeCallback(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                String comment = sernet.verinice.service.commands.Messages
                        .getString("check.old.links");
                for (Id id : ids) {
                    Query query = session.createQuery(
                            "update CnALink c set c.comment=:comment where c.id=:id");
                    query.setParameter("id", id);
                    query.setParameter("comment", comment);
                    query.executeUpdate();
                }
                return null;
            }
        });
    }

    /**
     * Delete all old dp links of the migrated controls. This should be the last
     */
    private void deleteOldLinks(final Set<Id> linkData) {
        IBaseDao<CnALink, Serializable> dao = getDaoFactory().getDAO(CnALink.class);
            dao.executeCallback(new HibernateCallback() {

                @Override
                public Object doInHibernate(Session session)
                        throws HibernateException, SQLException {
                Id[] linkDataArray = linkData.toArray(new CnALink.Id[linkData.size()]);
                int i = linkDataArray.length / 5;
                for (int j = 0; j < i; j++) {
                    Query query = session.createQuery("delete CnALink c where c.id=:id1 or "
                            + "c.id=:id2 or c.id=:id3 or c.id=:id4 or c.id=:id5");
                    query.setParameter("id1", linkDataArray[0 + (j * 5)]);
                    query.setParameter("id2", linkDataArray[1 + (j * 5)]);
                    query.setParameter("id3", linkDataArray[2 + (j * 5)]);
                    query.setParameter("id4", linkDataArray[3 + (j * 5)]);
                    query.setParameter("id5", linkDataArray[4 + (j * 5)]);
                    query.executeUpdate();
                }
                // update the rest
                for (int j = i * 5; j < linkDataArray.length; j++) {
                    Id id = linkDataArray[j];
                    Query query = session.createQuery("delete CnALink c where c.id=:id");
                    query.setParameter("id", id);
                    query.executeUpdate();
                }
                session.flush();
                return null;
                }
            });
    }

    /**
     * Collects the links to remove in the linkData set.
     *
     * @return the affected processes
     * @throws CommandException
     */
    private Set<CnATreeElement> collectLinksToRemove(CnATreeElement control, Set<Id> linkData) {
        VeriniceGraph processGraph = getGraph();
        Set<Edge> allRelations = processGraph.getEdgesByElementType(control, Process.TYPE_ID);

        String cUuid = control.getUuid();
        Set<CnATreeElement> affectedProcesses = new HashSet<>(allRelations.size());
        for (Edge edge : allRelations) {
            if (!RELATIONS.contains(edge.getType())) {
                continue;// ensure we only take the right link types (might be
                         // unnecessary)
            }

            if (edge.getSource().getUuid().equals(cUuid)) {
                affectedProcesses.add(edge.getTarget());
            } else {
                affectedProcesses.add(edge.getSource());
            }
            linkData.add(new CnALink.Id(edge.getSource().getDbId(), edge.getTarget().getDbId(),
                    edge.getType()));
        }
        return affectedProcesses;
    }


    /**
     * Write the state of the mapped control in the control properties.
     *
     * @param controls2Update
     *
     * @throws CommandException
     */
    private void updateControlWithToms(CnATreeElement control, Set<PropertyType> toms,
            Set<CnATreeElement> controls2Update) {
        for (PropertyType property : toms) {
            control.getEntity().setSimpleValue(property, "1");
        }
        controls2Update.add(control);
    }

    /**
     * Get the TOM set for a given control.
     */
    private Set<PropertyType> getTOMS(CnATreeElement control) {
        String title = control.getTitle();
        if (StringUtils.isEmpty(title)) {
            return Collections.emptySet();
        }
        return TomMapper.getInstance().getMapping(title.trim());
    }

    public List<String> getMissedControlNames() {
        return missedControlNames;
    }

    public List<String> getAffectedControlsNames() {
        return affectedControlsNames;
    }

    public List<String> getAffectedProcessNames() {
        return affectedProcessNames;
    }

    public int getNumberOfDeletedLinks() {
        return numberOfDeletedLinks;
    }

    public int getNumberOfCreatedLinks() {
        return numberOfCreatedLinks;
    }

    public Set<CnATreeElement> getProcesses() {
        return processes;
    }

    public int getAffectedNumberOfControls() {
        return affectedNumberOfControls;
    }

    public int getMissedNumberOfControls() {
        return missedNumberOfControls;
    }

}
