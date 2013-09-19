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

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.ComboModelLabelProvider;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * Wizard page of wizard {@link IndividualProcessWizard}.
 * User sets a relation type from affected  element to a person on this page.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RelationPage extends WizardPage {
    
    private static final Logger LOG = Logger.getLogger(RelationPage.class);

    public static final String NAME = "RELATION_PAGE"; //$NON-NLS-1$
    
    private String elementType;
    
    private HuiRelation relation;

    private Combo relationCombo;
    private ComboModel<HuiRelation> relationComboModel;
    
    private boolean isActive = true;
    
    protected RelationPage(String elementType) {
        super(NAME);
        setTitle(Messages.RelationPage_1);
        setMessage(Messages.RelationPage_2);
        this.elementType = elementType;
        initComboValues();
    }   

    private void addFormElements(Composite container) {
        Label typeLabel = new Label(container, SWT.NONE);
        typeLabel.setText(Messages.RelationPage_3);
        
        relationCombo = new Combo(container, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        relationCombo.setLayoutData(gd);
        relationCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                relationComboModel.setSelectedIndex(relationCombo.getSelectionIndex());
                relation=relationComboModel.getSelectedObject();
            }
        });
        if(!relationComboModel.isEmpty()) {
            relationCombo.setItems(relationComboModel.getLabelArray());
            relationCombo.select(0);
            relationComboModel.setSelectedIndex(0);
            relation=relationComboModel.getSelectedObject();
        } else {
            relationCombo.setEnabled(false);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        if(!isActive) {
            return true;
        }
        boolean complete = super.isPageComplete();
        if (LOG.isDebugEnabled()) {
            LOG.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete;
    }
    
    private void initComboValues() {
        relationComboModel = new ComboModel<HuiRelation>(new ComboModelLabelProvider<HuiRelation>() {
            @Override
            public String getLabel(HuiRelation relation) {
                return relation.getName();
            }
        });
        EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(elementType);
        Set<HuiRelation> personRelations = entityType.getPossibleRelations(PersonIso.TYPE_ID);
        for (HuiRelation huiRelation : personRelations) {
            relationComboModel.add(huiRelation);
        }
        if(!relationComboModel.isEmpty()) {
            relationComboModel.sort();
        } 
    }
    
    public boolean isRelation() {
        return !relationComboModel.isEmpty();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    @Override
    public IWizardPage getNextPage() {
        return (PropertyPage) getWizard().getPage(PropertyPage.NAME);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        final int defaultMarginWidth = 10;
        final Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = defaultMarginWidth;
        composite.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);  
        
        addFormElements(composite);
                  
        composite.pack(); 
        
        // Required to avoid an error in the system
        setControl(composite);
        setPageComplete(true);
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }

    public HuiRelation getRelation() {
        return relation;
    }
    
    public String getRelationId() {
        return (getRelation()!=null) ? getRelation().getId() : null;
    }
    
    public String getRelationName() {
        return (getRelation()!=null) ? getRelation().getName() : null;
    }

    public void setRelationId(String relationId) {
        
        if(relationId!=null && relationComboModel!=null) {
            int size = relationComboModel.getSize();
            for (int i = 0; i < size; i++) {
                HuiRelation rel = relationComboModel.getObject(i);
                if(relationId.equals(rel.getId())) {
                    relationComboModel.setSelectedIndex(i);
                    relationCombo.select(i);
                    this.relation = rel;
                    break;
                }
            }
        }
    }   
}
