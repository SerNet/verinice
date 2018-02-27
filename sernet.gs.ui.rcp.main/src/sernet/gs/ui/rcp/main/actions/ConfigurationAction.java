/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.actions.helper.UpdateConfigurationHelper;
import sernet.gs.ui.rcp.main.bsi.dialogs.AccountDialog;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IRightsServiceClient;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.NonModalWizardDialog;
import sernet.verinice.rcp.account.AccountWizard;
import sernet.verinice.service.commands.LoadConfiguration;
import sernet.verinice.service.commands.SaveConfiguration;

/**
 * ConfigurationAction creates and changes user account. Data of an account is
 * set to entity {@link sernet.verinice.model.common.configuration.Configuration}.
 * 
 * Account is edited by {@link AccountDialog}. 
 * Account configuration is saved by command {@link SaveConfiguration}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ConfigurationAction extends Action implements IObjectActionDelegate,  RightEnabledUserInteraction{

	static final Logger LOG = Logger.getLogger(ConfigurationAction.class);
	
	public static final String ID = "sernet.gs.ui.rcp.main.personconfiguration"; //$NON-NLS-1$

	Configuration configuration;

	private ICommandService commandService;
	
	private IRightsServiceClient rightsService;

	public ConfigurationAction() {
        super();
    }
	
	public ConfigurationAction(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {

    }

    /*
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        if(!checkRights()) {
            configuration = null;
            return;
        }
        
        Activator.inheritVeriniceContextState();

        if(configuration==null) {
            loadConfiguration();
        }
        if(configuration==null) {
            return;
        }

        //final TitleAreaDialog dialog = createDialog();
        final TitleAreaDialog dialog = createWizard();
        if (dialog.open() != Window.OK) {
            configuration = null;
            return;
        }

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new UpdateConfigurationHelper(configuration));
        } catch (Exception e) {
            LOG.error("Error while saving configuration.", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.ConfigurationAction_5);
        } finally {
            configuration = null;
        }
    }

    private TitleAreaDialog createWizard() {
        AccountWizard wizard = new AccountWizard(configuration);                 
        WizardDialog wizardDialog = new NonModalWizardDialog(Display.getCurrent().getActiveShell(),wizard);
        return wizardDialog;
    }
    
	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
    @SuppressWarnings("unchecked")
	public void run(IAction action) {
	    run();
	}


    private void loadConfiguration() {
        IWorkbenchWindow window1 = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IStructuredSelection selection = (IStructuredSelection) window1.getSelectionService().getSelection();
		if (selection == null) {
			return;
		}
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			try {
				Object o = iter.next();
				if( o instanceof CnATreeElement) {

    				CnATreeElement person = (CnATreeElement) o;
    
    				LOG.debug("Loading configuration for user " + person.getTitle()); //$NON-NLS-1$
    				LoadConfiguration command = new LoadConfiguration(person);
    				command = ServiceFactory.lookupCommandService().executeCommand(command);
    				configuration = command.getConfiguration();
    
    				if (configuration == null) {
    					// create new configuration
    				    configuration = Configuration.createDefaultAccount();
    				    configuration.setPerson(person);   				    
    				}
    
    			}
			} catch (CommandException e) {
				ExceptionUtil.log(e, Messages.ConfigurationAction_2);
			} catch (RuntimeException e) {
				ExceptionUtil.log(e, Messages.ConfigurationAction_3);
			}
		}
    }

    public IAuthService getAuthService() {
        return ServiceFactory.lookupAuthService();
    }

	@Override
    public void selectionChanged(IAction action, ISelection selection) {
		if (action.isEnabled()) {
			// Conditions for availability of this action:
			// - Database connection must be open (Implicitly assumes that login
			// credentials have
			// been transferred and that the server can be queried. This is
			// neccessary since this
			// method will be called before the server connection is enabled.)
			// - permission handling is needed by IAuthService implementation
			boolean b = CnAElementHome.getInstance().isOpen() && ServiceFactory.isPermissionHandlingNeeded();

			action.setEnabled(b && checkRights());
		}
	}
	
	public Configuration getConfiguration() {
        return configuration;
    }


    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
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

    @Override
    public boolean checkRights() {
        return ((RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE)).isEnabled(getRightID());
    }


    @Override
    public String getRightID() {
        return ActionRightIDs.ACCOUNTSETTINGS;
    }

    IRightsServiceClient getRightService() {
        if (rightsService == null) {
            rightsService = (IRightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }

}
