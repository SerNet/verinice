/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server.commands;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadGenericElementByType;
import sernet.gs.ui.rcp.main.service.taskcommands.FindResponsiblePerson;

/**
 * Iterates over all {@link MassnahmenUmsetzung} instances and prepares the following collections containing
 * {@link Configuration} instances which need to be notified of ...
 * <ul>
 * <li>... expiring completion dates of their own measures</li>
 * <li>... expiring revision dates of their own measures</li>
 * <li>... expiring completion dates of any measure</li>
 * <li>... expiring revision dates of any measure</li>
 * </ul>
 *
 * <p>The result can be retrieved as a {@link Collection} instance
 * containing {@link ExpirationInfo} instances. This class makes
 * it simple to access the required information.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class PrepareExpirationNotification extends GenericCommand {
	
	private Map<Configuration, ExpirationInfo> resultMap = new HashMap<Configuration, ExpirationInfo>();
	
	private Map<Person, Configuration> personCache = new HashMap<Person, Configuration>(); 
	
	public PrepareExpirationNotification() {
		// Intentionally does nothing.
	}
	
	public Collection<ExpirationInfo> getExpirationInfo()
	{
		return resultMap.values();
	}
	
	public void execute() {
		LoadGenericElementByType<Configuration> lc = new LoadGenericElementByType<Configuration>(Configuration.class);
		try {
			lc = (LoadGenericElementByType<Configuration>) getCommandService().executeCommand(lc);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
		
		// Prepares a set of Configuration instances which needs to get informed about any
		// expiration. 
		Set<Configuration> globalNotifees = new HashSet<Configuration>();
		for (Configuration c : lc.getElements())
		{
			if (c.isNotificationEnabled()
					&& c.isNotificationExpirationEnabled()
					&& c.isNotificationGlobal())
			{
				globalNotifees.add(c);
			}
		}
		
		LoadCnAElementByType<MassnahmenUmsetzung> lmu = new LoadCnAElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class, false);
		
		try {
			lmu = (LoadCnAElementByType<MassnahmenUmsetzung>) getCommandService().executeCommand(lmu);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
		
		for (MassnahmenUmsetzung mu : lmu.getElements())
		{
			if (mu.isCompleted())
			{
				handleCompletedMeasure(mu, globalNotifees);
			}
			else
			{
				handleIncompletedMeasure(mu, globalNotifees);
			}
		}
		
	}
	
	private Configuration retrieveConfiguration(Person p)
	{
		Configuration result = personCache.get(p);
		if (result == null)
		{
			LoadConfiguration lc = new LoadConfiguration(p, false);
			try {
				lc = (LoadConfiguration) getCommandService().executeCommand(lc);
			} catch (CommandException e) {
				throw new RuntimeException(e);
			}
			
			result = lc.getConfiguration();
			if (result == null)
				result = new Configuration();
			
			personCache.put(p, result);
		}
		
		return result;
	}
	
	public void clear()
	{
		personCache.clear();
	}
	
	private void addGlobalNotificationRevision(Configuration c, MassnahmenUmsetzung mu)
	{
		ExpirationInfo ei = resultMap.get(c);
		if (ei == null)
		{
			ei = new ExpirationInfo(c);
			resultMap.put(c, ei);
			
			c.getNotificationEmail();
		}
		
		// Hydrates fields which will be needed later
		mu.getTitel();
		mu.getParent().getParent().getTitel();
		
		ei.addGlobalExpiredRevision(mu);
	}
	
	private void addGlobalNotificationCompletion(Configuration c, MassnahmenUmsetzung mu)
	{
		ExpirationInfo ei = resultMap.get(c);
		if (ei == null)
		{
			ei = new ExpirationInfo(c);
			resultMap.put(c, ei);
			
			c.getNotificationEmail();
		}
		
		// Hydrates fields which will be needed later
		mu.getTitel();
		mu.getParent().getParent().getTitel();
		
		ei.addGlobalExpiredCompletion(mu);
	}

	private void addExpiringRevisionDateNotifee(Configuration c)
	{
		ExpirationInfo i = resultMap.get(c);
		if (i == null)
		{
			i = new ExpirationInfo(c);
			resultMap.put(c, i);
			
			c.getNotificationEmail();
		}
		
		i.setRevisionExpired(true);
	}
	
	private void addExpiringCompletionDateNotifee(Configuration c)
	{
		ExpirationInfo i = resultMap.get(c);
		if (i == null)
		{
			i = new ExpirationInfo(c);
			resultMap.put(c, i);
			
			c.getNotificationEmail();
		}
		
		i.setCompletionExpired(true);
	}

	private void handleCompletedMeasure(MassnahmenUmsetzung mu, Set<Configuration> globalNotifees)
	{
		FindResponsiblePerson frp = new FindResponsiblePerson(mu.getDbId(), MassnahmenUmsetzung.P_VERANTWORTLICHE_ROLLEN_INITIIERUNG);
		
		try {
			frp = (FindResponsiblePerson) getCommandService().executeCommand(frp);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
		
		Date date = mu.getNaechsteRevision();
		Calendar deadline = Calendar.getInstance();
		if (date != null)
			deadline.setTime(date);

		for (Person p : frp.getFoundPersons())
		{
			Configuration c = retrieveConfiguration(p);
			if (c != null
					&& c.isNotificationEnabled()
					&& !c.isNotificationGlobal())
			{
				Calendar limit = Calendar.getInstance();
				limit.add(Calendar.DAY_OF_WEEK, c.getNotificationExpirationDays());
				
				if (limit.after(deadline))
				{
					addExpiringRevisionDateNotifee(c);
				}
			}
		}
		
		for (Configuration c : globalNotifees)
		{
			Calendar limit = Calendar.getInstance();
			limit.add(Calendar.DAY_OF_WEEK, c.getNotificationExpirationDays());
			
			if (limit.after(deadline))
			{
				addGlobalNotificationRevision(c, mu);
			}
		}
		
	}
	
	private void handleIncompletedMeasure(MassnahmenUmsetzung mu, Set<Configuration> globalNotifees)
	{
		FindResponsiblePerson frp = new FindResponsiblePerson(mu.getDbId(), MassnahmenUmsetzung.P_VERANTWORTLICHE_ROLLEN_UMSETZUNG);
		
		try {
			frp = (FindResponsiblePerson) getCommandService().executeCommand(frp);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}

		Date date = mu.getUmsetzungBis();
		Calendar deadline = Calendar.getInstance();
		if (date != null)
			deadline.setTime(date);

		for (Person p : frp.getFoundPersons())
		{
			Configuration c = retrieveConfiguration(p);
			if (c != null
					&& c.isNotificationEnabled()
					&& !c.isNotificationGlobal())
			{
				Calendar limit = Calendar.getInstance();
				limit.add(Calendar.DAY_OF_WEEK, c.getNotificationExpirationDays());
				
				if (limit.after(deadline))
				{
					addExpiringCompletionDateNotifee(c);
				}
			}
		}
		
		for (Configuration c : globalNotifees)
		{
			Calendar limit = Calendar.getInstance();
			limit.add(Calendar.DAY_OF_WEEK, c.getNotificationExpirationDays());
			
			if (limit.after(deadline))
			{
				addGlobalNotificationCompletion(c, mu);
			}
		}
		
	}

}
