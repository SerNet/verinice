/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.bpm;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.osgi.util.NLS;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.ui.velocity.VelocityEngineUtils;

import sernet.gs.server.security.DummyAuthentication;
import sernet.gs.service.VeriniceCharset;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.hibernate.HibernateDao;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.Messages;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * Quartz job configured by Spring in veriniceserver-jbpm.xml, veriniceserver-plain.xml
 * and veriniceserver-plain.properties
 * 
 * NotificationJob send one email to a user if user is newly assigned to a task.
 * Emails are created by velocity template engine (http://velocity.apache.org)
 * Template files: sernet/verinice/bpm/TaskNotification[_LANG].vm
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class NotificationJob extends QuartzJobBean implements StatefulJob {

    private static final String DEFAULT_ADDRESS = Messages.getString("NotificationJob.0"); //$NON-NLS-1$
    
    public static final String TEMPLATE_NUMBER = "n";    //$NON-NLS-1$
    public static final String TEMPLATE_URL = "url";    //$NON-NLS-1$
    public static final String TEMPLATE_EMAIL = "email"; //$NON-NLS-1$
    public static final String TEMPLATE_EMAIL_FROM = "emailFrom"; //$NON-NLS-1$
    public static final String TEMPLATE_REPLY_TO = "replyTo"; //$NON-NLS-1$
    public static final String TEMPLATE_NAME = "name"; //$NON-NLS-1$
    public static final String TEMPLATE_ADDRESS = "address"; //$NON-NLS-1$
    
    // template path without lang code "_en" and file extension ".vm"
    public static final String TEMPLATE_BASE_PATH = "sernet/verinice/bpm/TaskNotification"; //$NON-NLS-1$
    public static final String TEMPLATE_EXTENSION = ".vm"; //$NON-NLS-1$
    
    private final Logger log = Logger.getLogger(NotificationJob.class);

    private static VeriniceContext.State state;

    private HibernateDao<TaskImpl, Long> jbpmTaskDao;

    private IBaseDao<Configuration, Integer> configurationDao;

    private ITaskService taskService;

    private JavaMailSender mailSender;   
    
    private VelocityEngine velocityEngine;
    
    private boolean enabled = false;
    
    private String emailFrom;
    
    private String replyTo;
    
    private String taskListPath;
    
    private String url;

    private Map<String,String> model = new HashMap<String,String>();
    
    // NotificationJob can not do a real login
    // authentication is a fake instance to run secured commands and dao actions
    // without a login
    private DummyAuthentication authentication = new DummyAuthentication(); 
    
    /*
     * Send notification if task notification is enabled.
     * 
     * Toggle enablement: veriniceserver-plain.properties
     * property veriniceserver.notification.enabled
     * 
     * @see
     * org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org
     * .quartz.JobExecutionContext)
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if(isEnabled()) {
            doExecute(context);
        }
    }
    
    /**
     * Send email to a user if user is newly assigned to a task
     * 
     * @param context Quarzt context
     */
    private void doExecute(JobExecutionContext context) {
        VeriniceContext.setState(NotificationJob.state);

        // NotificationJob can not do a real login
        // authentication is a fake instance to run secured commands and dao actions
        // without a login
        boolean dummyAuthAdded = false;
        SecurityContext ctx = SecurityContextHolder.getContext(); 
        try {                    
            if(ctx.getAuthentication()==null) {
                ctx.setAuthentication(authentication);
                dummyAuthAdded = true;
            }    
            
            sendNotification(context);  
            
        } finally {
            if(dummyAuthAdded) {
                ctx.setAuthentication(null);
            }
        }
    
    }

    private void sendNotification(JobExecutionContext context) {
        // search for user names
        List<String> nameList = getUserList(context);
        for (String name : nameList) {
            ITaskParameter param = new TaskParameter(name);
            if(context.getPreviousFireTime()!=null) {
                // select all tasks created since last timer execution
                param.setSince(context.getPreviousFireTime());
            } else {
                // first execution since java vm starts, select all tasks created since 24 hours
                Calendar yesterday = Calendar.getInstance();
                yesterday.add(Calendar.DATE, -1);
                param.setSince(yesterday.getTime());
            }
            final List<ITask> taskList = getTaskService().getTaskList(param);
            loadUserData(name);
            if(getEmail()!=null) {         
                if (log.isDebugEnabled()) {
                    log.debug("User/Email: " + name + "/" + getEmail() + ", number of tasks: " + taskList.size()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                model.put(TEMPLATE_NUMBER,String.valueOf(taskList.size()));
                if(getUrl()!=null && !getUrl().isEmpty()) {
                    model.put(TEMPLATE_URL,getUrl());
                } else {
                    model.put(TEMPLATE_URL,VeriniceContext.getServerUrl() + getTaskListPath());
                }
                model.put(TEMPLATE_EMAIL_FROM,getEmailFrom());
                model.put(TEMPLATE_REPLY_TO,getReplyTo());
                MimeMessagePreparator preparator = new MimeMessagePreparator() {
                    
                    public void prepare(MimeMessage mimeMessage) throws MessagingException {
                       MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                       message.setTo(getEmail());
                       message.setFrom(getEmailFrom());
                       if(getReplyTo()!=null && !getReplyTo().isEmpty()) {
                           message.setReplyTo(getReplyTo());
                       }
                       message.setSubject(NLS.bind(Messages.getString("NotificationJob.1"), new Object[] {taskList.size()})); //$NON-NLS-1$
                       String text = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), getTemplatePath(), VeriniceCharset.CHARSET_UTF_8.name(), model);
                       message.setText(text, false);
                    }
             
                 };
                 this.mailSender.send(preparator);
            }
        }
    }

    /**
     * Returns the bundle/jar relative path to the velocity email template.
     * First a localized template is search by the default locale of the java vm.
     * If localized template is not found default/english template is returned.
     * 
     * Localized template path: <TEMPLATE_BASE_PATH>_<LANG_CODE>.vm
     * Default template path: <TEMPLATE_BASE_PATH>.vm
     * 
     * @return bundle/jar relative path to the velocity email template
     */
    protected String getTemplatePath() {      
        String langCode = Locale.getDefault().getLanguage();
        String path = TEMPLATE_BASE_PATH + "_" + langCode + TEMPLATE_EXTENSION; //$NON-NLS-1$
        if(this.getClass().getClassLoader().getResource(path)==null) {
            path = TEMPLATE_BASE_PATH + TEMPLATE_EXTENSION;
        }
        return path;
    }

    /**
     * Returns a list with user login names of users who are assigned to a task
     * since last timer execution or since 24 hours if timer is executed the first time.
     * 
     * @param Quartz context
     * @return a list with user login names
     */
    private List<String> getUserList(JobExecutionContext context) {
        StringBuilder sb = new StringBuilder("select distinct task.assignee from org.jbpm.pvm.internal.task.TaskImpl task where createTime >= ?"); //$NON-NLS-1$
        Object[] params = null;
        if(context.getPreviousFireTime()!=null) {
            // select all tasks created since last execution
            params = new Object[] {context.getPreviousFireTime()};
        } else {
            // first execution since java vm starts, select all tasks created since 24 hours
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DATE, -1);
            params = new Object[]{yesterday.getTime()};
        }
        final String hql = sb.toString();
        List nameList = getJbpmTaskDao().findByQuery(hql, params);
        return nameList;
    }

    private void loadUserData(String name) {
        if (name != null) {
            String hql = "select conf.dbId,emailprops.propertyValue from Configuration as conf " + //$NON-NLS-1$
            "inner join conf.entity as entity " + //$NON-NLS-1$
            "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
            "inner join propertyList.properties as props " + //$NON-NLS-1$
            "inner join conf.entity as entity2 " + //$NON-NLS-1$
            "inner join entity2.typedPropertyLists as propertyList2 " + //$NON-NLS-1$
            "inner join propertyList2.properties as emailprops " + //$NON-NLS-1$
            "where props.propertyType = ? " + //$NON-NLS-1$
            "and props.propertyValue like ? " + //$NON-NLS-1$
            "and emailprops.propertyType = ?";          //$NON-NLS-1$
            
            Object[] params = new Object[]{Configuration.PROP_USERNAME,name,Configuration.PROP_NOTIFICATION_EMAIL};        
            List<Object[]> configurationList = getConfigurationDao().findByQuery(hql,params);
            Integer dbId = null;
            if (configurationList != null && configurationList.size() == 1) {
                model.put(TEMPLATE_EMAIL, (String) configurationList.get(0)[1]);
                dbId = (Integer) configurationList.get(0)[0];
                loadPerson(dbId);
            }
        }
    }

    /**
     * @param dbId
     * @param result
     */
    private void loadPerson(Integer dbId) {
        if(dbId!=null) {
            String hql = "from Configuration as conf " + //$NON-NLS-1$
            "inner join fetch conf.person as person " + //$NON-NLS-1$
            "inner join fetch person.entity as entity " + //$NON-NLS-1$
            "inner join fetch entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
            "inner join fetch propertyList.properties as props " + //$NON-NLS-1$
            "where conf.dbId = ? "; //$NON-NLS-1$
            
            Object[] params = new Object[]{dbId};        
            List<Configuration> configurationList = getConfigurationDao().findByQuery(hql,params);
            for (Configuration configuration : configurationList) {
                CnATreeElement element = configuration.getPerson();
                if(element instanceof PersonIso) {
                    PersonIso person = (PersonIso) element;
                    model.put(TEMPLATE_NAME, person.getSurname());
                    String anrede = person.getAnrede();
                    if(anrede!=null && !anrede.isEmpty()) {
                        model.put(TEMPLATE_ADDRESS, person.getAnrede());
                    } else {
                        model.put(TEMPLATE_ADDRESS, DEFAULT_ADDRESS);
                    }
                }
            }
        }   
        
    }
    
    public String getEmail() {
        return (model!=null) ? model.get(TEMPLATE_EMAIL) : null;
    }

    public void setWorkObjects(VeriniceContext.State workObjects) {
        NotificationJob.state = workObjects;
    }

    public VeriniceContext.State getWorkObjects() {
        return NotificationJob.state;
    }

    public HibernateDao<TaskImpl, Long> getJbpmTaskDao() {
        return jbpmTaskDao;
    }

    public void setJbpmTaskDao(HibernateDao<TaskImpl, Long> jbpmTaskDao) {
        this.jbpmTaskDao = jbpmTaskDao;
    }

    public IBaseDao<Configuration, Integer> getConfigurationDao() {
        return configurationDao;
    }

    public void setConfigurationDao(IBaseDao<Configuration, Integer> configurationDao) {
        this.configurationDao = configurationDao;
    }

    public ITaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(ITaskService taskService) {
        this.taskService = taskService;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getTaskListPath() {
        return taskListPath;
    }

    public void setTaskListPath(String taskListPath) {
        this.taskListPath = taskListPath;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}