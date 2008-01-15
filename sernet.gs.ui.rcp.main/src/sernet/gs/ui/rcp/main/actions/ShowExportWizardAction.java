package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ICommandIds;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.bsi.wizards.ExportWizard;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;


public class ShowExportWizardAction extends Action {
	
	public static final String ID = "sernet.gs.ui.rcp.main.showexportwizardaction";
	private final IWorkbenchWindow window;
	
	public ShowExportWizardAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
		setId(ID);
		setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.REPORT));
		setEnabled(false);
		CnAElementFactory.getInstance().addLoadListener(new IModelLoadListener() {

			public void closed(BSIModel model) {
				setEnabled(false);
			}

			public void loaded(BSIModel model) {
				setEnabled(true);
			}
			
		});
	}
	
	
	public void run() {
		try {
			PlatformUI.getWorkbench().getProgressService().
			busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Öffne OpenOffice Export...", IProgressMonitor.UNKNOWN);
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
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Öffnen von OO Export fehlgeschlagen.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Öffnen von OO Export fehlgeschlagen.");
		}
	}
	
}
