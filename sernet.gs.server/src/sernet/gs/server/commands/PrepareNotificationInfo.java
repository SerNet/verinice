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
package sernet.gs.server.commands;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementsByEntityIds;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadGenericElementByType;
import sernet.gs.ui.rcp.main.service.taskcommands.FindResponsiblePerson;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.iso27k.service.commands.CreateIsoModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.commands.LoadConfiguration;

/**
 * Iterates over all {@link MassnahmenUmsetzung} instances and prepares the following collections containing
 * {@link Configuration} instances which need to be notified of ...
 * <ul>
 * <li>... expiring completion dates of their own measures</li>
 * <li>... expiring revision dates of their own measures</li>
 * <li>... expiring completion dates of any measure</li>
 * <li>... expiring revision dates of any measure</li>
 * <li>... modified measures</li>
 * <li>... assigned measures</li>
 * </ul>
 *
 * <p>The result can be retrieved as a {@link Collection} instance
 * containing {@link NotificationInfo} instances. This class makes
 * it simple to access the required information.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class PrepareNotificationInfo extends GenericCommand {
	
	private transient Logger log = Logger.getLogger(CreateIsoModel.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CreateIsoModel.class);
        }
        return log;
    }

	private Map<Configuration, NotificationInfo> resultMap = new HashMap<Configuration, NotificationInfo>();
	
	private Map<CnATreeElement, Configuration> personCache = new HashMap<CnATreeElement, Configuration>(); 
	
	public PrepareNotificationInfo() {
		// Intentionally does nothing.
	}
	
	public Collection<NotificationInfo> getNotificationInfos()
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
		
		collectExpirationNotifees(lc.getElements());
		
		// The following command rely upon inspection of the ChangeLogEntry instances.
		// All results of insertion and modification changes are taken into account. If an
		// element has been deleted in the same time period, it is assumed that the database
		// won't be able to retrieve the neccessary instance anymore.
		// 
		// E.g. someone modifies some MassnahmenUmsetzung, later this one is removed. The
		// instance is not in the database anymore so it won't be regarded.
		
		// TODO rschuster: Test this.
		collectModificationNotifees(lc.getElements());
		collectAssignmentNotifees(lc.getElements());
	}
	
	private void collectExpirationNotifees(List<Configuration> configurations) {
		
		// Prepares sets of Configuration instances which need to get informed about
		// the completion or revision expiration of any measure.
		Set<Configuration> globalNotifees = new HashSet<Configuration>();
		Set<Configuration> globalAuditorNotifees = new HashSet<Configuration>();
		for (Configuration c : configurations)
		{
			if (c.isNotificationEnabled()
					&& c.isNotificationExpirationEnabled()
					&& c.isNotificationGlobal())
			{
				globalNotifees.add(c);
			}
			
			if (c.isNotificationEnabled()
					&& c.isAuditorNotificationExpirationEnabled()
					&& c.isAuditorNotificationGlobal())
			{
				globalAuditorNotifees.add(c);
			}
		}
		
		handleMeasures(globalNotifees, globalAuditorNotifees);
		
	}

    private void handleMeasures(Set<Configuration> globalNotifees, Set<Configuration> globalAuditorNotifees) {
        LoadCnAElementByType<MassnahmenUmsetzung> lmu = new LoadCnAElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class, false);
		
		try {
			lmu = (LoadCnAElementByType<MassnahmenUmsetzung>) getCommandService().executeCommand(lmu);
		} catch (CommandException e) {
		    getLog().error("Error in collectExpirationNotifees", e);
			throw new RuntimeException(e);
		}
		
		for (MassnahmenUmsetzung mu : lmu.getElements())
		{
			if (mu.isCompleted())
			{
				handleCompletedMeasure(mu, globalAuditorNotifees);
			}
			else
			{
				handleIncompletedMeasure(mu, globalNotifees);
			}
		}
    }
	
	private Configuration retrieveConfiguration(CnATreeElement p)
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
			if (result == null){
				result = new Configuration();
			}
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
		NotificationInfo ei = resultMap.get(c);
		if (ei == null)
		{
			ei = new NotificationInfo(c);
			resultMap.put(c, ei);
			
			c.getNotificationEmail();
		}
		
		// Hydrates fields which will be needed later
		mu.getTitle();
		mu.getParent().getParent().getTitle();
		
		ei.addGlobalExpiredRevision(mu);
	}
	
	private void addGlobalNotificationCompletion(Configuration c, MassnahmenUmsetzung mu)
	{
		NotificationInfo ei = resultMap.get(c);
		if (ei == null)
		{
			ei = new NotificationInfo(c);
			resultMap.put(c, ei);
			
			c.getNotificationEmail();
		}
		
		// Hydrates fields which will be needed later
		mu.getTitle();
		mu.getParent().getParent().getTitle();
		
		ei.addGlobalExpiredCompletion(mu);
	}

	private void addExpiringRevisionDateNotifee(Configuration c)
	{
		NotificationInfo i = resultMap.get(c);
		if (i == null)
		{
			i = new NotificationInfo(c);
			resultMap.put(c, i);
			
			c.getNotificationEmail();
		}
		
		i.setRevisionExpired(true);
	}
	
	private void addExpiringCompletionDateNotifee(Configuration c)
	{
		NotificationInfo i = resultMap.get(c);
		if (i == null)
		{
			i = new NotificationInfo(c);
			resultMap.put(c, i);
			
			c.getNotificationEmail();
		}
		
		i.setCompletionExpired(true);
	}

	/**
	 * Retrieves a list of {@link Person} instances which is directly responsible for the given measure.
	 * 
	 * <p>Directly means that the respective field of {@link MassnahmenUmsetzung} are used to specify the
	 * responsibility. Indirect responsibility is achieved through the responsibility property of parent
	 * object of the measure. This method is not dealing with this kind in any way.</p>
	 * 
	 * <p>When no direct responsibility is set, the method returns an empty list</p>.
	 * 
	 * <p>When <code>isCompletion</code> is <code>true</code> then the method returns the {@link Person}
	 * objects which are set to be responsible for <em>completing</em> a measure. When the value is
	 * <code>false</code> the persons which are responsible for <em>revising</em> the measure are retrieved.</p>
	 * 
	 * @param mu
	 * @param useImplementor
	 * @return
	 */
	private List<Person> retrievePersonsDirectlyResponsible(MassnahmenUmsetzung mu, boolean isCompletion)
	{
		String field = (isCompletion ? MassnahmenUmsetzung.P_UMSETZUNGDURCH_LINK : MassnahmenUmsetzung.P_NAECHSTEREVISIONDURCH_LINK);
		PropertyList pl = mu.getEntity().getProperties(field);
		List<Property> props = null; 
			
		if (pl != null){
			props = pl.getProperties();
		}
		if (props != null && !props.isEmpty())
		{
			List<Integer> ids = new ArrayList<Integer>(props.size());
			for (Property p : props)
			{
				ids.add(Integer.valueOf(p.getPropertyValue()));
			}
			
			LoadCnAElementsByEntityIds<Person> le = new LoadCnAElementsByEntityIds<Person>(Person.class, ids);
			try
			{
				le = getCommandService().executeCommand(le);
			}
			catch (CommandException ce)
			{
			    getLog().error("Error while executing command: LoadCnAElementsByEntityIds", ce);
				throw new RuntimeException("Error while executing command: LoadCnAElementsByEntityIds", ce);
			}
			
			return le.getElements();
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Returns a list of {@link Person} which are responsible to <em>complete</em> the given measure.
	 * 
	 * <p>The method first tries to find direct responsibles. If there are none, the indirect responsibilities
	 * are used instead.</p>
	 * 
	 * @param mu
	 * @return
	 */
	private List<Person> retrievePersonsResponsibleForCompletion(MassnahmenUmsetzung mu)
	{
		List<Person> res = retrievePersonsDirectlyResponsible(mu, true);
		if (res.isEmpty())
		{
			FindResponsiblePerson frp = new FindResponsiblePerson(mu.getDbId(), MassnahmenUmsetzung.P_VERANTWORTLICHE_ROLLEN_UMSETZUNG);
			
			try {
				frp = getCommandService().executeCommand(frp);
				
				res = frp.getFoundPersons();
			} catch (CommandException e) {
				throw new RuntimeException(e);
			}
		}
		
		return res;
	}

	private void handleCompletedMeasure(MassnahmenUmsetzung mu, Set<Configuration> globalNotifees)
	{
		Date date = mu.getNaechsteRevision();
		Calendar deadline = Calendar.getInstance();
		if (date != null){
			deadline.setTime(date);
		}
		for (CnATreeElement p : retrievePersonsDirectlyResponsible(mu, false))
		{
			Configuration c = retrieveConfiguration(p);
			if (c != null
					&& c.isNotificationEnabled()
					&& c.isAuditorNotificationExpirationEnabled()
					&& !c.isAuditorNotificationGlobal())
			{
				Calendar limit = Calendar.getInstance();
				limit.add(Calendar.DAY_OF_WEEK, c.getAuditorNotificationExpirationDays());
				
				if (limit.after(deadline))
				{
					addExpiringRevisionDateNotifee(c);
				}
			}
		}
		
		for (Configuration c : globalNotifees)
		{
			Calendar limit = Calendar.getInstance();
			limit.add(Calendar.DAY_OF_WEEK, c.getAuditorNotificationExpirationDays());
			
			if (limit.after(deadline))
			{
				addGlobalNotificationRevision(c, mu);
			}
		}
		
	}
	
	private void handleIncompletedMeasure(MassnahmenUmsetzung mu, Set<Configuration> globalNotifees)
	{

		Date date = mu.getUmsetzungBis();
		Calendar deadline = Calendar.getInstance();
		if (date != null){
			deadline.setTime(date);
		}
		for (CnATreeElement p : retrievePersonsResponsibleForCompletion(mu))
		{
			Configuration c = retrieveConfiguration(p);
			if (c != null
					&& c.isNotificationEnabled()
					&& c.isNotificationExpirationEnabled()
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

	private void collectModificationNotifees(List<Configuration> configurations)
	{
		// Prepares a set of Configuration instances which needs to get informed about any
		// change in a measure 
		Set<Configuration> globalNotifees = new HashSet<Configuration>();
		for (Configuration c : configurations)
		{
			if (c.isNotificationEnabled()
					&& c.isNotificationMeasureModification()
					&& c.isNotificationGlobal())
			{
				globalNotifees.add(c);
			}
		}
		
		Calendar keydate = Calendar.getInstance();
		keydate.add(Calendar.DAY_OF_YEAR, -1);
		
		GetChangedElementsSince gces = new GetChangedElementsSince(keydate, ChangeLogEntry.TYPE_UPDATE, MassnahmenUmsetzung.class);
		
		try {
			gces = (GetChangedElementsSince) getCommandService().executeCommand(gces);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
		
		for (CnATreeElement cte : gces.getChangedElements())
		{
			MassnahmenUmsetzung mu = (MassnahmenUmsetzung) cte;
			handleChangedMeasure(mu, globalNotifees);
		}
		
	}
	
	private void handleChangedMeasure(MassnahmenUmsetzung mu, Set<Configuration> globalNotifees)
	{
		for (CnATreeElement p : retrievePersonsResponsibleForCompletion(mu))
		{
			Configuration c = retrieveConfiguration(p);
			if (c != null
					&& c.isNotificationEnabled()
					&& c.isNotificationMeasureModification()
					&& !c.isNotificationGlobal())
			{
				addModifiedMeasure(c, mu);
			}
		}
		
		for (Configuration c : globalNotifees)
		{
			addModifiedMeasure(c, mu);
		}
		
	}
	
	private void addModifiedMeasure(Configuration c, MassnahmenUmsetzung mu)
	{
		NotificationInfo i = resultMap.get(c);
		if (i == null)
		{
			i = new NotificationInfo(c);
			resultMap.put(c, i);
			
			c.getNotificationEmail();
		}
		
		mu.getTitle();
		mu.getParent().getParent().getTitle();
		
		i.addModifiedMeasure(mu);
	}

	private void collectAssignmentNotifees(List<Configuration> configurations)
	{
		// Prepares a set of Configuration instances which needs to get informed about any
		// new assignments 
		Set<Configuration> globalNotifees = new HashSet<Configuration>();
		for (Configuration c : configurations)
		{
			if (c.isNotificationEnabled()
					&& c.isNotificationMeasureAssignment()
					&& c.isNotificationGlobal())
			{
				globalNotifees.add(c);
			}
		}
		
		Calendar keydate = Calendar.getInstance();
		keydate.add(Calendar.DAY_OF_YEAR, -1);
		
		GetChangedElementsSince gces = new GetChangedElementsSince(keydate, ChangeLogEntry.TYPE_INSERT, BausteinUmsetzung.class);
		
		try {
			gces = getCommandService().executeCommand(gces);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
		
		for (CnATreeElement cte : gces.getChangedElements())
		{
				for (CnATreeElement child : cte.getChildren())
				{
					if (child instanceof MassnahmenUmsetzung)
					{
						handleAssignedMeasure((MassnahmenUmsetzung) child, globalNotifees);
					}
					else
					{
						getLog().warn("Retrieved a child of an element that is supposed to be a BausteinUmsetzung instance that is not a MassnahmenUmsetzung.");
					}
				}
				
		}
		
		// FIXME New assignment can also happen (and in fact do so in a more straightforward
		// way) when someone adds a new link of type 'responsible for'. However no changelog entries
		// are created in that case.
		/*
		gces = new GetChangedElementsSince(keydate, ChangeLogEntry.TYPE_INSERT, CnALink.class);
		try {
			gces = getCommandService().executeCommand(gces);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
		*/
	}
	
	private void handleAssignedMeasure(MassnahmenUmsetzung mu, Set<Configuration> globalNotifees)
	{
		for (CnATreeElement p : retrievePersonsResponsibleForCompletion(mu))
		{
			Configuration c = retrieveConfiguration(p);
			if (c != null
					&& c.isNotificationEnabled()
					&& c.isNotificationMeasureAssignment()
					&& !c.isNotificationGlobal())
			{
				addAssignedMeasure(c, mu);
			}
		}
		
		for (Configuration c : globalNotifees)
		{
			addAssignedMeasure(c, mu);
		}
		
	}
	
	private void addAssignedMeasure(Configuration c, MassnahmenUmsetzung mu)
	{
		NotificationInfo i = resultMap.get(c);
		if (i == null)
		{
			i = new NotificationInfo(c);
			resultMap.put(c, i);
			
			c.getNotificationEmail();
		}
		
		mu.getTitle();
		mu.getParent().getParent().getTitle();
		
		i.addAssignedMeasure(mu);
	}
	
}
