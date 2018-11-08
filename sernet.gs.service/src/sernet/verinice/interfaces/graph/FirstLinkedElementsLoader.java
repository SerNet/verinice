/*******************************************************************************
 * Copyright (c) 2018 Urs Zeidler
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
 *     Urs Zeidler - uz[at]sernet.de initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.model.common.CnATreeElement;

/**
 * This graph loader loads the first elements not in the scope but linked by
 * object in the scope. So this loader will load only one layer of the outside
 * linked objects.
 */
public final class FirstLinkedElementsLoader extends GraphElementLoader {

    private static final long serialVersionUID = -6378519669050966133L;

    @Override
    public List<CnATreeElement> loadElements() {
        if (ArrayUtils.isEmpty(getScopeIds())) {
            throw new IllegalArgumentException(
                    "This graph loader only works on a set of scopes. The scopes are currently set to: "
                            + getScopeIds());
        }

        return getObjects(getLinkedObjectsOutsideOfScope());
    }

    /**
     * Loads all the elements in the uuidToLoad collection. 
     */
    @SuppressWarnings("unchecked")
    private List<CnATreeElement> getObjects(Collection<String> uuidToLoad) {
        if (uuidToLoad == null || uuidToLoad.isEmpty()) {
            return Collections.emptyList();
        }

        DetachedCriteria criteria = DetachedCriteria.forClass(CnATreeElement.class);
        criteria.add(Restrictions.in("uuid", uuidToLoad));
        criteria.setFetchMode("entity", FetchMode.JOIN);
        criteria.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
        criteria.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        criteria.setFetchMode("linksDown", FetchMode.JOIN);
        criteria.setFetchMode("linksDown.dependency", FetchMode.JOIN);
        criteria.setFetchMode("linksDown.dependency.entity", FetchMode.JOIN);
        criteria.setFetchMode("linksDown.dependency.entity.typedPropertyLists", FetchMode.JOIN);
        criteria.setFetchMode("linksUp", FetchMode.JOIN);
        criteria.setFetchMode("linksUp.dependency", FetchMode.JOIN);
        criteria.setFetchMode("linksUp.dependency.entity", FetchMode.JOIN);
        criteria.setFetchMode("linksUp.dependency.entity.typedPropertyLists", FetchMode.JOIN);

        return getCnaTreeElementDao().findByCriteria(criteria);
    }

    /**
     * Select the uuids of all CnaTreeelements outside the given scopes which
     * are connected to CnaTreeelements inside the scopes.
     */
    @SuppressWarnings("unchecked")
    private Collection<String> getLinkedObjectsOutsideOfScope() {
        return (Collection<String>) getCnaTreeElementDao().executeCallback(s -> {
            SQLQuery sqlQuery = s.createSQLQuery("select t.uuid from cnalink l\n"
                    + "inner join cnatreeelement as s on s.dbid=l.dependant_id\n"
                    + "inner join cnatreeelement as t on t.dbid=l.dependency_id\n"
                    + "where s.scope_id<>t.scope_id and ( s.scope_id in(:scopes))\n"
                    + "union select s.uuid from cnalink l\n"
                    + "inner join cnatreeelement as s on s.dbid=l.dependant_id\n"
                    + "inner join cnatreeelement as t on t.dbid=l.dependency_id\n"
                    + "where s.scope_id<>t.scope_id and ( t.scope_id in(:scopes))");
            sqlQuery.setParameterList("scopes", getScopeIds());
            return sqlQuery.list();
        });
    }
}