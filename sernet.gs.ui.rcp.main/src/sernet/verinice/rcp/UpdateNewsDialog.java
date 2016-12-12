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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.equinox.internal.p2.ui.ProvUIActivator;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * A dialog that informs the user that there is a new update available and
 * offers actions to update now or later. Displaying the dialog is optional
 * via preference. Dialog will be shown only once per client-session,
 * on startup time, when internal server is ready. 
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 */
public class UpdateNewsDialog extends Dialog {
    
    private static final String SHOP_URL = "https://shop.verinice.com/";  //$NON-NLS-1$
    
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
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
        gridData.heightHint = 300;
        container.setLayoutData(gridData);
        createContent(container);
        return container;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText(Messages.UpdateNewsDialog_1);
      Point cursorLocation = Display.getCurrent().getCursorLocation();
      final int shellLocationXSubtrahend = 200;
      final int shellLocationYSubtrahend = 400;
      newShell.setLocation(new Point(cursorLocation.x-shellLocationXSubtrahend,
              cursorLocation.y-shellLocationYSubtrahend));
    }

    protected Button createButton(Composite arg0, int arg1, String arg2, boolean arg3) {
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
        browser.addLocationListener(new LocationListener() {
            
            @Override
            public void changing(LocationEvent event) {
                try {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(event.location));
                } catch (PartInitException | MalformedURLException e) {
                    LOG.error("Error opening Link in external Browser", e); //$NON-NLS-1$
                }
                
            }
            
            @Override
            public void changed(LocationEvent event) {
                // do nothing
            }
        });
        
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
        
        Button openShop = new Button(buttonComposite, SWT.PUSH );
        openShop.setText(Messages.UpdateNewsDialog_13);
        gridData = new GridData();
        openShop.setLayoutData(gridData);
        openShop.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {            
                try {
                    Program.launch(SHOP_URL);
                } catch (Exception e) {
                    LOG.error("Error while loading shop", e); //$NON-NLS-1$
                } 
            }
        });
        
        Button updateLater = new Button(buttonComposite, SWT.PUSH );
        updateLater.setText(Messages.UpdateNewsDialog_2);
        gridData = new GridData();
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
                    LOG.error("URL of given Updatesite is not valid", e); //$NON-NLS-1$
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
        ProvisioningSession session = ProvUIActivator.getDefault().getProvisioningUI().getSession();
        
        UpdateOperation operation = new UpdateOperation(session);
        
        operation.getProvisioningContext().setArtifactRepositories(new URI[]{updateSiteURL.toURI()});
        operation.getProvisioningContext().setMetadataRepositories(new URI[]{updateSiteURL.toURI()});
        
        // check if updates are available    
        IStatus status = operation.resolveModal(null);
        if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
            LOG.debug("detected there is nothing to update"); //$NON-NLS-1$
            MessageDialog.openInformation(
                    null, 
                    Messages.UpdateNewsDialog_5, 
                    Messages.UpdateNewsDialog_4);
            return;
        }
        // execute update
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
                    LOG.debug("Restarting application manually requested after update"); //$NON-NLS-1$
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
            LOG.debug("creating provisioningJob from p2-api"); //$NON-NLS-1$
            final ProvisioningJob provisioningJob = operation.getProvisioningJob(null);
            if (provisioningJob != null) {
                LOG.debug("performing update using provisioning job"); //$NON-NLS-1$
                performUpdate(provisioningJob);
            }
            else {
                LOG.debug("provisioning job was null"); //$NON-NLS-1$
                handleUpdateError(operation);
            }
        } catch (Exception e){
            LOG.error("Error executing update", e); //$NON-NLS-1$
        }
    }

    private void handleUpdateError(UpdateOperation operation) {
        if (operation.hasResolved()) {
            MessageDialog.openError(null, 
                    Messages.UpdateNewsDialog_8, Messages.UpdateNewsDialog_9 
                    + ":\t" + operation.getResolutionResult()); //$NON-NLS-1$
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
                  LOG.debug("Running update job modal"); //$NON-NLS-1$
                  provisioningJob.addJobChangeListener(new IJobChangeListener() {                     
                      @Override
                      public void sleeping(IJobChangeEvent arg0) {
                          LOG.debug("Update-Job sleeping"); //$NON-NLS-1$
                      }                     
                      @Override
                      public void scheduled(IJobChangeEvent arg0) {
                          LOG.debug("Update-Job scheduled"); //$NON-NLS-1$
                      }                 
                      @Override
                      public void running(IJobChangeEvent arg0) {
                          LOG.debug("Update-Job running"); //$NON-NLS-1$
                      }                   
                      @Override
                      public void done(IJobChangeEvent arg0) {
                          LOG.debug("Update-Job done"); //$NON-NLS-1$
                          restartApplication();                     
                      }                    
                      @Override
                      public void awake(IJobChangeEvent arg0) {
                          LOG.debug("Update-Job awake"); //$NON-NLS-1$
                      }                    
                      @Override
                      public void aboutToRun(IJobChangeEvent arg0) {
                          LOG.debug("Update-Job about to run"); //$NON-NLS-1$                       
                      } 
                  });
                  provisioningJob.schedule();
              }
            }
        });            
    }            
    
    private Display getDisplay(){
        if(Display.getCurrent() == null){
            return Display.getDefault();
        }
        return Display.getCurrent();
    }     
}