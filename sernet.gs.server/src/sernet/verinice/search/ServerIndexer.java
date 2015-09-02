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

import org.apache.log4j.Logger;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ServerIndexer {
    
    private static final Logger LOG = Logger.getLogger(ServerIndexer.class);
    
    private String indexOnStartup = Boolean.FALSE.toString();
    
    private Indexer indexer;
    
    public void init() {        
        if(Boolean.parseBoolean(getIndexOnStartup())) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Indexing on startup is enabled.");
            }
            getIndexer().nonBlockingIndexing();
        }
    }
    
    public Indexer getIndexer() {
        return indexer;
    }

    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    public String getIndexOnStartup() {
        return indexOnStartup;
    }

    public void setIndexOnStartup(String indexOnStartup) {
        this.indexOnStartup = indexOnStartup;
    }
}
