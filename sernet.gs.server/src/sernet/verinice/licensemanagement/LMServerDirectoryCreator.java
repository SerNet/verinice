/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.licensemanagement;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import sernet.verinice.interfaces.IDirectoryCreator;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LMServerDirectoryCreator implements IDirectoryCreator {
    
    private static final Logger LOG = Logger.getLogger(LMServerDirectoryCreator.class);
    
    /*
     * location of ES-working directory, injected by spring, different in Tier2 and Tier3
     */
    private Resource vnlLocation;
    
    /**
     * create the configured directory on server startup if not already existant
     */
    public void init(){
        getRootDirectoryPath();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDirectoryCreator#create()
     */
    @Override
    public String create() {
        return getRootDirectoryPath();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDirectoryCreator#create(java.lang.String)
     */
    @Override
    public String create(String subDirectory) {
        return FilenameUtils.concat(getRootDirectoryPath(), subDirectory);
    }
    
    private String getRootDirectoryPath(){
        String location = null;
        try {
            // should be the case for tier3 mode, store vnl in <servlet>/WEB-INF/vnl
            File vnlLocation = getVnlLocation().getFile();
            if ( !vnlLocation.exists() ){
                Path path = Files.createDirectory(Paths.get(vnlLocation.toURI()));
                LOG.debug("Path:\t" + path.toAbsolutePath() + " for storing vnl created");
            };
            location = getVnlLocation().getFile().getAbsoluteFile().getAbsolutePath();
        } catch (Exception e){
            LOG.error("Error getting root directory path. Vnl location is: " + getVnlLocation(), e);
        }    
        if (LOG.isDebugEnabled()){
            LOG.debug("vnl root directory: " + location);
        }
        return location;
    }
    
    /**
     * @return the vnlLocation
     */
    public Resource getVnlLocation() {
        return vnlLocation;
    }

    /**
     * @param vnlLocation the vnlLocation to set
     */
    public void setVnlLocation(Resource vnlLocation) {
        this.vnlLocation = vnlLocation;
    }

}
