/*******************************************************************************
 * Copyright (c) 2010 Andreas Becker <andreas[at]becker[dot]name>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Andreas Becker <andreas[at]becker[dot]name> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.ITVerbund;

/**
 * Dialog class for the export dialog.
 * 
 * @author Andreas Becker
 */
public class ExportDialog extends TitleAreaDialog {
    /**
     * Indicates if the output should be encrypted.
     */
    private boolean encryptOutput = false;
    private ITVerbund selectedITNetwork;
    private String sourceId;
    private String storageLocation;
    private boolean restrictedToEntityTypes = false;
    private HashMap<String, String> entityTypesToBeExported = new HashMap<String, String>();

    public ExportDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        /*
         * ++++ Dialog title, message and layout:
         * ++++++++++++++++++++++++++++++++++
         */

        setTitle(Messages.ExportDialog_0);
        setMessage(Messages.ExportDialog_1, IMessageProvider.INFORMATION);

        final Composite composite = (Composite) super.createDialogArea(parent);
        ((GridLayout) composite.getLayout()).marginWidth = 10;
        ((GridLayout) composite.getLayout()).marginHeight = 10;

        /*
         * ++++ Widgets for source ID:+++++++++++++++++++++++
         */

        final Composite compositeSourceId = new Composite(composite, SWT.NONE);
        compositeSourceId.setLayout(new RowLayout());
        final Label lblSourceId = new Label(compositeSourceId, SWT.NONE);
        lblSourceId.setText(Messages.ExportDialog_7);
        final Text txtSourceId = new Text(compositeSourceId, SWT.SINGLE | SWT.BORDER);
        txtSourceId.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                sourceId = ((Text) e.getSource()).getText();
            }
        });

        /*
         * ++++ Widgets for selection of an IT network:
         * ++++++++++++++++++++++++++++++++++++++++
         */

        final Label lblITNetwork = new Label(composite, SWT.NONE);
        lblITNetwork.setText(Messages.ExportDialog_2);

        LoadCnAElementByType<ITVerbund> cmdLoadVerbuende = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);

        try {
            cmdLoadVerbuende = ServiceFactory.lookupCommandService().executeCommand(cmdLoadVerbuende);
        } catch (CommandException ex) {
            setMessage(Messages.ExportDialog_8, IMessageProvider.ERROR);
            return null;
        }

        final Group groupITNetworks = new Group(composite, SWT.NONE);
        GridLayout groupITNetworksLayout = new GridLayout(1, true);
        GridData groupITNetworksLayoutData = new GridData();
        groupITNetworksLayoutData.verticalIndent = 10;
        groupITNetworksLayoutData.horizontalIndent = 20;
        groupITNetworks.setLayoutData(groupITNetworksLayoutData);
        groupITNetworks.setLayout(groupITNetworksLayout);
        List<ITVerbund> itNetworks = cmdLoadVerbuende.getElements();
        Iterator<ITVerbund> itNetworksIter = itNetworks.iterator();

        SelectionListener itNetworksListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedITNetwork = (ITVerbund) ((Button) e.getSource()).getData();
                super.widgetSelected(e);
            }
        };

        while (itNetworksIter.hasNext()) {
            final Button radioITNetwork = new Button(groupITNetworks, SWT.RADIO);
            ITVerbund itNetwork = itNetworksIter.next();
            radioITNetwork.setText(itNetwork.getTitle()); //$NON-NLS-1$
            radioITNetwork.setData(itNetwork);
            radioITNetwork.addSelectionListener(itNetworksListener);

            LoadChildrenForExpansion cmdLoadChildren = new LoadChildrenForExpansion(itNetwork);
            try {
                cmdLoadChildren = ServiceFactory.lookupCommandService().executeCommand(cmdLoadChildren);
                itNetwork = (ITVerbund) cmdLoadChildren.getElementWithChildren();
            } catch (CommandException ex) {
                setMessage(Messages.ExportDialog_8, IMessageProvider.ERROR);
                return null;
            }
        }

        /*
         * ++++ Widgets for restriction to certain object types:
         * +++++++++++++++++++++++++++++++++++++++++++++++++
         */

        final Button checkboxRestrict = new Button(composite, SWT.CHECK);
        checkboxRestrict.setText(Messages.ExportDialog_3);
        GridData checkboxRestrictData = new GridData();
        checkboxRestrictData.verticalIndent = 15;
        checkboxRestrict.setLayoutData(checkboxRestrictData);

        final Group groupObjectTypes = new Group(composite, SWT.NONE);
        GridData groupObjectTypesData = new GridData();
        groupObjectTypesData.verticalIndent = 10;
        groupObjectTypesData.horizontalIndent = 20;
        groupObjectTypes.setLayoutData(groupObjectTypesData);
        GridLayout groupObjectTypesLayout = new GridLayout();
        groupObjectTypesLayout.numColumns = 3;
        groupObjectTypes.setLayout(groupObjectTypesLayout);

        Collection<EntityType> entityTypes = HUITypeFactory.getInstance().getAllEntityTypes();
        Iterator<EntityType> entityTypesIter = entityTypes.iterator();

        //while (entityTypesIter.hasNext()) {
            EntityType entityType = entityTypesIter.next();

            if (!(entityType.getId().equals("itverbund"))) //$NON-NLS-1$
            {
                final Button cbxEntityType = new Button(groupObjectTypes, SWT.CHECK);
                cbxEntityType.setData(entityType);
                cbxEntityType.setText(entityType.getName());
                cbxEntityType.setEnabled(false);
                cbxEntityType.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        /*
                         * +++ Based on selection, add/remove this entity type
                         * from the list of entitytypes-to-be-exported:
                         * ++++++++++++++++++++++++++++++++++++++++++++++++
                         */

                        EntityType entityType = (EntityType) ((Button) e.getSource()).getData();
                        boolean selected = ((Button) e.getSource()).getSelection();

                        if (selected) {
                            entityTypesToBeExported.put(entityType.getId(), "");
                        } else {
                            entityTypesToBeExported.remove(entityType.getId());
                        }
                    }
                });
            }
        //}

        checkboxRestrict.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                restrictedToEntityTypes = checkboxRestrict.getSelection();

                Control[] children = groupObjectTypes.getChildren();
                for (int i = 0; i < children.length; i++) {
                    if (children[i] instanceof Button)
                        ((Button) children[i]).setEnabled(checkboxRestrict.getSelection());
                }
            }
        });

        /*
         * ++++ Widgets to enable/disable encryption:
         * ++++++++++++++++++++++++++++++++++++++
         */

        final Composite encryptionOptionComposite = new Composite(composite, SWT.NONE);
        encryptionOptionComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
        ((RowLayout) encryptionOptionComposite.getLayout()).marginTop = 15;

        final Button encryptionCheckbox = new Button(encryptionOptionComposite, SWT.CHECK);
        encryptionCheckbox.setText("Encrypt output");
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

        /*
         * +++++ Widgets to browse for storage location:
         * ++++++++++++++++++++++++++++++++++++++++
         */

        final Composite compositeSaveLocation = new Composite(composite, SWT.NONE);
        compositeSaveLocation.setLayout(new RowLayout(SWT.HORIZONTAL));
        ((RowLayout) compositeSaveLocation.getLayout()).marginTop = 15;
        final Label labelLocation = new Label(compositeSaveLocation, SWT.NONE);
        labelLocation.setText(Messages.ExportDialog_5);
        final Text txtLocation = new Text(compositeSaveLocation, SWT.SINGLE | SWT.BORDER);
        short textLocationWidth = 300;
        txtLocation.setSize(textLocationWidth, 30);
        final RowData textLocationData = new RowData();
        textLocationData.width = textLocationWidth;
        txtLocation.setLayoutData(textLocationData);
        composite.pack();
        final Button buttonBrowseLocations = new Button(compositeSaveLocation, SWT.NONE);
        buttonBrowseLocations.setText(Messages.ExportDialog_6);

        buttonBrowseLocations.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
                dialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$
                String exportPath = dialog.open();
                if (exportPath != null) {
                    txtLocation.setText(exportPath);
                    storageLocation = exportPath;
                } else {
                    txtLocation.setText(""); //$NON-NLS-1$
                    storageLocation = ""; //$NON-NLS-1$
                }
            }
        });
        return composite;
    }

    /* Getters and Setters: */

    public ITVerbund getSelectedITNetwork() {
        return selectedITNetwork;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public boolean getEncryptOutput() {
        return encryptOutput;
    }

    public boolean isRestrictedToEntityTypes() {
        return restrictedToEntityTypes;
    }

    public HashMap<String, String> getEntityTypesToBeExported() {
        return entityTypesToBeExported;
    }
}
