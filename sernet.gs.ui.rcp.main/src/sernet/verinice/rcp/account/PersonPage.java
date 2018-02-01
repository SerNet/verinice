package sernet.verinice.rcp.account;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.IComboModelLabelProvider;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.rcp.ElementSelectionComponent;
import sernet.verinice.service.commands.LoadCnAElementByEntityTypeId;
import sernet.verinice.service.commands.LoadConfiguration;
import sernet.verinice.service.commands.RetrieveCnATreeElement;

/**
 * Wizard page of wizard {@link AccountWizard}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class PersonPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(PersonPage.class);
    public static final String PAGE_NAME = "account-wizard-person-page"; //$NON-NLS-1$

    private CnATreeElement person;
    private CnATreeElement group;
    private CnATreeElement scope;

    private ComboModel<CnATreeElement> comboModelScope;
    private Combo comboScope;

    private ComboModel<CnATreeElement> comboModelGroup;
    private Combo comboGroup;

    private ElementSelectionComponent personComponent;

    private Label personLabel;

    private boolean isNewAccount = false;

    protected PersonPage() {
        super(PAGE_NAME);
    }

    protected void initGui(Composite parent) {
        setTitle(Messages.PersonPage_1);
        selectMessage();

        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.PersonPage_0);
        comboScope = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboScope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboScope.setEnabled(isNewAccount());
        comboScope.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModelScope.setSelectedIndex(comboScope.getSelectionIndex());
                scope = comboModelScope.getSelectedObject();
                checkIfScopeIsPersonScope();
                personComponent.setTypeId(getPersonTypeId());
                personComponent.setScopeId(getScopeId());
                personComponent.loadElements();
                loadGroups();
            }
        });

        label = new Label(parent, SWT.NONE);
        label.setText(Messages.PersonPage_5);
        comboGroup = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboGroup.setEnabled(isNewAccount());
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
        personComponent = new ElementSelectionComponent(personComposite, getPersonTypeId(),
                getGroupId());
        personComponent.setScopeOnly(true);
        personComponent.setShowScopeCheckbox(false);
        personComponent.setHeight(200);
        personComponent.init();
        personComponent.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                selectPerson();
            }
        });
        personLabel = new Label(parent, SWT.NONE);

        showSelectedPerson();
    }

    protected void checkIfScopeIsPersonScope() {
        if (scope != null && person != null) {
            if (scope.getDbId() != person.getScopeId()) {
                deselectPerson();
            }
        }

    }

    private void deselectPerson() {
        personComponent.deselectElements();
        selectPerson();
    }

    private void selectMessage() {
        if (isNewAccount()) {
            setMessage(Messages.PersonPage_2);
        } else {
            setMessage(Messages.PersonPage_3, DialogPage.INFORMATION);
        }
    }

    private void selectPerson() {
        setErrorMessage(null);
        if (!isNewAccount()) {
            return;
        }
        List<CnATreeElement> selectedElements = personComponent.getSelectedElements();
        if (selectedElements != null && !selectedElements.isEmpty()) {
            person = selectedElements.get(0);
        } else {
            person = null;
        }

        validatePerson();
        showSelectedPerson();
        setPageComplete(isPageComplete());
    }

    private void validatePerson() {
        try {
            if (person != null) {
                LoadConfiguration command = new LoadConfiguration(person);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                Configuration account = command.getConfiguration();
                if (account != null) {
                    person = null;
                    setErrorMessage(Messages.PersonPage_4);
                }
            }
        } catch (Exception e) {
            LOG.error("Error while validating person", e); //$NON-NLS-1$
        }
    }

    private void showSelectedPerson() {
        if (person != null) {
            person = Retriever.checkRetrieveElement(person);
            GenericPerson genericPerson = new GenericPerson(person);
            personLabel.setText(Messages.PersonPage_6 + genericPerson.getName());
        } else {
            personLabel.setText(Messages.PersonPage_7);
        }
        personLabel.pack();
    }

    protected void initData() throws Exception {
        comboModelScope = new ComboModel<>(
                new IComboModelLabelProvider<CnATreeElement>() {
                    @Override
                    public String getLabel(CnATreeElement element) {
                        return element.getTitle();
                    }
                });
        comboModelGroup = new ComboModel<>(
                new IComboModelLabelProvider<CnATreeElement>() {
                    @Override
                    public String getLabel(CnATreeElement element) {
                        return element.getTitle();
                    }
                });
        loadScopes();
        personComponent.loadElements();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        boolean complete = (getPerson() != null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete;
    }

    private List<CnATreeElement> loadEntitiesByTypeId(String typeId) throws CommandException {
        LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(typeId);
        return getCommandService().executeCommand(command).getElements();
    }

    private void loadScopes() throws CommandException {
        comboModelScope.clear();
        comboModelScope.addAll(loadEntitiesByTypeId(Organization.TYPE_ID));
        comboModelScope.addAll(loadEntitiesByTypeId(ITVerbund.TYPE_ID_HIBERNATE));
        comboModelScope.addAll(loadEntitiesByTypeId(ItNetwork.TYPE_ID));

        comboModelScope.sort();
        comboModelScope.addNoSelectionObject(Messages.PersonPage_8);
        getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                comboScope.setItems(comboModelScope.getLabelArray());
                if (scope != null) {
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

    private static String findGroupTypeIdByPersonTypeId(String personTypeId) {
        switch (personTypeId) {
        case PersonIso.TYPE_ID:
            return PersonGroup.TYPE_ID;
        case BpPerson.TYPE_ID:
            return BpPersonGroup.TYPE_ID;
        default:
            return null;
        }
    }

    private void loadGroups() {
        try {
            comboModelGroup.clear();
            String groupTypeId = findGroupTypeIdByPersonTypeId(getPersonTypeId());
            if (groupTypeId != null) {
                loadPersonGroups(groupTypeId);
            }
            getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    comboGroup.setItems(comboModelGroup.getLabelArray());
                    if (group != null) {
                        comboModelGroup.setSelectedObject(group);
                        selectFirstIfNoGroupIsSelected();
                        comboGroup.select(comboModelGroup.getSelectedIndex());
                    } else {
                        comboGroup.select(0);
                        comboModelGroup.setSelectedIndex(comboGroup.getSelectionIndex());
                    }
                    group = comboModelGroup.getSelectedObject();
                    personComponent.setGroupId(getGroupId());
                    personComponent.loadElementsAndSelect(person);
                }
            });
        } catch (Exception e) {
            LOG.error("Error while loading groups", e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }

    private void loadPersonGroups(String typeId) throws CommandException {
        LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(typeId,
                getScopeId());
        command = getCommandService().executeCommand(command);
        comboModelGroup.addAll(command.getElements());
        comboModelGroup.sort();
        comboModelGroup.addNoSelectionObject(Messages.PersonPage_9);
    }

    private void selectFirstIfNoGroupIsSelected() {
        if (comboModelGroup.getSelectedIndex() == -1) {
            comboModelGroup.setSelectedIndex(0);
        }
    }

    private void loadGroup(CnATreeElement person) {
        try {
            String personTypeId = person.getTypeId();
            String groupTypeId = findGroupTypeIdByPersonTypeId(personTypeId);
            if (groupTypeId != null) {
                RetrieveCnATreeElement retrieveCommand = new RetrieveCnATreeElement(groupTypeId,
                        person.getParentId(), RetrieveInfo.getPropertyInstance());
                retrieveCommand = getCommandService().executeCommand(retrieveCommand);
                group = retrieveCommand.getElement();
            }
        } catch (CommandException e) {
            LOG.error("Error while loading group.", e); //$NON-NLS-1$
        }
    }

    private void loadScope() {
        try {
            String personTypeId = getPersonTypeId();
            if (personTypeId == null) {
                return;
            }
            String typeId;
            if (BpPerson.TYPE_ID.equals(getPersonTypeId())) {
                typeId = ItNetwork.TYPE_ID;
            } else if (Person.TYPE_ID.equals(getPersonTypeId())) {
                typeId = ITVerbund.TYPE_ID;
            } else {
                typeId = Organization.TYPE_ID;
            }
            RetrieveCnATreeElement retrieveCommand = new RetrieveCnATreeElement(typeId,
                    getScopeId(), RetrieveInfo.getPropertyInstance());
            retrieveCommand = getCommandService().executeCommand(retrieveCommand);
            scope = retrieveCommand.getElement();
        } catch (CommandException e) {
            LOG.error("Error while loading group.", e); //$NON-NLS-1$
        }
    }

    private Integer getScopeId() {
        Integer id = null;
        if (scope != null) {
            id = scope.getDbId();
        }
        if (person != null) {
            id = person.getScopeId();
        }
        return id;
    }

    private Integer getGroupId() {
        Integer id = null;
        if (group != null) {
            id = group.getDbId();
        }
        return id;
    }

    private String getPersonTypeId() {
        if (person != null) {
            return person.getTypeId();
        }
        if (scope == null) {
            return PersonIso.TYPE_ID;
        }

        if (scope.isItNetwork()) {
            return BpPerson.TYPE_ID;
        } else if (scope.isItVerbund()) {
            return Person.TYPE_ID;
        } else {
            return PersonIso.TYPE_ID;
        }
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
        if (person != null) {
            loadScope();
            loadGroup(person);
        }
    }

    public boolean isNewAccount() {
        return isNewAccount;
    }

    public void setNewAccount(boolean isNewAccount) {
        this.isNewAccount = isNewAccount;
    }

}
