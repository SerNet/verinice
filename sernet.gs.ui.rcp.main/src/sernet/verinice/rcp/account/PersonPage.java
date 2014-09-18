package sernet.verinice.rcp.account;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByScopeId;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.ComboModelLabelProvider;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.rcp.ElementSelectionComponent;
import sernet.verinice.service.commands.LoadCnAElementByEntityTypeId;

/**
 * Wizard page of wizard {@link AccountWizard}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class PersonPage extends BaseWizardPage {
    
    private static final Logger LOG = Logger.getLogger(PersonPage.class);    
    public static final String PAGE_NAME = "account-wizard-person-page";
     
    private CnATreeElement person;
    private CnATreeElement group;
    private CnATreeElement scope;   
    
    private ComboModel<CnATreeElement> comboModelScope;
    private Combo comboScope;
    
    private ComboModel<CnATreeElement> comboModelGroup;
    private Combo comboGroup;
    
    private ElementSelectionComponent personComponent;
    
    protected PersonPage() {
        super(PAGE_NAME);
    }

    protected void initGui(Composite parent) {
        setTitle("Account");
        setMessage("Select Person");   
        
        comboScope = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboScope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboScope.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModelScope.setSelectedIndex(comboScope.getSelectionIndex());
                scope = comboModelScope.getSelectedObject();               
                personComponent.setTypeId(getPersonTypeId());
                personComponent.setScopeId(getScopeId());
                personComponent.loadElements();
                loadGroups();
            }
        });
        
        comboGroup = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);      
        comboGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboGroup.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModelGroup.setSelectedIndex(comboGroup.getSelectionIndex());
                group = comboModelGroup.getSelectedObject();  
                personComponent.setGroupId(getGroupId());
                personComponent.loadElements();
            }
        });

        final Composite personComposite = createEmptyComposite(parent);
        
        personComponent = new ElementSelectionComponent(personComposite, getPersonTypeId(), getGroupId());
        personComponent.setScopeOnly(true);
        personComponent.setShowScopeCheckbox(false);
        personComponent.init();
        personComponent.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setErrorMessage(null);             
                List<CnATreeElement> selectedElements = personComponent.getSelectedElements();
                if(selectedElements!=null && !selectedElements.isEmpty()) {
                    person = selectedElements.get(0);
                    setPageComplete(isPageComplete());
                }
            }        
        });
        
        
    }

    protected void initData() throws Exception {
        comboModelScope = new ComboModel<CnATreeElement>(new ComboModelLabelProvider<CnATreeElement>() {
            @Override
            public String getLabel(CnATreeElement element) {
                return element.getTitle();
            }       
        }); 
        comboModelGroup = new ComboModel<CnATreeElement>(new ComboModelLabelProvider<CnATreeElement>() {
            @Override
            public String getLabel(CnATreeElement element) {
                return element.getTitle();
            }       
        }); 
        loadScopes();
        personComponent.loadElements();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        boolean complete = (getPerson()!=null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete;
    }
    
    private void loadScopes() throws CommandException {
        comboModelScope.clear();
        LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(Organization.TYPE_ID);
        command = getCommandService().executeCommand(command);
        comboModelScope.addAll(command.getElements());
        command = new LoadCnAElementByEntityTypeId(ITVerbund.TYPE_ID_HIBERNATE);
        command = getCommandService().executeCommand(command);
        comboModelScope.addAll(command.getElements());
        comboModelScope.sort();
        comboModelScope.addNoSelectionObject("all");
        getDisplay().syncExec(new Runnable(){
            @Override
            public void run() {
                comboScope.setItems(comboModelScope.getLabelArray());
                if(scope!=null) {
                    comboModelScope.setSelectedObject(scope);
                    comboScope.select(comboModelScope.getSelectedIndex());
                    personComponent.setScopeId(getScopeId());
                    loadGroups();
                } else {
                    comboScope.select(0);
                    comboModelScope.setSelectedIndex(comboScope.getSelectionIndex()); 
                }               
            }
        });
    }
    
    private void loadGroups() {
        try {
            comboModelGroup.clear();
            if(PersonIso.TYPE_ID.equals(getPersonTypeId())) {
                LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(PersonGroup.TYPE_ID, getScopeId());
                command = getCommandService().executeCommand(command);
                comboModelGroup.addAll(command.getElements());       
                comboModelGroup.sort();
                comboModelGroup.addNoSelectionObject("all");
            }
            getDisplay().syncExec(new Runnable(){
                @Override
                public void run() {
                    comboGroup.setItems(comboModelGroup.getLabelArray());
                    if(group!=null) {
                        comboModelGroup.setSelectedObject(group);
                        comboGroup.select(comboModelGroup.getSelectedIndex());
                        personComponent.setGroupId(getGroupId());
                    } else {
                        comboGroup.select(0);
                        comboModelGroup.setSelectedIndex(comboGroup.getSelectionIndex()); 
                    }
                    personComponent.loadElementsAndSelect(person);
                }
            });
        } catch(Exception e) {
            LOG.error("Error while loading groups", e);
            throw new RuntimeException(e);
        }
    }
    
    private void laodGroup() {
       try {
            if(PersonIso.TYPE_ID.equals(getPersonTypeId()) && person!=null) {
                RetrieveCnATreeElement retrieveCommand = new RetrieveCnATreeElement(
                    PersonGroup.TYPE_ID, 
                    person.getParentId(),
                    RetrieveInfo.getPropertyInstance());              
                retrieveCommand = getCommandService().executeCommand(retrieveCommand);              
                group = retrieveCommand.getElement();
            }
        } catch (CommandException e) {
            LOG.error("Error while loading group.", e);
        }
    }

    private void loadScope() {
        try {
            if(getPersonTypeId()==null) {
                return;
            }
            String typeId = Organization.TYPE_ID;
            if(Person.TYPE_ID.equals(getPersonTypeId())) {
                typeId = ITVerbund.TYPE_ID;
            }
            RetrieveCnATreeElement retrieveCommand = new RetrieveCnATreeElement(
                typeId, 
                getScopeId(),
                RetrieveInfo.getPropertyInstance());              
            retrieveCommand = getCommandService().executeCommand(retrieveCommand);              
            scope = retrieveCommand.getElement();
        } catch (CommandException e) {
            LOG.error("Error while loading group.", e);
        }
    }
    
    private Integer getScopeId() {
        Integer id = null;
        if(scope!=null) {
            id = scope.getDbId();
        }
        if(person!=null) {
            id = person.getScopeId();
        }
        return id;
    }
    
    private Integer getGroupId() {
        Integer id = null;
        if(group!=null) {
            id = group.getDbId();
        }
        return id;
    }

    private String getPersonTypeId() {
        if(person!=null) {
            return person.getTypeId();
        }
        String typeId = PersonIso.TYPE_ID;
        if(scope instanceof ITVerbund) {
            typeId = Person.TYPE_ID;
        }
        return typeId;
    }
    
    private static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    public CnATreeElement getPerson() {
        return person;
    }

    public void setPerson(CnATreeElement person) {
        this.person = person;
        loadScope();
        laodGroup();
    }

}
