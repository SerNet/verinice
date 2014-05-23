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

import java.io.File;

/**
 * IDirectoryHandler is used by {@link IFileSystemTraverser}
 * to handle directories while traversing a file system.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IDirectoryHandler {
    
    /**
     * Called when a directory is entered.
     * 
     * @param directory A directory
     */
    void enter(File directory, TraverserContext context);
    
    /**
     * Called when a directory is left after the content is processed.
     * 
     * @param directory A directory
     */
    void leave(File directory, TraverserContext context);
}
