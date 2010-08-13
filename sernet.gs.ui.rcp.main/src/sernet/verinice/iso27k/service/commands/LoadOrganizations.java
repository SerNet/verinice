/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service.commands;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class LoadOrganizations extends GenericCommand {

	final Comparator<Organization> orgComparator = new OrgSorter();
	
	private List<Organization> organizationList;
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		RetrieveInfo ri = new RetrieveInfo();
		ri.setChildren(true).setParent(true).setSiblings(true);
		List<Organization> orgList = getDaoFactory().getDAO(Organization.class).findAll(ri);
		for (Organization organization : orgList) {
			hydrate(organization);
		}
		//Collections.sort(orgList, orgComparator);
		setOrganizationList(orgList);
	}
	
	/**
	 * @param organization
	 */
	private void hydrate(Organization organization) {
		organization.getTitle();
		/*
		if(organization.getChildren()!=null) {
			organization.getChildren().size();
		}
		*/
	}

	public List<Organization> getOrganizationList() {
		return organizationList;
	}

	public void setOrganizationList(List<Organization> organizationList) {
		this.organizationList = organizationList;
	}
	
	class OrgSorter implements Comparator<Organization>,Serializable {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Organization o1, Organization o2) {
			int result = 1;
			if(o1!=null && o1.getTitle()!=null && o2!=null && o2.getTitle()!=null) {
				result = o1.getTitle().compareTo(o2.getTitle());
			} else if(o1!=null && o1.getTitle()!=null) {
				result = -1;
			} else if(o2==null || o2.getTitle()==null) {
				result = 0;
			}
			return result;
		}
		
	}

}
