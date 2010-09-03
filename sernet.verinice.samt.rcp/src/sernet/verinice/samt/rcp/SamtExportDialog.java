/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.samt.rcp;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("restriction")
public class SamtExportDialog extends TitleAreaDialog
{   
    private static final Logger LOG = Logger.getLogger(SamtExportDialog.class);
    
    /**
     * Indicates if the output should be encrypted.
     */
    private boolean encryptOutput = false;
    private CnATreeElement selectedElement;
    private String filePath;

    public SamtExportDialog(Shell activeShell)
    {
        this(activeShell,null);
    }

    /**
     * @param activeShell
     * @param selectedOrganization
     */
    public SamtExportDialog(Shell activeShell, Organization selectedOrganization) {
        super(activeShell);
        selectedElement = selectedOrganization;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        /*++++
         * Dialog title, message and layout:
         *++++++++++++++++++++++++++++++++++*/
        
        setTitle(Messages.SamtExportDialog_0);
        setMessage(Messages.SamtExportDialog_1, IMessageProvider.INFORMATION);
        
        final Composite composite = (Composite) super.createDialogArea(parent);
        ((GridLayout)composite.getLayout()).marginWidth = 10;
        ((GridLayout)composite.getLayout()).marginHeight = 10;
        
        /*++++
         * Widgets for selection of an IT network:
         *++++++++++++++++++++++++++++++++++++++++*/
        
        final Label lblITNetwork = new Label(composite, SWT.NONE);
        lblITNetwork.setText(Messages.SamtExportDialog_2);
        
        LoadCnAElementByType<Organization> cmdLoadOrganization = new LoadCnAElementByType<Organization>(Organization.class);
        try
        {
            cmdLoadOrganization = ServiceFactory.lookupCommandService().executeCommand(cmdLoadOrganization);
        }
        catch (CommandException ex)
        {
            LOG.error("Error while loading organizations", ex); //$NON-NLS-1$
            setMessage(Messages.SamtExportDialog_4, IMessageProvider.ERROR);
            return null;
        }
        
        final Group groupOrganization = new Group(composite, SWT.NONE);
        GridLayout groupOrganizationLayout = new GridLayout(1, true);
        GridData groupOrganizationLayoutData = new GridData();
        groupOrganizationLayoutData.verticalIndent = 10;
        groupOrganizationLayoutData.horizontalIndent = 20;
        groupOrganization.setLayoutData(groupOrganizationLayoutData);
        groupOrganization.setLayout(groupOrganizationLayout);
        List<Organization> organizationList = cmdLoadOrganization.getElements();
        Iterator<Organization> organizationIter = organizationList.iterator();
        
        SelectionListener organizationListener = new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                selectedElement = (CnATreeElement) ( (Button) e.getSource() ).getData();
                super.widgetSelected(e);
            }
        };
        
        CnATreeElement oldSelectedElement = selectedElement;
        selectedElement=null;
        while( organizationIter.hasNext() )
        {
            final Button radioOrganization = new Button(groupOrganization,SWT.RADIO);
            Organization organization = organizationIter.next();
            radioOrganization.setText(organization.getTitle());
            radioOrganization.setData(organization );
            radioOrganization.addSelectionListener(organizationListener);
            if(oldSelectedElement!=null && oldSelectedElement.equals(organization)) {
                radioOrganization.setSelection(true);
                selectedElement = organization;
            }
            if(organizationList.size()==1) {
                radioOrganization.setSelection(true);
                selectedElement = organization;
            }
        }
        
        /*++++
         * Widgets to enable/disable encryption:
         *++++++++++++++++++++++++++++++++++++++*/
         
        final Composite encryptionOptionComposite = new Composite(composite, SWT.NONE);
        encryptionOptionComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
        ((RowLayout) encryptionOptionComposite.getLayout()).marginTop = 15;
        
        final Button encryptionCheckbox = new Button(encryptionOptionComposite, SWT.CHECK);
        encryptionCheckbox.setText(Messages.SamtExportDialog_5);
        encryptionCheckbox.setSelection(encryptOutput);
        encryptionCheckbox.setEnabled(true);
        encryptionCheckbox.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkBox = (Button) e.getSource();
                encryptOutput = checkBox.getSelection();
            }
        });
        encryptionOptionComposite.pack();
        
        /*+++++
         * Widgets to browse for storage location:
         *++++++++++++++++++++++++++++++++++++++++*/
        
        final Composite compositeSaveLocation = new Composite(composite,SWT.NONE);
        compositeSaveLocation.setLayout(new RowLayout(SWT.HORIZONTAL));
        ((RowLayout) compositeSaveLocation.getLayout()).marginTop = 15;
        final Label labelLocation = new Label(compositeSaveLocation, SWT.NONE);
        labelLocation.setText(Messages.SamtExportDialog_6);
        final Text txtLocation = new Text(compositeSaveLocation, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        short textLocationWidth = 300;
        txtLocation.setSize(textLocationWidth, 30);
        final RowData textLocationData = new RowData();
        textLocationData.width = textLocationWidth;
        txtLocation.setLayoutData(textLocationData);
        txtLocation.addKeyListener(new KeyListener() {             
            @Override
            public void keyReleased(KeyEvent e) {
                filePath = txtLocation.getText();
                
            }          
            @Override
            public void keyPressed(KeyEvent e) {
                // nothing to do
            }
        });
            
        composite.pack();
        final Button buttonBrowseLocations = new Button(compositeSaveLocation, SWT.NONE);
        buttonBrowseLocations.setText(Messages.SamtExportDialog_7);
        
        buttonBrowseLocations.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(),SWT.SAVE);
                dialog.setText(Messages.SamtExportDialog_3);
                dialog.setFilterExtensions(new String[]{ "*.xml" }); //$NON-NLS-1$
                dialog.setFilterNames(new String[]{ Messages.SamtExportDialog_8 }); 
                String exportPath = dialog.open();
                if( exportPath != null )
                {
                    txtLocation.setText(exportPath);
                    filePath = exportPath;
                }
                else
                {
                    txtLocation.setText(""); //$NON-NLS-1$
                    filePath = ""; //$NON-NLS-1$
                }
            }
        });
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        StringBuilder sb = new StringBuilder();
        if(filePath==null || filePath.isEmpty()) {
            sb.append(Messages.SamtExportDialog_10);         
        } else {
            try {
                new File(filePath).createNewFile();
            } catch (Exception e) {
                sb.append(Messages.SamtExportDialog_11);  
            }
        }
        if(selectedElement==null) {
           sb.append(Messages.SamtExportDialog_12);         
        } 
        if(sb.length()>0) {
            sb.append(Messages.SamtExportDialog_13);
            setMessage(sb.toString(), IMessageProvider.ERROR);
        } 
        else {     
            super.okPressed();
        }
    }
    
    /* Getters and Setters: */
    
    public CnATreeElement getSelectedElement()
    {
        return selectedElement;
    }

    public String getFilePath()
    {   
        return filePath;
    }
    
    public boolean getEncryptOutput()
    {
        return encryptOutput;
    }
    
}
