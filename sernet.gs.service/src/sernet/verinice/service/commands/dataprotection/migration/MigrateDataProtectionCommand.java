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

import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IElementEntityDao;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Process;

/**
 * This command migrates one or more iso organizations from the old data
 * protection to the new one. Each control linked to a process which can be
 * fount in the migration table of the {@link TomMapper} will be migrated. This
 * means it removes all old links from type RELATIONS, and add the link
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
    private transient Map<CnATreeElement, Set<CnATreeElement>> ds_links2create;
    private transient Set<CnATreeElement> controls2Update;
    private transient Set<CnALink.Id> linkData;
    private Set<String> missedControlNames = new HashSet<>();
    private Set<String> affectedControlsNames;
    private Set<String> affectedProcessNames;
    private int numberOfDeletedLinks;
    private int numberOfCreatedLinks;
    private int affectedNumberOfControls;

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
    }

    @Override
    public void executeWithGraph() {
        VeriniceGraph processGraph = getGraph();
        processes = processGraph.getElements(Process.TYPE_ID);
        Set<CnATreeElement> controls = processGraph.getElements(Control.TYPE_ID);

        numberOfCreatedLinks = 0;
        ds_links2create = new HashMap<>(controls.size());
        controls2Update = new HashSet<>(controls.size());
        affectedControlsNames = new HashSet<>(controls.size());
        affectedProcessNames = new HashSet<>(processes.size());
        linkData = new HashSet<>(controls.size() * RELATIONS.size() * processes.size());

        for (CnATreeElement control : controls) {
            Set<PropertyType> toms = getTOMS(control);
            if (toms != null && !toms.isEmpty()) {
                try {
                    updateControlWithToms(control, toms);
                    Set<CnATreeElement> affectedProcesses = removeLinks(control, processGraph);
                    ds_links2create.put(control, affectedProcesses);
                    affectedControlsNames.add(control.getTitle());
                } catch (CommandException e) {
                    LOG.error("Error while transforming data protection controls. Current Control: "
                            + control.getTitle(), e);
                }
            } else {
                missedControlNames.add(control.getTitle());
            }
        }
        numberOfDeletedLinks = linkData.size();
        affectedNumberOfControls = ds_links2create.size();
        for (CnATreeElement cnATreeElement : processes) {
            affectedProcessNames.add(cnATreeElement.getTitle());
        }
        persitData();
        LOG.info("command finished");
    }

    /**
     * For performance reasons we write the data in big chunks to the database
     * and don't use commands.
     *
     * @param removeLinkCmd
     */
    private void persitData() {
        try {

            LOG.info("update controls");
            IElementEntityDao elementEntityDao = getDaoFactory().getElementEntityDao();
            for (CnATreeElement cnATreeElement : controls2Update) {
                elementEntityDao.mergeEntityOfElement(cnATreeElement, false);
            }

            LOG.info("Create ds links");
            IBaseDao<CnALink, Serializable> linkDao = getDaoFactory().getDAO(CnALink.class);
            for (Entry<CnATreeElement, Set<CnATreeElement>> entry : ds_links2create.entrySet()) {
                for (CnATreeElement process : entry.getValue()) {
                    CnALink link = new CnALink(process, entry.getKey(),
                            REL_PROCESS_CONTROL_OBJECTIVES, "auto migrated");
                    linkDao.merge(link, false);
                    numberOfCreatedLinks++;
                }
            }
            linkDao.flush();

            LOG.info("delete Links");
            IBaseDao<CnALink, Serializable> dao = getDaoFactory().getDAO(CnALink.class);
                dao.executeCallback(new HibernateCallback() {

                    @Override
                    public Object doInHibernate(Session session)
                            throws HibernateException, SQLException {
                    for (CnALink.Id id : linkData) {
                        Query query = session.createQuery("delete CnALink c where c.id=:id");
                        query.setParameter("id", id);
                        query.executeUpdate();
                    }
                    session.flush();
                    return null;
                    }
                });
        } catch (Exception e) {
            LOG.error("Error while delete links", e);
        }
    }

    /**
     * Remove the old dataprotection links between control and process.
     *
     * @return the affected processes
     * @throws CommandException
     */
    private Set<CnATreeElement> removeLinks(CnATreeElement control, VeriniceGraph processGraph)
            throws CommandException {
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
            CnALink.Id e = new CnALink.Id(edge.getSource().getDbId(), edge.getTarget().getDbId(),
                    edge.getType());
            linkData.add(e);
        }
        return affectedProcesses;
    }


    /**
     * Write the state of the mapped control in the control properties.
     *
     * @throws CommandException
     */
    private void updateControlWithToms(CnATreeElement control, Set<PropertyType> toms)
            throws CommandException {
        for (PropertyType property : toms) {
            control.getEntity().setSimpleValue(property, "1");
        }
        controls2Update.add(control);
    }

    /**
     * Get the TOMS set for a given control.
     */
    private Set<PropertyType> getTOMS(CnATreeElement control) {
        String title = control.getTitle();
        if (StringUtils.isEmpty(title)) {
            return null;
        }
        return TomMapper.getInstance().getMapping(title.trim());
    }

    public Set<String> getMissedControlNames() {
        return missedControlNames;
    }

    public Set<String> getAffectedControlsNames() {
        return affectedControlsNames;
    }

    public Set<String> getAffectedProcessNames() {
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

}
