package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.sernet.sync.sync.SyncRequest;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.wizards.ImportCSVWizard;
import sernet.gs.ui.rcp.main.bsi.wizards.Messages;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;

public class ImportCSVAction extends RightsEnabledAction {

    private static final Logger LOG = Logger.getLogger(ImportCSVAction.class);

    public static final String ID = "sernet.gs.ui.rcp.main.importcsvaction"; //$NON-NLS-1$

    private SyncRequest sr = null;

    private boolean insert;
    private boolean update;
    private boolean delete;

    public ImportCSVAction(IWorkbenchWindow window, String label) {
        super(ActionRightIDs.IMPORTCSV, label);
        setId(ID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        // Display.getCurrent().getActiveShell()
        ImportCSVWizard wizard = new ImportCSVWizard();
        final WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
        int resultFromWizardDialog = wizardDialog.open();
        if (resultFromWizardDialog == WizardDialog.CANCEL) {
            return;
        }
        sr = wizard.getSyncRequest();
        insert = wizard.getInsertState();
        update = wizard.getUpdateState();
        delete = wizard.getDeleteState();

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask(Messages.ImportCSVAction_1, IProgressMonitor.UNKNOWN);
                        Activator.inheritVeriniceContextState();
                        try {
                            doImport();
                        } catch (Exception e) {
                            LOG.error("Error while importing CSV data.", e); //$NON-NLS-1$
                            throw new RuntimeException("Error while importing CSV data.", e); //$NON-NLS-1$
                        }
                    } finally {
                        if (monitor != null) {
                            monitor.done();
                        }
                    }
                }
            });
        } catch (Exception e) {
            LOG.error("Error while importing CSV data.", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.ImportCSVWizard_1);
        }
    }

    protected void doImport() throws CommandException, SyncParameterException {
        SyncCommand command = new SyncCommand(new SyncParameter(insert, update, delete, false, SyncParameter.EXPORT_FORMAT_XML_PURE), sr);

        command = ServiceFactory.lookupCommandService().executeCommand(command);

        Set<CnATreeElement> importRootObjectSet = command.getImportRootObject();
        Set<CnATreeElement> changedElement = command.getElementSet();
        updateModel(importRootObjectSet, changedElement);
        if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)) {
            createValidations(changedElement);
        }
    }

    private void updateModel(Set<CnATreeElement> importRootObjectSet, Set<CnATreeElement> changedElement) {
        if (importRootObjectSet != null && !importRootObjectSet.isEmpty()) {
            for (CnATreeElement importRootObject : importRootObjectSet) {
                CnAElementFactory.getModel(importRootObject).childAdded(importRootObject.getParent(), importRootObject);
                CnAElementFactory.getModel(importRootObject).databaseChildAdded(importRootObject);
                if (changedElement != null) {
                    for (CnATreeElement cnATreeElement : changedElement) {
                        CnAElementFactory.getModel(cnATreeElement).childAdded(cnATreeElement.getParent(), cnATreeElement);
                        CnAElementFactory.getModel(cnATreeElement).databaseChildAdded(cnATreeElement);
                    }
                }
            }
        } else {
            if (changedElement != null) {
                for (CnATreeElement cnATreeElement : changedElement) {
                    CnAElementFactory.getModel(cnATreeElement).childChanged(cnATreeElement);
                    CnAElementFactory.getModel(cnATreeElement).databaseChildChanged(cnATreeElement);
                }
            }
        }
    }

    private void createValidations(Set<CnATreeElement> elmts) {
        for (CnATreeElement elmt : elmts) {
            ServiceFactory.lookupValidationService().createValidationForSingleElement(elmt);
        }
        if (!elmts.isEmpty()) {
            CnAElementFactory.getModel(((CnATreeElement) elmts.toArray()[0])).validationAdded(((CnATreeElement) elmts.toArray()[0]).getScopeId());
        }
    }
}