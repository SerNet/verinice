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
package sernet.verinice.rcp.unify;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementComparator;
import sernet.verinice.model.common.ITitleAdaptor;
import sernet.verinice.rcp.WizardPageEnteringAware;
import sernet.verinice.service.commands.unify.UnifyElement;
import sernet.verinice.service.commands.unify.UnifyMapping;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class UnifyPageMapping extends WizardPageEnteringAware {
    
    private static final Logger LOG = Logger.getLogger(UnifyPageMapping.class);
    
    private static final Comparator<UnifyMapping> COMPARATOR = new ElementComparator<UnifyMapping>( new ITitleAdaptor<UnifyMapping>() {
        @Override
        public String getTitle(UnifyMapping mapping) {
            return mapping.getSourceElement().getTitle();
        }
    });
    
    private TableViewer table;
    
    private final Color colorDifferentTitle, colorNoMapping;
    
    private boolean copyLinksEnabled = false;
    private boolean deleteSourceLinksEnabled = false;
    private boolean copyAttributesEnabled = false;
    
    /**
     * @param pageName
     */
    protected UnifyPageMapping() {
        super(UnifyWizard.PAGE_SELECT_MAPPING_ID);
        final int rgbMax = 255;
        final int dtBlue = 170;
        final int cnmGreen = 210;
        final int cnmBlue = cnmGreen;
        setTitle(Messages.UnifyPageMapping_0);   
        Device device = Display.getCurrent();
        colorDifferentTitle = new Color(device, rgbMax, rgbMax, dtBlue);
        colorNoMapping = new Color(device, rgbMax, cnmGreen, cnmBlue);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        final int characterAmountToConvert = 40;
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(characterAmountToConvert);
        composite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        composite.setLayout(gridLayout);
        
        table = createTable(composite);
        table.setContentProvider(new ArrayContentProvider());       
        table.refresh(true);         

        Composite checkboxComposite = new Composite(composite, SWT.RESIZE);
        GridData cbcGd = new GridData(SWT.FILL, SWT.FILL, true, false);
        cbcGd.minimumHeight = 200;
        checkboxComposite.setLayoutData(cbcGd);
        checkboxComposite.setLayout(gridLayout);
        
        GridData buttonGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        buttonGridData.minimumHeight = 10;
        
        Button copyLinksCheckbox = new Button(checkboxComposite, SWT.CHECK);
        copyLinksCheckbox.setEnabled(true);
        copyLinksCheckbox.setSelection(copyLinksEnabled);
        copyLinksCheckbox.setText(Messages.UnifyPageMapping_8);
        copyLinksCheckbox.setLayoutData(buttonGridData);
        
        final Button deleteSourceLinksCheckbox = new Button(checkboxComposite, SWT.CHECK);
        deleteSourceLinksCheckbox.setEnabled(false);
        deleteSourceLinksCheckbox.setText(Messages.UnifyPageMapping_9);
        deleteSourceLinksCheckbox.setSelection(deleteSourceLinksEnabled);
        deleteSourceLinksCheckbox.setLayoutData(buttonGridData);
        deleteSourceLinksCheckbox.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(e.getSource() instanceof Button){
                    setDeleteSourceLinksEnabled(((Button)e.getSource()).getSelection());
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        
        copyLinksCheckbox.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(e.getSource() instanceof Button){
                    setCopyLinksEnabled(((Button)e.getSource()).getSelection());
                    toggleButtonEnabled(deleteSourceLinksCheckbox, ((Button)e.getSource()).getSelection());
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        Button copyAttributesCheckbox = new Button(checkboxComposite, SWT.CHECK);
        copyAttributesCheckbox.setEnabled(true);
        copyAttributesCheckbox.setText(Messages.UnifyPageMapping_10);
        copyAttributesCheckbox.setSelection(copyAttributesEnabled);
        copyAttributesCheckbox.setLayoutData(buttonGridData);
        copyAttributesCheckbox.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(e.getSource() instanceof Button){
                    setCopyAttributesEnabled(((Button)e.getSource()).getSelection());
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        
        setPageComplete(false);
        
        setControl(composite);
    }
    
    
    /**
     * Called when this wizuard page is entered.
     * Override this in your subclass.
     */
    protected void pageEntered() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Page entered..."); //$NON-NLS-1$
        }
        try {
            loadAndShowMapping();
        } catch(Exception e) {
            LOG.error("Error while loading mappings.", e); //$NON-NLS-1$
            showError(Messages.UnifyPageMapping_3);
        }     
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        List<UnifyMapping> mappings = getUnifyWizard().getMappings();
        return (mappings!=null && !mappings.isEmpty());
    }
    
    private void loadAndShowMapping() {
        getUnifyWizard().loadMapping();
        List<UnifyMapping> mappings = getUnifyWizard().getMappings();
        if(mappings!=null) {
            Collections.sort(mappings, COMPARATOR);
            table.setInput(mappings);
            table.refresh();
        }
        setPageComplete(isPageComplete());
    }
    
 
    private void showError(String message) {
        MessageDialog.openError(this.getShell(), Messages.UnifyPageMapping_4, message);
    }
    
    private UnifyWizard getUnifyWizard() {
        return (UnifyWizard) getWizard();
    }

    private TableViewer createTable(Composite parent) {

        final int defaultColumnWeight = 50;
        
        Composite tableComposite = new Composite(parent, SWT.Resize);
        GridData tGd = new GridData(SWT.FILL, SWT.FILL, true, true);
        tGd.minimumHeight = 87;
        tableComposite.setLayoutData(tGd);
        
        TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableComposite.setLayout(tableColumnLayout);
        
        TableViewer tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

        Table internalTable = tableViewer.getTable();
        // Spaltenköpfe und Zeilenbegrenzungen sichtbar machen
        internalTable.setHeaderVisible(true);
        internalTable.setLinesVisible(true);
        
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableViewer.getControl().setLayoutData(gd);

        tableViewer.setUseHashlookup(true);

        TableViewerColumn sourceColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        sourceColumn.getColumn().setText(Messages.UnifyPageMapping_5);
        sourceColumn.setLabelProvider(new CellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                UnifyMapping mapping = (UnifyMapping) cell.getElement();
                String sourceTitle = mapping.getSourceElement().getTitle();
                if(sourceTitle!=null) {
                    cell.setText(sourceTitle);
                }
                setCellColor(cell,mapping);              
            }
         
        });
        tableColumnLayout.setColumnData(sourceColumn.getColumn(), new ColumnWeightData(defaultColumnWeight));
        
        TableViewerColumn destinationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        destinationColumn.getColumn().setText(Messages.UnifyPageMapping_6);
        destinationColumn.setLabelProvider(new CellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                UnifyMapping mapping = (UnifyMapping) cell.getElement();
                cell.setText(mapping.getDestinationText());
                setCellColor(cell,mapping); 
            }
        });
        tableColumnLayout.setColumnData(destinationColumn.getColumn(), new ColumnWeightData(defaultColumnWeight));
        
        return tableViewer;
    }
    
    protected void setCellColor(ViewerCell cell, UnifyMapping mapping) {
        List<UnifyElement> destinationList = mapping.getDestinationElements();
        if(destinationList==null || destinationList.isEmpty()) {
            cell.setBackground(colorNoMapping);
        } else {
            UnifyElement source = mapping.getSourceElement();
            if(!isTitleEquals(mapping)) {
                cell.setBackground(colorDifferentTitle);
            }
        }
        
    }

    private boolean isTitleEquals(UnifyMapping mapping){
        UnifyElement source = mapping.getSourceElement();
        String destinationText = mapping.getDestinationText();
        return source!=null 
                && source.getTitle()!=null 
                && destinationText!=null && 
                source.getTitle().equals(destinationText);
    }


    public boolean isCopyLinksEnabled() {
        return copyLinksEnabled;
    }

    public boolean isDeleteSourceLinksEnabled() {
        return deleteSourceLinksEnabled;
    }

    public void setCopyLinksEnabled(boolean copyLinksEnabled) {
        this.copyLinksEnabled = copyLinksEnabled;
        getUnifyWizard().setCopyLinks(copyLinksEnabled);
    }
    
    private void toggleButtonEnabled(Button b, boolean enabled){
        if(b != null){
            b.setEnabled(enabled);
        }
    }

    public void setDeleteSourceLinksEnabled(boolean deleteSourceLinksEnabled) {
        this.deleteSourceLinksEnabled = deleteSourceLinksEnabled;
        getUnifyWizard().setDeleteSourceLinks(deleteSourceLinksEnabled);
    }




    public boolean isCopyAttributesEnabled() {
        return copyAttributesEnabled;
    }

    public void setCopyAttributesEnabled(boolean copyAttributesEnabled) {
        this.copyAttributesEnabled = copyAttributesEnabled;
        getUnifyWizard().setDontCopyPropertyValues(copyAttributesEnabled);
    }




    class ActionLabelProvider extends ColumnLabelProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            String text = Messages.UnifyPageMapping_7;
            if (element instanceof CnATreeElement) {
                text = ((CnATreeElement) element).getTitle();
            }
            return text;
        }
        
    }

}
