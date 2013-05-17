/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.validation;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;

/**
 *
 */
public class CreateValidationsJob implements IRunnableWithProgress {
    
    private Integer scopeId;
    
    public CreateValidationsJob(Integer scope){
        super();
        this.scopeId = scope;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            createValidations(scopeId, monitor);
        } catch (CommandException e) {
            throw new InterruptedException(e.getLocalizedMessage());
        }
    }
    
    private void createValidations(Integer scope, IProgressMonitor monitor) throws CommandException{
        Activator.inheritVeriniceContextState();
        monitor.beginTask(Messages.CreateValidationJob_1, IProgressMonitor.UNKNOWN);
        ServiceFactory.lookupValidationService().createValidationsForScope(scope);
        monitor.done();
    }

}
