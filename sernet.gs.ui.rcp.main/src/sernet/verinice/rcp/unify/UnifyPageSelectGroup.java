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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementComparator;
import sernet.verinice.model.common.ITitleAdaptor;
import sernet.verinice.rcp.WizardPageEnteringAware;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class UnifyPageSelectGroup extends WizardPageEnteringAware {
    
    private static final Logger LOG = Logger.getLogger(UnifyPageSelectGroup.class);
    
    private TableViewer table;
    
    private static final Comparator<CnATreeElement> COMPARATOR = new ElementComparator<CnATreeElement>( new ITitleAdaptor<CnATreeElement>() {
        @Override
        public String getTitle(CnATreeElement element) {
            return element.getTitle();
        }
    });
    
    /**
     * @param pageName
     */
    protected UnifyPageSelectGroup() {
        super(UnifyWizard.PAGE_SELECT_GROUP_ID);
        this.setTitle(Messages.UnifyPageSelectGroup_0);   
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {   
        final int compCharToConvert = 40;
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(compCharToConvert);
        composite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        composite.setLayout(gridLayout);
        
        table = createTable(composite,Messages.UnifyPageSelectGroup_1);
        table.setLabelProvider(new ActionLabelProvider());
        table.setContentProvider(new ArrayContentProvider());       
        table.refresh(true);
        table.addSelectionChangedListener(new ISelectionChangedListener() {           
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                selectSourceAndDestination();                
            }
        });
        
        List<CnATreeElement> groupList = getUnifyWizard().getGroups();
        Collections.sort(groupList, COMPARATOR);
        table.setInput(groupList); 
        table.getTable().setSelection(0);
        
        setPageComplete(false);
        
        selectSourceAndDestination();
        
        setControl(composite);
    }

    private void selectSourceAndDestination() {
        IStructuredSelection selection = (IStructuredSelection) table.getSelection();
        getUnifyWizard().setSource((CnATreeElement) selection.getFirstElement());
        for (CnATreeElement element : getUnifyWizard().getGroups()) {
            if(!element.equals(getUnifyWizard().getSource())) {
                getUnifyWizard().setDestination(element);
                break;
            }
        }
        setPageComplete(getUnifyWizard().getSource()!=null && getUnifyWizard().getDestination()!=null);
    }
  
    
    private UnifyWizard getUnifyWizard() {
        return (UnifyWizard) getWizard();
    }

    private TableViewer createTable(Composite parent, String title) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText(title);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        int style = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;
        
        TableViewer table = new TableViewer(parent, style | SWT.MULTI);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.getControl().setLayoutData(gd);

        table.setUseHashlookup(true);

        return table;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.WizardPageEnteringAware#pageEntered()
     */
    @Override
    protected void pageEntered() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("pageEntered..."); //$NON-NLS-1$
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.WizardPageEnteringAware#pageLeft()
     */
    @Override
    protected void pageLeft() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("pageLeft..."); //$NON-NLS-1$
        }
    }
       
    class ActionLabelProvider extends ColumnLabelProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object o) {
            String text = Messages.UnifyPageSelectGroup_4;
            if (o instanceof CnATreeElement) {
                CnATreeElement element = (CnATreeElement) o;
                StringBuilder sb = new StringBuilder();
                sb.append(getUnifyWizard().getUuidParentInformationMap().get(element.getUuid()));
                sb.append(" > "); //$NON-NLS-1$
                sb.append(element.getTitle());
                text = sb.toString();
            }
            return text;
        }
        
    }

}
