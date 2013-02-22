/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;

@SuppressWarnings("serial")
public class LoadReportControlGroups extends GenericCommand implements ICachedCommand{

	private transient Logger log = Logger
			.getLogger(LoadReportControlGroups.class);

	private Organization rootObject;

	private List<CnATreeElement> result = new ArrayList<CnATreeElement>();

	private Integer dbId = null;
	
    private boolean resultInjectedFromCache = false;

	public Logger getLog() {
		if (log == null) {
			log = Logger.getLogger(LoadReportControlGroups.class);
		}
		return log;
	}

	public LoadReportControlGroups(Integer orgDbId) {
		this.dbId = orgDbId;
	}
	
	public LoadReportControlGroups(){
		// default constructor for use with JavaScript within BIRT
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}
	
	@Override
	public void execute() {
	    if(!resultInjectedFromCache){
	        LoadCnAElementById command = new LoadCnAElementById(
	                Organization.TYPE_ID, dbId);
	        try {
	            command = getCommandService().executeCommand(
	                    command);
	        } catch (CommandException e) {
	            log.error("Error while executing a command", e);
	        }
	        Object o = command.getFound();
	        if (o instanceof Organization) {
	            rootObject = (Organization) o;
	        }

	        Queue<ControlGroup> sortedResults = sortResults(rootObject);
	        for (ControlGroup g : sortedResults) {
	            if(!result.contains(g.getParent())){
	                result.add(g.getParent());
	            }
	        }
	    }
	}

	private Queue<ControlGroup> sortResults(CnATreeElement group) {
	    Queue<ControlGroup> finalList = new LinkedList<ControlGroup>();
	    if (group != null) {
	        try {
	            LoadReportElements elementLoader = new LoadReportElements(ControlGroup.TYPE_ID, group.getDbId(), true);
	            elementLoader = getCommandService().executeCommand(elementLoader);
	            List<CnATreeElement> commandResults = elementLoader.getElements();
	            ArrayList<ControlGroup> sortedList = new ArrayList<ControlGroup>(0);
	            for(CnATreeElement c : commandResults){
	                if(c instanceof ControlGroup && !sortedList.contains((ControlGroup)c) && hasOnlyControlChildren((ControlGroup)c)){
	                    sortedList.add((ControlGroup)c);
	                }
	            }
	            Collections.sort(sortedList,new Comparator<ControlGroup>() {

	                @Override
	                public int compare(ControlGroup o1, ControlGroup o2) {
	                    NumericStringComparator comparator = new NumericStringComparator();
	                    return comparator.compare(o1.getTitle(), o2.getTitle());
	                }
	            });
	            finalList.addAll(sortedList);
	        } catch (CommandException e) {
	            getLog().error("Error while executing command", e);
	        }
	    }
	    return finalList;
	}

	private boolean hasOnlyControlChildren(ControlGroup group) {
		for (CnATreeElement element : group.getChildren()) {
			if (!(element instanceof Control)) {
				return false;
			}
		}
		if(group.getChildren().size() == 0){
		    return false;
		}
		return true;
	}

	public List<CnATreeElement> getResult() {
		return result;
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(dbId);
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.result = (ArrayList<CnATreeElement>)result;
        resultInjectedFromCache = true;
        if(getLog().isDebugEnabled()){
            getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.result;
    }

}
