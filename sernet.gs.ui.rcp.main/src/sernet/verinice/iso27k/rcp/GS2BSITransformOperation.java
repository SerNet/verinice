/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh[at]sernet[dot]de>.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.iso27k.service.GS2BSITransformService;
import sernet.verinice.iso27k.service.IModelUpdater;
import sernet.verinice.iso27k.service.IProgressObserver;
import sernet.verinice.model.iso27k.Group;

public class GS2BSITransformOperation implements IRunnableWithProgress {

	private GS2BSITransformService service;
	
	private IModelUpdater modelUpdater;
	
	@SuppressWarnings("rawtypes")
	private Group selectedGroup;
	
	private boolean isScenario = false;
	
	private Object data;

	@SuppressWarnings("rawtypes")
	public GS2BSITransformOperation(Group selectedGroup, Object data) {
		this.selectedGroup = selectedGroup;	
		modelUpdater = new RcpModelUpdater();
		this.data = data;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)  {
	    IProgressObserver progressObserver = new RcpProgressObserver(monitor);
		service = new GS2BSITransformService(progressObserver,modelUpdater,this.selectedGroup, data);
		Activator.inheritVeriniceContextState();
		service.run();
		isScenario = service.isScenario();
	}

	/**
	 * @return
	 */
	public int getNumberProcessed() {
		int n = 0;
		if(service!=null) {
			n = service.getNumberProcessed();
		}
		return n;
	}


	public boolean isScenario() {
		return isScenario;
	}
}
