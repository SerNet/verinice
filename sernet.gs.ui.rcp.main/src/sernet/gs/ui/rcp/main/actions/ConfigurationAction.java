package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.BulkEditDialog;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;

public class ConfigurationAction implements IObjectActionDelegate {

	public static final String ID = "sernet.gs.ui.rcp.main.personconfiguration";


	private Configuration configuration;

	private IWorkbenchPart targetPart;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		
		IWorkbenchWindow window = targetPart.getSite().getWorkbenchWindow();
		IStructuredSelection selection = (IStructuredSelection) window
				.getSelectionService().getSelection();
		if (selection == null)
			return;
		EntityType entType = null;
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			try {
				Object o = iter.next();
				if (o == null || !(o instanceof Person))
					continue;

				Person elmt = null;
				if (o instanceof Person)
					elmt = (Person) o;

				Logger.getLogger(this.getClass()).debug(
						"Loading configuration for user " + elmt.getTitel());
				LoadConfiguration command = new LoadConfiguration(elmt);
				command = ServiceFactory.lookupCommandService().executeCommand(
						command);
				configuration = command.getConfiguration();

				if (configuration == null) {
					// create new configuration
					Logger
							.getLogger(this.getClass())
							.debug(
									"No config found, creating new configuration object.");
					CreateConfiguration command2 = new CreateConfiguration(elmt);
					command2 = ServiceFactory.lookupCommandService()
							.executeCommand(command2);
					configuration = command2.getConfiguration();
				}

				entType = HUITypeFactory.getInstance().getEntityType(
						configuration.getEntity().getEntityType());
			} catch (CommandException e) {
				ExceptionUtil.log(e, "Fehler beim Laden der Konfiguration");
			} catch (RuntimeException e) {
				ExceptionUtil.log(e, "Fehler beim Laden der Konfiguration");
			}
		}

		final BulkEditDialog dialog = new BulkEditDialog(window.getShell(),
				entType, true, "Benutzereinstellungen", configuration.getEntity());
		if (dialog.open() != InputDialog.OK)
			return;

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							// save configuration:
							SaveElement<Configuration> command = new SaveElement<Configuration>(configuration);
							try {
								command = ServiceFactory.lookupCommandService()
										.executeCommand(command);
							} catch (CommandException e) {
								ExceptionUtil.log(e, "Fehler beim Speichern der Konfiguration.");
							}
						}

					});
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Fehler beim Speichern der Konfiguration.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Abgebrochen.");
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {}

}
