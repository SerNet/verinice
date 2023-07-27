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
import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.apache.commons.lang.StringUtils;

/**
 * Wraps a verinice query. A query is restricted to 200 elements by default. An
 * empty query causes a search over all elements without any restrictions.
 * 
 * The user scope is taken into account by an empty query.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class VeriniceQuery implements Serializable {

    public static final int DEFAULT_LIMIT = 200;
    public static final int MAX_LIMIT = 200000;

    public static final String EMPTY_QUERY = StringUtils.EMPTY;

    private int limit = 0;

    private String query = EMPTY_QUERY;

    private int scopeId;

    /*
     * false by default, as implemented in all implementations of
     * sernet.verinice.interfaces.IAuthService.isScopeOnly()
     */
    private boolean isScopeOnly = false;

    /**
     * Initializes a verinice query object.
     *
     * @param query
     *            If query is null the query string is set to "" and if the
     *            query contains slashes they will be removed.
     * @param limit
     *            If limit <= 0 the limit is set to {@link #DEFAULT_LIMIT}.
     */
    public VeriniceQuery(String query, int limit) {
        this(query, limit, -1);
    }

    public VeriniceQuery(String query, int limit, int scopeId) {
        this.query = query == null ? EMPTY_QUERY : sanitizeQuery(query);
        this.limit = limit > 0 ? limit : MAX_LIMIT;
        this.scopeId = scopeId;
    }

    /**
     * Limits the results. Default value is 200
     */
    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getQuery() {
        return query;
    }

    private static String sanitizeQuery(String query) {
        return Normalizer.normalize(query, Form.NFC).replace("/", "");
    }

    /**
     * Tests if a query was set. Empty query means "".
     */
    public boolean isQueryEmpty() {
        return EMPTY_QUERY.equals(query);
    }

    public int getScopeId() {
        return scopeId;
    }

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
