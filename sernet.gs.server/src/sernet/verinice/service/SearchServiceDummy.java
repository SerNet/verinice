/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service;

import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResult;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class SearchServiceDummy implements ISearchService {

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.search.ISearchService#query(sernet.verinice.model.search.VeriniceQuery)
     */
    @Override
    public VeriniceSearchResult query(VeriniceQuery query) {
        return new VeriniceSearchResult();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.search.ISearchService#query(sernet.verinice.model.search.VeriniceQuery, java.lang.String)
     */
    @Override
    public VeriniceSearchResult query(VeriniceQuery query, String elementTypeId) {
        return new VeriniceSearchResult();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.search.ISearchService#index()
     */
    @Override
    public void index() {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.search.ISearchService#reindex()
     */
    @Override
    public void reindex() {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.search.ISearchService#remove(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void remove(CnATreeElement element) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.search.ISearchService#add(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void add(CnATreeElement element) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.search.ISearchService#update(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void update(CnATreeElement element) {
    }

    @Override
    public int getImplementationtype() {
        return ISearchService.ES_IMPLEMENTATION_TYPE_DUMMY;
    }

    @Override
    public boolean isReindexRunning() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setReindexRunning(boolean running) {
        // TODO Auto-generated method stub

    }

}
