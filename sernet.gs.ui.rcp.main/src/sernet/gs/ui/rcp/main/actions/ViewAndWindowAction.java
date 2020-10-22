/*******************************************************************************
 * Copyright (c) 2021 Urs Zeidler.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public abstract class ViewAndWindowAction extends RightsEnabledAction
        implements ISelectionListener, ISelectionChangedListener {

    private IWorkbenchWindow window;
    private IViewSite site;

    protected ViewAndWindowAction(String rightID, String text) {
        super(rightID, text);
    }

    protected Shell getShell() {
        return window == null ? site.getShell() : window.getShell();
    }

    protected void setWindow(IWorkbenchWindow window) {
        if (this.site != null) {
            throw new IllegalStateException("Cannot set window when site is set");
        }
        this.window = window;
        window.getSelectionService().addSelectionListener(this);
    }

    protected void setSite(IViewSite site) {
        if (this.window != null) {
            throw new IllegalStateException("Cannot set site when window is set");
        }
        this.site = site;
        site.getSelectionProvider().addSelectionChangedListener(this);
    }

    /**
     * Get the current selection from the windows or the view.
     */
    private ISelection getCurrentSelection() {
        return window == null ? site.getSelectionProvider().getSelection()
                : window.getSelectionService().getSelection();
    }

    protected abstract void selectionChanged(IStructuredSelection structuredSelection);
    protected abstract void doRun(IStructuredSelection structuredSelection);

    public final void doRun() {
        ISelection currentSelection = getCurrentSelection();
        if (currentSelection instanceof IStructuredSelection) {
            doRun((IStructuredSelection) currentSelection);
        }
    }
    
    @Override
    public final void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection structuredSelection = event.getStructuredSelection();
        selectionChanged(structuredSelection);
    }

    /**
     * Action is not enabled when the selection is not a IStructuredSelection.
     */
    @Override
    public final void selectionChanged(IWorkbenchPart part, ISelection input) {
        if (input instanceof IStructuredSelection) {
            selectionChanged((IStructuredSelection) input);
        } else {
            setEnabled(false);
        }
    }

}
