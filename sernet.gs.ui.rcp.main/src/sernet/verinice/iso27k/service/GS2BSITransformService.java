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
import org.hibernate.mapping.Array;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.service.commands.SaveElement;

public class GS2BSITransformService {
	
	private IProgressObserver progressObserver;

	private Logger LOG = null;
	
	private int numberOfControls;
	
	private List itemList;
	
	private IModelUpdater modelUpdater;
	
	private Group selectedGroup;
	
	private int numberProcessed;

	private IAuthService authService;

	private ICommandService commandService;
	
	private boolean isScenario = false;
	
	public GS2BSITransformService(IProgressObserver progressObserver,
			IModelUpdater modelUpdater, Group selectedGroup, List items) {
		this.progressObserver = progressObserver;
		LOG = Logger.getLogger(GS2BSITransformService.class);
		itemList = items;
		this.modelUpdater = modelUpdater;
		this.selectedGroup = selectedGroup;
	}

	public boolean isScenario() {
		return isScenario;
	}

	public void run(){
		try{
			this.numberOfControls = 0;
			progressObserver.beginTask(Messages.getString("ControlTransformService.4", numberOfControls), numberOfControls); //$NON-NLS-1$
			for(Object o : itemList){
				insertItem(progressObserver, selectedGroup, o);
			}
		} catch (Exception e){
			LOG.error("Error while transforming GS element to ISM element", e);
		}
	}
	
	/**
	 * @param monitor
	 * @param group 
	 * @param item
	 */
	@SuppressWarnings("unchecked")
	private void insertItem(IProgressObserver monitor, Group group, Object item) {
		if(monitor.isCanceled()) {
			LOG.warn("Transforming canceled. " + numberProcessed + " of " + numberOfControls + " items transformed."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}
		SaveElement command = null;
		boolean errorOccured = false;
		List<CnATreeElement> elements = new ArrayList<CnATreeElement>();
		if(item !=null) {
            
			if(item instanceof Massnahme){
				Massnahme m = (Massnahme)item;
				elements.add((CnATreeElement)generateControl(m));
			}
			if (item instanceof Gefaehrdung){
				Gefaehrdung g = (Gefaehrdung)item;
				elements.add(generateScenario(g, group));
			}
			if(item instanceof Baustein){
			    Baustein b = (Baustein)item;
			    if(group.canContain(new IncidentScenario())){
			        for(Gefaehrdung g : b.getGefaehrdungen()){
			            elements.add(generateScenario(g, group));
			        }
			    } else if(group.canContain(new Control())){
			        for(Massnahme m : b.getMassnahmen()){
			            elements.add(generateControl(m));
			        }
	            }
			}
			if(elements.size() > 0) {
			    for(CnATreeElement e : elements){
			        monitor.setTaskName(getText(numberOfControls,numberProcessed,e.getTitle()));
			        if (group.canContain(e)) {
					    group.addChild(e);
					    e.setParentAndScope(group);
					    if (LOG.isDebugEnabled()) {
					        LOG.debug("Creating element,  UUID: " + e.getUuid() + ", title: " + e.getTitle());    //$NON-NLS-1$ //$NON-NLS-2$
					    }
			        } else {
//					     throw new ItemTransformException("Error while transforming massnahme into control"); //$NON-NLS-1$
			            LOG.warn("trying to drop an item into a group that is unable to accept this type of items");
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
			                numberOfControls++;
			            } catch (CommandException ce) {
			                LOG.error("Error while inserting control", ce); //$NON-NLS-1$
			                throw new RuntimeException("Error while inserting control", ce); //$NON-NLS-1$
			            }
			            e = (CnATreeElement) command.getElement();
			            e.setParentAndScope(group);
			            modelUpdater.childAdded(group, e);
			            monitor.processed(1);
			            numberProcessed++;
			        }
				}				
			}
		}
	}
	
	/**
	 * @param n
	 * @param i
	 * @param title
	 */
	private String getText(int n, int i, String title) {
		return Messages.getString("ControlTransformService.2", i, n, title); //$NON-NLS-1$
	}
	
	private Control generateControl(Massnahme m){
		Control c = new Control();
		c.setTitel(m.getId() + " " + m.getTitel());
		try {
			String description = GSScraperUtil.getInstance().getModel().getMassnahmeHtml(m.getUrl(), m.getStand());
			c.setDescription(description);
		} catch (GSServiceException e) {
			LOG.error("Error while transforming massnahme into control", e);
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
}
