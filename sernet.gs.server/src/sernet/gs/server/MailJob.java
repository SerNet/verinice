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

import sernet.gs.server.commands.PrepareExpirationNotification;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.commands.CommandException;

public class MailJob extends QuartzJobBean implements StatefulJob {
	
	private static final Logger log = Logger.getLogger(MailJob.class);
	
	private boolean notificationEnabled;
	
	private PrepareExpirationNotification penCommand;
	
	private JavaMailSender mailSender;
	
	private ICommandService commandService;
	
	public PrepareExpirationNotification getPenCommand() {
		return penCommand;
	}

	public void setPenCommand(PrepareExpirationNotification penCommand) {
		this.penCommand = penCommand;
	}

	public boolean isNotificationEnabled() {
		return notificationEnabled;
	}

	public void setNotificationEnabled(boolean notificationEnabled) {
		this.notificationEnabled = notificationEnabled;
	}

	public JavaMailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		if (!notificationEnabled)
			return;
		
		try {
			commandService.executeCommand(penCommand);
		} catch (CommandException e) {
			throw new JobExecutionException("Exception when retrieving expiration information.", e);
		}
		
		// Send mails for an expiring completion
		for (Map.Entry<Configuration, List<MassnahmenUmsetzung>> e
				: penCommand.getGlobalExpiringCompletionDateNotifees().entrySet())
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
				: penCommand.getGlobalExpiringRevisionDateNotifees().entrySet())
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

		for (Configuration c : penCommand.getExpiringCompletionDateNotifees())
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
		
		for (Configuration c : penCommand.getExpiringRevisionDateNotifees())
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
	
	public ICommandService getCommandService() {
		return commandService;
	}

	public void setCommandService(ICommandService commandService) {
		this.commandService = commandService;
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
