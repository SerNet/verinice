/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 * Contributors:
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.interfaces.report.ReportTypeException;
import sernet.verinice.rcp.RightsEnabledActionDelegate;
import sernet.verinice.report.rcp.GenerateReportDialog;
import sernet.verinice.report.rcp.Messages;

/**
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public abstract class ReportAction extends RightsEnabledActionDelegate
        implements IWorkbenchWindowActionDelegate, RightEnabledUserInteraction {

    private static final Logger LOG = Logger.getLogger(ReportAction.class);

    Shell shell;

    protected GenerateReportDialog dialog;
    protected List<?> rootObjects;

    private boolean generationSuccessful = false;

    private boolean isContextMenuCall;

    @Override
    public void init(IWorkbenchWindow window) {
        try {
            shell = window.getShell();
        } catch (Exception t) {
            LOG.error("Error creating dialog", t); //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.rcp.RightsEnabledActionDelegate#doRun(org.eclipse.jface.
     * action.IAction)
     */
    @Override
    public void doRun(IAction action) {
        try {
            openDialog();
            if (dialog.open() == Dialog.OK) {
                final IReportOptions ro = prepareReportOptions();

                PlatformUI.getWorkbench().getProgressService()
                        .busyCursorWhile(new IRunnableWithProgress() {
                            @Override
                            public void run(IProgressMonitor monitor)
                                    throws InvocationTargetException, InterruptedException {
                                runUIBlockingReportGeneration(ro, monitor);
                            }
                        });

            }
        } catch (Exception t) {
            ExceptionUtil.log(t, Messages.GenerateReportDialog_32);
            setGenerationSuccessful(Boolean.FALSE);
        }

        provideUserFeedback();
    }

    protected void openDialog() {
        if (!isContextMenuCall() || rootObjects == null || rootObjects.isEmpty()) {
            dialog = new GenerateReportDialog(shell);
        } else if (rootObjects.size() == 1 && isContextMenuCall()) {
            dialog = new GenerateReportDialog(shell, rootObjects.get(0));
        } else if (rootObjects != null && rootObjects.size() > 1 && isContextMenuCall()) {
            dialog = new GenerateReportDialog(shell, rootObjects,
                    IReportType.USE_CASE_ID_GENERAL_REPORT);
        }
        dialog.setContextMenuCall(isContextMenuCall());
    }

    private IReportOptions prepareReportOptions() {
        final IReportOptions ro = new IReportOptions() {
            Integer rootElmt;
            Integer[] rootElmts;

            @Override
            public boolean isToBeEncrypted() {
                return false;
            }

            @Override
            public boolean isToBeCompressed() {
                return false;
            }

            @Override
            public IOutputFormat getOutputFormat() {
                return dialog.getOutputFormat();
            }

            @Override
            public File getOutputFile() {
                return dialog.getOutputFile();
            }

            @Override
            public void setRootElement(Integer rootElement) {
                rootElmt = rootElement;
            }

            @Override
            public Integer getRootElement() {
                return rootElmt;
            }

            @Override
            public Integer[] getRootElements() {
                return rootElmts;
            }

            @Override
            public void setRootElements(Integer[] rootElements) {
                this.rootElmts = rootElements;
            }

            @Override
            public String getServerURL() {
                return Activator.getDefault().getPreferenceStore()
                        .getString(PreferenceConstants.VNSERVER_URI);
            }
        };
        if (dialog.getRootElement() != null) {
            ro.setRootElement(dialog.getRootElement());
        } else if (dialog.getRootElements() != null && dialog.getRootElements().length > 0) {
            ro.setRootElements(dialog.getRootElements());
        }
        return ro;
    }

    private void provideUserFeedback() {
        if (isGenerationSuccessful()) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    String path = dialog.getOutputFile().getAbsolutePath();
                    String reportName = dialog.getReportMetaData().getOutputname();
                    MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
                            Messages.GenerateReportDialog_30,
                            Messages.bind(Messages.GenerateReportDialog_31,
                                    new Object[] { reportName, path }));
                }
            });
        }
    }

    private void runUIBlockingReportGeneration(final IReportOptions ro, IProgressMonitor monitor) {
        monitor.beginTask(Messages.GenerateReportAction_1, IProgressMonitor.UNKNOWN);
        Activator.inheritVeriniceContextState();
        try {
            dialog.getReportType().createReport(ro);
            dialog.getReportType().createReport(dialog.getReportMetaData());
            setGenerationSuccessful(true);
        } catch (ReportTypeException exception) {
            if (exception.causedBySecurityException()) {
                ExceptionUtil.log(exception, Messages.GenerateReportDialog_34);
            } else {
                ExceptionUtil.log(exception, Messages.GenerateReportDialog_32);
            }
            setGenerationSuccessful(false);
        } finally {
            monitor.done();
        }
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.GENERATEORGREPORT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.
     * action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            rootObjects = treeSelection.toList();
        }
    }

    public boolean isContextMenuCall() {
        return isContextMenuCall;
    }

    /**
     * @return the generationSuccessful
     */
    private boolean isGenerationSuccessful() {
        return generationSuccessful;
    }

    /**
     * @param generationSuccessful
     *            the generationSuccessful to set
     */
    private void setGenerationSuccessful(boolean generationSuccessful) {
        this.generationSuccessful = generationSuccessful;
    }

}
