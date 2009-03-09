/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;


public class NullMonitor implements IProgress {

	public void beginTask(String name, int totalWork) {
		// TODO Auto-generated method stub

	}

	public void done() {
		// TODO Auto-generated method stub

	}

	public void internalWorked(double work) {
		// TODO Auto-generated method stub

	}

	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCanceled(boolean value) {
		// TODO Auto-generated method stub

	}

	public void setTaskName(String name) {
		// TODO Auto-generated method stub

	}

	public void subTask(String name) {
		// TODO Auto-generated method stub

	}

	public void worked(int work) {
		// TODO Auto-generated method stub

	}

}
