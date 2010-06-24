/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.web;

import java.util.ArrayList;
import java.util.List;

import javax.faces.convert.Converter;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.taskcommands.ILoadChildren;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;

/**
 * JSF managed bean for asset-navigation data and methods.
 * You can use this bean whenever your view needs asset-navigation elements.
 * 
 * In such a case you inject AssetNavigationBean in your managed bean by JSF configuration.
 * See {@link ToDoBean} for an example.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AssetNavigationBean {

	private static final Logger LOG = Logger.getLogger(AssetNavigationBean.class);
	
	final static int SOURCE_VERBUND = 1;
	
	final static int SOURCE_ELEMENT = 2;
	
	List<ITVerbund> itVerbundList;
	
	ITVerbund selectedItVerbund;
	
	Converter itVerbundConverter = new ItVerbundConverter(this);
	
	String selectedItVerbundTitel;
	
	Integer selectedElementId;
	
	private List<CnATreeElement> gebaeudeList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> raumList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> clienteList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> serverList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> netzList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> anwendungList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> personList = new ArrayList<CnATreeElement>(10);
	
	public AssetNavigationBean() {
		super();
		loadItVerbundList();
	}

	public void loadItVerbundList() {
		//ServerInitializer.inheritVeriniceContextState();
		ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
		LoadCnAElementByType<ITVerbund> command = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);
		try {
			service.executeCommand(command);
			setItVerbundList(command.getElements());
		} catch (Exception e) {
			LOG.error("Error while loading IT-Verbuende", e);
			Util.addError("verbundForm", Util.getMessage("error"));
		}	
	}
	
	public void loadChildren(ILoadChildren command, int source) {
		Integer itVerbundId = null;
		try {
			setSelectedItVerbund((ITVerbund) getItVerbundConverter().getAsObject(null, null, getSelectedItVerbundTitel()));
			itVerbundId = (getSelectedItVerbund()==null) ? null : getSelectedItVerbund().getDbId();
			if(itVerbundId!=null || getSelectedElementId()!=null) {
				ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
				int id = itVerbundId;
				if(SOURCE_ELEMENT==source && getSelectedElementId()!=null) {
					id = getSelectedElementId();
				}
				command.setId(id);
				service.executeCommand(command);			
				if(SOURCE_VERBUND==source) {
					setAnwendungList(command.getAnwendungList());
					setClienteList(command.getClienteList());
					setGebaeudeList(command.getGebaeudeList());
					setNetzList(command.getNetzList());
					setPersonList(command.getPersonList());
					setRaumList(command.getRaumList());
					setServerList(command.getServerList());
				}	
			} 
		} catch (CommandException e) {
			LOG.error("Error while loading todos for id: " + itVerbundId, e);
			Util.addError("verbundForm", Util.getMessage("error"));
		}
		
	}
	
	public void setItVerbundList(List<ITVerbund> itVerbundList) {
		this.itVerbundList = itVerbundList;
	}

	public List<ITVerbund> getItVerbundList() {
		return itVerbundList;
	}
	
	public void setSelectedItVerbund(ITVerbund selectedItVerbund) {
		this.selectedItVerbund = selectedItVerbund;
	}

	public ITVerbund getSelectedItVerbund() {
		return selectedItVerbund;
	}
	
	public Converter getItVerbundConverter() {
		return itVerbundConverter;
	}

	public void setItVerbundConverter(Converter itVerbundConverter) {
		this.itVerbundConverter = itVerbundConverter;
	}

	public String getSelectedItVerbundTitel() {
		return selectedItVerbundTitel;
	}

	public void setSelectedItVerbundTitel(String selectedItVerbundId) {
		this.selectedItVerbundTitel = selectedItVerbundId;
	}
	
	public Integer getSelectedElementId() {
		return selectedElementId;
	}

	public void setSelectedElementId(Integer selectedElementId) {
		this.selectedElementId = selectedElementId;
	}
	
	public List<CnATreeElement> getGebaeudeList() {
		return gebaeudeList;
	}

	public void setGebaeudeList(List<CnATreeElement> gebaeudeList) {
		this.gebaeudeList = gebaeudeList;
	}

	public List<CnATreeElement> getRaumList() {
		return raumList;
	}

	public void setRaumList(List<CnATreeElement> raumList) {
		this.raumList = raumList;
	}

	public List<CnATreeElement> getClienteList() {
		return clienteList;
	}

	public void setClienteList(List<CnATreeElement> clienteList) {
		this.clienteList = clienteList;
	}

	public List<CnATreeElement> getServerList() {
		return serverList;
	}

	public void setServerList(List<CnATreeElement> serverList) {
		this.serverList = serverList;
	}

	public List<CnATreeElement> getNetzList() {
		return netzList;
	}

	public void setNetzList(List<CnATreeElement> netzList) {
		this.netzList = netzList;
	}

	public List<CnATreeElement> getAnwendungList() {
		return anwendungList;
	}

	public void setAnwendungList(List<CnATreeElement> anwendungList) {
		this.anwendungList = anwendungList;
	}

	public List<CnATreeElement> getPersonList() {
		return personList;
	}

	public void setPersonList(List<CnATreeElement> personList) {
		this.personList = personList;
	}
}
