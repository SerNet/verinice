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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.config.AnonymousBeanDefinitionParser;
import org.springframework.security.context.SecurityContextHolder;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.INoAccessControl;
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
 * A {@link Configuration} instance can either be in a collection for their
 * own measures or in one for any.</p>
 * 
 * <p>A {@link Configuration} instance can be in a collection for an expiring
 * completion and an expiring revision date at the same time.</p>
 * 
 * <p>A <code>Notifee</code> is the recipient of a notification.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class PrepareExpirationNotification extends GenericCommand {
	
	private Set<Configuration> expiringCompletionDateNotifees = new HashSet<Configuration>();

	private Set<Configuration> expiringRevisionDateNotifees = new HashSet<Configuration>();
	
	private Map<Configuration, List<MassnahmenUmsetzung>> globalExpiringCompletionDateNotifees = new HashMap<Configuration, List<MassnahmenUmsetzung>>();
	
	private Map<Configuration, List<MassnahmenUmsetzung>> globalExpiringRevisionDateNotifees = new HashMap<Configuration, List<MassnahmenUmsetzung>>();
	
	private Map<Person, Configuration> personCache = new HashMap<Person, Configuration>(); 
	
	public PrepareExpirationNotification() {
		// Intentionally does nothing.
	}
	
	/**
	 * Returns the collection of {@link Configuration} instances which need
	 * to get notified of an expiring completion date (of one of their own
	 * measures).
	 * 
	 * @return
	 */
	public Set<Configuration> getExpiringCompletionDateNotifees()
	{
		return expiringCompletionDateNotifees;
	}
	
	/**
	 * Returns the collection of {@link Configuration} instances which need
	 * to get notified of an expiring revision date (of one of their own
	 * measures).
	 * 
	 * @return
	 */
	public Set<Configuration> getExpiringRevisionDateNotifees()
	{
		return expiringRevisionDateNotifees;
	}
	
	/**
	 * Returns the collection of {@link Configuration} instances which need
	 * to get notified of an expiring completion date of <em>any</em>
	 * measures.
	 * 
	 * <p>The measures in question are assigned to each {@link Configuration}
	 * instance.</p>
	 * 
	 * @return
	 */
	public Map<Configuration, List<MassnahmenUmsetzung>> getGlobalExpiringCompletionDateNotifees()
	{
		return globalExpiringCompletionDateNotifees;
	}
	
	/**
	 * Returns the collection of {@link Configuration} instances which need
	 * to get notified of an expiring revision date of <em>any</em>
	 * measures.
	 * 
	 * <p>The measures in question are assigned to each {@link Configuration}
	 * instance.</p>
	 * 
	 * @return
	 */
	public Map<Configuration, List<MassnahmenUmsetzung>> getGlobalExpiringRevisionDateNotifees()
	{
		return globalExpiringRevisionDateNotifees;
	}
	
	public void execute() {
		LoadGenericElementByType<Configuration> lc = new LoadGenericElementByType<Configuration>(Configuration.class);
		try {
			lc = (LoadGenericElementByType<Configuration>) getCommandService().executeCommand(lc);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
		
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
					c.getNotificationEmail();
					expiringRevisionDateNotifees.add(c);
				}
			}
		}
		
		for (Configuration c : globalNotifees)
		{
			Calendar limit = Calendar.getInstance();
			limit.add(Calendar.DAY_OF_WEEK, c.getNotificationExpirationDays());
			
			if (limit.after(deadline))
			{
				c.getNotificationEmail();
				addGlobalNotificationRevision(c, mu);
			}
		}
		
	}

	private void addGlobalNotificationRevision(Configuration c, MassnahmenUmsetzung mu)
	{
		List<MassnahmenUmsetzung> list = globalExpiringRevisionDateNotifees.get(c);
		if (list == null)
		{
			list = new ArrayList<MassnahmenUmsetzung>();
			globalExpiringRevisionDateNotifees.put(c, list);
		}
		
		// Hydrates fields which will be needed later
		mu.getTitel();
		c.getNotificationEmail();
		
		list.add(mu);
	}
	
	private void addGlobalNotificationCompletion(Configuration c, MassnahmenUmsetzung mu)
	{
		List<MassnahmenUmsetzung> list = globalExpiringCompletionDateNotifees.get(c);
		if (list == null)
		{
			list = new ArrayList<MassnahmenUmsetzung>();
			globalExpiringCompletionDateNotifees.put(c, list);
		}
		
		// Hydrates fields which will be needed later
		mu.getTitel();
		c.getNotificationEmail();
		
		list.add(mu);
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
					c.getNotificationEmail();
					expiringCompletionDateNotifees.add(c);
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
