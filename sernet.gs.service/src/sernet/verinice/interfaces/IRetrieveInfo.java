/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *      Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

import java.util.Objects;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;

public interface IRetrieveInfo {

    /**
     * @return true if properties are joined and retrieved
     */
    boolean isProperties();

    IRetrieveInfo setProperties(boolean properties);

    /**
     * @return true if links-up are joined and retrieved
     */
    boolean isLinksUp();

    IRetrieveInfo setLinksUp(boolean linksUp);

    /**
     * @return true if properties of links-up are joined and retrieved
     */
    boolean isLinksUpProperties();

    IRetrieveInfo setLinksUpProperties(boolean linksUpProperties);

    /**
     * @return true if links-down are joined and retrieved
     */
    boolean isLinksDown();

    IRetrieveInfo setLinksDown(boolean linksDown);

    /**
     * @return true if properties of links-down are joined and retrieved
     */
    boolean isLinksDownProperties();

    IRetrieveInfo setLinksDownProperties(boolean linksDownProperties);

    /**
     * @return true if children are joined and retrieved
     */
    boolean isChildren();

    IRetrieveInfo setChildren(boolean children);

    /**
     * @return true if properties of children are joined and retrieved
     */
    boolean isChildrenProperties();

    IRetrieveInfo setChildrenProperties(boolean childrenProperties);

    IRetrieveInfo setGrandchildren(boolean grandchildren);

    boolean isGrandchildren();

    boolean isParent();

    boolean isParentPermissions();

    IRetrieveInfo setParent(boolean parent);

    boolean isSiblings();

    IRetrieveInfo setSiblings(boolean siblings);

    boolean isPermissions();

    IRetrieveInfo setPermissions(boolean permissions);

    IRetrieveInfo setChildrenPermissions(boolean childrenPermissions);

    boolean isChildrenPermissions();

    /**
     * @return true if inner joins are used
     */
    boolean isInnerJoin();

    IRetrieveInfo setInnerJoin(boolean innerJoin);

    default void configureCriteria(DetachedCriteria criteria) {
        if (isProperties()) {
            criteria.setFetchMode("entity", FetchMode.JOIN);
            criteria.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
            criteria.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        }
        if (isPermissions()) {
            criteria.setFetchMode("permissions", FetchMode.JOIN);
        }

        if (isLinksDown()) {
            criteria.setFetchMode("linksDown", FetchMode.JOIN);
            criteria.setFetchMode("linksDown.dependency", FetchMode.JOIN);
            if (isLinksDownProperties()) {
                criteria.setFetchMode("linksDown.dependency.entity", FetchMode.JOIN);
                criteria.setFetchMode("linksDown.dependency.entity.typedPropertyLists",
                        FetchMode.JOIN);
                criteria.setFetchMode("linksDown.dependency.entity.typedPropertyLists.properties",
                        FetchMode.JOIN);
            }
        }
        if (isLinksUp()) {
            criteria.setFetchMode("linksUp", FetchMode.JOIN);
            criteria.setFetchMode("linksUp.dependant", FetchMode.JOIN);
            if (isLinksUpProperties()) {
                criteria.setFetchMode("linksUp.dependant.entity", FetchMode.JOIN);
                criteria.setFetchMode("linksUp.dependant.entity.typedPropertyLists",
                        FetchMode.JOIN);
                criteria.setFetchMode("linksUp.dependant.entity.typedPropertyLists.properties",
                        FetchMode.JOIN);
            }
        }
        if (isParent()) {
            criteria.setFetchMode("parent", FetchMode.JOIN);
            if (isSiblings()) {
                criteria.setFetchMode("parent.children", FetchMode.JOIN);
            }
            if (isParentPermissions()) {
                criteria.setFetchMode("parent.permissions", FetchMode.JOIN);
            }
        }
        if (isChildren()) {
            criteria.setFetchMode("children", FetchMode.JOIN);
            DetachedCriteria criteriaChildren = null;
            DetachedCriteria criteriaEntity = null;
            if (isInnerJoin()) {
                criteriaChildren = criteria.createCriteria("children");
            }
            if (isChildrenProperties()) {
                criteria.setFetchMode("children.entity", FetchMode.JOIN);
                if (isInnerJoin()) {
                    criteriaEntity = Objects.requireNonNull(criteriaChildren)
                            .createCriteria("entity");
                }
                criteria.setFetchMode("children.entity.typedPropertyLists", FetchMode.JOIN);
                if (isInnerJoin()) {
                    Objects.requireNonNull(criteriaEntity).createCriteria("typedPropertyLists");
                }
                criteria.setFetchMode("children.entity.typedPropertyLists.properties",
                        FetchMode.JOIN);
            }
            if (isChildrenPermissions()) {
                criteria.setFetchMode("children.permissions", FetchMode.JOIN);
                if (isInnerJoin()) {
                    Objects.requireNonNull(criteriaChildren).createCriteria("permissions");
                }
            }
        }
        if (isGrandchildren()) {
            criteria.setFetchMode("children.children", FetchMode.JOIN);
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }

}