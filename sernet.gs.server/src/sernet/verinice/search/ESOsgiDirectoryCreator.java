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

import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import sernet.verinice.interfaces.IDirectoryCreator;
import sernet.verinice.interfaces.IVeriniceConstants;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ESOsgiDirectoryCreator implements IDirectoryCreator {

    private static final Logger LOG = Logger.getLogger(ESOsgiDirectoryCreator.class);
    
    private static final String DEFAULT_INDEX_DIRECTORY = "elasticsearch/";
    
    /*
     * location of ES-working directory, injected by spring, different in Tier2 and Tier3
     */
    private String indexLocation;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDirectoryCreator#create()
     */
    @Override
    public String create() {
        return getTierDependentIndexLocation();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDirectoryCreator#create(java.lang.String)
     */
    @Override
    public String create(String subDirectory) {
        String location = FilenameUtils.concat(getTierDependentIndexLocation(), subDirectory);
        return location;
    }
    
 
    
    private String getTierDependentIndexLocation(){
        String location = null;
        try{
            // should be the case for tier2 mode, store index in <userhome>/elasticsearch
            location = FileUtils.toFile(new URL(FilenameUtils.concat(
                    System.getProperty(IVeriniceConstants.OSGI_INSTANCE_AREA), 
                    getIndexLocation()))).getAbsolutePath();
        } catch (Exception e){
            LOG.error("Error while getting directory path", e);
        }
        
        if(LOG.isDebugEnabled()){
            LOG.debug("Storing ES-Index in: " + location);
        }
        return location;
    }
    
    public String getIndexLocation() {
        if(indexLocation==null) {
            indexLocation = DEFAULT_INDEX_DIRECTORY;
        }
        return indexLocation;
    }

    public void setIndexLocation(String indexLocation) {
        this.indexLocation = indexLocation;
    }

}
