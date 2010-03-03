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
package sernet.verinice.iso27k.rcp.action;

import org.eclipse.ui.IStartup;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.rcp.StatusResult;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class Initializer implements IStartup {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		final StatusResult result = Activator.startServer();
		Activator.initDatabase(JobScheduler.getInitMutex(),result);
		Activator.createModel(JobScheduler.getInitMutex(),result);
	}

}
