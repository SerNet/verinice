/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.springframework.dao.DataIntegrityViolationException;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Delete items on user request.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class DeleteActionDelegateBsi extends DeleteActionDelegate {

    private static final Logger LOG = Logger.getLogger(DeleteActionDelegateBsi.class);
 
    @Override
    protected void doDelete(final List<CnATreeElement> deleteList) throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                Object sel = null;
                try {
                    Activator.inheritVeriniceContextState();
                    monitor.beginTask(Messages.DeleteActionDelegate_11, deleteList.size());
                    for (Iterator iter = deleteList.iterator(); iter.hasNext();) {
                        sel = iter.next();

                        // do not delete last ITVerbund:
                        if (sel instanceof ITVerbund && CnAElementHome.getInstance().getItverbuende().size() < 2) {
                            ExceptionUtil.log(new Exception(Messages.DeleteActionDelegate_12), Messages.DeleteActionDelegate_13);
                            return;
                        }

                        CnATreeElement el = (CnATreeElement) sel;
                        monitor.setTaskName(Messages.DeleteActionDelegate_14);
                        removeElement(el);
                        monitor.worked(1);
                    }
                } catch (DataIntegrityViolationException dive) {
                    deleteElementWithAccountAsync((CnATreeElement) sel);
                } catch (Exception e) {
                    LOG.error(DEFAULT_ERR_MSG, e);
                    ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                }
            }             
        });
    }
    
}
