package sernet.verinice.rcp.account;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.DialogPage;
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
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.ComboModel;
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
                int selectionIndex = comboScope.getSelectionIndex();
                if (selectionIndex == 0) {
                    person = null;
                }
                comboModelScope.setSelectedIndex(selectionIndex);
                scope = comboModelScope.getSelectedObject();
                checkIfScopeIsPersonScope();
                personComponent.setTypeIds(getPersonTypeIds());
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
        personComponent = new ElementSelectionComponent(personComposite, getPersonTypeIds(),
                getScopeId(), getGroupId());
        personComponent.setScopeOnly(true);
        personComponent.setShowScopeCheckbox(false);
        personComponent.setHeight(200);
        personComponent.init();
        personComponent.getViewer().addSelectionChangedListener(event -> selectPerson());
        personLabel = new Label(parent, SWT.NONE);

        showSelectedPerson();
    }

    protected void checkIfScopeIsPersonScope() {
        if (scope != null && person != null && !scope.getDbId().equals(person.getScopeId())) {
            deselectPerson();

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
        comboModelScope = new ComboModel<>(CnATreeElement::getTitle);
        comboModelGroup = new ComboModel<>(CnATreeElement::getTitle);
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
        getDisplay().syncExec(() -> {
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
            Set<String> groupTypeIds = getPersonTypeIds().stream()
                    .map(PersonPage::findGroupTypeIdByPersonTypeId).filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (!groupTypeIds.isEmpty()) {
                loadPersonGroups(groupTypeIds);
            }
            getDisplay().syncExec(() -> {
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
            });
        } catch (Exception e) {
            LOG.error("Error while loading groups", e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }

    private void loadPersonGroups(Set<String> typeIds) {
        List<CnATreeElement> elements = typeIds.stream().flatMap(typeId -> {
            LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(typeId,
                    getScopeId());
            try {
                command = getCommandService().executeCommand(command);
            } catch (CommandException e) {
                throw new RuntimeCommandException(e);
            }
            return command.getElements().stream();
        }).collect(Collectors.toList());

        comboModelGroup.addAll(elements);
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

    private void loadScope(String personTypeId) {
        try {
            String typeId;
            if (BpPerson.TYPE_ID.equals(personTypeId)) {
                typeId = ItNetwork.TYPE_ID;
            } else if (Person.TYPE_ID.equals(personTypeId)) {
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

    private Set<String> getPersonTypeIds() {
        if (person != null) {
            return Collections.singleton(person.getTypeId());
        }
        if (scope == null) {
            return new HashSet<>(
                    Arrays.asList(Person.TYPE_ID, PersonIso.TYPE_ID, BpPerson.TYPE_ID));
        }

        if (scope.isItNetwork()) {
            return Collections.singleton(BpPerson.TYPE_ID);
        } else if (scope.isItVerbund()) {
            return Collections.singleton(Person.TYPE_ID);
        } else {
            return Collections.singleton(PersonIso.TYPE_ID);
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
            loadScope(person.getTypeId());
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
