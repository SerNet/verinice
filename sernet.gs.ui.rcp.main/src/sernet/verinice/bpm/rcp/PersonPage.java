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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.rcp.ElementSelectionComponent;

/**
 * Wizard page of wizard {@link IndividualProcessWizard}.
 * User selects an assignee for the task on this page.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class PersonPage extends WizardPage {
    
    private static final Logger LOG = Logger.getLogger(PersonPage.class);

    public static final String NAME = "PERSON_PAGE"; //$NON-NLS-1$
    
    private CnATreeElement selectedPerson;
    
    private String selectedLogin;
    
    private String personTypeId = PersonIso.TYPE_ID;
    
    private boolean isActive = true;
    
    private ElementSelectionComponent component;
    
    /**
     * @param pageName
     */
    protected PersonPage() {
        super(NAME);
        setTitle(Messages.PersonPage_1);
        setMessage(Messages.PersonPage_2);
    }   

    /**
     * @param composite
     */
    private void addFormElements(Composite composite) {
        component = new ElementSelectionComponent(composite, personTypeId, null);
        component.setScopeOnly(false);
        component.setShowScopeCheckbox(false);
        component.init();
        component.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setErrorMessage(null);             
                List<CnATreeElement> selectedElements = component.getSelectedElements();
                boolean valid = laodAndCheckPerson(selectedElements);
                setPageComplete(valid);        
            }
        
        });
    }
    
    private boolean laodAndCheckPerson(List<CnATreeElement> selectedElements) {
        boolean valid = true;
        if(selectedElements==null || selectedElements.isEmpty()) {
            valid = false;
        }
        if(valid && selectedElements.size()>1) {
            valid = false;
            setErrorMessage(Messages.PersonPage_3);
        }
        if(valid) {
            selectedPerson = selectedElements.get(0);
            selectedLogin = loadLogin(selectedPerson);
            if(selectedLogin==null) {
                valid = false;
                selectedPerson = null;             
                setErrorMessage(Messages.PersonPage_4);
            }
        }
        return valid;
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
        final int marginWidth = 10;
        final Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = marginWidth;
        composite.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);  
        
        addFormElements(composite);
                  
        composite.pack(); 
        
        // Required to avoid an error in the system
        setControl(composite);
        setPageComplete(false);
    }
    
    private String loadLogin(CnATreeElement element)  {
        String login = null;
        try {
            LoadConfiguration command = new LoadConfiguration(element);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            Configuration configuration = command.getConfiguration();
            if(configuration!=null) {
                login = configuration.getUser();        
            }
        } catch(CommandException e) {
            LOG.error("Error while loading account data.", e); //$NON-NLS-1$
        }
        return login;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public CnATreeElement getSelectedPerson() {
        return selectedPerson;
    }

    public void setSelectedPerson(CnATreeElement selectedPerson) {
        if(selectedPerson!=null) {
            this.selectedPerson = selectedPerson;
            setErrorMessage(null);                   
            setPageComplete(true);
        }
    }

    public String getSelectedLogin() {
        return selectedLogin;
    }

    public void setPersonTypeId(String personTypeId) {
        this.personTypeId = personTypeId;
        if(component!=null) {
            component.loadElementsAndSelect(selectedPerson);  
        }
    }

    public void setSelectedLogin(String selectedLogin) {
        this.selectedLogin = selectedLogin;
        setPageComplete(selectedLogin!=null);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {      
        super.setVisible(visible);       
        if (visible) {
            component.loadElementsAndSelect(selectedPerson);   
        }
        
    }
    

}
