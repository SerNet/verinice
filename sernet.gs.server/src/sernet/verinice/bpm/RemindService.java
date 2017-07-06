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
package sernet.verinice.bpm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bpm.Messages;
import sernet.verinice.model.bpm.MissingParameterException;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RemindService implements IRemindService {

    private static final Logger LOG = Logger.getLogger(RemindService.class);
    
    public static final String P_ANREDE = "person_anrede"; //$NON-NLS-1$
    public static final String P_NAME = "nachname"; //$NON-NLS-1$
    
    private static final String DEFAULT_ADDRESS = Messages.getString("NotificationJob.0"); //$NON-NLS-1$
    
    private static final String EMAIL_CC_PROPERTY = "${veriniceserver.notification.email.cc}";
    private static final String EMAIL_BCC_PROPERTY = "${veriniceserver.notification.email.bcc}";
    
    private JavaMailSender mailSender;   
    
    private VelocityEngine velocityEngine;
    
    private boolean enabled = false;
    
    private String emailFrom;
    
    private String replyTo;
    
    private String emailCc;
    
    private String emailBcc;
    
    private String url;
    
    private IBaseDao<CnATreeElement, Integer> elementDao;
    
    private IBaseDao<Configuration, Integer> configurationDao;

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IRemindService#sendEmail(java.util.Map)
     */
    @Override
    @SuppressWarnings("restriction")
    public void sendEmail(final Map<String,String> parameter, final boolean html) {
        if(!isEnabled()){
            LOG.info("Sending emails is disabled in the in the properties.(veriniceserver.notification.enabled in veriniceserver-plain.properties[.local])");
            return;
        }

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws MessagingException {
               parameter.put(TEMPLATE_URL, getUrl());
               MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
               message.setTo(parameter.get(TEMPLATE_EMAIL));         
               message.setFrom(getEmailFrom());
               if(getEmailCc()!=null) {
                   message.setCc(getEmailCc());
               }
               if(getEmailBcc()!=null) {
                   message.setBcc(getEmailBcc());
               }
               String replyTo0 = getReplyTo();
               if(replyTo0!=null && !replyTo0.isEmpty()) {
                   message.setReplyTo(replyTo0);
               }
               message.setSubject(parameter.get(TEMPLATE_SUBJECT)); //$NON-NLS-1$
               String text = VelocityEngineUtils.mergeTemplateIntoString(
                       getVelocityEngine(), 
                       parameter.get(TEMPLATE_PATH), 
                       VeriniceCharset.CHARSET_UTF_8.name(), 
                       parameter);
               message.setText(text, html);
            }
         };
         if (LOG.isDebugEnabled()) {
             LOG.debug("Sending email... parameter: ");
             for (String key : parameter.keySet()) {
                 LOG.debug( key + ": "+ parameter.get(key));
             }
         }
         
         getMailSender().send(preparator);
         
         if (LOG.isDebugEnabled()) {
             LOG.debug("Email was send successfully.");
             LOG.debug("Email send, parameter: ");
             for (String key : parameter.keySet()) {
                 LOG.debug( key + ": "+ parameter.get(key));
             }
             LOG.debug("CC: " + getEmailCc());
             LOG.debug("BCC: "+ getEmailBcc() );
         }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IRemindService#loadUserData(java.lang.String)
     */
    @Override
    public Map<String,String> loadUserData(String name) throws MissingParameterException {
        Map<String,String> model = new HashMap<String,String>();
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
            
            String escaped = name.replace("\\", "\\\\");
            Object[] params = new Object[]{Configuration.PROP_USERNAME,escaped,Configuration.PROP_NOTIFICATION_EMAIL};        
            List<Object[]> configurationList = getConfigurationDao().findByQuery(hql,params);
            Integer dbId = null;
            if (configurationList != null && configurationList.size() == 1) {
                String email = (String) configurationList.get(0)[1];
                if(email==null || email.trim().isEmpty()) {
                    throw new MissingParameterException("Email address of user " + name + " not set.");
                }
                model.put(TEMPLATE_EMAIL, email);
                dbId = (Integer) configurationList.get(0)[0];
                loadPerson(dbId, model);
            }
        }
        return model;
    }

    private void loadPerson(Integer dbId, Map<String,String> model) {
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
                String anrede = null;
                String name = null;
                if(element instanceof PersonIso) {
                    PersonIso person = (PersonIso) element;
                    name = person.getSurname();
                    anrede = person.getAnrede();              
                }
                if(element instanceof Person) {
                    Person person = (Person) element;
                    name = person.getEntity().getSimpleValue(P_NAME);
                    anrede = person.getEntity().getSimpleValue(P_ANREDE);                  
                }              
                if(anrede!=null && !anrede.isEmpty()) {
                    model.put(TEMPLATE_ADDRESS, anrede);
                } else {
                    model.put(TEMPLATE_ADDRESS, DEFAULT_ADDRESS);
                }
                if(name!=null ) {
                    model.put(TEMPLATE_NAME, name);
                } else {
                    model.put(TEMPLATE_NAME, "");
                }
            }
        }   
        
    }
    
    @Override
    public CnATreeElement retrieveElement(String uuid, RetrieveInfo ri) {
        return getElementDao().findByUuid(uuid, ri);
    }
    
    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public String getEmailCc() {
        return emailCc;
    }

    public void setEmailCc(String emailCc) {
        if(!EMAIL_CC_PROPERTY.equals(emailCc)) {
            this.emailCc = emailCc;
        }
    }

    public String getEmailBcc() {
        return emailBcc;
    }

    public void setEmailBcc(String emailBcc) {
        if(!EMAIL_BCC_PROPERTY.equals(emailBcc)) {
            this.emailBcc = emailBcc;
        }
    }

    public String getUrl() {
        return url;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }

    public IBaseDao<Configuration, Integer> getConfigurationDao() {
        return configurationDao;
    }

    public void setConfigurationDao(IBaseDao<Configuration, Integer> configurationDao) {
        this.configurationDao = configurationDao;
    }

}
