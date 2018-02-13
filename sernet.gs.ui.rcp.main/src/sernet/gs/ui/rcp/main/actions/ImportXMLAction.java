//Neu hinzugefügt vom Projektteam: XML import

package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import sernet.gs.ui.rcp.main.bsi.dialogs.XMLImportDialog;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.rcp.RightsEnabledActionDelegate;

/**
 * Eclipse ActionDelegate which is called to import XML or VNA data.
 * Rights profile management is enabled for this action.
 * 
 * Action opens {@link XMLImportDialog} to import data.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ImportXMLAction extends RightsEnabledActionDelegate implements IViewActionDelegate, RightEnabledUserInteraction {
	
	public static final String ID = "sernet.gs.ui.rcp.main.importxmlaction";

	public void run() {
		final XMLImportDialog dialog = new XMLImportDialog(Display.getCurrent().getActiveShell(), false);	
        if (dialog.open() != Window.OK) {
            return;
        }
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void doRun(IAction action) {
        run();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {   
    }
    
    @Override
    public String getRightID(){
        return ActionRightIDs.XMLIMPORT;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) { 
    }

}

