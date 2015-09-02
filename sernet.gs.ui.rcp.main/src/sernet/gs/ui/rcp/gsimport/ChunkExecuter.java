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
package sernet.gs.ui.rcp.gsimport;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class ChunkExecuter<T> {
    
 private int maxNumberPerCommand = 100;
    
    private List<T> elementList;
    private IProgress monitor;
    
    ICommandService commandService;
    
    public ChunkExecuter(List<T> elementList, IProgress monitor) {
        super();
        this.elementList = elementList;
        this.monitor = monitor;
    }

    public void execute() throws CommandException {   
        int n = elementList.size();
        int current = 0;
        Iterator<T> elementIterator = this.elementList.iterator();
        while(elementIterator.hasNext()) {
            List<T> chunkList = new LinkedList<T>();
            for (int i = 0; i < maxNumberPerCommand; i++) {
                if(elementIterator.hasNext()) {
                    chunkList.add(elementIterator.next());
                } else {
                    break;
                }
            }
            
            executeChunk(chunkList);
            monitor.worked(chunkList.size());
            current+=chunkList.size();
            monitor.subTask(current + " von " + n + " gespeichert...");
        }
        
    }
    
    protected abstract void executeChunk(List<T> chunkList) throws CommandException;

    protected ICommandService getCommandService() {
        if(commandService==null) {
            commandService = createCommandService();
        }
        return commandService;
    }
    
    private ICommandService createCommandService() {
        return ServiceFactory.lookupCommandService();
    }

    public int getMaxNumberPerCommand() {
        return maxNumberPerCommand;
    }

    public void setMaxNumberPerCommand(int maxNumberPerCommand) {
        this.maxNumberPerCommand = maxNumberPerCommand;
    }
}
