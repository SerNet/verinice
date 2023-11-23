/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.fei.rcp;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IDropActionDelegate;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.rcp.InfoDialogWithShowToggle;
import sernet.verinice.rcp.RightEnabledUserInteraction;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class FileElementDropAdapter implements IDropActionDelegate, RightEnabledUserInteraction {

    private static final Logger LOG = Logger.getLogger(FileElementDropAdapter.class);

    private int numberOfFiles = 0;
    private List<FileExceptionNoStop> errorList;

    /*
     * @see org.eclipse.ui.part.IDropActionDelegate#run(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public boolean run(Object data, final Object target) {
        try {
            if (!checkRights()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("User is not allowed to perform this file drop.");
                }
                return false;
            }

            ByteArrayInputStream bai = new ByteArrayInputStream((byte[]) data);
            ObjectInputStream oi = new ObjectInputStream(bai);
            final String[] filePathes = (String[]) oi.readObject();

            boolean startImport = true;
            boolean doConfirm = !Activator.getDefault().getPreferenceStore()
                    .getBoolean(PreferenceConstants.FEI_DND_CONFIRM);

            if (doConfirm) {
                MessageDialogWithToggle dialog = InfoDialogWithShowToggle.openYesNoCancelQuestion(
                        Messages.FileElementDropAdapter_0, getMessage(filePathes),
                        Messages.FileElementDropAdapter_1, PreferenceConstants.FEI_DND_CONFIRM);
                startImport = IDialogConstants.YES_ID == dialog.getReturnCode();
            }

            if (startImport) {
                IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                progressService.run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor arg0)
                            throws InvocationTargetException, InterruptedException {
                        try {
                            importFiles(filePathes, (Group<CnATreeElement>) target);
                        } catch (Exception e) {
                            LOG.error("Error while importing data.", e); //$NON-NLS-1$
                        }
                    }
                });
                showResult();
            }
            return true;
        } catch (Exception e) {
            LOG.error("Error while performing file drop", e); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * @param filePathes
     * @param target
     */
    private void importFiles(String[] filePathes, Group<CnATreeElement> target) {
        Activator.inheritVeriniceContextState();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Importing files from: " + filePathes[0] + " to group: " + target.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        numberOfFiles = 0;
        for (String file : filePathes) {
            FileElementImportTraverser traverser = new FileElementImportTraverser(file, target);
            traverser.traverseFileSystem();
            numberOfFiles += traverser.getNumberOfFiles();
            addErrors(traverser.getErrorList());
        }
    }

    private void addErrors(List<FileExceptionNoStop> errorList) {
        if (errorList != null && !errorList.isEmpty()) {
            if (this.errorList == null) {
                this.errorList = new LinkedList<>();
            }
            this.errorList.addAll(errorList);
        }
    }

    protected void showResult() {
        if (!Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.FEI_SHOW_RESULT)) {
            Display.getDefault()
                    .asyncExec(() -> InfoDialogWithShowToggle.openInformation(
                            Messages.FileElementDropAdapter_0, createResultMessage(),
                            Messages.FileElementDropAdapter_8,
                            PreferenceConstants.FEI_SHOW_RESULT));
        }
    }

    private String createResultMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(NLS.bind(Messages.FileElementDropAdapter_7, numberOfFiles));
        if (errorList != null && !errorList.isEmpty()) {
            sb.append("\n\n"); //$NON-NLS-1$
            sb.append(Messages.FileElementDropAdapter_9).append("\n"); //$NON-NLS-1$
            for (FileExceptionNoStop error : errorList) {
                sb.append(NLS.bind(Messages.FileElementDropAdapter_10, error.getPath(),
                        error.getMessage())).append("\n"); //$NON-NLS-1$
            }
        }
        return sb.toString();
    }

    /**
     * @param filePathes
     * @return
     */
    private String getMessage(String[] filePathes) {
        String message = ""; //$NON-NLS-1$
        if (filePathes != null && filePathes.length == 1) {
            message = NLS.bind(Messages.FileElementDropAdapter_5, filePathes[0]);
        }
        if (filePathes != null && filePathes.length > 1) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String dir : filePathes) {
                if (!first) {
                    sb.append("\n"); //$NON-NLS-1$
                }
                sb.append("  ").append(dir); //$NON-NLS-1$
                first = false;
            }
            message = NLS.bind(Messages.FileElementDropAdapter_6, sb.toString());
        }
        return message;
    }

    private Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.ADDFILE;
    }

}
