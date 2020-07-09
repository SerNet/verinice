/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin
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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads the database IDs of the subtree with the given element as root.
 * 
 * The command does not use recursion to load the IDs. The IDs are loaded with
 * only one SQL statement.
 */
public class LoadSubtreeIds extends GenericCommand {

    private static final long serialVersionUID = 3225159241813826719L;

    private CnATreeElement element;

    private Set<Integer> dbIdsOfSubtree = new HashSet<>();

    private String typeId;

    public LoadSubtreeIds(CnATreeElement element) {
        this(element, null);
    }

    public LoadSubtreeIds(CnATreeElement element, String typeId) {
        super();
        this.element = element;
        this.typeId = typeId;
    }

    /*
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        validateElement(this.element);

        List<Object[]> parentChildRelationships = loadDbAndParentIdsOfScope(element.getScopeId());
        Map<Object, List<Object[]>> childIdsByParentId = parentChildRelationships.stream()
                .collect(Collectors.groupingBy(item -> (Integer) item[1]));

        Set<Object[]> childrenOnCurrentLevel = Collections
                .singleton(new Object[] { element.getDbId(), null, element.getTypeId() });
        while (!childrenOnCurrentLevel.isEmpty()) {
            Set<Object[]> childrenOnNextLevel = new HashSet<>();
            for (Object[] childData : childrenOnCurrentLevel) {
                Integer childId = (Integer) childData[0];
                String childType = (String) childData[2];
                if (typeId == null || typeId.equals(childType)) {
                    dbIdsOfSubtree.add(childId);
                }
                List<Object[]> children = childIdsByParentId.get(childId);
                if (children != null) {
                    childrenOnNextLevel.addAll(children);
                }
            }
            childrenOnCurrentLevel = childrenOnNextLevel;
        }
    }

    private List<Object[]> loadDbAndParentIdsOfScope(Integer scopeId) {
        @SuppressWarnings("unchecked")
        List<Object[]> parentChildRelationships = (List<Object[]>) getElementDAO()
                .executeCallback(session -> {
                    Criteria criteria = session.createCriteria(CnATreeElement.class);
                    criteria.add(Restrictions.isNotNull("parentId"));
                    if (scopeId != null) {
                        criteria.add(Restrictions.eq("scopeId", scopeId));
                    }

                    ProjectionList projectionList = Projections.projectionList();
                    projectionList.add(Projections.property("dbId"));
                    projectionList.add(Projections.property("parentId"));
                    projectionList.add(Projections.property("objectType"));
                    criteria.setProjection(projectionList);
                    return criteria.list();
                });
        return parentChildRelationships;
    }

    private void validateElement(CnATreeElement element) {
        if (element == null) {
            throw new IllegalArgumentException("Element is null.");
        }
    }

    public Set<Integer> getDbIdsOfSubtree() {
        return dbIdsOfSubtree;
    }

    private IBaseDao<CnATreeElement, Serializable> getElementDAO() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

}
