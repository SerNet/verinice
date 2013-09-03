package sernet.verinice.fei.rcp;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.rcp.IllegalSelectionException;
import sernet.verinice.rcp.InfoDialogWithShowToggle;
import sernet.verinice.rcp.RightsEnabledHandler;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class FileElementImportHandler extends RightsEnabledHandler {
	
    private static final Logger LOG = Logger.getLogger(FileElementImportHandler.class);
    private ISchedulingRule iSchedulingRule = new Mutex();
    
    private int numberOfFiles = 0;
    private List<FileExceptionNoStop> errorList;
    
    /**
	 * The constructor.
	 */
	public FileElementImportHandler() {
	    super();
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
	    try {
    	    final CnATreeElement selectedElement = getSelectedElement(event);
    	    
            IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
    	    final String path = selectDirectory(window.getShell());
    	    if(selectedElement instanceof IISO27kGroup && path!=null) {
    
                IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                progressService.run(true, true, new IRunnableWithProgress() {                 
                    @Override
                    public void run(IProgressMonitor arg0) throws InvocationTargetException, InterruptedException {
                        try { 
                            importFiles(new String[]{path}, (Group<CnATreeElement>)selectedElement);                     
                        } catch (Exception e) {
                            LOG.error("Error while importing data.", e); //$NON-NLS-1$
                        }
                    }
                });
                showResult();              
            }
	    } catch (Exception e) {
            LOG.error("Error while importing files and elements", e); //$NON-NLS-1$
        }
		return null;
	}
	

    private void importFiles(String[] filePathes, Group<CnATreeElement> target) {
        Activator.inheritVeriniceContextState();
        numberOfFiles=0;
        for (String file : filePathes) {
            FileElementImportTraverser traverser = new FileElementImportTraverser(file, target);
            traverser.traverseFileSystem();
            numberOfFiles += traverser.getNumberOfFiles();
            addErrors(traverser.getErrorList());
        }
        
    }
    
    private void addErrors(List<FileExceptionNoStop> errorList) {
        if(errorList!=null && !errorList.isEmpty()) {
            if(this.errorList==null) {
                this.errorList = new LinkedList<FileExceptionNoStop>();
            }
            this.errorList.addAll(errorList);
        }      
    }
    
    protected void showResult() {
        if(!Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.FEI_SHOW_RESULT)) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialogWithToggle dialog = InfoDialogWithShowToggle.openInformation(
                        Messages.FileElementDropAdapter_0,  
                        createResultMessage(),
                        Messages.FileElementDropAdapter_8,
                        PreferenceConstants.FEI_SHOW_RESULT);
                }
            });
        }
    }
    
    private String createResultMessage() {
        StringBuilder sb = new StringBuilder();        
        sb.append(NLS.bind(Messages.FileElementDropAdapter_7,numberOfFiles));
        if(errorList!=null && !errorList.isEmpty()) {
            sb.append("\n\n"); //$NON-NLS-1$
            sb.append(Messages.FileElementDropAdapter_9).append("\n"); //$NON-NLS-1$
            for (FileExceptionNoStop error : errorList) {         
                sb.append(NLS.bind(Messages.FileElementDropAdapter_10,error.getPath(),error.getMessage())).append("\n"); //$NON-NLS-1$               
            }
        }
        return sb.toString();
    }
    
    private String selectDirectory(Shell shell) {
        DirectoryDialog dialog = new DirectoryDialog(shell);
        return dialog.open();
    }

    /**
     * Returns the selected tree element from an event. If there is more than one
     * selected element or selected element is not an
     * element a {@link IllegalSelectionException} is thrown.
     * 
     * @param event A ExecutionEvent
     * @return A list with selected {@link CnATreeElement}s
     * @throws IllegalSelectionException
     */
    @SuppressWarnings("unchecked")
    private CnATreeElement getSelectedElement(ExecutionEvent event) {
        CnATreeElement element = null;
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof StructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            try {
                List<CnATreeElement> elements = structuredSelection.toList();
                if(elements!=null && elements.size()>1) {
                    throw new IllegalSelectionException("More than one element selected."); //$NON-NLS-1$
                }
                if(elements!=null && elements.size()==1) {
                    element = elements.get(0);
                }
            } catch (ClassCastException e) {
                LOG.warn("One of the selected element is not a CnATreeElement. Will not return any selected element."); //$NON-NLS-1$
                if (LOG.isDebugEnabled()) {
                    LOG.debug("stackstrace: ", e); //$NON-NLS-1$
                }
                throw new IllegalSelectionException("Wrong element selected."); //$NON-NLS-1$
            }
        }
        return element;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */    
    @Override
    public String getRightID() {
        return ActionRightIDs.ADDFILE;
    }
}
