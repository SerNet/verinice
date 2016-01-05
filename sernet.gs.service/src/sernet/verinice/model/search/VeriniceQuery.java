/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.search;

import java.io.Serializable;

/**
 * Wraps a verinice query. A query is restricted to 200 elements by default. An
 * empty query causes a search over all elements without any restrictions.
 * 
 * The user scope is taken into account by an empty query.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class VeriniceQuery implements Serializable{

    public final static int DEFAULT_LIMIT = 200;
    public final static int MAX_LIMIT = 200000;

    public final static String EMPTY_QUERY = "";

    private int limit = 0;

    private String query = EMPTY_QUERY;
    
    private int scopeId = -1;

    /*
     * false by default, as implemented in all implementations of {@link
     * sernet.verinice.interfaces.IAuthService.isScopeOnly()}
     */
    private boolean isScopeOnly = false;

    /**
     * Inits a verinice query object.
     *
     * @param query
     *            If query is null the query string is set to "" and if the
     *            query containes slashes they will be removed.
     * @param limit
     *            If limit <= 0 the limit is set to {@link #DEFAULT_LIMIT}.
     */
    public VeriniceQuery(String query, int limit) {
        this.query = query == null ? EMPTY_QUERY : escapeQuery(query);
        if(limit==0) {
            this.limit = MAX_LIMIT;
        } else {
            this.limit = (limit >= 0) ? limit : DEFAULT_LIMIT;
        }
    }
    
    public VeriniceQuery(String query, int limit, int scopeId){
        this(query, limit);
        this.scopeId = scopeId;
    }

    /**
     * Limits the results. Default value is 200
     * 
     * @return
     */
    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getQuery() {
        return escapeQuery(query);
    }

    private String escapeQuery(String query) {
        String escapedQuery = query.replaceAll("/", "");
        return escapedQuery;
    }

    /**
     * Tests if a query was set. Empty query means "".
     */
    public boolean isQueryEmpty() {
        return query == null || EMPTY_QUERY.equals(query);
    }

    /**
     * @return the scopeId
     */
    public int getScopeId() {
        return scopeId;
    }

    /**
     * @param scopeId the scopeId to set
     */
    public void setScopeId(int scopeId) {
        this.scopeId = scopeId;
    }

    public boolean isScopeOnly() {
        return isScopeOnly;
    }

    public void setScopeOnly(boolean isScopeOnly) {
        this.isScopeOnly = isScopeOnly;
    }
}
