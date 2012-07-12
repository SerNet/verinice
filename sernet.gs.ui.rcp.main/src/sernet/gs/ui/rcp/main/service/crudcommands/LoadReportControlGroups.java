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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;

@SuppressWarnings("serial")
public class LoadReportControlGroups extends GenericCommand {

	private transient Logger log = Logger
			.getLogger(LoadReportControlGroups.class);

	private Organization root_object;

	private List<CnATreeElement> result = new ArrayList<CnATreeElement>();

	private Integer dbId = null;

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

		LoadCnAElementById command = new LoadCnAElementById(
				Organization.TYPE_ID, dbId);
		try {
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
		} catch (CommandException e) {
			log.error("Error while executing a command", e);
		}
		Object o = command.getFound();
		if (o instanceof Organization) {
			root_object = (Organization) o;
		}

		Queue<ControlGroup> sortedResults = sortResults(root_object);
		for (ControlGroup g : sortedResults) {
			result.add(g);
		}
	}

	private Queue<ControlGroup> sortResults(CnATreeElement group) {
		Queue<ControlGroup> finalList = new LinkedList<ControlGroup>();
		if (group != null) {
			Set<CnATreeElement> children = group.getChildren();
			List<String> sortedTitleList = new ArrayList<String>();
			for (CnATreeElement e : children) {
				if (e instanceof ControlGroup) {
					ControlGroup g = (ControlGroup) e;
					sortedTitleList.add(g.getTitle());
				}
			}
			Collections.sort(sortedTitleList);
			for (String title : sortedTitleList) {
				ControlGroup g = null;
				for (CnATreeElement e : children) {
					if (e instanceof ControlGroup) {
						g = (ControlGroup) e;
						if (g.getTitle().equals(title)) {
							if (!finalList.contains(g) && !hasOnlyControlChildren(g)) {
								finalList.offer(g);
							}
							for (ControlGroup child : sortResults(g)) {
								if (!finalList.contains(child) && !hasOnlyControlChildren(g)) {
									finalList.offer(child);
								}
							}
						}
					}
				}
			}
		}
		return finalList;
	}

	private boolean hasOnlyControlChildren(ControlGroup group) {
		for (CnATreeElement element : group.getChildren()) {
			if (element instanceof ControlGroup) {
				return false;
			}
		}
		return true;
	}

	public List<CnATreeElement> getResult() {
		return result;
	}

}
