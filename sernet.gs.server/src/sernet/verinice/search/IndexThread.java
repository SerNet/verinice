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
package sernet.verinice.search;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionResponse;

import sernet.gs.service.ServerInitializer;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndexThread implements Callable<ActionResponse> {

    private static final Logger LOG = Logger.getLogger(IndexThread.class);
    
    private ISearchDao searchDao;

    private CnATreeElement element;
   
    public IndexThread() {
        super();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ActionResponse call() throws Exception {
        ServerInitializer.inheritVeriniceContextState();
        String json = JsonBuilder.getJson(element);
        if (LOG.isDebugEnabled()) {
            LOG.debug("JSON: " + json);
        }
        return getSearchDao().updateOrIndex(element.getUuid(), json);
    }
    
    public CnATreeElement getElement() {
        return element;
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    public ISearchDao getSearchDao() {
        return searchDao;
    }

    public void setSearchDao(ISearchDao searchDao) {
        this.searchDao = searchDao;
    }


}
