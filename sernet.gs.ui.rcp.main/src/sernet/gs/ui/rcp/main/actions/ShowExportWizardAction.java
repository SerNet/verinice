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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.wizards.ExportWizard;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.iso27k.ISO27KModel;

public class ShowExportWizardAction extends Action {

    public static final String ID = "sernet.gs.ui.rcp.main.showexportwizardaction"; //$NON-NLS-1$
    private final IWorkbenchWindow window;

    public ShowExportWizardAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.REPORT));
        setEnabled(false);

        CnAElementFactory.getInstance().addLoadListener(new IModelLoadListener() {

            public void closed(BSIModel model) {
                setEnabled(false);
            }

            public void loaded(BSIModel model) {
                setEnabled(true);
            }

            public void loaded(ISO27KModel model) {
                setEnabled(true);            
            }

        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Activator.inheritVeriniceContextState();

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.ShowExportWizardAction_1, IProgressMonitor.UNKNOWN);

                    ExportWizard wizard = new ExportWizard();
                    wizard.init(window.getWorkbench(), null);
                    final WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            dialog.open();
                        }
                    });
                }
            });
        } catch (InterruptedException e) {
            ExceptionUtil.log(e, Messages.ShowExportWizardAction_2);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.ShowExportWizardAction_3);
        }
    }

}
