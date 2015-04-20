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

    private final static int DEFAULT_LIMIT = 200;

    private final static String EMPTY_QUERY = "";

    private int limit = DEFAULT_LIMIT;

    private String query = EMPTY_QUERY;

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
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
