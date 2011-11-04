/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import sernet.verinice.interfaces.IAuthService;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IRightsServiceClient;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.ComboModelLabelProvider;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.Profile;
import sernet.verinice.model.auth.OriginType;
import sernet.verinice.model.auth.Userprofile;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
@SuppressWarnings("restriction")
public class UserprofileDialog extends TitleAreaDialog {

    private Label labelLogin;
    private Combo comboLogin;
    private ComboModel<String> comboModel;
    
    private TableViewer tableSelected;
    private TableViewer table;
    
    Button addAllButton;
    Button removeAllButton;
    
    Auth auth;
    Userprofile userprofile;
    private List<Profile> selectedProfiles = new ArrayList<Profile>();   
    private List<Profile> unselectedProfiles;
    private List<Profile> allProfiles;
    

    IRightsServiceClient rightsService;

    public UserprofileDialog(Shell parent) {
        super(parent);
        auth = getRightService().getConfiguration();
        allProfiles = auth.getProfiles().getProfile();
        unselectedProfiles = new ArrayList<Profile>(allProfiles.size());    
    }


    @Override
    protected Control createDialogArea(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        Composite comboComposite = new Composite(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        comboComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        comboComposite.setLayout(gridLayout);
        
        Label label = new Label(comboComposite, SWT.WRAP);
        label.setText("User / Group");

        comboLogin = new Combo(comboComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        //comboLogin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comboLogin.addSelectionListener(new SelectionAdapter() {
              public void widgetSelected(SelectionEvent e) {
                  comboModel.setSelectedIndex(comboLogin.getSelectionIndex());
                  loadProfiles();
              }

            });
        comboModel = new ComboModel<String>(new ComboModelLabelProvider() {
            @Override
            public String getLabel(Object label) {
                return (String) label;
            }       
        });
        
        Composite leftCenterRightComposite = new Composite(composite, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = convertHeightInCharsToPixels(20);
        leftCenterRightComposite.setLayoutData(gridData);
        gridLayout = new GridLayout(3, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftCenterRightComposite.setLayout(gridLayout);

        Composite leftComposite = new Composite(leftCenterRightComposite, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(40);
        leftComposite.setLayoutData(gridData);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftComposite.setLayout(gridLayout);

        Composite centerComposite = new Composite(leftCenterRightComposite, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        centerComposite.setLayout(gridLayout);
        centerComposite.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        Composite rightComposite = new Composite(leftCenterRightComposite, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(40);
        rightComposite.setLayoutData(gridData);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        rightComposite.setLayout(gridLayout);

        tableSelected = createTable(leftComposite);
        tableSelected.setLabelProvider(new ProfileLabelProvider());
        //table.setComparator();
        tableSelected.setContentProvider(new ArrayContentProvider());     
        tableSelected.refresh(true);
       
        table = createTable(rightComposite);
        table.setLabelProvider(new ProfileLabelProvider());
        //fTree.setComparator();
        table.setContentProvider(new ArrayContentProvider());       
        table.refresh(true);
        
      
        createButtons(centerComposite);
        
        initializeContent();
        
        String username = ((IAuthService)VeriniceContext.get(VeriniceContext.AUTH_SERVICE)).getUsername();
        loadProfiles(username);  
        comboModel.setSelectedObject(username);
        comboLogin.select(comboModel.getSelectedIndex());

        Dialog.applyDialogFont(composite);
        
        return composite;
    }

    /**
     * 
     */
    
    private void initializeContent() {
        tableSelected.setInput(selectedProfiles); 
        
        setUnselected();
        table.setInput(unselectedProfiles);
        
        for (Userprofile userprofile : auth.getUserprofiles().getUserprofile()) {
            comboModel.add(userprofile.getLogin());
        }
        comboLogin.setItems(comboModel.getLabelArray());
        
    }


    /**
     * 
     */
    private void setUnselected() {
        Map<String, String> mapSelected = new HashMap<String, String>(allProfiles.size());
        for (Profile profile : selectedProfiles) {
            mapSelected.put(profile.getName(), profile.getName());
        }
        unselectedProfiles.clear();
        for (Profile profile : allProfiles) {
            if(!mapSelected.containsKey(profile.getName())) {
                // create a clone without actions
                Profile profileWithoutAction = new Profile();
                profileWithoutAction.setName(profile.getName());
                unselectedProfiles.add(profileWithoutAction);
            }
        }
    }
    
    /**
     * 
     */
    protected void loadProfiles() {
        String selected = comboModel.getSelectedObject();
        loadProfiles(selected);
    }
    
    /**
     * @param username
     */
    private void loadProfiles(String username) {
        table.remove(unselectedProfiles);
        tableSelected.remove(selectedProfiles);
        for (Userprofile userprofile :  auth.getUserprofiles().getUserprofile()) {
            if(username.equals(userprofile.getLogin())) {
                this.userprofile = userprofile;
                selectedProfiles = userprofile.getProfile();
                break;
            }
        }
        setUnselected();
        table.setInput(unselectedProfiles);
        tableSelected.setInput(selectedProfiles);
        table.refresh(true);
        tableSelected.refresh(true);
        removeAllButton.setEnabled(!selectedProfiles.isEmpty());
        addAllButton.setEnabled(!unselectedProfiles.isEmpty());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {    
        getRightService().updateConfiguration(auth);
        super.okPressed();
    }

    private TableViewer createTable(Composite parent) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText("Table");
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        TableViewer table = new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.getControl().setLayoutData(gd);

        table.setUseHashlookup(true);

        return table;
    }
    
    private void createButtons(Composite parent) {
        Label spacer = new Label(parent, SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,false));

        final Button addButton = new Button(parent, SWT.PUSH);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,false));
        addButton.setText("<- Add");
        addButton.setEnabled(!table.getSelection().isEmpty());
        
        addAllButton = new Button(parent, SWT.PUSH);
        addAllButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP,true, false));
        addAllButton.setText("<<- Add all");
        addAllButton.setEnabled(!unselectedProfiles.isEmpty());

        final Button removeButton = new Button(parent, SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP,true, false));
        removeButton.setText("Remove ->");
        removeButton.setEnabled(!table.getSelection().isEmpty());
      
        removeAllButton = new Button(parent, SWT.PUSH);
        removeAllButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
        removeAllButton.setText("Remove all ->>");
        removeAllButton.setEnabled(!selectedProfiles.isEmpty());

        table.addSelectionChangedListener(new ISelectionChangedListener() {          
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                        addButton.setEnabled(!event.getSelection().isEmpty());
                    }
                });

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                addSelection();
                removeAllButton.setEnabled(true);
                addAllButton.setEnabled(!table.getSelection().isEmpty());
            }
        });

        table.addDoubleClickListener(new IDoubleClickListener() {           
            @Override
            public void doubleClick(DoubleClickEvent event) {
                addSelection();
                removeAllButton.setEnabled(true);
                addAllButton.setEnabled(!table.getSelection().isEmpty());
            }
        });

        tableSelected.addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        removeButton.setEnabled(!event.getSelection().isEmpty());
                    }
                });

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                removeSelection();
                addAllButton.setEnabled(true);
                removeAllButton.setEnabled(!selectedProfiles.isEmpty());
            }
        });

        tableSelected.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                removeSelection();
                addAllButton.setEnabled(true);
                removeAllButton.setEnabled(!selectedProfiles.isEmpty());
            }
        });

        addAllButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public void widgetSelected(SelectionEvent e) { 
                selectedProfiles.addAll(unselectedProfiles);
                unselectedProfiles.clear();
                table.refresh();
                tableSelected.refresh();
                addAllButton.setEnabled(false);
                removeAllButton.setEnabled(true);
                userprofile.setOrigin(null);
            }
        });

        removeAllButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                unselectedProfiles.addAll(selectedProfiles);
                selectedProfiles.clear();
                table.refresh();
                tableSelected.refresh();
                removeAllButton.setEnabled(false);
                addAllButton.setEnabled(true);
                userprofile.setOrigin(null);
            }
        });

    }
    
    
    /**
     * Moves selected elements in the tree into the table
     */
    private void addSelection() {
        IStructuredSelection selection = (IStructuredSelection) table.getSelection();
        List selectionList = selection.toList();
        selectedProfiles.addAll(selectionList);
        unselectedProfiles.removeAll(selectionList);
        Object[] selectedElements = selection.toArray();
        tableSelected.add(selectedElements);
        table.remove(selectedElements);
        tableSelected.setSelection(selection);
        table.getControl().setFocus();
        userprofile.setOrigin(null);
    }
    
    /**
     * Moves the selected elements in the table into the tree
     */
    private void removeSelection() {
        IStructuredSelection selection = (IStructuredSelection) tableSelected.getSelection();
        List selectionList = selection.toList();
        selectedProfiles.removeAll(selectionList);
        unselectedProfiles.addAll(selectionList);
        Object[] selectedElements = selection.toArray();
        table.add(selectedElements);
        tableSelected.remove(selectedElements); 
        table.setSelection(selection);
        tableSelected.getControl().setFocus();
        userprofile.setOrigin(null);
    }

    private IRightsServiceClient getRightService() {
        if (rightsService == null) {
            rightsService = (IRightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }

    class ProfileLabelProvider extends ColumnLabelProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            String text = "unknown";
            if (element instanceof Profile) {
                Profile profile = (Profile) element;
                text = profile.getName();
            }
            return text;
        }
        
    }
}
