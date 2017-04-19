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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.interfaces.CommandException;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class MultiselectWidget<T> {

    private static final Logger LOG = Logger.getLogger(MultiselectWidget.class);
    
    protected List<T> itemList;   
    private ITreeSelection selection;
    private T selectedElement;
    protected Set<T> selectedElementSet = new HashSet<T>();  
    protected Set<T> preSelectedElements = new HashSet<T>(10);
    
    protected boolean showOnlySelected = true;
    protected boolean showOnlySelectedCheckbox = true;
    protected boolean showFilterTextfield = true;
    protected boolean showSelectAllCheckbox = false;
    protected boolean showDeselectAllCheckbox = false;
    
    protected String filterString = null;
    
    protected Text filter;
    protected Group group;
    protected ScrolledComposite scrolledComposite;
    protected Composite innerComposite;
    
    private Map<T, Button> checkboxMap;
    
    private Button checkboxOnlySelected;
    private Button buttonSelectAll;
    private Button buttonDeselectAll;
    
    private SelectionListener organizationListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            Button checkbox = (Button) e.getSource();
            selectedElement = (T) checkbox.getData();
            if(checkbox.getSelection()) {
                selectedElementSet.add(selectedElement);
                preSelectedElements.add(selectedElement);
            } else {
                selectedElementSet.remove(selectedElement);
                preSelectedElements.remove(selectedElement);
            }
            super.widgetSelected(e);
        }
    };
    
    private String title;
 
    protected MultiselectWidget() {
        super();
    }

    /**
     * @param parent
     * @param style
     * @throws CommandException 
     */
    public MultiselectWidget(Composite parent) {
        try{
            initData();
            initGui(parent);
        } catch( CommandException e ) {
           String message = "Error while creating widget."; //$NON-NLS-1$
           LOG.error(message, e);
           throw new RuntimeException(message, e);
        }
    }
    
    public MultiselectWidget(Composite parent, ITreeSelection selection) throws CommandException {
        this.selection = selection;
        initData();
        initGui(parent);
    }
    
    public MultiselectWidget(Composite parent, T selectedElement) throws CommandException {
        this.selectedElement = selectedElement;
        initData();
        initGui(parent);
    }
    
    public MultiselectWidget(Composite parent, ITreeSelection selection, T selectedElement) throws CommandException {     
        this.selection = selection;
        this.selectedElement = selectedElement;
        initData();
        initGui(parent);
    }

    protected void initGui(Composite parent) {
        final int gdMinimumWidth = 550;
        final int gdHeightHint = 200; 
        
        createFilterPanel(parent);
        
        group = new Group(parent, SWT.NONE);
        if(getTitle()!=null) {
            group.setText(getTitle());
        }
        GridLayout groupOrganizationLayout = new GridLayout(1, true);
        group.setLayout(groupOrganizationLayout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        gd.minimumWidth = gdMinimumWidth;
        gd.heightHint = gdHeightHint; 
        group.setLayoutData(gd);

        scrolledComposite = new ScrolledComposite(group, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setExpandHorizontal(true);
        
        innerComposite = new Composite (scrolledComposite, SWT.NONE); 
        scrolledComposite.setContent(innerComposite); 
        innerComposite.setLayoutData(new GridData (SWT.FILL, SWT.FILL,true, false)); 
        innerComposite.setLayout(new GridLayout (1, false));   
           
        if(selection != null && !selection.isEmpty()){
            Iterator<T> iter = selection.iterator();
            while(iter.hasNext()){
                preSelectedElements.add(iter.next());
            }
        } else if(selectedElement != null) {
            preSelectedElements.add(selectedElement);
        }
        
        checkboxMap = new HashMap<T, Button>();
        
        addCheckboxes();   
    }

    private void createFilterPanel(Composite parent) {
        if(isToolbarVisible()) {
            Composite filterComp = createFilterComposite(parent);
            if(isShowFilterTextfield()) {
                createFilterTextfield(filterComp);
            }         
            if(isShowOnlySelectedCheckbox()) {
                createSelectedCheckbox(filterComp);
            }         
            if(isShowDeselectAllCheckbox()) {
                createDeselectAllButton(filterComp);
            }         
            if(isShowSelectAllCheckbox()) {
                createSelectAllButton(filterComp);
            }
        }
    }

    private boolean isToolbarVisible() {
        return isShowFilterTextfield() || isShowOnlySelectedCheckbox() || isShowSelectAllCheckbox();
    }

    private void createFilterTextfield(Composite filterComp) {
        Label label = new Label(filterComp, SWT.NONE);
        label.setText(Messages.MultiselectWidget_1);
        filter = new Text(filterComp, SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        filter.setLayoutData(gridData);
        filter.addKeyListener(new KeyListener() {             
            @Override
            public void keyReleased(KeyEvent e) {
                filterString = filter.getText();
                if(filterString!=null) {
                    filterString = filterString.trim();
                    if(filterString.isEmpty()) {
                        filterString = null;
                    }
                }
                removeCheckboxes();
                addCheckboxes();
            }              
            @Override
            public void keyPressed(KeyEvent e) {  
            }
        });
    }

    private void createSelectedCheckbox(Composite filterComp) {
        checkboxOnlySelected = new Button(filterComp, SWT.CHECK);
        checkboxOnlySelected.setText(Messages.MultiselectWidget_3);
        checkboxOnlySelected.setSelection(showOnlySelected);
        checkboxOnlySelected.addSelectionListener(new SelectionListener() {          
            @Override
            public void widgetSelected(SelectionEvent e) {
                showOnlySelected = checkboxOnlySelected.getSelection();
                removeCheckboxes();
                addCheckboxes();
            }            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    private void createSelectAllButton(Composite composite) {
        buttonSelectAll = new Button(composite, SWT.PUSH);
        configureToggleButton(composite, buttonSelectAll, Messages.MultiselectWidget_SelectAll);
        buttonSelectAll.addSelectionListener(new SelectionListener() {          
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (T item : checkboxMap.keySet()) {
                    Button checkbox = checkboxMap.get(item);
                    checkbox.setSelection(true);
                    selectedElement = item; 
                    selectedElementSet.add(item);
                    preSelectedElements.add(item);
                }
            }            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }
    
    private void createDeselectAllButton(Composite composite) {
        buttonDeselectAll = new Button(composite, SWT.PUSH);
        configureToggleButton(composite, buttonDeselectAll, Messages.MultiselectWidget_Deselect_All);     
        buttonDeselectAll.addSelectionListener(new SelectionListener() {          
            @Override
            public void widgetSelected(SelectionEvent e) {             
                for (T item : checkboxMap.keySet()) {
                    Button checkbox = checkboxMap.get(item);
                    checkbox.setSelection(false);
                }
                selectedElement = null; 
                selectedElementSet.clear();
                preSelectedElements.clear();
            }            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }
    
    private void configureToggleButton(Composite composite, Button button, String text) {
        button.setText(text);
        GridData gridData = new GridData();
        gridData.widthHint = 120;
        button.setLayoutData(gridData);
    }

    protected void addCheckboxes() {
        for(T item : itemList) {        
            if(isItemVisible(item)) {
                boolean selected = preSelectedElements.contains(item);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(item.toString() + " is visible, select state is: " + selected); //$NON-NLS-1$
                }
                Button checkbox = new Button(innerComposite, SWT.CHECK);
                checkbox.setText(getLabel(item));
                checkbox.setData(item);
                checkbox.addSelectionListener(organizationListener);
                if (selected) {
                    checkbox.setSelection(true);
                    selectedElement = item; 
                    selectedElementSet.add(item);             
                }
                if (itemList.size() == 1) {
                    checkbox.setSelection(true);
                    selectedElement = item;
                    selectedElementSet.add(item);
                }
                checkboxMap.put(item, checkbox);
            }
        }
        
        scrolledComposite.setVisible(true);
        Point size = innerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT); 
        size.y += itemList.size() * 2;
        innerComposite.setSize(size); 
        group.layout(); 
    }
    
    protected boolean isItemVisible(T item) {
        boolean selected = preSelectedElements.contains(item);
        String itemLabel = getLabel(item);
        boolean containsFilter = (filterString==null) || itemLabel.toLowerCase().contains(filterString.toLowerCase());
        return (!isShowOnlySelected() || selected) && containsFilter;
    }

    protected void removeCheckboxes() {
        removeListenerFromCheckboxes(organizationListener);
        for (Button checkbox : checkboxMap.values()) {
            checkbox.dispose();
        }
        checkboxMap.clear();
    }

    protected void removeListenerFromCheckboxes(SelectionListener listener) {
        for (Button checkbox : checkboxMap.values()) {
            checkbox.removeSelectionListener(listener);
        }
    }

    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    protected abstract String getLabel(T elmt);
    
    protected abstract void initData() throws CommandException;
    
    private Composite createFilterComposite(Composite composite) {
        Composite comboComposite = new Composite(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        comboComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(4, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        comboComposite.setLayout(gridLayout);
        return comboComposite;
    }
    
    public void setShowOnlySelected(boolean showOnlySelected) {
        this.showOnlySelected = showOnlySelected;
     
    }

    public void setShowOnlySelectedCheckbox(boolean showOnlySelectedCheckbox) {
        this.showOnlySelectedCheckbox = showOnlySelectedCheckbox;
    }

    public boolean isShowOnlySelected() {
        return showOnlySelected;
    }

    public boolean isShowOnlySelectedCheckbox() {
        return showOnlySelectedCheckbox;
    }

    public boolean isShowFilterTextfield() {
        return showFilterTextfield;
    }

    public boolean isShowSelectAllCheckbox() {
        return showSelectAllCheckbox;
    }
    
    public void setShowSelectAllButton(boolean showSelectAllCheckbox) {
        this.showSelectAllCheckbox = showSelectAllCheckbox;
    }
    
    public boolean isShowDeselectAllCheckbox() {
        return showDeselectAllCheckbox;
    }

    public void setShowDeselectAllButton(boolean showDeselectAllCheckbox) {
        this.showDeselectAllCheckbox = showDeselectAllCheckbox;
    }

    public void addSelectionListener(SelectionListener listener) {
        if(listener==null) {
            return;
        }
        if(checkboxMap!=null) {
            for (Button checkbox : checkboxMap.values()) {
                checkbox.addSelectionListener(listener);
            }
        }
        if(buttonSelectAll!=null) {
            buttonSelectAll.addSelectionListener(listener);
        }
        if(buttonDeselectAll!=null) {
            buttonDeselectAll.addSelectionListener(listener);
        }
    }
    
    public void removeSelectionListener(SelectionListener listener) {
        if(listener==null) {
            return;
        }
        if(checkboxMap!=null) {
            for (Button checkbox : checkboxMap.values()) {
                checkbox.removeSelectionListener(listener);
            }
        }
        if(buttonSelectAll!=null) {
            buttonSelectAll.removeSelectionListener(listener);
        }
        if(buttonDeselectAll!=null) {
            buttonDeselectAll.removeSelectionListener(listener);
        }
    }
    
    protected abstract List<T> sortItems(List<T> list);

    public T getSelectedElement() {
        return selectedElement;
    }

    public void setSelectedElement(T selectedElement) {
        this.selectedElement = selectedElement;
    }

    public Set<T> getSelectedElementSet() {
        return selectedElementSet;
    }

    public void deselectCheckboxForElement(T element) {
        setSelectionForElement(element, false);
    }
    public void selectCheckboxForElement(T element) {
        setSelectionForElement(element, false);
    }

    private void setSelectionForElement(T element, boolean selection) {
        Button checkbox = checkboxMap.get(element);
        if(checkbox!=null) {
            checkbox.setSelection(selection);
        }
    }
    
    

}
