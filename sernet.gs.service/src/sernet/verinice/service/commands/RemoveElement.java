/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 *     Henning Heinold <h.heinold@tarent.de> - cascade when deleting CnATreeElement with FinishedRiskAnalysis
 *     Alexander Ben Nasrallah
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.CollectionUtil;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.service.SecurityException;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.IFinishedRiskAnalysisListsDao;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Removes tree-elements.
 *
 * Children, links and attachments are deleted by hibernate cascading (see
 * CnATreeElement.hbm.xml)
 *
 * The order of deletion is unspecified.
 *
 * @author Alexander Koderman <ak[at]sernet[dot]de>.
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class RemoveElement extends GenericCommand
        implements IChangeLoggingCommand, INoAccessControl {

    private static final Logger LOG = Logger.getLogger(RemoveElement.class);

    private transient IFinishedRiskAnalysisListsDao raListDao;

    private CnATreeElement element;
    private ArrayList<Integer> dbIdTypeIds;
    private String stationId;

    private transient IBaseDao<CnATreeElement, Serializable> dao;

    public RemoveElement(CnATreeElement... elements) {
        // only transfer id of element to keep footprint small:
        dbIdTypeIds = Stream.of(elements).map(CnATreeElement::getDbId)
                .collect(Collectors.toCollection(ArrayList::new));
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    public RemoveElement(Collection<? extends CnATreeElement> elements) {
        this(elements.toArray(new CnATreeElement[] {}));
    }

    @Override
    public void execute() {
        dao = getDaoFactory().getDAO(CnATreeElement.class);
        dbIdTypeIds.forEach(this::removeElement);
    }

    private void removeElement(Integer dbid) {
        try {
            this.element = dao.findById(dbid);
            if (this.element == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Element was not found in db. Db-Id: " + dbid);
                }
                return;
            }
            // first we check if the operation is allowed for the element and
            // the children
            dao.checkRights(element);
            checkRightsOfSubtree(element);
            removeElement(element);
        } catch (SecurityException e) {
            LOG.error("SecurityException while deleting element: " + element, e);
            throw e;
        } catch (RuntimeException e) {
            LOG.error("RuntimeException while deleting element: " + element, e);
            throw e;
        } catch (Exception e) {
            LOG.error("Exception while deleting element: " + element, e);
            throw new RuntimeCommandException(e);
        }
    }

    private void removeElement(CnATreeElement element) throws CommandException {
        if (element instanceof IBpElement) {
            // We could be removing an element that has a safeguard as one
            // of its children. Since we want our manual event listeners to be
            // fired for those and their links as well (via element.remove()),
            // we need to delete them by hand. This is not an optimal solution
            // and should be replaced by Hibernate event listeners someday.
            // (see VN-2084)

            LoadSubtreeIds loadSubtreeIds = new LoadSubtreeIds(element);

            LoadSubtreeIds subTrees = getCommandService().executeCommand(loadSubtreeIds);
            Set<Integer> ids = subTrees.getDbIdsOfSubtree();
            List<CnATreeElement> elements = new ArrayList<>(ids.size());
            for (List<Integer> chunk : CollectionUtil.partition(List.copyOf(ids),
                    IDao.QUERY_MAX_ITEMS_IN_LIST)) {
                DetachedCriteria criteria = DetachedCriteria.forClass(CnATreeElement.class)
                        .add(Restrictions.in("dbId", chunk));
                RetrieveInfo.getPropertyInstance().configureCriteria(criteria);
                elements.addAll(dao.findByCriteria(criteria));
            }
            Set<CnATreeElement> persons = elements.stream().filter(CnATreeElement::isPerson)
                    .collect(Collectors.toSet());
            if (!persons.isEmpty()) {
                removeConfigurations(persons);
            }
            removeElements(elements);
            return;

        }

        if (element.isPerson()) {
            removeConfigurations(Set.of(element));
        }

        if (element instanceof IBSIStrukturElement || element instanceof IBSIStrukturKategorie) {
            removeAllRiskAnalyses(element);
        }

        if (element instanceof ITVerbund) {
            CnATreeElement cat = ((ITVerbund) element).getCategory(PersonenKategorie.TYPE_ID);

            // A defect in the application allowed that ITVerbund instances
            // without a category are
            // created. With this tiny check we can ensure that they can be
            // deleted.
            if (cat != null) {
                Set<CnATreeElement> personen = cat.getChildren();
                if (!personen.isEmpty()) {
                    removeConfigurations(personen);
                }
            }
        }

        if (element instanceof FinishedRiskAnalysis) {
            FinishedRiskAnalysis analysis = (FinishedRiskAnalysis) element;
            removeRiskAnalysis(analysis);
        }

        if (element instanceof GefaehrdungsUmsetzung && element.getParent() != null) {
            GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) element;
            removeFromLists(element.getParent().getDbId(), gef);
        }

        /*
         * Special case the deletion of FinishedRiskAnalysis instances: Before
         * the instance is deleted itself their children must be removed
         * manually (otherwise referential integrity is violated and Hibernate
         * reports an error).
         *
         * Using the children as an array ensure that there won't be a
         * ConcurrentModificationException while deleting the elements.
         */
        CnATreeElement[] children = element.getChildrenAsArray();

        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof FinishedRiskAnalysis) {
                removeRiskAnalysis((FinishedRiskAnalysis) children[i]);
            }
        }

        if (element instanceof ITVerbund) {
            removeAllGefaehrdungsUmsetzungen(element);
        }

        element.remove();

        dao.delete(element);
    }

    private void removeElements(List<CnATreeElement> elements) {
        List<Entity> allEntities = elements.stream().map(CnATreeElement::getEntity)
                .collect(Collectors.toList());
        List<PropertyList> allPropertyLists = allEntities.stream()
                .flatMap(e -> e.getTypedPropertyLists().values().stream())
                .collect(Collectors.toList());
        List<Property> allProperties = allPropertyLists.stream()
                .flatMap(l -> l.getProperties().stream()).collect(Collectors.toList());

        @NonNull
        IBaseDao<Property, Serializable> propertyDao = getDaoFactory().getDAO(Property.class);
        IBaseDao<PropertyList, Serializable> propertyListDao = getDaoFactory()
                .getDAO(PropertyList.class);

        propertyDao.delete(allProperties);
        allPropertyLists.forEach(it -> it.getProperties().clear());
        propertyDao.flush();

        propertyListDao.delete(allPropertyLists);
        allEntities.forEach(it -> it.getTypedPropertyLists().clear());
        propertyListDao.flush();

        elements.forEach(it -> {
            it.remove();
            it.getChildren().clear();
        });
        dao.delete(elements);
    }

    private void checkRightsOfSubtree(CnATreeElement element) throws CommandException {
        LoadSubtreeIds loadSubtreeIdsCommand = new LoadSubtreeIds(element);
        loadSubtreeIdsCommand = getCommandService().executeCommand(loadSubtreeIdsCommand);
        Set<Integer> dbIdsOfSubtree = loadSubtreeIdsCommand.getDbIdsOfSubtree();
        Map<Serializable, Serializable> dbIdsToScopeIds = new HashMap<>(dbIdsOfSubtree.size());
        dbIdsOfSubtree.forEach(id -> dbIdsToScopeIds.put(id, element.getScopeId()));
        dao.checkRights(dbIdsToScopeIds);
    }

    /**
     * special handling for deletion of instances of
     * {@link GefaehrdungsUmsetzung} that does not belong to a
     * {@link FinishedRiskAnalysis} should only be called if @param element is
     * of type {@link ITVerbund} and always after all riskanalyses are removed /
     * deleted
     */
    private void removeAllGefaehrdungsUmsetzungen(CnATreeElement element) {
        String hqlQuery = "from CnATreeElement element where element.objectType = ? AND element.scopeId = ?";
        Object[] params = new Object[] { GefaehrdungsUmsetzung.HIBERNATE_TYPE_ID,
                element.getDbId() };
        List<?> elementsToDelete = getDaoFactory().getDAO(GefaehrdungsUmsetzung.class)
                .findByQuery(hqlQuery, params);
        for (Object o : elementsToDelete) {
            if (o instanceof GefaehrdungsUmsetzung) {
                getDaoFactory().getDAO(GefaehrdungsUmsetzung.class)
                        .delete((GefaehrdungsUmsetzung) o);
            }
        }
    }

    private void removeAllRiskAnalyses(CnATreeElement element) throws CommandException {
        if (ImportBsiGroup.TYPE_ID.equals(element.getTypeId())) {
            removeRiskAnalysisFromBSIImportGroup(element.getDbId());
        } else if (element.isItVerbund()) {
            removeRiskAnalysisForScope(element.getScopeId());
        } else if (element instanceof IBSIStrukturKategorie) {
            removeRiskAnalysesFromBSICategory(element);

        }
        // handling for instances of ISBSIStrukturElement not necessary
    }

    private void removeRiskAnalysesFromBSICategory(CnATreeElement element) throws CommandException {
        StringBuilder sb = new StringBuilder();
        sb.append("select element.dbId from CnATreeElement element where ");
        sb.append("element.scopeId = :scopeId and element.parentId = :parentId");
        String hql = sb.toString();

        // since the values of the list will be ints, we can't parameterize the
        // list
        @SuppressWarnings("rawtypes")
        List deleteList = dao.findByQuery(hql, new String[] { "scopeId", "parentId" },
                new Object[] { element.getScopeId(), element.getDbId() });
        for (Object o : deleteList) {
            if (o instanceof Integer) {
                LoadRiskAnalyses loadRiskAnalyses = new LoadRiskAnalyses((Integer) o, true);
                List<FinishedRiskAnalysis> riskAnalysesList = getCommandService()
                        .executeCommand(loadRiskAnalyses).getRaList();
                for (FinishedRiskAnalysis finishedRiskAnalysis : riskAnalysesList) {
                    removeRiskAnalysis(finishedRiskAnalysis);
                }
            }
        }
    }

    private void removeRiskAnalysisFromBSIImportGroup(Integer groupDbId) throws CommandException {
        String hql = "from CnATreeElement itv where itv.objectType = 'it-verbund' and itv.parentId = ("
                + " select importbsigroup.dbId from CnATreeElement importbsigroup where "
                + "importbsigroup.objectType = 'import-bsi' and importbsigroup.dbId = :dbId)";
        List<CnATreeElement> deleteList = dao.findByQuery(hql, new String[] { "dbId" },
                new Object[] { groupDbId });
        for (CnATreeElement o : deleteList) {
            if (o instanceof ITVerbund) {
                removeRiskAnalysisForScope(((ITVerbund) o).getScopeId());
            }
        }
    }

    private void removeRiskAnalysisForScope(int scopeId) throws CommandException {
        LoadRiskAnalyses loadRiskAnalyses = new LoadRiskAnalyses(scopeId);
        loadRiskAnalyses = getCommandService().executeCommand(loadRiskAnalyses);
        List<FinishedRiskAnalysis> raList = loadRiskAnalyses.getRaList();
        for (FinishedRiskAnalysis finishedRiskAnalysis : raList) {
            removeRiskAnalysis(finishedRiskAnalysis);
        }
    }

    private void removeRiskAnalysis(FinishedRiskAnalysis finishedRiskAnalysis) {
        List<FinishedRiskAnalysisLists> list = getRaListDao()
                .findByFinishedRiskAnalysisId(finishedRiskAnalysis.getDbId());

        for (FinishedRiskAnalysisLists ra : list) {
            removeChildren(finishedRiskAnalysis, ra);
            getRaListDao().delete(ra);
        }
    }

    private void removeChildren(FinishedRiskAnalysis analysis, FinishedRiskAnalysisLists lists) {
        Set<CnATreeElement> children = analysis.getChildren();
        for (CnATreeElement child : children) {
            if (child instanceof GefaehrdungsUmsetzung) {
                GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) child;
                lists.removeGefaehrdungCompletely(gef);
            }
        }
    }

    /**
     * Remove from all referenced lists.
     */
    private void removeFromLists(int analysisId, GefaehrdungsUmsetzung gef)
            throws CommandException {
        FindRiskAnalysisListsByParentID command = new FindRiskAnalysisListsByParentID(analysisId);
        getCommandService().executeCommand(command);
        FinishedRiskAnalysisLists lists = command.getFoundLists();
        if (lists != null) {
            lists.removeGefaehrdungCompletely(gef);
        }
    }

    private void removeConfigurations(Set<CnATreeElement> persons) {
        IBaseDao<@NonNull Configuration, Serializable> configurationDao = getDaoFactory()
                .getDAO(Configuration.class);
        @SuppressWarnings("unchecked")
        List<Configuration> configurations = configurationDao.findByCriteria(DetachedCriteria
                .forClass(Configuration.class).add(Restrictions.in("person", persons)));
        if (!configurations.isEmpty()) {
            configurationDao.delete(configurations);
            // When a Configuration instance got deleted the server needs to
            // update
            // its cached role map. This is done here.
            getCommandService().discardUserData();
        }
    }

    @Override
    public void clear() {
        element = null;
    }

    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_DELETE;
    }

    @Override
    public List<CnATreeElement> getChangedElements() {
        return Collections.<CnATreeElement> singletonList(element);
    }

    @Override
    public String getStationId() {
        return stationId;
    }

    public IFinishedRiskAnalysisListsDao getRaListDao() {
        if (raListDao == null) {
            raListDao = getDaoFactory().getFinishedRiskAnalysisListsDao();
        }
        return raListDao;
    }
}
