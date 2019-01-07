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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.bpm.IndividualServiceParameter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.commands.LoadPersonForLogin;

/**
 * Wizard to start jBPM process "individual-task" defined in
 * individual-task.jpdl.xml
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndividualProcessWizard extends Wizard {

    private static final Logger LOG = Logger.getLogger(IndividualProcessWizard.class);

    private DescriptionPage descriptionPage;
    private DatePage datePage;
    private PersonPage personPage;
    private RelationPage relationPage;
    private PropertyPage propertyPage;
    private TemplatePage templatePage;

    private String elementTitle;

    private String elementType;

    private String personTypeId = PersonIso.TYPE_ID;

    private List<String> uuids;

    public static final String PREFERENCE_NAME = "task_templates"; //$NON-NLS-1$

    public static final String PREFERENCE_NODE_NAME = "bpm"; //$NON-NLS-1$

    public IndividualProcessWizard(List<String> selectedUuids, String elementTitle, String typeId) {
        super();
        this.uuids = new ArrayList<>(selectedUuids);
        this.elementTitle = elementTitle;
        this.elementType = typeId;
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.IndividualProcessWizard_2);
    }

    @Override
    public void addPages() {
        descriptionPage = new DescriptionPage(elementTitle);
        addPage(descriptionPage);
        // create relation before datepage
        relationPage = new RelationPage(elementType, personTypeId);
        datePage = new DatePage(relationPage.isRelation());
        personPage = new PersonPage();
        personPage.setPersonTypeId(getPersonTypeId());
        propertyPage = new PropertyPage(elementType);
        templatePage = new TemplatePage();

        addPage(datePage);
        addPage(personPage);
        addPage(relationPage);
        addPage(propertyPage);
        addPage(templatePage);
    }

    public void setTemplate(IndividualServiceParameter template) {
        Date dueDate = template.getDueDate();
        datePage.setDueDate(dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        datePage.setPeriod(template.getReminderPeriodDays());
        if (template.getAssignee() != null || !relationPage.isRelation()) {
            datePage.setAssigneeSelectionMode(DatePage.ASSIGNEE_SELECTION_DIRECT);
            relationPage.setActive(false);
            if (template.getAssignee() != null) {
                personPage.setSelectedLogin(template.getAssignee());
                CnATreeElement person = loadPersonForLogin(template.getAssignee());
                personPage.setSelectedPerson(person);
            }
        }
        if (template.getAssigneeRelationId() != null && relationPage.isRelation()) {
            datePage.setAssigneeSelectionMode(DatePage.ASSIGNEE_SELECTION_RELATION);
            relationPage.setRelationId(template.getAssigneeRelationId());
            personPage.setActive(false);
        }
        propertyPage.setPropertyIds(template.getProperties());
        descriptionPage.setTaskTitle(template.getTitle());
        descriptionPage.setTaskDescription(template.getDescription());
        descriptionPage.setReleaseProcessSelected(template.isWithAReleaseProcess());
    }

    public void setTemplateForRejectedRealization(IndividualServiceParameter template) {
        setTemplate(template);
        descriptionPage.disableTemplatesAndReleaseProcessCheckbox();
        descriptionPage.setTitle(Messages.DescriptionPage_14);
        descriptionPage.setMessage(Messages.DescriptionPage_15);
    }

    public IndividualServiceParameter getParameter() {
        IndividualServiceParameter parameter = new IndividualServiceParameter();
        parameter.setTypeId(getElementType());
        if (DatePage.ASSIGNEE_SELECTION_RELATION.equals(datePage.getAssigneeSelectionMode())) {
            parameter.setAssigneeRelationId(getAssigneeRelationId());
            parameter.setAssigneeRelationName(getAssigneeRelationName());
        } else {
            parameter.setAssignee(getAssigneeLoginName());
        }
        parameter.setTitle(getTaskTitle());
        parameter.setDescription(getDescription());
        parameter.setDueDate(getDueDate());
        parameter.setReminderPeriodDays(getPeriod());
        parameter.setProperties(getProperties());
        parameter.setPropertyNames(getPropertyNames());
        parameter.setWithAReleaseProcess(isWithAReleaseProcess());
        return parameter;
    }

    public void saveTemplate() {
        templatePage.saveTemplate(overwriteTemplate());
    }

    @Override
    public boolean performFinish() {

        return true;
    }

    public CnATreeElement getSelectedPerson() {
        return personPage.getSelectedPerson();
    }

    public String getAssigneeLoginName() {
        return personPage.getSelectedLogin();
    }

    public String getAssigneeRelationId() {
        return relationPage.getRelationId();
    }

    public String getAssigneeRelationName() {
        return relationPage.getRelationName();
    }

    public String getTaskTitle() {
        return descriptionPage.getTaskTitle();
    }

    public String getDescription() {
        return descriptionPage.getTaskDescription();
    }

    public Date getDueDate() {
        Date dueDate = null;
        LocalDate localDate = datePage.getDueDate();
        if (localDate != null) {
            ZonedDateTime zonedDateTime = localDate.atTime(LocalTime.now())
                    .atZone(ZoneId.systemDefault());
            dueDate = Date.from(zonedDateTime.toInstant());
        }
        return dueDate;
    }

    public Integer getPeriod() {
        return datePage.getPeriod();
    }

    public boolean overwriteTemplate() {
        return descriptionPage.isOverwriteTemplate();
    }

    public boolean isWithAReleaseProcess() {
        return descriptionPage.isWithAReleaseProcess();
    }

    public String getElementType() {
        return elementType;
    }

    public String getPersonTypeId() {
        return personTypeId;
    }

    public void setPersonTypeId(String personTypeId) {
        this.personTypeId = personTypeId;
    }

    public List<String> getUuids() {
        return uuids;
    }

    public Set<String> getProperties() {
        List<PropertyType> selectedTypes = propertyPage.getSelectedProperties();
        Set<String> typeIds = new HashSet<>(selectedTypes.size());
        for (PropertyType type : selectedTypes) {
            typeIds.add(type.getId());
        }
        return typeIds;
    }

    public Set<String> getPropertyNames() {
        List<PropertyType> selectedTypes = propertyPage.getSelectedProperties();
        Set<String> names = new HashSet<>(selectedTypes.size());
        for (PropertyType type : selectedTypes) {
            names.add(type.getName());
        }
        return names;
    }

    private CnATreeElement loadPersonForLogin(String login) {
        CnATreeElement person = null;
        try {
            LoadPersonForLogin command = new LoadPersonForLogin(login);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            person = command.getPerson();

        } catch (CommandException e) {
            LOG.error("Error while loading person.", e); //$NON-NLS-1$
        }
        return person;
    }

}
