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
package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.iso27k.ISO27KModel;

public class ReloadAction extends Action {

    public static final String ID = "sernet.gs.ui.rcp.main.reloadaction"; //$NON-NLS-1$
    
    public ReloadAction(IWorkbenchWindow window, String label) {
        setText(label);
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.RELOAD));
        setEnabled(false);
        CnAElementFactory.getInstance().addLoadListener(new IModelLoadListener() {
            @Override
            public void closed(BSIModel model) {
                setEnabled(false);
            }
            @Override
            public void loaded(BSIModel model) {
                setEnabled(true);
            }
            @Override
            public void loaded(ISO27KModel model) {
                setEnabled(true);
            }
            @Override
            public void loaded(BpModel model) {
                setEnabled(true);
            }
            @Override
            public void loaded(CatalogModel model) {
                // nothing to do
            }
        });
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Activator.inheritVeriniceContextState();
            // close editors:
        try{
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true /* ask save */);
            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            IRunnableWithProgress operation = new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try{
                        monitor.beginTask(Messages.ReloadAction_2, IProgressMonitor.UNKNOWN);
                        CnAElementFactory.getInstance().reloadAllModelsFromDatabase();
                    } catch (Exception e){
                        ExceptionUtil.log(e, Messages.ReloadAction_1);
                    } finally {
                        monitor.done();
                    }
                }
            };
            progressService.run(true, true, operation);
        } catch (InvocationTargetException e)  {
            ExceptionUtil.log(e, Messages.ReloadAction_1); //$NON-NLS-1$
        } catch (InterruptedException e) {
            ExceptionUtil.log(e, Messages.ReloadAction_1); //$NON-NLS-1$
        }
    }
}
