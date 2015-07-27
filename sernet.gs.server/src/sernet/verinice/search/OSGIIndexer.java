/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.search;

import org.apache.log4j.Logger;

/**
 * Handles elastic search indexing for tier2-mode.
 *
 */
public class OSGIIndexer {

    private static final Logger LOG = Logger.getLogger(OSGIIndexer.class);

    private String indexOnStartup = Boolean.FALSE.toString();

    private Indexer indexer;

    public OSGIIndexer(){}

    public void run() {
        if (Boolean.parseBoolean(getIndexOnStartup())) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Indexing on startup is enabled.");
            }

            indexer.nonBlockingIndexing();
        }
    }

    /**
     * @return the indexer
     */
    public Indexer getIndexer() {
        return indexer;
    }

    /**
     * @param indexer
     *            the indexer to set
     */
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
