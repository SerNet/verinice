/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions.helper;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.actions.ConfigurationAction;
import sernet.gs.ui.rcp.main.actions.Messages;
import sernet.gs.ui.rcp.main.bsi.dialogs.AccountDialog;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IRightsServiceClient;
import sernet.verinice.interfaces.PasswordException;
import sernet.verinice.interfaces.UsernameExistsException;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.commands.SaveConfiguration;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class UpdateConfigurationHelper implements IRunnableWithProgress {

    private final Logger LOG = Logger.getLogger(UpdateConfigurationHelper.class);

    private final Configuration configuration;

    private IRightsServiceClient rightsService;

    private ICommandService commandService;

    /**
     * @param dialog
     * @param configurationAction
     *            TODO
     */
    public UpdateConfigurationHelper(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        Activator.inheritVeriniceContextState();
        try {
            final boolean updatePassword = updateNameAndPassword(configuration.getUserNew(), configuration.getPassNew());
            // save configuration:
            SaveConfiguration<Configuration> command = new SaveConfiguration<Configuration>(configuration, updatePassword);
            command = getCommandService().executeCommand(command);
            getRightService().reload();
        } catch (final UsernameExistsException e) {
            final String logMessage = "Configuration can not be saved. Username exists: " + e.getUsername(); //$NON-NLS-1$
            final String messageTitle = Messages.ConfigurationAction_7;
            final String userMessage = NLS.bind(Messages.ConfigurationAction_7, e.getUsername());
            handleException(e, logMessage, messageTitle, userMessage);
        } catch (final PasswordException e) {
            final String logMessage = "Configuration can not be saved. " + e.getMessage(); //$NON-NLS-1$
            final String messageTitle = Messages.ConfigurationAction_6;
            final String userMessage = e.getMessage();
            handleException(e, logMessage, messageTitle, userMessage);
        } catch (Exception e) {
            LOG.error("Error while saving configuration.", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.ConfigurationAction_5);
        }
    }

    private void handleException(final Exception e, final String logMessage, final String messageTitle, final String userMessage) {
        LOG.info(logMessage);
        if (LOG.isDebugEnabled()) {
            LOG.debug("stacktrace: ", e); //$NON-NLS-1$
        }
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(Display.getDefault().getActiveShell(), messageTitle, userMessage);
            }
        });
    }

    /**
     * Checks if the user has entered a new password. If so, the cleartext
     * password is saved.
     *
     * @param string
     *
     * @param entity
     *            the entity containing the users input
     * @param string
     * @return true if a new cleartext password was saved, that needs to be
     *         hashed.
     */
    boolean updateNameAndPassword(String newName, String newPassword) {
        boolean updated = false;
        final String oldName = configuration.getUser();
        if (isNewName(oldName, newName) && (newPassword == null || newPassword.isEmpty())) {
            if (getAuthService().isHandlingPasswords()) {
                throw new PasswordException(Messages.ConfigurationAction_9);
            }
        }
        configuration.setUser(newName);
        if (newPassword != null && !newPassword.isEmpty()) {
            configuration.setPass(newPassword);
            updated = true;
        }
        return updated;
    }

    private boolean isNewName(String oldName, String name) {
        boolean result = false;
        if (oldName != null) {
            if (name == null) {
                result = true;
            } else {
                result = !oldName.equals(name);
            }
        } else {
            result = name != null;
        }
        return result;
    }

    private IRightsServiceClient getRightService() {
        if (rightsService == null) {
            rightsService = (IRightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }

    private ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

    private IAuthService getAuthService() {
        return ServiceFactory.lookupAuthService();
    }

}