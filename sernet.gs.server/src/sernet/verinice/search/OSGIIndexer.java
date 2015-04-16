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
import org.springframework.core.task.TaskExecutor;

/**
 * handles elastic search indexing for tier2-mode, starts {@link Indexer} asynchroneously
 */
public class OSGIIndexer {
    
    private static final Logger LOG = Logger.getLogger(OSGIIndexer.class);
    
    private TaskExecutor taskExecutor;
    
    private Indexer indexer;
    
    public OSGIIndexer(TaskExecutor taskExecutor){
        this.taskExecutor = taskExecutor;
    }
    
    public void run(){
        taskExecutor.execute(new Runnable() {
            
            @Override
            public void run() {
                indexer.init();
                if(LOG.isInfoEnabled()){
                    LOG.info("Standalone Indexing finished");
                }
            }
        });
    }

    /**
     * @return the indexer
     */
    public Indexer getIndexer() {
        return indexer;
    }

    /**
     * @param indexer the indexer to set
     */
    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    /**
     * @return the taskExecutor
     */
    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    /**
     * @param taskExecutor the taskExecutor to set
     */
    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

}
