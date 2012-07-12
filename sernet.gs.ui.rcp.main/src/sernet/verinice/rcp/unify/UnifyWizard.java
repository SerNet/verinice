/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.rcp.unify;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadParentTitles;
import sernet.verinice.service.commands.LoadUnifyMapping;
import sernet.verinice.service.commands.Unify;
import sernet.verinice.service.commands.UnifyMapping;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class UnifyWizard extends Wizard {

    private static final Logger LOG = Logger.getLogger(UnifyWizard.class);
    
    public static final String PAGE_SELECT_GROUP_ID = "select-group"; //$NON-NLS-1$
    public static final String PAGE_SELECT_MAPPING_ID = "mapping"; //$NON-NLS-1$
    
    private List<CnATreeElement> groups;
    
    private CnATreeElement source;
    
    private CnATreeElement destination;

    private List<UnifyMapping> mappings;
    
    private Map<String, String> uuidParentInformationMap;
    
    private ICommandService commandService;
    
    /**
     * @param groups
     */
    public UnifyWizard(List<CnATreeElement> groups) {
        super();
        setWindowTitle(Messages.UnifyWizard_1);
        this.setNeedsProgressMonitor(true);
        this.groups = groups;
        try {
            loadParentInformation();
        } catch (CommandException e) {
            LOG.error("Error while loading parent information", e); //$NON-NLS-1$
            showError(Messages.UnifyWizard_3);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        UnifyPageSelectGroup pageSelectGroup = new UnifyPageSelectGroup();
        addPage(pageSelectGroup);
        UnifyPageMapping pageMapping = new UnifyPageMapping();
        addPage(pageMapping);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#canFinish()
     */
    @Override
    public boolean canFinish() {
        return getSource()!=null && getDestination()!=null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.UnifyWizard_4, IProgressMonitor.UNKNOWN);
                    Activator.inheritVeriniceContextState();
                    try {
                        unify();
                    } catch (Exception e) {
                        LOG.error("Error while unifying elements.", e); //$NON-NLS-1$
                        showError(Messages.UnifyWizard_6);
                    }
                    monitor.done();
                }
             });
        } catch (Exception e) {
            LOG.error("Error while unifying elements.", e); //$NON-NLS-1$
            showError(Messages.UnifyWizard_6);
        }
        return true;     
    }
    
    /**
     * @throws CommandException 
     * 
     */
    private void unify() throws CommandException {
        if(mappings==null) {
            loadMapping();
        }
        Unify command = new Unify(mappings);
        command = getCommandService().executeCommand(command);
        refresh(command.getChangedElements());
    }

    /**
     * @param changedElements
     */
    private void refresh(List<CnATreeElement> changedElements) {
        for (CnATreeElement element : changedElements) {
            element.setParent(null);
            CnAElementFactory.getModel(element).childChanged(element);
        }    
    }
    
    private void loadParentInformation() throws CommandException {
        List<String> uuidList = createGroupUuidList();
        LoadParentTitles command = new  LoadParentTitles(uuidList);
        command = getCommandService().executeCommand(command);
        uuidParentInformationMap = command.getParentInformationMap();
    }

    /**
     * @return
     */
    private List<String> createGroupUuidList() {
        List<String> uuidList = new ArrayList<String>(groups.size());
        for (CnATreeElement element : groups) {
            uuidList.add(element.getUuid());
        }
        return uuidList;
    }

    public void loadMapping() { 
        try {
            loadMappingFromServer();
        } catch (CommandException e) {
            LOG.error("Command exception while loading unify mapping from server.", e); //$NON-NLS-1$
            showError(Messages.UnifyWizard_6);
        } catch (Exception e) {
            LOG.error("Error while loading unify mapping from server.", e); //$NON-NLS-1$
            showError(Messages.UnifyWizard_6);
        }
    }

    private void loadMappingFromServer() throws CommandException {
        LoadUnifyMapping command = new LoadUnifyMapping(getSource().getUuid(), getDestination().getUuid());
        command = getCommandService().executeCommand(command);    
        mappings = command.getMappings();
    }
    

    private void showError(String message) {
        MessageDialog.openError(this.getShell(), Messages.UnifyWizard_0, message);
    }

    /**
     * @return the mappings
     */
    protected List<UnifyMapping> getMappings() {
        return mappings;
    }

    /**
     * @return the groups
     */
    protected List<CnATreeElement> getGroups() {
        return groups;
    }

    /**
     * @return the uuidParentInformationMap
     */
    protected Map<String, String> getUuidParentInformationMap() {
        return uuidParentInformationMap;
    }

    /**
     * @return the source
     */
    protected CnATreeElement getSource() {
        return source;
    }

    /**
     * @return the destination
     */
    protected CnATreeElement getDestination() {
        return destination;
    }

    /**
     * @param groups the groups to set
     */
    protected void setGroups(List<CnATreeElement> groups) {
        this.groups = groups;
    }

    /**
     * @param source the source to set
     */
    protected void setSource(CnATreeElement source) {
        this.source = source;
    }

    /**
     * @param destination the destination to set
     */
    protected void setDestination(CnATreeElement destination) {
        this.destination = destination;
    }
    
    public ICommandService getCommandService() {
        if(commandService==null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

}
