/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh[at]sernet[dot]de>.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.SaveElement;

public class GS2BSITransformService {
	
	private IProgressObserver progressObserver;

	private Logger log = null;
	
	private int numberOfControls;
	
	private List itemList;
	
	private IModelUpdater modelUpdater;
	
	private Group selectedGroup;
	
	private int numberProcessed;

	private IAuthService authService;

	private ICommandService commandService;
	
	private boolean isScenario = false;
	
	public GS2BSITransformService(IProgressObserver progressObserver,
			IModelUpdater modelUpdater, Group selectedGroup, Object data) {
		this.progressObserver = progressObserver;
		log = Logger.getLogger(GS2BSITransformService.class);
		itemList = new ArrayList<Object>(0);
		if(data instanceof Object[]){
		    Object[] o = (Object[]) data;
		    for(Object object : o){
		        itemList.add(object);
		    }
		} else if (data instanceof Object){
		    itemList.add(data);
		}
		this.modelUpdater = modelUpdater;
		this.selectedGroup = selectedGroup;
	}

	public boolean isScenario() {
		return isScenario;
	}

	public void run(){
		try{
			this.numberOfControls = itemList.size();
			progressObserver.beginTask(Messages.getString("GS2BSITransformService.0", numberOfControls), -1); //$NON-NLS-1$
			for(Object o : itemList){
				insertItem(progressObserver, selectedGroup, o);
			}
		} catch (Exception e){
			getLog().error("Error while transforming GS element to ISM element", e);
		}
	}
	
	/**
	 * @param monitor
	 * @param group 
	 * @param item
	 */
	private void insertItem(IProgressObserver monitor, Group group, Object item) {
		if(monitor.isCanceled()) {
			getLog().warn("Transforming canceled. " + numberProcessed + " items transformed."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}
		List<CnATreeElement> elements = new ArrayList<CnATreeElement>();
		if(item !=null) {
            
			if(item instanceof Massnahme){
				Massnahme m = (Massnahme)item;
				elements.add((CnATreeElement)generateControl(m, group));
			}
			if (item instanceof Gefaehrdung){
				Gefaehrdung g = (Gefaehrdung)item;
				elements.add(generateScenario(g, group));
			}
			if(item instanceof Baustein){
			    try {
			        Baustein b = (Baustein)item;
			        if(group.canContain(new IncidentScenario())){
			            IncidentScenarioGroup newGroup = new IncidentScenarioGroup(group);
			            newGroup.setTitel(b.getId() + " " + b.getTitel());
			            CnATreeElement saveNew = null;
			            saveNew = CnAElementFactory.getInstance().saveNew(group,
			                    IncidentScenarioGroup.TYPE_ID,
			                    new BuildInput<IncidentScenarioGroup>(newGroup),
			                    false /* do not notify single elements*/);
			            saveNew.setTitel(b.getId() + " " + b.getTitel());
			            CnAElementHome.getInstance().updateEntity(saveNew);
			            CnAElementFactory.getLoadedModel().childAdded(group,saveNew);
			            for(Gefaehrdung g : b.getGefaehrdungen()){
			                IncidentScenario scen = generateScenario(g, (IncidentScenarioGroup)saveNew); 
			                elements.add(scen);
			            }
			        } else if(group.canContain(new Control())){
			            ControlGroup newGroup = new ControlGroup(group);
			            newGroup.setTitel(b.getTitel());
                        CnATreeElement saveNew = null;
                        saveNew = CnAElementFactory.getInstance().saveNew(group,
                                ControlGroup.TYPE_ID,
                                new BuildInput<ControlGroup>(newGroup),
                                false /* do not notify single elements*/);
                        saveNew.setTitel(b.getId() + " " + b.getTitel());
                        CnAElementHome.getInstance().updateEntity(saveNew);
                        CnAElementFactory.getLoadedModel().childAdded(group,saveNew);
			            for(Massnahme m : b.getMassnahmen()){
			                elements.add(generateControl(m, (ControlGroup)saveNew));
			            }
			        }
			    } catch (Exception e) {
			        getLog().error("Error while transforming baustein", e);
			    }
			}

		}
		saveItems(elements, monitor);
	}
	
	private void saveItems(List<CnATreeElement> elements, IProgressObserver monitor){
	    SaveElement command = null;
	    boolean errorOccured = false;
        if(elements.size() > 0) {
            for(CnATreeElement e : elements){
                monitor.setTaskName(getText(numberProcessed,e.getTitle()));
                if(e.getParent().canContain(e)){
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Creating element,  UUID: " + e.getUuid() + ", title: " + e.getTitle());    //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } else {
                    getLog().warn("trying to drop an item into a group that is unable to accept this type of items");
                    errorOccured = true;
                }   
                if(!errorOccured){
                    try {
                        HashSet<Permission> newperms = new HashSet<Permission>();
                        newperms.add(Permission.createPermission(e, getAuthService().getUsername(), true, true));
                        e.setPermissions(newperms);
                        command = new SaveElement(e);
                        if(e instanceof IncidentScenario){
                            isScenario = true;
                        }
                        command = getCommandService().executeCommand(command);
                    } catch (CommandException ce) {
                        getLog().error("Error while inserting control", ce); //$NON-NLS-1$
                        throw new RuntimeException("Error while inserting control", ce); //$NON-NLS-1$
                    }
                    e = (CnATreeElement) command.getElement();
                    RetrieveInfo info = new RetrieveInfo().setParent(true).setChildren(true).setProperties(true);
                    LoadElementByUuid<CnATreeElement> c2 = new LoadElementByUuid<CnATreeElement>(e.getUuid(), info);
                    try {
                        c2 = ServiceFactory.lookupCommandService().executeCommand(c2);
                        e = c2.getElement();
                        c2 = new LoadElementByUuid<CnATreeElement>(e.getParent().getUuid(), info);
                        c2 = ServiceFactory.lookupCommandService().executeCommand(c2);
                        e.setParent(c2.getElement());
                    } catch (CommandException e1) {
                        getLog().error("Error while loading element", e1);
                    }
                    CnATreeElement parent =  e.getParent();
                    parent.addChild(e);
                    e.setParentAndScope(parent);
                    modelUpdater.childAdded((Group)parent, e);
                    monitor.processed(1);
                    numberProcessed++;
                }
            }               
        }
	}
	
	/**
	 * @param i
	 * @param title
	 */
	private String getText(int i, String title) {
		return Messages.getString("GS2BSITransformService.1", i, title); //$NON-NLS-1$
	}
	
	private Control generateControl(Massnahme m, CnATreeElement parent){
		Control c = new Control(parent);
		c.setTitel(m.getId() + " " + m.getTitel());
		try {
			String description = GSScraperUtil.getInstance().getModel().getMassnahmeHtml(m.getUrl(), m.getStand());
			c.setDescription(description);
		} catch (GSServiceException e) {
			getLog().error("Error while transforming massnahme into control", e);
		}
		return c;
	}
	
	private IncidentScenario generateScenario(Gefaehrdung g, Group parent){
		IncidentScenario s = new IncidentScenario(parent);
		if(g.getTitel() != null){
			String title = g.getId() + " " + g.getTitel();
			s.setTitel(title);
		} else {
			s.setTitel("Dummy Scenario");
		}
		// TODO: add description from threat to scenario
		return s;
	}
	
	public int getNumberOfControls() {
		return numberOfControls;
	}
	
    public int getNumberProcessed() {
        return numberProcessed;
    }

    public IAuthService getAuthService() {
        if (authService == null) {
            authService = createAuthService();
        }
        return authService;
    }

    private IAuthService createAuthService() {
        return ServiceFactory.lookupAuthService();
    }
    
    public ICommandService getCommandService() {
		if (commandService == null) {
			commandService = createCommandServive();
		}
		return commandService;
	}

	private ICommandService createCommandServive() {
		return ServiceFactory.lookupCommandService();
	}
	
	private Logger getLog(){
	    if(log == null){
	        log = Logger.getLogger(GS2BSITransformService.class);
	    }
	    return log;
	}
}
