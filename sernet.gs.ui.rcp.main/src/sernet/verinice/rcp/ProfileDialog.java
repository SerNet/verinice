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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IRightsServiceClient;
import sernet.verinice.model.auth.Action;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.Profile;
import sernet.verinice.model.auth.ProfileRef;
import sernet.verinice.model.auth.Userprofile;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
@SuppressWarnings("restriction")
public class ProfileDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(ProfileDialog.class);
    
    private Text  textName;
    private Label translated;
    
    private TableViewer tableSelected;
    private TableViewer table;
    
    private Button addAllButton;
    private Button removeAllButton;
    
    private Auth auth;
    private String profileName;
    private String profileNameOld;
    private Profile profile;  
    private List<Action> selectedActions = new ArrayList<Action>(); 
    private List<Action> selectedActionsOld = new ArrayList<Action>();   
    private List<Action> unselectedActions;
    private List<String> allActions;
    

    private IRightsServiceClient rightsService;

    public ProfileDialog(Shell parent) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        auth = getRightService().getConfiguration();
        allActions = Arrays.asList(ActionRightIDs.getAllRightIDs());
        unselectedActions = new ArrayList<Action>(allActions.size());    
    }


    /**
     * @param shell
     * @param auth2
     * @param profileRef
     */
    public ProfileDialog(Shell parent, Auth auth, String profileName) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.auth = auth;
        this.profileName = profileName;
        allActions = Arrays.asList(ActionRightIDs.getAllRightIDs());
        unselectedActions = new ArrayList<Action>(allActions.size());  
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.ProfileDialog_0);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final int numColumnsCombo = 4;
        final int numColumns4ColComposite = 3;
        final int minWidthGridLayout = 200;
        final int gridDataHeightCharacterAmount = 20;
        final int gridDataWidthCharacterAmount = 40;
        
        setTitle(Messages.ProfileDialog_1);
        setMessage(Messages.ProfileDialog_2);
        setTitleImage(ImageCache.getInstance().getImage(ImageCache.PROFILE_64));
        
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.FILL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite comboComposite = new Composite(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.horizontalAlignment=GridData.HORIZONTAL_ALIGN_FILL;
        comboComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(numColumnsCombo, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        comboComposite.setLayout(gridLayout);
        
        Label label = new Label(comboComposite, SWT.WRAP);
        label.setText(Messages.ProfileDialog_3);       

        textName = new Text(comboComposite,SWT.BORDER);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.minimumWidth = minWidthGridLayout;
        textName.setLayoutData(gridData);
        textName.addFocusListener(new FocusListener() {
            
            @Override
            public void focusLost(FocusEvent e) {
                if(ProfileDialog.this.profile!=null) {
                    ProfileDialog.this.profile.setName(textName.getText());
                }
            }
            
            @Override
            public void focusGained(FocusEvent e) {
                // nothing to do
            }
        });
        
        Label labelTranslated = new Label(comboComposite, SWT.WRAP);
        labelTranslated.setText(Messages.ProfileDialog_4);
        
        translated = new Label(comboComposite, SWT.WRAP);
        
        Composite fourColumnComposite = new Composite(composite, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = convertHeightInCharsToPixels(gridDataHeightCharacterAmount);
        fourColumnComposite.setLayoutData(gridData);
        gridLayout = new GridLayout(numColumns4ColComposite, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        fourColumnComposite.setLayout(gridLayout);

        Composite leftComposite = new Composite(fourColumnComposite, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(gridDataWidthCharacterAmount);
        leftComposite.setLayoutData(gridData);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftComposite.setLayout(gridLayout);

        Composite centerComposite = new Composite(fourColumnComposite, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        centerComposite.setLayout(gridLayout);
        centerComposite.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        Composite rightComposite = new Composite(fourColumnComposite, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(gridDataWidthCharacterAmount);
        rightComposite.setLayoutData(gridData);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        rightComposite.setLayout(gridLayout);
        
        tableSelected = createTable(leftComposite,Messages.ProfileDialog_5);
        tableSelected.setLabelProvider(new ActionLabelProvider());
        tableSelected.setComparator(new ActionTableComparator());
        tableSelected.setContentProvider(new ArrayContentProvider());     
        tableSelected.refresh(true);
       
        table = createTable(rightComposite,Messages.ProfileDialog_6);
        table.setLabelProvider(new ActionLabelProvider());
        table.setComparator(new ActionTableComparator());
        table.setContentProvider(new ArrayContentProvider());       
        table.refresh(true);
            
        createButtons(centerComposite);
        
        initializeContent();

        Dialog.applyDialogFont(composite);
        
        return composite;
    }

    /**
     * 
     */ 
    private void initializeContent() {
        loadProfiles(profileName);
        tableSelected.setInput(selectedActions);   
        setUnselected();
        table.setInput(unselectedActions);      
    }
 
    /**
     * @param username
     */
    private void loadProfiles(String profileName) {
        table.remove(unselectedActions);
        tableSelected.remove(selectedActions);
        if(profileName!=null) {
            for (Profile prf :  auth.getProfiles().getProfile()) {
                if(profileName.equals(prf.getName())) {
                    this.profile = prf;
                    selectedActions = prf.getAction();
                    selectedActionsOld = new ArrayList<Action>(selectedActions);
                    textName.setText(prf.getName());
                    profileNameOld = prf.getName();
                    translated.setText(getRightService().getMessage(prf.getName()));
                    break;
                }
            }
        } else {
            this.profile = new Profile();
            selectedActions = profile.getAction();
            auth.getProfiles().getProfile().add(this.profile);
        }
        setUnselected();
        table.setInput(unselectedActions);
        tableSelected.setInput(selectedActions);
        table.refresh(true);
        tableSelected.refresh(true);
        removeAllButton.setEnabled(!selectedActions.isEmpty());
        addAllButton.setEnabled(!unselectedActions.isEmpty());
        
        
    }
    
    private void setUnselected() {
        Map<String, String> mapSelected = new HashMap<String, String>(allActions.size());
        for (Action action : selectedActions) {
            mapSelected.put(action.getId(), action.getId());
        }
        unselectedActions.clear();
        for (String name : allActions) {
            if(!mapSelected.containsKey(name)) {
                Action action = new Action();
                action.setId(name);
                unselectedActions.add(action);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        try {
            if(!this.profile.getName().equals(this.profileName)) {
                updateProfileRefs();
            }
            getRightService().updateConfiguration(auth);   
            super.okPressed();
        } catch(Exception e) {           
            final String message = "Error while saving profiles.";
            LOG.error(message, e);
            MessageDialog.openError(this.getShell(), "Error", message);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected void cancelPressed() {
        if(this.profile!=null) {
            this.profile.setName(profileNameOld);
            this.profile.getAction().clear();
            for (Action action : selectedActionsOld) {
                this.profile.getAction().add(action);
            }
        }
        super.okPressed();
    }

    /**
     * 
     */
    private void updateProfileRefs() {      
        for (Userprofile userprofile : auth.getUserprofiles().getUserprofile()) {          
            for (ProfileRef profileRef : userprofile.getProfileRef()) {
                if(profileRef.getName().equals(this.profileName)) {
                    profileRef.setName(this.profile.getName());
                }
            }
        }
    }


    private TableViewer createTable(Composite parent, String title) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText(title);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        TableViewer table0 = new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        table0.getControl().setLayoutData(gd);

        table0.setUseHashlookup(true);

        return table0;
    }
    
    private void createButtons(Composite parent) {
        Label spacer = new Label(parent, SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,false));

        final Button addButton = new Button(parent, SWT.PUSH);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,false));
        addButton.setText(Messages.ProfileDialog_7);
        addButton.setEnabled(!table.getSelection().isEmpty());
        
        addAllButton = new Button(parent, SWT.PUSH);
        addAllButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP,true, false));
        addAllButton.setText(Messages.ProfileDialog_8);
        addAllButton.setEnabled(!unselectedActions.isEmpty());

        final Button removeButton = new Button(parent, SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP,true, false));
        removeButton.setText(Messages.ProfileDialog_9);
        removeButton.setEnabled(!table.getSelection().isEmpty());
      
        removeAllButton = new Button(parent, SWT.PUSH);
        removeAllButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
        removeAllButton.setText(Messages.ProfileDialog_10);
        removeAllButton.setEnabled(!selectedActions.isEmpty());

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
                removeAllButton.setEnabled(!selectedActions.isEmpty());
            }
        });

        tableSelected.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                removeSelection();
                addAllButton.setEnabled(true);
                removeAllButton.setEnabled(!selectedActions.isEmpty());
            }
        });

        addAllButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public void widgetSelected(SelectionEvent e) { 
                selectedActions.addAll(unselectedActions);
                unselectedActions.clear();
                table.refresh();
                tableSelected.refresh();
                addAllButton.setEnabled(false);
                removeAllButton.setEnabled(true);
                profile.setOrigin(null);
            }
        });

        removeAllButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                unselectedActions.addAll(selectedActions);
                selectedActions.clear();
                table.refresh();
                tableSelected.refresh();
                removeAllButton.setEnabled(false);
                addAllButton.setEnabled(true);
                profile.setOrigin(null);
            }
        });

    }
   

    /**
     * Moves selected elements in the tree into the table
     */
    private void addSelection() {
        IStructuredSelection selection = (IStructuredSelection) table.getSelection();
        List selectionList = selection.toList();
        selectedActions.addAll(selectionList);
        unselectedActions.removeAll(selectionList);
        Object[] selectedElements = selection.toArray();
        tableSelected.add(selectedElements);
        table.remove(selectedElements);
        tableSelected.setSelection(selection);
        table.getControl().setFocus();
        profile.setOrigin(null);
    }
    
    /**
     * Moves the selected elements in the table into the tree
     */
    private void removeSelection() {
        IStructuredSelection selection = (IStructuredSelection) tableSelected.getSelection();
        List selectionList = selection.toList();
        selectedActions.removeAll(selectionList);
        unselectedActions.addAll(selectionList);
        Object[] selectedElements = selection.toArray();
        table.add(selectedElements);
        tableSelected.remove(selectedElements); 
        table.setSelection(selection);
        tableSelected.getControl().setFocus();
        profile.setOrigin(null);
    }

    /**
     * @return the auth
     */
    public Auth getAuth() {
        return auth;
    }


    private IRightsServiceClient getRightService() {
        if (rightsService == null) {
            rightsService = (IRightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }

    class ActionLabelProvider extends ColumnLabelProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            String text = Messages.ProfileDialog_11;
            if (element instanceof Action) {
                text = ((Action) element).getId();
            }
            // get translated message
            text = getRightService().getMessage(text);
            return text;
        }
        
    }
    
    class ActionTableComparator extends ViewerComparator {
        private int propertyIndex;
        private static final int ASCENDING = 0;
        private static final int DESCENDING = 1;
        private int direction = ASCENDING;
        private Collator collator = Collator.getInstance();

        public ActionTableComparator() {
            this.propertyIndex = 0;
            direction = ASCENDING;
        }

        public int getDirection() {
            return direction == 1 ? SWT.DOWN : SWT.UP;
        }

        public void setColumn(int column) {
            if (column == this.propertyIndex) {
                // Same column as last sort; toggle the direction
                direction = 1 - direction;
            } else {
                // New column; do an ascending sort
                this.propertyIndex = column;
                direction = DESCENDING;
            }
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            Action p1 = (Action) e1;
            Action p2 = (Action) e2;
            int rc = 0;
            switch (propertyIndex) {
            case 0:            
                rc = collator.compare(getRightService().getMessage(p1.getId()), getRightService().getMessage(p2.getId()));
                break;
            default:
                rc = 0;
            }
            // If descending order, flip the direction
            if (direction == DESCENDING) {
                rc = -rc;
            }
            return rc;
        }
    }

}
