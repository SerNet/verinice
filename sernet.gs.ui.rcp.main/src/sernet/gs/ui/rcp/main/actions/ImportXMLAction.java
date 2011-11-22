//Neu hinzugefügt vom Projektteam: XML import

package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.Perspective;
import sernet.gs.ui.rcp.main.bsi.dialogs.XMLImportDialog;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.RightEnabledUserInteraction;

public class ImportXMLAction extends ActionDelegate implements IViewActionDelegate, RightEnabledUserInteraction {
	
	public static final String ID = "sernet.gs.ui.rcp.main.importxmlaction";
	private IViewPart view;
	private Action action = null;
	
	public ImportXMLAction() {
//        setText("Import...");
//        setId(ID);
//        setEnabled(true);
    }
	
	public ImportXMLAction(IWorkbenchWindow window, String label) {
//        setText(label);
//		setId(ID);
//		action = this;
//		setEnabled(true);
	}

	public void run() {
		final XMLImportDialog dialog = new XMLImportDialog(Display.getCurrent().getActiveShell());	
        if (dialog.open() != Window.OK) {
            return;
        }
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        run();
    }
    
    @Override
    public void init(final IAction action){
        if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
            IInternalServerStartListener listener = new IInternalServerStartListener(){
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if(e.isStarted()){
                        action.setEnabled(checkRights());
                    }
                }

            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            action.setEnabled(checkRights());
        }
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

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // TODO Auto-generated method stub
        
    }


}

