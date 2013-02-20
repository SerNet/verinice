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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.CnaTreeElementTitleFilter;
import sernet.gs.ui.rcp.main.bsi.dialogs.Messages;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByEntityTypeId;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementsForScope;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementSelectionComponent {

    private Composite container;

    private TableViewer viewer;
    private Text text;
    private Button checkbox;
    
    private CnaTreeElementTitleFilter filter;
    
    private List<CnATreeElement> elementList;
    private CnATreeElement inputElmt;
    private String entityType;
    private boolean scopeOnly;
    private boolean showScopeCheckbox;
    private static final String COLUMN_IMG = "_img"; //$NON-NLS-1$
    private static final String COLUMN_LABEL = "_label"; //$NON-NLS-1$
    
    private List<CnATreeElement> selectedElements = new ArrayList<CnATreeElement>();
    
    public ElementSelectionComponent(Composite container, String type, CnATreeElement inputElmt) {
        super();
        this.container = container;
        this.entityType = type;
        this.inputElmt = inputElmt;
        scopeOnly = true;
        showScopeCheckbox = true;
    }
    
    public void init() {
        
        final int formAttachmentDefaultOffset = 5;
        final int column1Width = 25;
        final int column2Width = 200;
        final int formData2Numerator = 100;
        final int formData3Numerator = formData2Numerator;
        container.setLayout(new FormLayout());
             
        Label label1 = new Label(container, SWT.NULL);
        label1.setText(Messages.CnATreeElementSelectionDialog_3);

        FormData formData = new FormData();
        formData.top = new FormAttachment(0, formAttachmentDefaultOffset);
        formData.left = new FormAttachment(0, formAttachmentDefaultOffset);
        label1.setLayoutData(formData);
        label1.pack();
        
        text = new Text(container, SWT.BORDER);
        FormData formData2 = new FormData();
        formData2.top = new FormAttachment(0, formAttachmentDefaultOffset);
        formData2.left = new FormAttachment(label1, formAttachmentDefaultOffset);
        formData2.right = new FormAttachment(formData2Numerator, (-1) * formAttachmentDefaultOffset);
        text.setLayoutData(formData2);
        text.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
                filter.setPattern(text.getText());
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
        
        if(isShowScopeCheckbox()) {
            SelectionListener listener = new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Button button = (e.getSource() instanceof Button) ? (Button)e.getSource() : null;
                    if(button != null){
                        scopeOnly = button.getSelection();
                        loadElements();
                    }
                }
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            };
            checkbox = SWTElementFactory.generateCheckboxButton(container, Messages.CnATreeElementSelectionDialog_4, true, listener);
            FormData checkboxFD = new FormData();
            checkboxFD.top = new FormAttachment(text, formAttachmentDefaultOffset);
            checkboxFD.left = new FormAttachment(0, formAttachmentDefaultOffset);
            checkbox.setLayoutData(checkboxFD);
            checkbox.pack();
        }
        
        viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        FormData formData3 = new FormData();
        if(isShowScopeCheckbox()) {
            formData3.top = new FormAttachment(checkbox, formAttachmentDefaultOffset);
        } else {
            formData3.top = new FormAttachment(text, formAttachmentDefaultOffset);
        }
        formData3.left = new FormAttachment(0, formAttachmentDefaultOffset);
        formData3.right = new FormAttachment(formData3Numerator, (-1) * formAttachmentDefaultOffset);
        formData3.bottom = new FormAttachment(formData3Numerator, (-1) * formAttachmentDefaultOffset);
        viewer.getTable().setLayoutData(formData3);
        viewer.getTable().setHeaderVisible(false);
        viewer.getTable().setLinesVisible(true);
        
        // image column:
        TableViewerColumn column1 = new TableViewerColumn(viewer, SWT.LEFT);
        column1.getColumn().setText(""); //$NON-NLS-1$
        column1.getColumn().setWidth(column1Width);
        column1.getColumn().setResizable(false);
        column1.setLabelProvider(new CellLabelProvider() {
            public void update(ViewerCell cell) {
                if (cell.getElement() instanceof PlaceHolder){
                    return;
                }
                CnATreeElement element = (CnATreeElement)cell.getElement();
                Image image = CnAImageProvider.getCustomImage(element);
                if(image==null) {
                    image = ImageCache.getInstance().getObjectTypeImage(element.getTypeId());
                }
                cell.setImage(image);
            }
        });
        
        // label column:
        TableViewerColumn column2 = new TableViewerColumn(viewer, SWT.LEFT);
        column2.getColumn().setWidth(column2Width);
        column2.setLabelProvider(new CellLabelProvider() {
            public void update(ViewerCell cell) {
                if (cell.getElement() instanceof PlaceHolder) {
                    cell.setText( ((PlaceHolder)cell.getElement()).getTitle() );
                    return;
                }
                cell.setText( makeTitle((CnATreeElement)cell.getElement()) );
            }
        });
        
        viewer.setColumnProperties(new String[] {COLUMN_IMG, COLUMN_LABEL});
        viewer.setContentProvider(new ArrayContentProvider());
        filter = new CnaTreeElementTitleFilter(viewer);
        viewer.setSorter(new ViewerSorter() {
            public int compare(Viewer viewer, Object e1, Object e2) {
                CnATreeElement elmt1 = (CnATreeElement) e1;
                CnATreeElement elmt2 = (CnATreeElement) e2;
                return makeTitle(elmt1).compareTo(makeTitle(elmt2));
            } 
        });
        
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                selectedElements = ((IStructuredSelection)viewer.getSelection()).toList();
            }
        });
        
        viewer.addDoubleClickListener(new IDoubleClickListener() {           
            @Override
            public void doubleClick(DoubleClickEvent event) {
                selectedElements = ((IStructuredSelection)viewer.getSelection()).toList();
            }
        });
    }
    
 
    public void loadElements() {
        loadElementsAndSelect(null);
    }

    public void loadElementsAndSelect(final CnATreeElement selected) {
        if (entityType == null || entityType.length()==0){
            return;
        }
        ArrayList temp = new ArrayList(1);
        temp.add(new PlaceHolder(Messages.CnATreeElementSelectionDialog_6));
        viewer.setInput(temp);
        
        WorkspaceJob job = new WorkspaceJob(Messages.CnATreeElementSelectionDialog_7) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();

                try {
                    monitor.setTaskName(Messages.CnATreeElementSelectionDialog_8);
                    scopeAndElmntDpntElmntSlctn(selected);                 
                } catch (Exception e) {
                    ExceptionUtil.log(e, Messages.CnATreeElementSelectionDialog_0);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();        
    }

    /**
     * 
     */
    protected void setSelection() {
        if(selectedElements!=null && !selectedElements.isEmpty()) {
            getViewer().setSelection(new StructuredSelection(selectedElements));
        }
    }

    private String makeTitle(CnATreeElement elmt) {
        StringBuilder sb = new StringBuilder();
        if(elmt instanceof IISO27kElement) {
            String abbreviation = ((IISO27kElement)elmt).getAbbreviation();
            if(abbreviation!=null && !abbreviation.isEmpty()) {
                sb.append(abbreviation).append(" ");
            }
        }
        
        sb.append(((CnATreeElement)elmt).getTitle());
        return sb.toString();
    }
    
    public List<CnATreeElement> getSelectedElements() {
        return selectedElements;
    }

    public Composite getContainer() {
        return container;
    }

    public TableViewer getViewer() {
        return viewer;
    }

    public boolean isScopeOnly() {
        return scopeOnly;
    }

    public void setScopeOnly(boolean scopeOnly) {
        this.scopeOnly = scopeOnly;
        if(this.checkbox!=null) {
            this.checkbox.setSelection(scopeOnly);
        }
    }

    public boolean isShowScopeCheckbox() {
        return showScopeCheckbox;
    }

    public void setShowScopeCheckbox(boolean showScopeCheckbox) {
        this.showScopeCheckbox = showScopeCheckbox;
    }

    /**
     * @param selectedPerson
     */
    public void setSelectedElement(CnATreeElement selectedElement) {
        if(selectedElement!=null) {
            int i = elementList.indexOf(selectedElement);
            if(i!=-1) {
                getViewer().getTable().deselectAll();
                getViewer().setSelection(new StructuredSelection(selectedElement));
                selectedElements = ((IStructuredSelection)viewer.getSelection()).toList();
            }
        }
    }

    private void loadAndSelectElements(final CnATreeElement selected, final List<CnATreeElement> list) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if (list !=null){
                    viewer.setInput(list);
                } else {
                    ArrayList temp = new ArrayList(0);
                    viewer.setInput(temp);
                }
                setSelectedElement(selected);
            }
        });
        elementList = list;
    }

    private void scopeAndElmntDpntElmntSlctn(final CnATreeElement selected) throws CommandException {
        if (scopeOnly && inputElmt!=null) {
            LoadElementsForScope command = new LoadElementsForScope(entityType, inputElmt.getDbId());
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            loadAndSelectElements(selected, command.getElements());
            
        } else {
            LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(entityType);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            loadAndSelectElements(selected, command.getElements());
        }
    }
}
