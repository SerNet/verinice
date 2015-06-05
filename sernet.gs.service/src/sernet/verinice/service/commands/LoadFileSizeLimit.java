/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.service.commands;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;

/**
 * Load the max file size for attachments 
 * from file veriniceserver-plain.properties, property: veriniceserver.filesize.max
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LoadFileSizeLimit extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadFileSizeLimit.class);
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadFileSizeLimit.class);
        }
        return log;
    }
    
    // Default value if no property is defined in veriniceserver-plain.properties
    // Default value is used in standalone version too
    public static final int FILE_SIZE_MAX_DEFAULT = 100;
    
    private int fileSizeMax = FILE_SIZE_MAX_DEFAULT;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        String property = PropertyLoader.getFileSizeMax();
        if(property!=null) {
            try {
                fileSizeMax = Integer.valueOf(property);
            } catch( NumberFormatException e ) {
                getLog().error("Error while parsing property " + PropertyLoader.FILESIZE_MAX + ". Value is not a number: " + property);
                fileSizeMax = FILE_SIZE_MAX_DEFAULT;
            }
        }
    }

    public int getFileSizeMax() {
        return fileSizeMax;
    }

}
