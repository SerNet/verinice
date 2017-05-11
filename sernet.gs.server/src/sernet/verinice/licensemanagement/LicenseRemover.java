/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/

package sernet.verinice.licensemanagement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import sernet.gs.server.security.DummyAuthenticationRunnable;
import sernet.gs.service.VeriniceCharset;
import sernet.verinice.bpm.NotificationJob;
import sernet.verinice.concurrency.CustomNamedThreadGroupFactory;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.licensemanagement.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.LicenseManagementException;

/**
 * Removes invalid licenses for users in tier3 mode and sends email
 * to user if configured
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseRemover {
    
    private static final Logger LOG = Logger.getLogger(LicenseRemover.class);
    
    private static final String DEFAULT_ADDRESS = Messages.getString(
            "LicenseRemover_Adress"); //$NON-NLS-1$

    IBaseDao<Configuration, Serializable> configurationDao;
    
    ILicenseManagementService licenseService;
    
    JavaMailSender mailSender;
    VelocityEngine velocityEngine;
    private String emailFrom;
    private String replyTo;
    private String emailCc;
    private String emailBcc;  
    
    // template path without lang code "_en" and file extension ".vm"
    public static final String TEMPLATE_BASE_PATH = "sernet/verinice/licensemanagement/LicenseInvalid"; //$NON-NLS-1$
    public static final String TEMPLATE_EXTENSION = ".vm"; //$NON-NLS-1$
    public static final String TEMPLATE_LICENSE = "license"; //$NON-NLS-1$
    public static final String TEMPLATE_EXPIRED_AT = "expirationdate"; //$NON-NLS-1$
    
    private static final String HQL = "from Configuration as conf " +
    "join fetch conf.entity as entity " +
    "join fetch entity.typedPropertyLists as propertyList " +
    "join fetch propertyList.properties as props ";
    
    Map<String, String> emailParam = new HashMap<>();
    
    private final class DummyAuthenticationRunnableExtension extends DummyAuthenticationRunnable {
        @Override
        public void doRun() {
            try {
                removeLicenses();
            } catch (Exception e) {
                LOG.error("Error while indexing elements.", e);
            }
        }
        
        /**
         * Loads all configurations in the database and perfoms validation-
         * check for all of them
         */
        private void removeLicenses(){
            List<Configuration> confs = (List)configurationDao.findByQuery(HQL, new Object[]{});
            for (Configuration configuration : confs){
                checkConfigurationForLicenseRemoval(configuration);
            }
        }
        
        /**
         * Checks all licenses for a given {@link Configuration} if still valid,
         * if not trigger removal of licenses and send email if needed
         * 
         * @param configuration
         */
        private void checkConfigurationForLicenseRemoval(Configuration configuration) {
            for (String licenseId : configuration.getAssignedLicenseIds()){
                checkLicenseIdForRemoval(configuration, licenseId);
            }
        }
        
        /**
         * Triggers removal check for a single pair of a given licenseId 
         * and {@link Configuration}
         * 
         * @param configuration
         * @param licenseId
         */
        private void checkLicenseIdForRemoval(Configuration configuration, String licenseId) {
            LicenseManagementEntry entry = null;
            try {
                entry = licenseService.getLicenseEntryForLicenseId(licenseId, true);
            } catch (LicenseManagementException e) {
                LOG.error("Error getting LicenseManagementEntry for"
                        + " licneseId:\t" + licenseId, e);
            }
            if (entry != null){
                validateByDate(configuration, licenseId, entry);
            }
        }
        
        /**
         * Checks if a {@link LicenseManagementEntry} is invalid by time and 
         * triggers removal of license and sending of email, if needed
         * 
         * @param configuration
         * @param licenseId
         * @param entry
         */
        private void validateByDate(Configuration configuration, String licenseId, LicenseManagementEntry entry) {
            LocalDate validUntil =
                    licenseService.decrypt(entry, 
                            LicenseManagementEntry.COLUMN_VALIDUNTIL);
            LocalDate currentDate = LocalDate.now();
            if (currentDate.isAfter(validUntil)){
                removeLicenseFromConfigurationAndSendEmail(configuration, licenseId, entry, validUntil);

            }
        }
        
        /**
         * Removes given licenseId from given Configuration and
         * sends email to user, if configured
         * 
         * @param configuration
         * @param licenseId
         * @param entry
         * @param validUntil
         */
        private void removeLicenseFromConfigurationAndSendEmail(Configuration configuration, String licenseId, LicenseManagementEntry entry, LocalDate validUntil) {
            configuration.removeLicensedContentId(licenseId);
            configurationDao.saveOrUpdate(configuration);

            if (configuration.getNotificationLicense()){
                MimeMessagePreparator preparator 
                = prepareEmail(configuration, 
                        licenseId, 
                        entry, 
                        validUntil);

                getMailSender().send(preparator);
            }
        }
        
        /**
         * @param configuration
         * @param licenseId
         * @param entry
         * @param validUntil
         * @return
         */
        private MimeMessagePreparator prepareEmail(Configuration configuration,
                String licenseId, LicenseManagementEntry entry,
                LocalDate validUntil) {
            loadPerson(configuration.getDbId());
            emailParam.put(NotificationJob.TEMPLATE_EMAIL, configuration.getNotificationEmail());
            emailParam.put(NotificationJob.TEMPLATE_EMAIL_FROM, getEmailFrom());
            emailParam.put(NotificationJob.TEMPLATE_REPLY_TO, getReplyTo());
            emailParam.put(NotificationJob.TEMPLATE_URL, getTemplatePath());
            String contentId = licenseService.decrypt(entry, LicenseManagementEntry.COLUMN_CONTENTID);
            
            emailParam.put("license", licenseId);
            emailParam.put("contentId", contentId);
            emailParam.put("expirationdate", validUntil.format(DateTimeFormatter.ISO_LOCAL_DATE));

            final String subject = Messages.getString("LicenseRemover_Subject");
            
            return new MimeMessagePreparator() {
                
                @Override
                public void prepare(MimeMessage mimeMessage) throws MessagingException {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                    setMessageProperties(subject, message);
                }


            };
        }

        /**
         * Sets the properties for sending an email on basis of a 
         * {@link MimeMessageHelper}
         * 
         * @param subject
         * @param message
         * @throws MessagingException
         */
        private void setMessageProperties(final String subject, MimeMessageHelper message) throws MessagingException {
            message.setTo(getEmail());
            message.setFrom(getEmailFrom());
            if (getReplyTo() != null && !getReplyTo().isEmpty()) {
                message.setReplyTo(getReplyTo());
            }
            message.setSubject(subject); //$NON-NLS-1$
            String text = VelocityEngineUtils.mergeTemplateIntoString(
                    getVelocityEngine(), getTemplatePath(), 
                    VeriniceCharset.CHARSET_UTF_8.name(), emailParam);
            message.setText(text, false);
            if (getEmailCc() != null) {
                message.setCc(getEmailCc());
            }
            if (getEmailBcc() != null) {
                message.setBcc(getEmailBcc());
            }
        }
    }
    

    
    public void runNonBlocking(){
        runRemoverThread();
    }
    
    private void runRemoverThread() {
        DummyAuthenticationRunnable dummyAuthenticationRunnable = 
                new DummyAuthenticationRunnableExtension();
        ThreadFactory threadFactory = new CustomNamedThreadGroupFactory("licenseRemover");
        ExecutorService exeService = Executors.newSingleThreadExecutor(threadFactory);
        exeService.execute(dummyAuthenticationRunnable);
        exeService.shutdown();
    }
    









    
    public String getEmail() {
        return (emailParam != null) ? emailParam.get(
                NotificationJob.TEMPLATE_EMAIL) : null;
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
        if (this.getClass().getClassLoader().getResource(path) == null) {
            path = TEMPLATE_BASE_PATH + TEMPLATE_EXTENSION;
        }
        return path;
    }
    
    /**
     * @param dbId
     * @param result
     */
    private void loadPerson(Integer dbId) {
        if (dbId != null) {
            String hql = "from Configuration as conf " + //$NON-NLS-1$
            "inner join fetch conf.person as person " + //$NON-NLS-1$
            "inner join fetch person.entity as entity " + //$NON-NLS-1$
            "inner join fetch entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
            "inner join fetch propertyList.properties as props " + //$NON-NLS-1$
            "where conf.dbId = ? "; //$NON-NLS-1$
            
            Object[] params = new Object[]{dbId};        
            List<Configuration> configurationList = getConfigurationDao().findByQuery(hql,params);
            for (Configuration configuration : configurationList) {
                prepareEmailViaConfiguration(configuration);
            }
        }   
        
    }

    /**
     * Retrieves person-Object of {@link Configuration} and extracts
     * values needed for sending mail out of person object
     * to set it to global attribute emailParam
     * 
     * @param configuration
     */
    private void prepareEmailViaConfiguration(Configuration configuration) {
        CnATreeElement element = configuration.getPerson();
        if (element instanceof PersonIso) {
            preparePersonIso(element);
        // handling for bsi persons
        } else if (element instanceof Person){
            prepareGSPerson(element);
        }
    }

    /**
     * Extracts properties for sending email from a ITGS-Person 
     * {@link Person}
     * 
     * @param element
     */
    private void prepareGSPerson(CnATreeElement element) {
        Person person = (Person) element;
        String nachname = person.getEntity() != null ? 
                (person.getEntity().getSimpleValue("nachname") != null
                ? person.getEntity().getSimpleValue("nachname") 
                        : "Kein Name") : "Kein Name";
        emailParam.put(NotificationJob.TEMPLATE_NAME, nachname);
        String anrede = person.getEntity() != null 
                ? (person.getEntity().getSimpleValue(
                        "person_anrede") != null 
                        ? person.getEntity().getSimpleValue(
                                "person_anrede") : DEFAULT_ADDRESS) 
                : DEFAULT_ADDRESS;
        emailParam.put(NotificationJob.TEMPLATE_ADDRESS, anrede);
    }

    /**
     * Extracts properties for sending email from a {@link PersonIso} 
     * {@link CnATreeElement}
     * @param element
     */
    private void preparePersonIso(CnATreeElement element) {
        PersonIso person = (PersonIso) element;
        emailParam.put(NotificationJob.TEMPLATE_NAME, person.getSurname());
        String anrede = person.getAnrede();
        if (anrede != null && !anrede.isEmpty()) {
            emailParam.put(NotificationJob.TEMPLATE_ADDRESS, person.getAnrede());
        } else {
            emailParam.put(NotificationJob.TEMPLATE_ADDRESS, DEFAULT_ADDRESS);
        }
    }

    /**
     * @return the configurationDao
     */
    public IBaseDao<Configuration, Serializable> getConfigurationDao() {
        return configurationDao;
    }

    /**
     * @param configurationDao the configurationDao to set
     */
    public void setConfigurationDao(IBaseDao<Configuration, Serializable> configurationDao) {
        this.configurationDao = configurationDao;
    }

    /**
     * @return the licenseService
     */
    public ILicenseManagementService getLicenseService() {
        return licenseService;
    }

    /**
     * @param licenseService the licenseService to set
     */
    public void setLicenseService(ILicenseManagementService licenseService) {
        this.licenseService = licenseService;
    }

    /**
     * @return the mailSender
     */
    public JavaMailSender getMailSender() {
        return mailSender;
    }

    /**
     * @param mailSender the mailSender to set
     */
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * @return the velocityEngine
     */
    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    /**
     * @param velocityEngine the velocityEngine to set
     */
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    /**
     * @return the emailFrom
     */
    public String getEmailFrom() {
        return emailFrom;
    }

    /**
     * @param emailFrom the emailFrom to set
     */
    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    /**
     * @return the replyTo
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * @param replyTo the replyTo to set
     */
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * @return the emailCc
     */
    public String getEmailCc() {
        return emailCc;
    }

    /**
     * @param emailCc the emailCc to set
     */
    public void setEmailCc(String emailCc) {
        this.emailCc = emailCc;
    }

    /**
     * @return the emailBcc
     */
    public String getEmailBcc() {
        return emailBcc;
    }

    /**
     * @param emailBcc the emailBcc to set
     */
    public void setEmailBcc(String emailBcc) {
        this.emailBcc = emailBcc;
    }

    /**
     * @return the emailParam
     */
    public Map<String, String> getEmailParam() {
        return emailParam;
    }

    /**
     * @param emailParam the emailParam to set
     */
    public void setEmailParam(Map<String, String> emailParam) {
        this.emailParam = emailParam;
    }

}
