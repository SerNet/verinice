/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;


/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public final class JobScheduler {
	
	
	private static final Mutex INIT_MUTEX = new Mutex();
	
	private static final IProgressMonitor INIT_PROGRESS_MONITOR = Job.getJobManager().createProgressGroup();
	
	private JobScheduler(){}
	
	public static ISchedulingRule getInitMutex() {
		return INIT_MUTEX;
	}
	
	public static IProgressMonitor getInitProgressMonitor() {
		return INIT_PROGRESS_MONITOR;
	}
	
	public static synchronized void scheduleInitJob(WorkspaceJob job) {
		JobScheduler.scheduleJob(job, JobScheduler.getInitMutex(), JobScheduler.getInitProgressMonitor());
	}
	
	public static synchronized void scheduleJob(WorkspaceJob job, ISchedulingRule rule, IProgressMonitor pm) {
		job.setRule(rule);
		job.setUser(true);
		job.setProgressGroup(pm,1);
		job.schedule();
	}
	
	public static synchronized void scheduleJob(WorkspaceJob job, ISchedulingRule rule) {
		job.setRule(rule);
		job.setUser(true);
		job.schedule();
	}
}
