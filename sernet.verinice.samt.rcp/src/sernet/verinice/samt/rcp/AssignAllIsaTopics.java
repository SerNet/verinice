package sernet.verinice.samt.rcp;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dialogs.CnATreeElementSelectionDialog;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.rcp.InfoDialogWithShowToggle;

@SuppressWarnings("restriction")
public class AssignAllIsaTopics implements IObjectActionDelegate, RightEnabledUserInteraction {

    private static final Logger LOG = Logger.getLogger(AssignAllIsaTopics.class);
    
    private IWorkbenchPart targetPart;

    private List<CnATreeElement> selectedElementList = new LinkedList<CnATreeElement>();
    
    private int numberOfTopics = 0;
    
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    @Override
    public void run(IAction action) {
        IWorkbenchWindow window = targetPart.getSite().getWorkbenchWindow();
        CnATreeElement firstSelected = null;
        if(selectedElementList!=null && !selectedElementList.isEmpty()) {
            firstSelected = selectedElementList.get(0);
        }
        CnATreeElementSelectionDialog dialog = new CnATreeElementSelectionDialog(window.getShell(), PersonIso.TYPE_ID, firstSelected);
        if (dialog.open() != Window.OK) {
            return;
        }
        final List<CnATreeElement> personList = dialog.getSelectedElements();
        
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
        
        try {
            progressService.run(true, true, new IRunnableWithProgress() {  
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();
                    numberOfTopics = 0;
                    for (CnATreeElement element : selectedElementList) {
                        if(element instanceof Audit) {
                            Audit audit = (Audit) element;
                            ControlGroup controlGroup = audit.getControlGroup();
                            linkChildren(controlGroup,personList);
                        }
                       
                    }
                }
            });
            InfoDialogWithShowToggle.openInformation(
                    Messages.AssignAllIsaTopics_0,  
                    Messages.bind(Messages.AssignAllIsaTopics_1, numberOfTopics), 
                    Messages.AssignAllIsaTopics_2,
                    SamtPreferencePage.INFO_CONTROLS_LINKED);
        } catch (InvocationTargetException t) {
            LOG.error("Error while creating tasks.",t); //$NON-NLS-1$
            ExceptionUtil.log(t, Messages.AssignAllIsaTopics_3); 
        }  catch (InterruptedException e){
            LOG.error("Error while creating tasks.",e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AssignAllIsaTopics_3);            
        }
           
    }

    private void linkChildren(CnATreeElement controlGroup, List<CnATreeElement> personList) {
        if(controlGroup!=null && personList!=null) {
            controlGroup = Retriever.checkRetrieveChildren(controlGroup);
            Set<CnATreeElement> children = controlGroup.getChildren();
            for (CnATreeElement child : children) {
                if(child!=null) {
                    if( ControlGroup.TYPE_ID.equals(child.getTypeId())) {
                        linkChildren((ControlGroup) child, personList);
                    }
                    if( child instanceof IControl ) {
                        CnAElementHome.getInstance().createLinksAccordingToBusinessLogic(child, personList,SamtTopic.REL_SAMTTOPIC_PERSON_ISO);
                        numberOfTopics++;
                    }
                }
            }
        }
        
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(checkRights());
        if(selection instanceof IStructuredSelection) {
            selectedElementList.clear();
            for (Iterator iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
                Object sel = iterator.next();
                if (sel instanceof CnATreeElement) {
                    selectedElementList.add((CnATreeElement)sel);
                }
            }
        }
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
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.ASSIGNALLISATOPICS;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // Do nothing
    }

}
