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
package sernet.verinice.rcp;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.OrganizationWidget;
import sernet.verinice.model.common.CnATreeElement;

/**
 * ServerConnectionToggleDialog is part of the server connection toggling process started by action
 * {@link ServerConnectionToggleAction}.
 * 
 * This dialog provides a list of all organizations and it-verbunds. User can select 
 * one or more from this list.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ServerConnectionToggleDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(ServerConnectionToggleDialog.class);
    
    private OrganizationWidget organizationWidget = null;
    
    private String serverUrl;
    
    protected ServerConnectionToggleDialog(Shell shell) {
        super(shell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        final int layoutMarginWidth = 10;
        final int layoutMarginHeight = layoutMarginWidth;
        final int layoutVerticalSpacing = layoutMarginHeight;
        final int urlTextMinimumWidth = 150;
        
        if(isServerMode()) {
            setTitle(Messages.ServerConnectionToggleDialog_0);
            setMessage(Messages.ServerConnectionToggleDialog_1, IMessageProvider.INFORMATION);
        } else {
            setTitle(Messages.ServerConnectionToggleDialog_2);
            setMessage(Messages.ServerConnectionToggleDialog_3, IMessageProvider.INFORMATION);
        }
        
        
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = layoutMarginWidth;
        layout.marginHeight = layoutMarginHeight;
        layout.verticalSpacing = layoutVerticalSpacing;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);
        
        Label urlLabel = new Label(composite, SWT.NONE);
        urlLabel.setText(Messages.ServerConnectionToggleDialog_4);
        
        final Text urlText = new Text(composite, SWT.BORDER);
        gd = new GridData(SWT.FILL, SWT.BOTTOM, true,false);
        gd.minimumWidth = urlTextMinimumWidth;
        urlText.setLayoutData(gd);
        urlText.addModifyListener(new ModifyListener() {              
            @Override
            public void modifyText(ModifyEvent e) {
                serverUrl = urlText.getText();          
            }
        });
        String url = getServerUrlPreference();
        if(url!=null) {
            urlText.setText(url);
        }
        if(isServerMode()) {
            urlText.setEnabled(false);
        }
        
        try {
            organizationWidget = new OrganizationWidget(composite);
        } catch (CommandException ex) {
            LOG.error("Error while loading organizations", ex); //$NON-NLS-1$
            MessageDialog.openError(getShell(), Messages.ServerConnectionToggleDialog_5, Messages.ServerConnectionToggleDialog_6);
            return null;
        }
        
        SelectionListener organizationListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
            }
        };
        
        organizationWidget.addSelectionLiustener(organizationListener);
        
        composite.pack();     
        return composite;
    }
    
    public static boolean isStandalone() {
        return PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER.equals(getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE));
    }
    
    public static boolean isServerMode() {
        return PreferenceConstants.OPERATION_MODE_REMOTE_SERVER.equals(getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE));
    }

    private String getServerUrlPreference() {        
        return getPreferenceStore().getString(PreferenceConstants.VNSERVER_URI);
    }

    private static IPreferenceStore getPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if(isServerMode()) {
            newShell.setText(Messages.ServerConnectionToggleDialog_7);
        } else {
            newShell.setText(Messages.ServerConnectionToggleDialog_8);
        }
    }

    /**
     * @return
     */
    public Set<CnATreeElement> getSelectedElementSet() {
        return organizationWidget.getSelectedElementSet();
    }

    public String getServerUrl() {
        return serverUrl;
    }

}
