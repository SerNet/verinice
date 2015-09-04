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

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import sernet.verinice.interfaces.IDirectoryCreator;

/**
 * This directory creator does not create directories. It just
 * returns the path of Elasticsearch index directory.
 * If running in Tomcat index directory is created by Elasticsearch on startup.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ESServerDirectoryCreator implements IDirectoryCreator {

    private static final Logger LOG = Logger.getLogger(ESServerDirectoryCreator.class);
    
    /*
     * location of ES-working directory, injected by spring, different in Tier2 and Tier3
     */
    private Resource indexLocation;
    
    /**
     * Index root directory is created by Elasticsearch. 
     * This method returns the path only.
     * 
     * @see sernet.verinice.interfaces.IDirectoryCreator#create()
     */
    @Override
    public String create() {
        return getRootDirectoryPath();
    }

    /**
     * Index root directory is created by Elasticsearch. 
     * This method returns the path only.
     * 
     * @see sernet.verinice.interfaces.IDirectoryCreator#create(java.lang.String)
     */
    @Override
    public String create(String subDirectory) {
        return FilenameUtils.concat(getRootDirectoryPath(), subDirectory);
    }
    
    private String getRootDirectoryPath(){
        String location = null;
        try {
            // should be the case for tier3 mode, store index in <servlet>/WEB-INF/elasticsearch
            location = getIndexLocation().getFile().getAbsoluteFile().getAbsolutePath();
        } catch (Exception e){
            LOG.error("Error getting file path", e);
        }    
        if(LOG.isDebugEnabled()){
            LOG.debug("Elasticsearch index root directory: " + location);
        }
        return location;
    }

    public Resource getIndexLocation() {
        return indexLocation;
    }

    public void setIndexLocation(Resource indexLocation) {
        this.indexLocation = indexLocation;
    }

}
