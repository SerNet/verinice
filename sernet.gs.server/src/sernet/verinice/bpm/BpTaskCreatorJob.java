/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.bpm;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import sernet.gs.server.security.DummyAuthentication;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.IIndividualService;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.interfaces.bpm.IndividualServiceParameter;
import sernet.verinice.model.bp.DeductionImplementationUtil;
import sernet.verinice.model.bp.ImplementationStatus;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;

public class BpTaskCreatorJob extends QuartzJobBean implements StatefulJob {

    private static final Logger LOG = Logger.getLogger(BpTaskCreatorJob.class);

    private boolean enabled = false;

    private IBaseDao<CnATreeElement, Integer> elementDao;
    private IBaseDao<Configuration, Integer> configurationDao;
    private ITaskService taskService;
    private IIndividualService individualService;

    private int implementationThresholdDays;
    private int implementationDueDateDays;
    private String implementationTaskTitle;
    private String implementationTaskDescription;
    private boolean implementationTaskWithReleaseProcess;
    private int implementationReminderPeriodDays;
    private Set<String> implementationFieldNamesRequirement;
    private Set<String> implementationFieldNamesSafeguard;

    private int nextRevisionThresholdDays;
    private int nextRevisionDueDateDays;
    private String nextRevisionTaskTitle;
    private String nextRevisionTaskDescription;
    private boolean nextRevisionTaskWithReleaseProcess;
    private int nextRevisionReminderPeriodDays;
    private Set<String> nextRevisionFieldNamesRequirement;
    private Set<String> nextRevisionFieldNamesSafeguard;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (!enabled) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("BP task creator is disabled.");
            }
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Starting BP task creation...");
        }

        SecurityContext ctx = null;
        boolean dummyAuthAdded = false;
        try {
            ServerInitializer.inheritVeriniceContextState();
            ctx = SecurityContextHolder.getContext();
            dummyAuthAdded = addSecurityContext(ctx);

            elementDao.executeCallback(new CreateTasks());

        } finally {
            if (dummyAuthAdded) {
                removeSecurityContext(ctx);
            }
        }
    }

    private static boolean addSecurityContext(SecurityContext ctx) {
        boolean dummyAuthAdded = false;
        if (ctx.getAuthentication() == null) {
            DummyAuthentication authentication = new DummyAuthentication();
            ctx.setAuthentication(authentication);
            dummyAuthAdded = true;
        }
        return dummyAuthAdded;
    }

    private static void removeSecurityContext(SecurityContext ctx) {
        if (ctx != null) {
            ctx.setAuthentication(null);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }

    public void setTaskService(ITaskService taskService) {
        this.taskService = taskService;
    }

    public void setIndividualService(IIndividualService individualService) {
        this.individualService = individualService;
    }

    public void setConfigurationDao(IBaseDao<Configuration, Integer> configurationDao) {
        this.configurationDao = configurationDao;
    }

    public void setImplementationThresholdDays(int implementationThresholdDays) {
        this.implementationThresholdDays = implementationThresholdDays;
    }

    public void setImplementationDueDateDays(int implementationDueDateDays) {
        this.implementationDueDateDays = implementationDueDateDays;
    }

    public void setImplementationTaskTitle(String implementationTaskTitle) {
        this.implementationTaskTitle = implementationTaskTitle;
    }

    public void setImplementationTaskDescription(String implementationTaskDescription) {
        this.implementationTaskDescription = implementationTaskDescription;
    }

    public void setImplementationReminderPeriodDays(int implementationReminderPeriodDays) {
        this.implementationReminderPeriodDays = implementationReminderPeriodDays;
    }

    public void setImplementationFieldNamesRequirement(String implementationFieldNamesRequirement) {
        this.implementationFieldNamesRequirement = Set
                .of(implementationFieldNamesRequirement.split(", *"));
    }

    public void setImplementationFieldNamesSafeguard(String implementationFieldNamesSafeguard) {
        this.implementationFieldNamesSafeguard = Set
                .of(implementationFieldNamesSafeguard.split(", *"));
    }

    public void setImplementationTaskWithReleaseProcess(
            boolean implementationTaskWithReleaseProcess) {
        this.implementationTaskWithReleaseProcess = implementationTaskWithReleaseProcess;
    }

    public void setNextRevisionThresholdDays(int nextRevisionThresholdDays) {
        this.nextRevisionThresholdDays = nextRevisionThresholdDays;
    }

    public void setNextRevisionDueDateDays(int nextRevisionDueDateDays) {
        this.nextRevisionDueDateDays = nextRevisionDueDateDays;
    }

    public void setNextRevisionTaskTitle(String nextRevisionTaskTitle) {
        this.nextRevisionTaskTitle = nextRevisionTaskTitle;
    }

    public void setNextRevisionTaskDescription(String nextRevisionTaskDescription) {
        this.nextRevisionTaskDescription = nextRevisionTaskDescription;
    }

    public void setNextRevisionTaskWithReleaseProcess(boolean nextRevisionTaskWithReleaseProcess) {
        this.nextRevisionTaskWithReleaseProcess = nextRevisionTaskWithReleaseProcess;
    }

    public void setNextRevisionReminderPeriodDays(int nextRevisionReminderPeriodDays) {
        this.nextRevisionReminderPeriodDays = nextRevisionReminderPeriodDays;
    }

    public void setNextRevisionFieldNamesRequirement(String nextRevisionFieldNamesRequirement) {
        this.nextRevisionFieldNamesRequirement = Set
                .of(nextRevisionFieldNamesRequirement.split(", *"));
    }

    public void setNextRevisionFieldNamesSafeguard(String nextRevisionFieldNamesSafeguard) {
        this.nextRevisionFieldNamesSafeguard = Set.of(nextRevisionFieldNamesSafeguard.split(", *"));
    }

    private final class CreateTasks implements HibernateCallback {

        private final List<ITask> existingTasks;
        private final Map<Integer, String> loginsByPersonId;
        private final Set<Integer> handledSafeguardIds;

        CreateTasks() {
            ITaskParameter searchParameter = new TaskParameter();
            searchParameter.setProcessKey(IIndividualProcess.KEY);
            searchParameter.setAllUser(true);
            existingTasks = taskService.getTaskList(searchParameter);
            loginsByPersonId = loadAccountConfiguration();
            handledSafeguardIds = new HashSet<>();
        }

        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            List<@NonNull BpRequirement> requirements = loadRequirements(session);
            if (LOG.isInfoEnabled()) {
                LOG.info("Found " + requirements.size() + " requirements");
            }

            for (BpRequirement requirement : requirements) {
                processRequirement(requirement);
            }
            return null;
        }

        private void processRequirement(BpRequirement requirement) {

            if (requirement.isDeductionOfImplementation()) {
                Set<CnATreeElement> safeguards = requirement.getLinksDown().stream().filter(
                        DeductionImplementationUtil::isRelevantLinkForImplementationStateDeduction)
                        .map(CnALink::getDependency).collect(Collectors.toSet());
                if (safeguards.isEmpty()) {
                    LOG.warn(
                            "Found requirement with deduction enabled but without linked safeguards: "
                                    + requirement);
                } else {
                    for (CnATreeElement safeguard : safeguards) {
                        if (handledSafeguardIds.add(safeguard.getDbId())) {
                            new ElementTaskCreator(safeguard, true, existingTasks, loginsByPersonId)
                                    .run();
                        }
                    }
                }
            } else {
                new ElementTaskCreator(requirement, false, existingTasks, loginsByPersonId).run();
            }
        }

        private Map<Integer, String> loadAccountConfiguration() {
            @SuppressWarnings("unchecked")
            List<@NonNull Configuration> configurations = configurationDao
                    .findByCriteria(DetachedCriteria.forClass(Configuration.class)
                            .setFetchMode("entity", FetchMode.JOIN)
                            .setFetchMode("entity.typedPropertyLists", FetchMode.JOIN)
                            .setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN));
            return configurations.stream().collect(
                    Collectors.toMap(c -> c.getPerson().getDbId(), Configuration::getUser));
        }

        private List<@NonNull BpRequirement> loadRequirements(Session session) {
            @SuppressWarnings("unchecked")
            List<Integer> catalogScopeDBIds = session.createCriteria(ItNetwork.class)
                    .createAlias("parent", "parent")
                    .add(Restrictions.eq("parent.objectType", CatalogModel.TYPE_ID))
                    .setProjection(Projections.property("dbId")).list();

            Criteria criteria = session.createCriteria(BpRequirement.class)
                    .setFetchMode("entity", FetchMode.JOIN)
                    .setFetchMode("entity.typedPropertyLists", FetchMode.JOIN)
                    .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

            if (!catalogScopeDBIds.isEmpty()) {
                criteria.add(Restrictions.not(Restrictions.in("scopeId", catalogScopeDBIds)));
            }

            @SuppressWarnings("unchecked")
            List<@NonNull BpRequirement> requirements = criteria.list();
            return requirements;
        }

    }

    private final class ElementTaskCreator implements Runnable {

        private final CnATreeElement element;
        private final boolean isSafeguard;
        private final Entity entity;
        private final ImplementationStatus implementationStatus;
        private final Set<ITask> existingTasksForElement;
        private final Map<Integer, String> loginsByPersonId;

        public ElementTaskCreator(CnATreeElement element, boolean isSafeguard,
                List<ITask> existingTasks, Map<Integer, String> loginsByPersonId) {
            this.element = element;
            entity = element.getEntity();

            this.isSafeguard = isSafeguard;
            this.existingTasksForElement = existingTasks.stream()
                    .filter(task -> element.getUuid().equals(task.getUuid()))
                    .collect(Collectors.toSet());
            this.loginsByPersonId = loginsByPersonId;
            if (isSafeguard) {
                implementationStatus = Safeguard.getImplementationStatus(
                        entity.getRawPropertyValue(Safeguard.PROP_IMPLEMENTATION_STATUS));
            } else {
                implementationStatus = BpRequirement.getImplementationStatus(
                        entity.getRawPropertyValue(BpRequirement.PROP_IMPLEMENTATION_STATUS));
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("implementationStatus: " + implementationStatus);
            }
        }

        @Override
        public void run() {
            LOG.info("Handle element " + element);
            Set<CnATreeElement> responsiblePersons = element.getLinksUp().stream()
                    .filter(link -> link.getRelationId()
                            .equals(isSafeguard ? "rel_bp_person_bp_safeguard_general"
                                    : "rel_bp_person_bp_requirement_general"))
                    .map(CnALink::getDependant).collect(Collectors.toSet());

            if (responsiblePersons.isEmpty()) {
                LOG.warn("No responsible persons linked with " + element);
            } else {
                handleElement(element, responsiblePersons);
            }
        }

        private void handleElement(CnATreeElement element, Set<CnATreeElement> responsiblePersons) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("handleElement " + element + ", login = " + responsiblePersons);
            }

            if (implementationStatus == null) {
                createTaskIfNotExists(element, responsiblePersons,
                        newImplementationTaskParameters());
            } else if (implementationStatus == ImplementationStatus.NO
                    || implementationStatus == ImplementationStatus.PARTIALLY) {
                Date implementationByDate = entity
                        .getDate(isSafeguard ? Safeguard.PROP_IMPLEMENTATION_BY_DATE
                                : BpRequirement.PROP_IMPLEMENTATION_BY_DATE);
                long daysUntilImplementationBy = getDaysUntil(implementationByDate);
                if (daysUntilImplementationBy < implementationThresholdDays) {
                    createTaskIfNotExists(element, responsiblePersons,
                            newImplementationTaskParameters());
                }
            } else if (implementationStatus == ImplementationStatus.YES
                    || implementationStatus == ImplementationStatus.NOT_APPLICABLE) {
                Date nextRevision = entity.getDate(isSafeguard ? Safeguard.PROP_NEXT_REVISION_DATE
                        : BpRequirement.PROP_NEXT_REVISION_DATE);
                long daysUntilNextRevision = getDaysUntil(nextRevision);
                if (daysUntilNextRevision < nextRevisionThresholdDays) {
                    createTaskIfNotExists(element, responsiblePersons,
                            newNextRevisionTaskParameters());
                }
            } else {
                LOG.error("Invalid implementation status value " + implementationStatus + " for "
                        + element);
            }
        }

        private TaskParameters newImplementationTaskParameters() {
            return new TaskParameters(implementationTaskTitle, implementationTaskDescription,
                    implementationDueDateDays, implementationReminderPeriodDays,
                    isSafeguard ? implementationFieldNamesSafeguard
                            : implementationFieldNamesRequirement,
                    implementationTaskWithReleaseProcess);
        }

        private TaskParameters newNextRevisionTaskParameters() {
            return new TaskParameters(nextRevisionTaskTitle, nextRevisionTaskDescription,
                    nextRevisionDueDateDays, nextRevisionReminderPeriodDays,
                    isSafeguard ? nextRevisionFieldNamesSafeguard
                            : nextRevisionFieldNamesRequirement,
                    nextRevisionTaskWithReleaseProcess);
        }

        private void createTaskIfNotExists(CnATreeElement element,
                Set<CnATreeElement> responsiblePersons, TaskParameters taskParameters) {
            for (CnATreeElement person : responsiblePersons) {
                String login = loginsByPersonId.get(person.getDbId());
                if (login == null) {
                    LOG.warn("No user account found for person " + person + " linked with "
                            + element);
                } else {
                    boolean taskExists = checkForExistingTask(login, taskParameters.title);

                    if (!taskExists) {
                        createTask(element, taskParameters, login);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("task exists");
                        }
                    }
                }
            }
        }

        private boolean checkForExistingTask(String login, String taskTitle) {
            return existingTasksForElement.stream().anyMatch(
                    t -> taskTitle.equals(t.getName()) && Optional.ofNullable(t.getAssignee())
                            .map(it -> it.endsWith(" [" + login + "]")).orElse(false));
        }

        private void createTask(CnATreeElement element, TaskParameters taskParameters,
                String login) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Create task " + implementationTaskTitle + " for " + element
                        + ", assignee: " + login);
            }
            IndividualServiceParameter parameter = new IndividualServiceParameter();
            parameter.setUuid(element.getUuid());
            parameter.setTypeId(element.getTypeId());
            parameter.setAssignee(login);
            parameter.setTitle(taskParameters.title);
            parameter.setDescription(taskParameters.description);
            parameter.setDueDate(Date
                    .from(ZonedDateTime.now().plusDays(taskParameters.dueDateDays).toInstant()));
            parameter.setReminderPeriodDays(taskParameters.reminderPeriodDays);
            parameter.setProperties(taskParameters.properties);
            parameter.setWithAReleaseProcess(taskParameters.withReleaseProcess);
            individualService.startProcess(parameter);
        }

        private long getDaysUntil(Date implementationByDate) {
            return ZonedDateTime.now().until(
                    implementationByDate.toInstant().atZone(ZoneId.systemDefault()),
                    ChronoUnit.DAYS);
        }

    }

    private static class TaskParameters {
        public final String title;
        public final String description;
        public final int dueDateDays;
        public final int reminderPeriodDays;
        public final Set<String> properties;
        public final boolean withReleaseProcess;

        TaskParameters(String title, String description, int dueDateDays, int reminderPeriodDays,
                Set<String> properties, boolean withReleaseProcess) {
            this.title = title;
            this.description = description;
            this.dueDateDays = dueDateDays;
            this.reminderPeriodDays = reminderPeriodDays;
            this.properties = properties;
            this.withReleaseProcess = withReleaseProcess;
        }
    }
}