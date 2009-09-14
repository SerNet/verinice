package sernet.gs.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.quartz.QuartzJobBean;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.taskcommands.PrepareExpirationNotification;
import sernet.hui.common.VeriniceContext;

public class MailJob extends QuartzJobBean implements StatefulJob {
	
	private static final Logger log = Logger.getLogger(MailJob.class);
	
	private JavaMailSender mailSender;
	
	private VeriniceContext.State workObjects;

	public VeriniceContext.State getWorkObjects() {
		return workObjects;
	}

	public void setWorkObjects(VeriniceContext.State workObjects) {
		this.workObjects = workObjects;
	}

	public JavaMailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		VeriniceContext.setState(workObjects);
		
		PrepareExpirationNotification pen = new PrepareExpirationNotification();
		
		try {
			pen = (PrepareExpirationNotification) 
				ServiceFactory.lookupCommandService().executeCommand(pen);
		} catch (CommandException e) {
		}
		
		// Send mails for an expiring completion
		for (Map.Entry<Configuration, List<MassnahmenUmsetzung>> e
				: pen.getGlobalExpiringCompletionDateNotifees().entrySet())
		{
			MessageHelper mh = new MessageHelper(e.getKey(), mailSender.createMimeMessage());
			
			try
			{
				for (MassnahmenUmsetzung mu : e.getValue())
				{
					mh.addCompletionExpirationEvent(mu);
				}
				mailSender.send(mh.createMailMessage());
			}
			catch (MessagingException me)
			{
				log.warn("failed to create/send notification message: " + me);
			}
		}
		
		// Send mails for an expiring revision
		for (Map.Entry<Configuration, List<MassnahmenUmsetzung>> e
				: pen.getGlobalExpiringRevisionDateNotifees().entrySet())
		{
			MessageHelper mh = new MessageHelper(e.getKey(), mailSender.createMimeMessage());
			
			try
			{
				for (MassnahmenUmsetzung mu : e.getValue())
				{
					mh.addRevisionExpirationEvent(mu);
				}
				mailSender.send(mh.createMailMessage());
			}
			catch (MessagingException me)
			{
				log.warn("failed to create/send notification message: " + me);
			}
		}

		for (Configuration c : pen.getExpiringCompletionDateNotifees())
		{
			MessageHelper mh = new MessageHelper(c, mailSender.createMimeMessage());
			
			try
			{
				mh.addCompletionExpirationEvent();
				mailSender.send(mh.createMailMessage());
			}
			catch (MessagingException me)
			{
				log.warn("failed to create/send notification message: " + me);
			}
		}
		
		for (Configuration c : pen.getExpiringRevisionDateNotifees())
		{
			MessageHelper mh = new MessageHelper(c, mailSender.createMimeMessage());
			
			try
			{
				mh.addRevisionExpirationEvent();
				mailSender.send(mh.createMailMessage());
			}
			catch (MessagingException me)
			{
				log.warn("failed to create/send notification message: " + me);
			}
		}

	}
	
	private static class MessageHelper
	{
		String to;
		
		List<String> events = new ArrayList<String>();
		
		MimeMessage mm;
		
		MessageHelper(Configuration recipient, MimeMessage mm)
		{
			this.to = recipient.getNotificationEmail();
			this.mm = mm;
		}
		
		void addCompletionExpirationEvent()
		{
			events.add("Die Frist zur Umsetzung einer Massnahme läuft ab.");
		}
		
		void addRevisionExpirationEvent()
		{
			events.add("Die Frist zur Revision einer Massnahme läuft ab.");
		}
		
		void addCompletionExpirationEvent(MassnahmenUmsetzung mu)
		{
			events.add("Die Frist zur Umsetzung einer Massnahme läuft ab: " + mu.getParent().getParent().getTitel());
		}
		
		void addRevisionExpirationEvent(MassnahmenUmsetzung mu)
		{
			events.add("Die Frist zur Revision einer Massnahme läuft ab." + mu.getParent().getParent().getTitel());
		}
		
		MimeMessage createMailMessage() throws MessagingException
		{
			mm.setFrom(new InternetAddress("mail@donotreply.com"));
			mm.setRecipient(RecipientType.TO, new InternetAddress(this.to));
			
			StringBuffer sb = new StringBuffer();
			sb.append("Es liegen neue Ereignisse für Sie vor:\n");
			
			for (String evt : events)
			{
				sb.append(" * " + evt + "\n");
			}
			
			sb.append("\n");
			
			sb.append("Mit freundlichen Grüßen,");
			sb.append("Ihr verinice-Benachrichtigungssystem");
			
			mm.setText(sb.toString());
			
			return mm;
		}
	}
	
}
