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
package sernet.verinice.bpm.rcp;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.auth.Action;

/**
 * Wizard page of wizard {@link IndividualProcessWizard}.
 * User sets properties on this page, which are visible in web frontend.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class PropertyPage extends WizardPage {
    
    private static final Logger LOG = Logger.getLogger(PropertyPage.class);

    public static final String NAME = "PROPERTY_PAGE"; //$NON-NLS-1$
    
    private String elementType;
    
    private TableViewer tableSelected;
    private TableViewer table;
    
    Button addAllButton;
    Button removeAllButton;
    
    private List<PropertyType> selectedProperties; 
    private List<PropertyType> unselectedProperties = new ArrayList<PropertyType>();
    private List<PropertyType> allProperties;
    private Map<String, PropertyType> allPropertiesMap;
    
    protected PropertyPage(String elementType) {
        super(NAME);
        setTitle(Messages.PropertyPage_1);
        setMessage(Messages.PropertyPage_2);
        this.elementType = elementType;
    }   

    private void addFormElements(Composite parent) {
        Composite composite = new Composite(parent, SWT.FILL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Composite fourColumnComposite = new Composite(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = convertHeightInCharsToPixels(20);
        fourColumnComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        fourColumnComposite.setLayout(gridLayout);

        Composite leftComposite = new Composite(fourColumnComposite, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(40);
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
        gridData.widthHint = convertWidthInCharsToPixels(40);
        rightComposite.setLayoutData(gridData);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        rightComposite.setLayout(gridLayout);
        
        tableSelected = createTable(leftComposite,Messages.PropertyPage_3);
        tableSelected.setLabelProvider(new PropertyTypeLabelProvider());
        tableSelected.setComparator(new PropertyTypeComparator());
        tableSelected.setContentProvider(new ArrayContentProvider());     
        tableSelected.refresh(true);
       
        table = createTable(rightComposite,Messages.PropertyPage_4);
        table.setLabelProvider(new PropertyTypeLabelProvider());
        table.setComparator(new PropertyTypeComparator());
        table.setContentProvider(new ArrayContentProvider());       
        table.refresh(true);
        
        initializeContent();
        
        createButtons(centerComposite);
    } 
    
    private void initializeContent() {
        EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(elementType);
        allProperties = entityType.getAllPropertyTypes();
        allPropertiesMap = new Hashtable<String, PropertyType>();
        for (PropertyType property : allProperties) {
            allPropertiesMap.put(property.getId(), property);
        }
        selectedProperties = new ArrayList<PropertyType>(allProperties.size());
        for (PropertyType property : allProperties) {
            selectedProperties.add(property);
        }
        tableSelected.setInput(selectedProperties);
        table.setInput(unselectedProperties);
    }
    
    /**
     * Moves selected elements in the tree into the table
     */
    private void addSelection() {
        IStructuredSelection selection = (IStructuredSelection) table.getSelection();
        List selectionList = selection.toList();
        selectedProperties.addAll(selectionList);
        unselectedProperties.removeAll(selectionList);
        Object[] selectedElements = selection.toArray();
        tableSelected.add(selectedElements);
        table.remove(selectedElements);
        tableSelected.setSelection(selection);
        table.getControl().setFocus();
    }
    
    /**
     * Moves the selected elements in the table into the tree
     */
    private void removeSelection() {
        IStructuredSelection selection = (IStructuredSelection) tableSelected.getSelection();
        List selectionList = selection.toList();
        selectedProperties.removeAll(selectionList);
        unselectedProperties.addAll(selectionList);
        Object[] selectedElements = selection.toArray();
        table.add(selectedElements);
        tableSelected.remove(selectedElements); 
        table.setSelection(selection);
        tableSelected.getControl().setFocus();
    }
    
    private void createButtons(Composite parent) {
        Label spacer = new Label(parent, SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,false));

        final Button addButton = new Button(parent, SWT.PUSH);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,false));
        addButton.setText(Messages.PropertyPage_5);
        addButton.setEnabled(!table.getSelection().isEmpty());
        
        addAllButton = new Button(parent, SWT.PUSH);
        addAllButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP,true, false));
        addAllButton.setText(Messages.PropertyPage_6);
        addAllButton.setEnabled(!unselectedProperties.isEmpty());

        final Button removeButton = new Button(parent, SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP,true, false));
        removeButton.setText(Messages.PropertyPage_7);
        removeButton.setEnabled(!table.getSelection().isEmpty());
      
        removeAllButton = new Button(parent, SWT.PUSH);
        removeAllButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
        removeAllButton.setText(Messages.PropertyPage_8);
        removeAllButton.setEnabled(!selectedProperties.isEmpty());

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
                removeAllButton.setEnabled(!selectedProperties.isEmpty());
            }
        });

        tableSelected.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                removeSelection();
                addAllButton.setEnabled(true);
                removeAllButton.setEnabled(!selectedProperties.isEmpty());
            }
        });

        addAllButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) { 
                selectedProperties.addAll(unselectedProperties);
                unselectedProperties.clear();
                table.refresh();
                tableSelected.refresh();
                addAllButton.setEnabled(false);
                removeAllButton.setEnabled(true);
            }
        });

        removeAllButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                unselectedProperties.addAll(selectedProperties);
                selectedProperties.clear();
                table.refresh();
                tableSelected.refresh();
                removeAllButton.setEnabled(false);
                addAllButton.setEnabled(true);
            }
        });

    }
    
    private TableViewer createTable(Composite parent, String title) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText(title);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        TableViewer table = new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.getControl().setLayoutData(gd);

        table.setUseHashlookup(true);

        return table;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 10;
        composite.setLayout(layout);
        //layout.marginHeight = 10;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);  
        
        addFormElements(composite);
                  
        // Build the separator line
        //Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        //separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        composite.pack(); 
        
        // Required to avoid an error in the system
        setControl(composite);
        setPageComplete(true);
    }
    
    public void setPropertyIds(Set<String> propertyIds) {
       selectedProperties.clear();
       unselectedProperties.clear();
       for (String id : propertyIds) {
           PropertyType type = allPropertiesMap.get(id);
           if(type!=null) {
               selectedProperties.add(type);
           }
       }
       if(!selectedProperties.isEmpty()) {
           setUnselected();
           table.refresh();
           tableSelected.refresh();
       } else {
           initializeContent();
       }
    }
    
    private void setUnselected() {
        Map<String, String> mapSelected = new HashMap<String, String>(allProperties.size());
        for (PropertyType profile : selectedProperties) {
            mapSelected.put(profile.getId(), profile.getId());
        }
        unselectedProperties.clear();
        for (PropertyType prop : allProperties) {
            if(!mapSelected.containsKey(prop.getId())) {              
                unselectedProperties.add(prop);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        boolean complete = super.isPageComplete();
        if (LOG.isDebugEnabled()) {
            LOG.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete;
    }
    
    public List<PropertyType> getSelectedProperties() {
        return selectedProperties;
    }

    class PropertyTypeLabelProvider extends ColumnLabelProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            String text = Messages.PropertyPage_10;
            if (element instanceof PropertyType) {
                text = ((PropertyType) element).getName();
            }
            return text;
        }
        
    }
    
    class PropertyTypeComparator extends ViewerComparator {
        private int propertyIndex;
        private static final int ASCENDING = 0;
        private static final int DESCENDING = 1;
        private int direction = ASCENDING;
        Collator collator = Collator.getInstance();

        public PropertyTypeComparator() {
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
            PropertyType p1 = (PropertyType) e1;
            PropertyType p2 = (PropertyType) e2;
            int rc = 0;
            switch (propertyIndex) {
            case 0:            
                rc = collator.compare(p1.getName(), p2.getName());
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
