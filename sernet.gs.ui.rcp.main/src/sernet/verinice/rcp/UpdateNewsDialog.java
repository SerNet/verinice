/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.ui.ProvUIActivator;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class UpdateNewsDialog extends Dialog {
    
    private String message;
    private URL updateSite;
    
    private static final Logger LOG = Logger.getLogger(UpdateNewsDialog.class);

    /**
     * @param parent
     * @param style
     */
    public UpdateNewsDialog(Shell parent, String text, URL updateSite) {
        super(parent);
        this.message = text;
        this.updateSite = updateSite;
        setShellStyle(SWT.CLOSE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
        setBlockOnOpen(true);
        Display display = parent.getDisplay();
        while (!parent.isDisposed()) {
            if (!display.readAndDispatch()){
                display.sleep();
            }
        }
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
        container.setLayoutData(gridData);
        createContent(container);
        container.getParent().pack();
        return container;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText(Messages.UpdateNewsDialog_1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButton(org.eclipse.swt.widgets.Composite, int, java.lang.String, boolean)
     */
    protected Button createButton(Composite arg0, int arg1, String arg2, boolean arg3) 
    {
        //Retrun null so that no default buttons like 'OK' and 'Cancel' will be created
        return null;
    }
    
    private void createContent(final Composite parent) {
        Composite dialogComposite = new Composite(parent, SWT.RESIZE | SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_BOTH |GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
        dialogComposite.setLayout(new GridLayout(4, false));
        gridData.verticalIndent = 1;
        dialogComposite.setLayoutData(gridData);
        
        Browser browser = new Browser(dialogComposite, SWT.RESIZE);
        gridData = new GridData(
                GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
//        gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, true, true);
        gridData.horizontalSpan = 4;
        browser.setLayoutData(gridData);
        browser.setText(message);
        browser.setJavascriptEnabled(false);
        
        Button showAgainCheck = new Button(dialogComposite, SWT.CHECK);
        showAgainCheck.setText(Messages.UpdateNewsDialog_0);
        showAgainCheck.setEnabled(true);
        showAgainCheck.setSelection(getShowAgainProperty());
        showAgainCheck.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setShowAgainProperty(((Button)event.getSource()).getSelection());
            }
        });
        gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
        gridData.horizontalSpan = 4;
        showAgainCheck.setLayoutData(gridData);
        
        Composite buttonComposite = new Composite(dialogComposite, SWT.BORDER);
        buttonComposite.setLayout(new GridLayout(2, false));
        gridData = new GridData(GridData.END, GridData.END, false, false);
        gridData.horizontalSpan = 4;
        buttonComposite.setLayoutData(gridData);
        
        Button updateLater = new Button(buttonComposite, SWT.PUSH );
        updateLater.setText(Messages.UpdateNewsDialog_2);
        gridData = new GridData();
        gridData.horizontalSpan = 1;
        updateLater.setLayoutData(gridData);
        updateLater.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                parent.getShell().close();
              }
        });
        
        Button updateNow = new Button(buttonComposite, SWT.PUSH );
        updateNow.setText(Messages.UpdateNewsDialog_3);
        updateNow.setLayoutData(gridData);
        updateNow.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try{
                    triggerUpdate(updateSite);
                    parent.getShell().close();
                } catch (URISyntaxException e){
                    LOG.error("URL of given Updatesite is not valid", e);
                }
            }
        });
        
        
    }
    
    private void setShowAgainProperty(boolean showAgain){
        Activator.getDefault().getPluginPreferences().setValue(PreferenceConstants.SHOW_UPDATE_NEWS_DIALOG, showAgain);
    }
    
    private boolean getShowAgainProperty() {
        return Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.SHOW_UPDATE_NEWS_DIALOG);
    }
    
    private void triggerUpdate(URL updateSiteURL) throws URISyntaxException{
        // create update operation
        UpdateOperation operation = new UpdateOperation(ProvUIActivator.getDefault().getProvisioningUI().getSession());
       
        // check if updates are available
        IStatus status = operation.resolveModal(null);
        if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
            MessageDialog.openInformation(
                    null, 
                    Messages.UpdateNewsDialog_5, 
                    Messages.UpdateNewsDialog_4);
            return;
        }
        
        updateUpdateSite(updateSiteURL);
        
        createAndExecuteUpdateJob(operation);
        
    
        restartApplication();
    }

    /**
     * 
     */
    private void restartApplication() {
        // restart application
        boolean restart = MessageDialog.openQuestion(null,
                Messages.UpdateNewsDialog_6,
                Messages.UpdateNewsDialog_7);
            if (restart) {
                PlatformUI.getWorkbench().restart();
            }
    }

    /**
     * @param operation
     */
    private void createAndExecuteUpdateJob(UpdateOperation operation) {
        try{
            final ProvisioningJob provisioningJob = operation.getProvisioningJob(null);
            if (provisioningJob != null) {
                performUpdate(provisioningJob); 
            }
            else {
                handleUpdateError(operation);
            }
        } catch (Exception e){
            LOG.error("Error executing update", e);
        }
    }

    /**
     * @param operation
     */
    private void handleUpdateError(UpdateOperation operation) {
        if (operation.hasResolved()) {
            MessageDialog.openError(null, 
                    Messages.UpdateNewsDialog_8, Messages.UpdateNewsDialog_9 
                    + ":\t" + operation.getResolutionResult());
        } else {
            MessageDialog.openError(null, 
                    Messages.UpdateNewsDialog_8, Messages.UpdateNewsDialog_9);
        }
    }

    /**
     * @param provisioningJob
     */
    private void performUpdate(final ProvisioningJob provisioningJob) {
        Display.getCurrent().syncExec(new Runnable() {

            @Override
            public void run() {
                boolean performUpdate = MessageDialog.openQuestion(
                        null,
                        Messages.UpdateNewsDialog_10,
                        Messages.UpdateNewsDialog_11);
                if (performUpdate) {
                    provisioningJob.schedule();
                }
            }
        });
    }

    /**
     * @param updateSiteURL
     * @throws URISyntaxException
     */
    private void updateUpdateSite(URL updateSiteURL) throws URISyntaxException {
        // set the updatesite
        removeAllUpdateSites();
        Activator.getDefault().getMetadataRepositoryManager().addRepository(updateSiteURL.toURI());
        Activator.getDefault().getArtifactRepositoryManager().addRepository(updateSiteURL.toURI());
    }
    
    private void removeAllUpdateSites(){
        IArtifactRepositoryManager artifactRepoManager = Activator.getDefault().getArtifactRepositoryManager();
        IMetadataRepositoryManager metaRepoManager =  Activator.getDefault().getMetadataRepositoryManager();
        for(URI repository : Activator.getDefault().getArtifactRepositoryManager().getKnownRepositories(IArtifactRepositoryManager.REPOSITORIES_ALL)){
            artifactRepoManager.removeRepository(repository);
            metaRepoManager.removeRepository(repository);
        }
    }


        
}