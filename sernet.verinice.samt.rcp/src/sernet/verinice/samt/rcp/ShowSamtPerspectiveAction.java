package sernet.verinice.samt.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.WorkbenchException;

public class ShowSamtPerspectiveAction implements IWorkbenchWindowActionDelegate {
    
    private static final Logger LOG = Logger.getLogger(ShowSamtPerspectiveAction.class);

    private IWorkbenchWindow window;

    @Override
    public void dispose() {
        this.window=null;
    }

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    @SuppressWarnings("restriction")
    @Override
    public void run(IAction action) {
        // Switch to AuditPerspective
        IPerspectiveDescriptor activePerspective = window.getActivePage().getPerspective();
        if(activePerspective==null || !activePerspective.getId().equals(SamtPerspective.ID)) {           
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    // switch perspective           
                    try {
                        window.getWorkbench().showPerspective(SamtPerspective.ID,window);
                    } catch (WorkbenchException e) {
                        LOG.error("Can not switch to perspective: " + SamtPerspective.ID, e);
                    }
                }
            });
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

}


