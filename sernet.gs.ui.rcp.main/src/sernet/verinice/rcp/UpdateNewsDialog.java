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
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
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
 * 
 * A dialog that informs the user that there is a new update available and
 * offers actions to update now or later. Displaying the dialog is optional
 * via preference. Dialog will be shown only once per client-session,
 * on startup time, when internal server is ready. 
 * 
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
        Display display = getDisplay();
        Shell shell = parent;
        while (!shell.isDisposed()) {
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
        gridData.heightHint = 300;
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
        //Return null so that no default buttons like 'OK' and 'Cancel' will be created
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
        gridData.horizontalSpan = 4;
        gridData.verticalSpan = 4;
        gridData.minimumHeight = (int)parent.getClientArea().height / 2;
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
        
        Composite buttonComposite = new Composite(dialogComposite, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(3, false));
        gridData = new GridData(GridData.END, GridData.END, true, false);
        gridData.horizontalSpan = 4;
        buttonComposite.setLayoutData(gridData);
        
        Button updateLater = new Button(buttonComposite, SWT.PUSH );
        updateLater.setText(Messages.UpdateNewsDialog_2);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        updateLater.setLayoutData(gridData);
        updateLater.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                parent.getShell().close();
              }
        });
        
        Button updateNow = new Button(buttonComposite, SWT.PUSH );
        updateNow.setText(Messages.UpdateNewsDialog_3);
        gridData = new GridData();
        gridData.horizontalSpan = 1;
        updateLater.setLayoutData(gridData);
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
    
    /**
     * button "Update now" is clicked by the user
     */
    private void triggerUpdate(URL updateSiteURL) throws URISyntaxException{
        // create update operation
        LOG.debug("Update against updatesite:\t" + updateSiteURL.toString() + " requested") ;
        UpdateOperation operation = new UpdateOperation(ProvUIActivator.getDefault().getProvisioningUI().getSession());
       
        updateUpdateSite(updateSiteURL);
        
        // check if updates are available
        IStatus status = operation.resolveModal(null);
        if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
            LOG.debug("detected there is nothing to update today");
            MessageDialog.openInformation(
                    null, 
                    Messages.UpdateNewsDialog_5, 
                    Messages.UpdateNewsDialog_4);
            return;
        }
        
        createAndExecuteUpdateJob(operation);
    }

    /**
     * restarts the application after successful update 
     */
    private void restartApplication() {
        getDisplay().syncExec(new Runnable() {
            
            @Override
            public void run() {
                //  restart application
                boolean restart = MessageDialog.openQuestion(null,
                        Messages.UpdateNewsDialog_6,
                        Messages.UpdateNewsDialog_7);
                if (restart) {
                    LOG.debug("Restarting application manually requested after update");
                    PlatformUI.getWorkbench().restart();
                }
            }
        });
    }

    /**
     * asks the p2-api for an instance of org.eclipse.runtime.jobs.Job that
     * is able to perform the update operation and hands that to an
     * execution method. afterwards the application will be restarted
     */
    private void createAndExecuteUpdateJob(UpdateOperation operation) {
        try{
            LOG.debug("creating provisioningJob from p2-api");
            final ProvisioningJob provisioningJob = operation.getProvisioningJob(null);
            if (provisioningJob != null) {
                LOG.debug("performing update using provisioning job");
                performUpdate(provisioningJob);
            }
            else {
                LOG.debug("provisioning job was null");
                handleUpdateError(operation);
            }
        } catch (Exception e){
            LOG.error("Error executing update", e);
        }
    }

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
     * executes the p2-update job in a modal way, so the user is
     * not able to interrupt it or do something else while updating
     */
    private void performUpdate(final ProvisioningJob provisioningJob) {
        
        getDisplay().syncExec(new Runnable() {
            
            @Override
            public void run() {
                boolean performUpdate = MessageDialog.openQuestion(
                        null,
                        Messages.UpdateNewsDialog_10,
                        Messages.UpdateNewsDialog_11);
              if (performUpdate) {
                  LOG.debug("Running update job modal");
                  provisioningJob.addJobChangeListener(new IJobChangeListener() {
                      
                      @Override
                      public void sleeping(IJobChangeEvent arg0) {
                          // TODO Auto-generated method stub
                          LOG.debug("Update-Job sleeping");
                      }
                      
                      @Override
                      public void scheduled(IJobChangeEvent arg0) {
                          // TODO Auto-generated method stub
                          LOG.debug("Update-Job scheduled");
                      }
                      
                      @Override
                      public void running(IJobChangeEvent arg0) {
                          // TODO Auto-generated method stub
                          LOG.debug("Update-Job running");
                      }
                      
                      @Override
                      public void done(IJobChangeEvent arg0) {
                          LOG.debug("Update-Job done");
                          restartApplication();
                          
                      }
                      
                      @Override
                      public void awake(IJobChangeEvent arg0) {
                          // TODO Auto-generated method stub
                          LOG.debug("Update-Job awake");
                      }
                      
                      @Override
                      public void aboutToRun(IJobChangeEvent arg0) {
                          // TODO Auto-generated method stub
                          LOG.debug("Update-Job about to run");
                          
                      }
                  });
                  provisioningJob.schedule();
              }
              

            }
        });
             
    }            
        

    /**
     * sets the updateSite configured in the news message as the
     * only one in the system, to ensure the update is taken from this one
     * 
     */
    private void updateUpdateSite(URL updateSiteURL) throws URISyntaxException {
        // set the updatesite
        LOG.debug("setting updatesite from verinice-news");
        removeAllUpdateSites();
        LOG.debug("adding updatesite:\t" + updateSiteURL.toString() + " to repositories");
        Activator.getDefault().getMetadataRepositoryManager().addRepository(updateSiteURL.toURI());
        Activator.getDefault().getArtifactRepositoryManager().addRepository(updateSiteURL.toURI());
    }
    
    /**
     * clears systems updateSite-Settings
     */
    private void removeAllUpdateSites(){
        LOG.debug("removing all existant updatesites");
        IArtifactRepositoryManager artifactRepoManager = Activator.getDefault().getArtifactRepositoryManager();
        IMetadataRepositoryManager metaRepoManager =  Activator.getDefault().getMetadataRepositoryManager();
        for(URI repository : Activator.getDefault().getArtifactRepositoryManager().getKnownRepositories(IArtifactRepositoryManager.REPOSITORIES_ALL)){
            LOG.debug("Removing repository:\t" + repository.toASCIIString());
            artifactRepoManager.removeRepository(repository);
            metaRepoManager.removeRepository(repository);
        }
    }
    
    private Display getDisplay(){
        if(Display.getCurrent() == null){
            return Display.getDefault();
        }
        return Display.getCurrent();
    }


        
}