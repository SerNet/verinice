/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.log4j.Logger;
import org.eclipse.osgi.util.NLS;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import sernet.gs.server.commands.NotificationInfo;
import sernet.gs.server.commands.PrepareNotificationInfo;
import sernet.gs.server.security.DummyAuthentication;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * A {@link MailJob} instance is a job that is to run once per day.
 * 
 * <p>An instance of this class is created in the Spring configuration.</p>
 * 
 * <p>The class runs the command that prepares all the neccessary information
 * to prepare the notification mails, then iterates through the results and generates
 * the individual messages and sends them.</p>
 * 
 * <p>To adjust the content of the notification mail modify the strings in the
 * <code>mailmessages.properties</code> file.</p>
 *  
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
public class MailJob extends QuartzJobBean implements StatefulJob {
	
	private static final Logger LOG = Logger.getLogger(MailJob.class);

	private boolean notificationEnabled;
	
	private PrepareNotificationInfo pniCommand;
	
	private JavaMailSender mailSender;
	
	private ICommandService commandService;
	
	private String notificationEmailFrom;
	
	private String notificationEmailReplyTo;

	private String notificationEmailLinkTo;
	
	// NotificationJob can not do a real login
    // authentication is a fake instance to run secured commands and dao actions
    // without a login
    private DummyAuthentication authentication = new DummyAuthentication();
	
	/**
     * @param notificationEmailLinkTo the notificationEmailLinkTo to set
     */
    public void setNotificationEmailLinkTo(String notificationEmailLinkTo) {
        this.notificationEmailLinkTo = notificationEmailLinkTo;
    }

    private DateFormat notificationEmailDateFormat;

	@Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
		
		// Do nothing if the notification feature is deactivated in the configuration.
		if (!notificationEnabled){
			return;
		}
		// NotificationJob can not do a real login
        // authentication is a fake instance to run secured commands and dao actions
        // without a login
		boolean dummyAuthAdded = false;
        SecurityContext securityContext = SecurityContextHolder.getContext(); 
        try {                    
            if(securityContext.getAuthentication()==null) {
                securityContext.setAuthentication(authentication);
                dummyAuthAdded = true;
            }
            if(pniCommand!=null) {
                pniCommand.clear();
            }
    		sendMail();
        } finally {
            if(dummyAuthAdded) {
                securityContext.setAuthentication(null);
            }
        }
		
	}

    private void sendMail() {
        // Retrieves the notification information.
        try {
        	commandService.executeCommand(pniCommand);
        } catch (CommandException e) {
        	LOG.warn("Exception when retrieving notification information. Notification mails may miss details!", e); //$NON-NLS-1$
        }
        
        // Iterates through the result, generate and send the individual messages.
        for (NotificationInfo ei : pniCommand.getNotificationInfos()) {
        	MessageHelper mh = new MessageHelper(
        			notificationEmailReplyTo,
        			notificationEmailFrom,
        			ei.getConfiguration(),
        			mailSender.createMimeMessage(),
        			notificationEmailDateFormat,
        			notificationEmailLinkTo);
        	
        	try {
        		mh = prepareMessageHelper(ei, mh);
        		mailSender.send(mh.createMailMessage());
        	} catch (MessagingException me) {
        		LOG.warn("failed to prepare notification message: " + me); //$NON-NLS-1$
        	} catch (MailSendException mse) {
        		LOG.warn("failed to send notification message: " + mse); //$NON-NLS-1$
        	}			
        }
    }

    private MessageHelper prepareMessageHelper(NotificationInfo ei, MessageHelper mh) {
        if (ei.isCompletionExpired()){
        	mh.addCompletionExpirationEvent();
        }
        if (ei.isRevisionExpired()){
        	mh.addRevisionExpirationEvent();
        }
        for (MassnahmenUmsetzung mu : ei.getGlobalExpiredCompletions()){
        	mh.addCompletionExpirationEvent(mu);
        }
        for (MassnahmenUmsetzung mu : ei.getGlobalExpiredRevisions()){
        	mh.addRevisionExpirationEvent(mu);
        }
        for (MassnahmenUmsetzung mu : ei.getModifiedMeasures()){
        	mh.addMeasureModifiedEvent(mu);
        }
        for (MassnahmenUmsetzung mu : ei.getAssignedMeasures()){
        	mh.addMeasureAssignmentEvent(mu);
        }
        return mh;
    }
	
	public void setCommandService(ICommandService commandService) {
		this.commandService = commandService;
	}

	public void setPniCommand(PrepareNotificationInfo pniCommand) {
		this.pniCommand = pniCommand;
	}

	public void setNotificationEnabled(boolean notificationEnabled) {
		this.notificationEnabled = notificationEnabled;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setNotificationEmailFrom(String notificationEmailFrom) {
		this.notificationEmailFrom = notificationEmailFrom;
	}

	public void setNotificationEmailReplyTo(String notificationEmailReplyTo) {
		this.notificationEmailReplyTo = notificationEmailReplyTo;
	}
	
	public void setNotificationEmailDateFormat(
			String notificationEmailDateFormat) {
		this.notificationEmailDateFormat = new SimpleDateFormat(notificationEmailDateFormat);
	}

	/**
	 * Simple class that helps preparing a notification mail's body.
	 * 
	 * <p>The mail's body contains all the information for a particular
	 * user. That means that the user does not get one mail per incident
	 * but all incidents are collected in a single mail. This class' main
	 * contribution is to make this possible.</p>
	 * 
	 * <p>When at least one measure's deadline, for a measure
	 * the user is responsible, expires (either completion or revision).
	 * There will be one event for either expiration kind. The idea is
	 * that a user knows which measure she is responsible for and will
	 * look for them after loggin in.</p>
	 * 
	 * <p>When a user wants to get notified of an expiration of any
	 * measure the mail will contain one sentence per measure and one
	 * sentence for the element that this measure belongs to.</p>
	 * 
	 * <p>When a user wants to get notified of changes to a measure
	 * (regardless of whether she is responsible for them or whether
	 * this affects all measures) the mail will contain one sentence
	 * per modified measure and one sentence for the element that
	 * this measure belongs to.</p>
	 * 
	 * @author Robert Schuster <r.schuster@tarent.de>
	 *
	 */
	private static class MessageHelper
	{
	    private String replyTo, from, to;
				
	    private List<String> events = new ArrayList<String>();
		
	    private Map<CnATreeElement, List<String>> globalExpirationEvents = new HashMap<CnATreeElement, List<String>>();

	    private Map<CnATreeElement, List<String>> measureModificationEvents = new HashMap<CnATreeElement, List<String>>();
		
	    private Map<CnATreeElement, List<String>> measureAssignmentEvents = new HashMap<CnATreeElement, List<String>>();
		
	    private MimeMessage mm;
		
	    private DateFormat dateFormat;

        private String linkTo;
		
		MessageHelper(String replyTo, String from, Configuration recipient, MimeMessage mm, DateFormat df, String linkTo)
		{
			this.replyTo = replyTo;
			this.from = from;
			this.to = recipient.getNotificationEmail();
			this.mm = mm;
			dateFormat = df;
			this.linkTo = linkTo;
		}
		
		void addCompletionExpirationEvent()
		{
			events.add(MailMessages.MailJob_1);
		}
		
		void addRevisionExpirationEvent()
		{
			events.add(MailMessages.MailJob_2);
		}
		
		String titleAndDate(MassnahmenUmsetzung mu, boolean isCompletion)
		{
			StringBuffer sb = new StringBuffer();
			sb.append(mu.getTitle());
			sb.append(" ("); //$NON-NLS-1$
			Calendar c = Calendar.getInstance();
			Date d = (isCompletion ? mu.getUmsetzungBis() : mu.getNaechsteRevision());
			if (d != null) {
				c.setTime(d);
			}
			
			sb.append(dateFormat.format(c.getTime()));
			sb.append(")"); //$NON-NLS-1$
			
			return sb.toString();
		}
		
		void addCompletionExpirationEvent(MassnahmenUmsetzung mu)
		{
			CnATreeElement cte = mu.getParent().getParent();
			List<String> l = globalExpirationEvents.get(cte);
			if (l == null)
			{
				l = new ArrayList<String>();
				globalExpirationEvents.put(cte, l);
			}
			
			String dateString = mu.getUmsetzungBis() != null 
									? dateFormat.format(mu.getUmsetzungBis())
									: MailMessages.MailJob_14;
			l.add(NLS.bind(MailMessages.MailJob_3, dateString + " " + mu.getTitle())); //$NON-NLS-1$
		}
		
		void addRevisionExpirationEvent(MassnahmenUmsetzung mu)
		{
			CnATreeElement cte = mu.getParent().getParent();
			List<String> l = globalExpirationEvents.get(cte);
			if (l == null)
			{
				l = new ArrayList<String>();
				globalExpirationEvents.put(cte, l);
			}
			String dateString = mu.getNaechsteRevision() != null
								? dateFormat.format(mu.getNaechsteRevision())
								: MailMessages.MailJob_16;
			l.add(NLS.bind(MailMessages.MailJob_4, dateString + " " + mu.getTitle())); //$NON-NLS-1$
		}
		
		void addMeasureModifiedEvent(MassnahmenUmsetzung mu)
		{
			CnATreeElement cte = mu.getParent().getParent();
			List<String> l = measureModificationEvents.get(cte);
			if (l == null)
			{
				l = new ArrayList<String>();
				measureModificationEvents.put(cte, l);
			}
			
			l.add("\t" + mu.getTitle() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		void addMeasureAssignmentEvent(MassnahmenUmsetzung mu)
		{
			CnATreeElement cte = mu.getParent().getParent();
			List<String> l = measureAssignmentEvents.get(cte);
			if (l == null)
			{
				l = new ArrayList<String>();
				measureAssignmentEvents.put(cte, l);
			}
			
			l.add("\t" + mu.getTitle() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		MimeMessage createMailMessage() throws MessagingException
		{
			mm.setFrom(new InternetAddress(from));
			mm.setReplyTo(new InternetAddress[] { new InternetAddress(replyTo) });
			mm.setRecipient(RecipientType.TO, new InternetAddress(this.to));
			
			mm.setSubject(MailMessages.MailJob_6);
			
			StringBuffer sb = new StringBuffer();
			sb.append(MailMessages.MailJob_7);
			sb.append(MailMessages.MailJob_18);
			sb.append(linkTo).append("\n\n"); //$NON-NLS-1$
			
			for (String evt : events)
			{
				sb.append(" * " + evt + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			for (Map.Entry<CnATreeElement, List<String>> e : globalExpirationEvents.entrySet())
			{
				sb.append(NLS.bind(MailMessages.MailJob_8, e.getKey().getTitle()));
				for (String s : e.getValue()) {
					sb.append(s);
				}
			}
			
			for (Map.Entry<CnATreeElement, List<String>> e : measureModificationEvents.entrySet())
			{
				sb.append(NLS.bind(MailMessages.MailJob_9, e.getKey().getTitle()));
				for (String s : e.getValue()) {
					sb.append(s);
				}
			}
			
			for (Map.Entry<CnATreeElement, List<String>> e : measureAssignmentEvents.entrySet())
			{
				sb.append(NLS.bind(MailMessages.MailJob_10, e.getKey().getTitle()));
				for (String s : e.getValue()){
					sb.append(s);
				}
			}
			
			sb.append("\n"); //$NON-NLS-1$
			
			sb.append(MailMessages.MailJob_11);
			
			mm.setText(sb.toString());
			
			return mm;
		}
	}
	
}
