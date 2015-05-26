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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
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
import sernet.hui.common.connect.IEntityElement;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;

/**
 * Wizard page of wizard {@link IndividualProcessWizard}.
 * User sets properties on this page, which are visible in web frontend.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("unchecked")
public class PropertyPage extends WizardPage {
    
    private static final Logger LOG = Logger.getLogger(PropertyPage.class);

    public static final String NAME = "PROPERTY_PAGE"; //$NON-NLS-1$
    
    private String elementType;
    
    private TreeViewer tableSelected;
    private TreeViewer table;
    private PropertyTreeContentProvider selectedContentProvider = new PropertyTreeContentProvider();
    private PropertyTreeContentProvider contentProvider = new PropertyTreeContentProvider();
    
    private Button addAllButton;
    private Button removeAllButton;
    
    private List selectedItems; 
    private List unselectedItems = new ArrayList();
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
        final int fourCCompositeCharLength = 20;
        final int fourCCompositeNumColumns = 3;
        final int leftCompositeCharLength = 40;
        final int rightCompositeCharLength = leftCompositeCharLength;

        Composite composite = new Composite(parent, SWT.FILL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Composite fourColumnComposite = new Composite(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = convertHeightInCharsToPixels(fourCCompositeCharLength);
        fourColumnComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(fourCCompositeNumColumns, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        fourColumnComposite.setLayout(gridLayout);

        Composite leftComposite = new Composite(fourColumnComposite, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(leftCompositeCharLength);
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
        gridData.widthHint = convertWidthInCharsToPixels(rightCompositeCharLength);
        rightComposite.setLayoutData(gridData);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        rightComposite.setLayout(gridLayout);
        
        tableSelected = createTreeTable(leftComposite,Messages.PropertyPage_3);
        tableSelected.setLabelProvider(new PropertyTypeLabelProvider());
        tableSelected.setContentProvider(selectedContentProvider); 
        tableSelected.refresh(true);
       
        table = createTreeTable(rightComposite,Messages.PropertyPage_4);
        table.setLabelProvider(new PropertyTypeLabelProvider());
        table.setContentProvider(contentProvider);    
        table.refresh(true);
        
        initializeContent();
        
        createButtons(centerComposite);
    } 
    
    @SuppressWarnings("rawtypes")
    private void initializeContent() {
        EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(elementType);
        allProperties = new LinkedList<PropertyType>();
        selectedProperties = new LinkedList<PropertyType>();
        allPropertiesMap = new Hashtable<String, PropertyType>();
        for (PropertyType property : entityType.getAllPropertyTypes()) {
            if(property.isVisible()) {
                allProperties.add(property);
                selectedProperties.add(property);
                allPropertiesMap.put(property.getId(), property);
            }
        }
        
        selectedItems = new LinkedList();     
        for (IEntityElement element : entityType.getElements()) {
           if(element instanceof PropertyType) {
               PropertyType propertyType = (PropertyType) element;
               if(propertyType.isVisible()) {
                   selectedItems.add(propertyType);
               }
           }
           if(element instanceof PropertyGroup) {
               selectedItems.add(element);
           }
        }
        
        selectedContentProvider.setVisibleTyps(selectedProperties);
        contentProvider.setVisibleTyps(unselectedProperties);
        tableSelected.setInput(selectedItems);
        table.setInput(selectedItems);
    }
    
    /**
     * Moves selected elements in the tree into the table
     */
    private void addSelection() {
        IStructuredSelection selection = (IStructuredSelection) table.getSelection();
        List selectionList = selection.toList();
        for (Object item : selectionList) {
            if(item instanceof PropertyType) {
                PropertyType propertyType = (PropertyType) item;
                selectedProperties.add(propertyType);
                unselectedProperties.remove(propertyType);
            }
            if(item instanceof PropertyGroup) {
                PropertyGroup group = (PropertyGroup) item;
                selectedProperties.addAll(group.getPropertyTypes());
                unselectedProperties.removeAll(group.getPropertyTypes());
            } 
        }
        selectedContentProvider.setVisibleTyps(selectedProperties);
        contentProvider.setVisibleTyps(unselectedProperties);       
        tableSelected.setInput(selectedItems);
        table.setInput(selectedItems);
        tableSelected.setSelection(selection);
        table.getControl().setFocus();
    }
    
    /**
     * Moves the selected elements in the table into the tree
     */
    private void removeSelection() {
        IStructuredSelection selection = (IStructuredSelection) tableSelected.getSelection();
        List selectionList = selection.toList();
        for (Object item : selectionList) {
            if(item instanceof PropertyType) {
                PropertyType propertyType = (PropertyType) item;
                selectedProperties.remove(propertyType);
                unselectedProperties.add(propertyType);
            }
            if(item instanceof PropertyGroup) {
                PropertyGroup group = (PropertyGroup) item;
                selectedProperties.removeAll(group.getPropertyTypes());
                unselectedProperties.addAll(group.getPropertyTypes());
            } 
        }
        selectedContentProvider.setVisibleTyps(selectedProperties);
        contentProvider.setVisibleTyps(unselectedProperties);      
        tableSelected.setInput(selectedItems);
        table.setInput(selectedItems);       
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
        addAllButton.setEnabled(!unselectedItems.isEmpty());

        final Button removeButton = new Button(parent, SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP,true, false));
        removeButton.setText(Messages.PropertyPage_7);
        removeButton.setEnabled(!table.getSelection().isEmpty());
      
        removeAllButton = new Button(parent, SWT.PUSH);
        removeAllButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
        removeAllButton.setText(Messages.PropertyPage_8);
        removeAllButton.setEnabled(!selectedItems.isEmpty());

        table.addSelectionChangedListener(new ISelectionChangedListener() {          
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                        addButton.setEnabled(!event.getSelection().isEmpty());
                    }
                });

        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
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
                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        removeButton.setEnabled(!event.getSelection().isEmpty());
                    }
                });

        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelection();
                addAllButton.setEnabled(true);
                removeAllButton.setEnabled(!selectedItems.isEmpty());
            }
        });

        tableSelected.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                removeSelection();
                addAllButton.setEnabled(true);
                removeAllButton.setEnabled(!selectedItems.isEmpty());
            }
        });

        addAllButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedProperties.addAll(unselectedProperties);
                unselectedProperties.clear();
                selectedContentProvider.setVisibleTyps(selectedProperties);
                contentProvider.setVisibleTyps(unselectedProperties);
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
            @Override
            public void widgetSelected(SelectionEvent e) {
                unselectedProperties.addAll(selectedProperties);
                selectedProperties.clear();
                selectedContentProvider.setVisibleTyps(selectedProperties);
                contentProvider.setVisibleTyps(unselectedProperties);
                table.refresh();
                tableSelected.refresh();
                removeAllButton.setEnabled(false);
                addAllButton.setEnabled(true);
            }
        });

    }
    
    private TreeViewer createTreeTable(Composite parent, String title) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText(title);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        int style = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;
        
        TreeViewer table = new TreeViewer(parent, style | SWT.MULTI);

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
        final int layoutMarginWidth = 10;
        final Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = layoutMarginWidth;
        composite.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);  
        
        addFormElements(composite);
                  
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
            if (element instanceof PropertyGroup) {
                text = ((PropertyGroup) element).getName();
            }
            return text;
        }
        
    }

}
