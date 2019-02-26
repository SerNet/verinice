/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin.
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
 *     Daniel Murygin - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp.converter;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.bp.converter.ConverterCommand;

/**
 * A wizard to convert one or more IT networks from old IT base protection to
 * new IT base protection.
 * 
 * @author Daniel Murygin
 */
public class ItNetworkConverterWizard extends Wizard {

    private static final Logger log = Logger.getLogger(ItNetworkConverterWizard.class);

    private CnATreeElement selectedItNetwork = null;
    private ItNetworkPage itNetworkPage;
    private ICommandService commandService;

    public ItNetworkConverterWizard() {
        super();
        init();
    }

    public ItNetworkConverterWizard(CnATreeElement selectedOrganization) {
        this();
        this.selectedItNetwork = selectedOrganization;
    }

    private void init() {
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.ItNetworkConverterWizard_WindowTitle);
    }

    @Override
    public void addPages() {
        itNetworkPage = new ItNetworkPage(selectedItNetwork);
        addPage(itNetworkPage);
    }

    @Override
    public boolean performFinish() {
        try {
            itNetworkPage.setMessage(Messages.ItNetworkConverterWizard_PageTitle,
                    DialogPage.INFORMATION);
            runConvertingInWizard();
            CnAElementFactory.getInstance().reloadBpModelFromDatabase();
        } catch (InvocationTargetException | InterruptedException e) {
            log.error("InvocationTargetException or InterruptedException during conversion", //$NON-NLS-1$
                    e);
            itNetworkPage.setMessage(Messages.ItNetworkConverterWizard_ErrorInformation,
                    DialogPage.ERROR);
            return false;
        }
        return true;
    }

    private void runConvertingInWizard() throws InvocationTargetException, InterruptedException {
        getContainer().run(true, false, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor progressMonitor)
                    throws InvocationTargetException, InterruptedException {
                progressMonitor.beginTask(Messages.ItNetworkConverterWizard_ConvertingIsRunning,
                        IProgressMonitor.UNKNOWN);
                runConverting();
                progressMonitor.done();
            }
        });
    }

    private void runConverting() {
        Set<CnATreeElement> selectedItNetworks = itNetworkPage.getSelectedElementSet();
        Set<String> itNetworkUuidSet = selectedItNetworks.stream().map(CnATreeElement::getUuid)
                .collect(Collectors.toSet());
        ConverterCommand command = new ConverterCommand(itNetworkUuidSet);
        try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            log.error(Messages.ItNetworkConverterWizard_Error, e);
        }
    }

    @Override
    public IWizardPage getStartingPage() {
        return itNetworkPage;
    }

    private ICommandService getCommandService() {
        if (commandService == null) {
            commandService = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
        }
        return commandService;
    }
}
