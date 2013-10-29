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
package sernet.verinice.iso27k.service;

/**
 * A technology independent interface to observe the progress of
 * an (long) running task.
 * 
 * Pattern for this interface was org.eclipse.core.runtime.IProgressMonitor
 * from the RCP platform.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IProgressObserver {

	/**
	 * Returns true if the task is canceled, false if not
	 * 
	 * @return  true if the task is canceled, false if not
	 */
	boolean isCanceled();

	/**
	 * Sets the name of the observed task
	 * 
	 * @param text
	 */
	void setTaskName(String text);

	/**
	 * Sets the number of processed items
	 * 
	 * @param n
	 */
	void processed(int n);

	/**
	 * Sets the name and number of items of the observed task
	 * and starts the task
	 * 
	 * @param string the name of the task
	 * @param numberOfItems  number of items of the task
	 */
	void beginTask(String string, int numberOfItems);

	/**
	 * Marks the operation as finished
	 */
	void done();

}
