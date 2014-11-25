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
package sernet.verinice.fei.rcp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.fei.service.PathFinderCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;

/**
 * PathFinder is a {@link IDirectoryHandler} which searches
 * verinice objects by a path defined in link.properties files.
 * 
 * The path in link.properties is defined like in UNIX filesystems.
 * Separator is a slash, two dots are the parent directory. 
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class PathFinder implements IDirectoryHandler {

    private static final Logger LOG = Logger.getLogger(PathFinder.class);
   
    public static final String PROPERTY_FILE_NAME = "link.properties";
    public static final String COMMENT = "#";
    
    private File dir;
    private String uuid;
    
    private ICommandService commandService;
    
    /* (non-Javadoc)
     * @see sernet.verinice.fei.rcp.IFileHandler#handle(java.io.File, sernet.verinice.fei.rcp.TraverserContext)
     */
    @Override
    public void enter(File file, TraverserContext context) {       
        try {
            if(file==null) {
                return;
            }
            if(file.isDirectory()) {
                dir = file;
            } else {
                dir = file.getParentFile();
            }
            CnATreeElement currentGroup = (CnATreeElement) context.getProperty(FileElementImportTraverser.CURRENT_DIRECTORY);
            if(currentGroup!=null) {
                uuid = currentGroup.getUuid();
            }
            CnATreeElement target = readPathFromProperty(context);
            if(target!=null) {
                context.addProperty(FileElementImportTraverser.LINK_TARGET, target);               
            }
        } catch (Exception e) {
            LOG.error("Error while finding path", e);
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.fei.rcp.IDirectoryHandler#leave(java.io.File, sernet.verinice.fei.rcp.TraverserContext)
     */
    @Override
    public void leave(File file, TraverserContext context) {
        //context.removeProperty(FileElementImportTraverser.LINK_TARGET);
    }


    private CnATreeElement readPathFromProperty(TraverserContext context) throws IOException, CommandException {      
        String filePath = dir.getPath() + File.separatorChar + PROPERTY_FILE_NAME;
        File f = new File(filePath);
        if(!f.exists()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Link property file not found: " + filePath);
            }
            return null;
        } else {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            CnATreeElement target = null;
            while (line != null) {
                if(!line.startsWith(COMMENT)) {
                    target = readPath(line);
                }
                line = br.readLine();
            }
            if(target==null) {
                // link.properties exitst but without valid target
                context.removeProperty(FileElementImportTraverser.LINK_TARGET);
            }
            return target;
        }
    }

    private CnATreeElement readPath(String line) throws CommandException {
        PathFinderCommand command = new PathFinderCommand(uuid, line);
        command = getCommandService().executeCommand(command);
        CnATreeElement target = command.getTarget();
        if (LOG.isDebugEnabled() && target!=null) {
            LOG.debug("Link target found for directory " + dir.getPath() + ": " + target.getTitle());
        }
        return target;
    }
 
    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = ServiceFactory.lookupCommandService();
        }
        return commandService;
    }


    
}
