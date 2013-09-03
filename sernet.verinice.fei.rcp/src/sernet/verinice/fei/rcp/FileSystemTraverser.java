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
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.Activator;

/**
 * FileSystemTraverser traverses a file system from a starting point.
 * You can add {@link IFileHandler} and {@link IDirectoryHandler} 
 * which are executed for every file and directory.
 * 
 * The {@link TraverserContext} is passed to the handler to share information.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class FileSystemTraverser implements IFileSystemTraverser {

    private static final Logger LOG = Logger.getLogger(FileSystemTraverser.class);

    private static final IFileHandler DUMMY_FILE_HANDLER = DummyFileHandler.getInstance();
    
    private String startPath;

    private List<IFileHandler> fileHandlerList;

    private List<IDirectoryHandler> directoryHandlerList;

    private List<FileExceptionNoStop> errorList;
    
    public FileSystemTraverser(String startPath) {
        super();
        this.startPath = startPath;
    }

    @Override
    public void traverseFileSystem() {
        Activator.inheritVeriniceContextState();
        File root = new File(startPath);
        process(root, new TraverserContext());
    }

    protected void process(File file, TraverserContext context) {     
        try {
            if (file.isFile()) {
                processFile(file, context);           
            } else if (file.isDirectory()) {
                processDir(file, context);
            }
        } catch (FileExceptionNoStop e) {
            LOG.warn("Erro while processing file: " + e.getPath() + ". " + e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stacktrace: ", e);
            }
            addError(e);
        }
    }

    protected void processFile(File file, TraverserContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(file.getName());
        }
        for (IFileHandler handler : getFileHandlerList()) {
            handler.handle(file, context);
        }
    }

    protected void processDir(File dir, TraverserContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(dir.getPath() + " [DIR]");
        }
        TraverserContext clonedContext = context.clone();
        for (IDirectoryHandler handler : getDirectoryHandlerList()) {
            handler.enter(dir, clonedContext);
        }
        File[] listOfFiles = dir.listFiles();
        if (listOfFiles != null) {
            // process files first
            for (int i = 0; i < listOfFiles.length; i++) {
                if(listOfFiles[i].isFile()) {
                    process(listOfFiles[i], clonedContext);
                }
            }
            // process directories
            for (int i = 0; i < listOfFiles.length; i++) {
                if(listOfFiles[i].isDirectory()) {
                    process(listOfFiles[i], clonedContext);
                }
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug(" [ACCESS DENIED]");
        }
        for (IDirectoryHandler handler : getDirectoryHandlerList()) {
            handler.leave(dir, clonedContext);
        }
    }
    
    public List<IFileHandler> getFileHandlerList() {
        if(fileHandlerList==null) {
            return fileHandlerList = new LinkedList<IFileHandler>();
        }
        return fileHandlerList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.fei.rcp.IFileSystemTraverser#addFileHandler(sernet.verinice.fei.rcp.IFileHandler)
     */
    @Override
    public void addFileHandler(IFileHandler fileHandler) {
        this.getFileHandlerList().add(fileHandler);
    }

    public List<IDirectoryHandler> getDirectoryHandlerList() {
        if(directoryHandlerList==null) {
            return directoryHandlerList = new LinkedList<IDirectoryHandler>();
        }
        return directoryHandlerList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.fei.rcp.IFileSystemTraverser#addDirectoryHandler(sernet.verinice.fei.rcp.IDirectoryHandler)
     */
    @Override
    public void addDirectoryHandler(IDirectoryHandler directoryHandler) {
        this.getDirectoryHandlerList().add(directoryHandler);
    }

    
    protected void addError(FileExceptionNoStop error) {
        if(errorList==null) {
            errorList = new LinkedList<FileExceptionNoStop>();
        }
        errorList.add(error);
    }
    
    protected List<FileExceptionNoStop> getErrorList() {
        return errorList;
    }
    
}

class DummyFileHandler implements IFileHandler {

    static IFileHandler instance = new DummyFileHandler();
    
    @Override
    public void handle(File file, TraverserContext context) {}

    /**
     * @return
     */
    public static IFileHandler getInstance() {
        return instance;
    }
    
}
