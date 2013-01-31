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
	
	public static final int SOURCE_VERBUND = 1;
	
	public static final int SOURCE_ELEMENT = 2;
	
	private List<ItVerbundWrapper> itVerbundList;
	
	private ITVerbund selectedItVerbund;
	
	private Converter itVerbundConverter = new ItVerbundConverter(this);
	
	private String selectedItVerbundTitel;
	
	private Integer selectedElementId;
	
	private static final int DEFAULT_LIST_SIZE = 10;
	
	private List<CnATreeElement> gebaeudeList = new ArrayList<CnATreeElement>(DEFAULT_LIST_SIZE);
	private List<CnATreeElement> raumList = new ArrayList<CnATreeElement>(DEFAULT_LIST_SIZE);
	private List<CnATreeElement> clienteList = new ArrayList<CnATreeElement>(DEFAULT_LIST_SIZE);
	private List<CnATreeElement> serverList = new ArrayList<CnATreeElement>(DEFAULT_LIST_SIZE);
	private List<CnATreeElement> netzList = new ArrayList<CnATreeElement>(DEFAULT_LIST_SIZE);
	private List<CnATreeElement> anwendungList = new ArrayList<CnATreeElement>(DEFAULT_LIST_SIZE);
	private List<CnATreeElement> personList = new ArrayList<CnATreeElement>(DEFAULT_LIST_SIZE);
    private List<CnATreeElement> tkKomponenteList = new ArrayList<CnATreeElement>(DEFAULT_LIST_SIZE);
    private List<CnATreeElement> sonstItList = new ArrayList<CnATreeElement>(DEFAULT_LIST_SIZE);
	
	public AssetNavigationBean() {
		super();
		loadItVerbundList();
	}

	public void loadItVerbundList() {
		ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
		LoadCnAElementByType<ITVerbund> command = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);
		try {
			service.executeCommand(command);
			setItVerbundList(getWrapperList(command.getElements()));
		} catch (Exception e) {
			LOG.error("Error while loading IT-Verbuende", e);
			Util.addError("verbundForm", Util.getMessage("error"));
		}	
	}
	
	/**
     * @param elements
     * @return
     */
    private List<ItVerbundWrapper> getWrapperList(List<ITVerbund> elements) {
        List<ItVerbundWrapper> wrapperList = new ArrayList<ItVerbundWrapper>();
        for (ITVerbund verbund : elements) {
            wrapperList.add(new ItVerbundWrapper(verbund));
        }
        return wrapperList;
    }

    public void loadChildren(ILoadChildren command, int source) {
		Integer itVerbundId = null;
		try {
			setSelectedItVerbund((ITVerbund) getItVerbundConverter().getAsObject(null, null, getSelectedItVerbundTitel()));
			itVerbundId = (getSelectedItVerbund()==null) ? null : getSelectedItVerbund().getDbId();
			if(itVerbundId!=null || getSelectedElementId()!=null) {
				ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
				int id = (itVerbundId!=null) ? itVerbundId.intValue() : -1;
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
                    setTkKomponenteList(command.getTkKomponenteList());
                    setSonstItList(command.getSonstItList());
				}	
			} 
		} catch (CommandException e) {
			LOG.error("Error while loading todos for id: " + itVerbundId, e);
			Util.addError("verbundForm", Util.getMessage("error"));
		}
		
	}
	
	public void setItVerbundList(List<ItVerbundWrapper> itVerbundList) {
		this.itVerbundList = itVerbundList;
	}

	public List<ItVerbundWrapper> getItVerbundList() {
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

    public List<CnATreeElement> getTkKomponenteList() {
        return tkKomponenteList;
    }

    public List<CnATreeElement> getSonstItList() {
        return sonstItList;
    }

    public void setTkKomponenteList(List<CnATreeElement> tkKomponenteList) {
        this.tkKomponenteList = tkKomponenteList;
    }

    public void setSonstItList(List<CnATreeElement> sonstItList) {
        this.sonstItList = sonstItList;
    }
}
