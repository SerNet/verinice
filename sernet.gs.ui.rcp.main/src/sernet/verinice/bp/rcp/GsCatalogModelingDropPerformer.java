/*******************************************************************************
 * Copyright (c) Urs Zeidler - uz[at]sernet.de
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
 *     Urs Zeidler - uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.IGSModel;
import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IGSModelElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.VeriniceElementTransfer;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.iso27k.rcp.GS2BSITransformOperation;
import sernet.verinice.iso27k.rcp.action.DropPerformer;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;
import sernet.verinice.iso27k.service.GS2BSITransformService;
import sernet.verinice.iso27k.service.ItemTransformException;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.service.parser.GSScraperUtil;

/**
 * This drop performer class transforms the gs types {@link Gefaehrdung} and
 * {@link Massnahme} into {@link BpThreat} and {@link Safeguard} and adds them
 * to the {@link Group}. A {@link Baustein} will transform all child depend on
 * the given target group.
 *
 * @see MetaDropAdapter
 * @author Urs Zeidler - uz[at]sernet.de
 */
public class GsCatalogModelingDropPerformer implements DropPerformer, RightEnabledUserInteraction {
    // TODO : GsCatalogModelingDropPerformer and BbModelingDropPerformer
    // share a lot in common could have a common class. Also other DropPerformer.

    /**
     * Strategy implementation for the {@link GS2BSITransformOperation}.
     * Transforms {@link Baustein}, {@link Gefaehrdung} and {@link Massnahme} to
     * their counterparts {@link BpThreat} and {@link Safeguard} in the
     * modernized grundschutz.
     *
     * @author uz[at]sernet.de
     *
     */
    private final class GsItem2BpTransformer implements GS2BSITransformService.ItemTransformer {

        @Override
        public void transformElement(Group<?> group, Object item, List<CnATreeElement> elements) {
            if (item instanceof Baustein) {
                Baustein b = (Baustein) item;
                if (group instanceof BpThreatGroup) {
                    List<Gefaehrdung> gefaehrdungen = b.getGefaehrdungen();
                    for (Gefaehrdung g : gefaehrdungen) {
                        transformGefaerdung(group, elements, g);
                    }
                } else if (group instanceof SafeguardGroup) {
                    for (Massnahme m : b.getMassnahmen()) {
                        transformMaßnahme(group, elements, m);
                    }
                }
            } else if (item instanceof Massnahme) {
                Massnahme m = (Massnahme) item;
                transformMaßnahme(group, elements, m);
            } else if (item instanceof Gefaehrdung) {
                Gefaehrdung g = (Gefaehrdung) item;
                transformGefaerdung(group, elements, g);
            }
        }

        /**
         * Transform {@link Gefaehrdung} to {@link BpThreat}
         *
         * @param group
         *            - the target group
         * @param elements
         *            - the list of transformed objects
         * @param g
         *            - the source object
         */
        private void transformGefaerdung(Group<?> group, List<CnATreeElement> elements, Gefaehrdung g) {
            BpThreat bpThreat = new BpThreat(group);
            bpThreat.setTitel(g.getId() + " " + g.getTitel()); //$NON-NLS-1$
            String description = "";
            try {
                description = GSScraperUtil.getInstance().getModel().getMassnahmeHtml(g.getUrl(), g.getStand());
                bpThreat.setObjectBrowserDescription(description);
            } catch (GSServiceException e) {
                log.error("Error setting description for safeguard", e); //$NON-NLS-1$
            }
            elements.add(bpThreat);
        }

        /**
         * Transform {@link Massnahme} to {@link Safeguard}
         *
         * @param group
         *            - the target group
         * @param elements
         *            - the list of transformed objects
         * @param m
         *            - the source object
         */
        private void transformMaßnahme(Group<?> group, List<CnATreeElement> elements, Massnahme m) {
            Safeguard safeguard = new Safeguard(group);
            safeguard.setTitle(m.getId() + " " + m.getTitel()); //$NON-NLS-1$
            String description = "";
            try {
                description = GSScraperUtil.getInstance().getModel().getMassnahmeHtml(m.getUrl(), m.getStand());
                safeguard.setObjectBrowserDescription(description);
            } catch (GSServiceException e) {
                log.error("Error setting description for safeguard", e); //$NON-NLS-1$
            }
            elements.add(safeguard);
        }
    }

    private static final Logger log = Logger.getLogger(GsCatalogModelingDropPerformer.class);
    private static final List<String> supportedDropTypeIds;
    static {
        supportedDropTypeIds = new ArrayList<>(2);
        supportedDropTypeIds.add(SafeguardGroup.TYPE_ID);
        supportedDropTypeIds.add(BpThreatGroup.TYPE_ID);
    }

    private boolean isActive = false;
    private CnATreeElement targetElement = null;

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.iso27k.rcp.action.DropPerformer#performDrop(java.lang.
     * Object, java.lang.Object, org.eclipse.jface.viewers.Viewer)
     */
    @Override
    public boolean performDrop(Object data, Object target, Viewer viewer) {
        List<IGSModel> draggedModules = getDraggedElements(data);
        if (log.isDebugEnabled()) {
            logParameter(draggedModules, targetElement);
        }
        transformDroppedElements(draggedModules, targetElement, viewer);
        return true;
    }

    private void transformDroppedElements(List<IGSModel> draggedModules, CnATreeElement target, Viewer viewer) {
        if (!isActive()) {
            return;
        }

        TreeSelection oldSelection = (TreeSelection) viewer.getSelection();
        if (log.isDebugEnabled()) {
            log.debug("performDrop..."); //$NON-NLS-1$
        }
        // because of validateDrop only Groups can be a target
        Group<?> group = (Group<?>) target;
        if (!CnAElementHome.getInstance().isNewChildAllowed(group)) {
            if (log.isDebugEnabled()) {
                log.debug("User is not allowed to add elements to this group"); //$NON-NLS-1$
            }
            return;
        }
        try {
            GS2BSITransformOperation operation = new GS2BSITransformOperation(group, draggedModules, new GsItem2BpTransformer());

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            progressService.run(true, true, operation);
            String message = MessageFormat.format(Messages.GsCatalogModelingDropPerformer_finished_dialog_message,
                    operation.getNumberProcessed(), ((Group<?>) target).getTitle());
            IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
            displayToggleDialog(message, Messages.GsCatalogModelingDropPerformer_finished_dialog_title,
                    Messages.GsCatalogModelingDropPerformer_finished_dialog_toggle_message, preferenceStore,
                    PreferenceConstants.INFO_CONTROLS_TRANSFORMED_TO_MODERNIZED_GS);

            // Restore old selection in tree
            if (!oldSelection.isEmpty()) {
                viewer.setSelection(oldSelection);
            }
        } catch (ItemTransformException e) {
            log.error(Messages.GsCatalogModelingDropPerformer_transform_error_message, e);
            showException(e);
        } catch (InvocationTargetException e) {
            log.error(Messages.GsCatalogModelingDropPerformer_transform_error_message, e);
            Throwable t = e.getTargetException();
            if (t instanceof ItemTransformException) {
                showException((ItemTransformException) t);
            } else {
                ExceptionUtil.log(e, Messages.GsCatalogModelingDropPerformer_transform_error_message);
            }
        } catch (Exception e) {
            log.error(Messages.GsCatalogModelingDropPerformer_transform_error_message, e);
            ExceptionUtil.log(e, Messages.GsCatalogModelingDropPerformer_transform_error_message);
        }
    }

    private void showException(ItemTransformException e) {
        final String message = Messages.GsCatalogModelingDropPerformer_exception_dialog_message + e.getMessage();
        MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.GsCatalogModelingDropPerformer_exception_dialog_title, message);
    }


    /**
     * Displays a toggle dialog when active after the transform process is done.
     *
     * @param message
     *            - the dialog message
     * @param titel
     *            - the title of the dialog
     * @param toggleMessage
     *            - the message explain he toggle function
     * @param preferenceStore
     *            - the preference Store to get the preferences from
     * @param preferenceConstant
     *            - the preference identifier
     */
    private void displayToggleDialog(String message, String titel, String toggleMessage, IPreferenceStore preferenceStore, String preferenceConstant) {
        boolean dontShow = preferenceStore.getBoolean(preferenceConstant);
        if (!dontShow) {
            MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), titel, message, toggleMessage, dontShow, preferenceStore, preferenceConstant);
            preferenceStore.setValue(preferenceConstant, dialog.getToggleState());
        }
    }

    private List<IGSModel> getDraggedElements(Object data) {
        List<IGSModel> elementList = null;
        if (data instanceof Object[]) {
            elementList = new LinkedList<>();
            for (Object o : (Object[]) data) {
                if (o instanceof IGSModel) {
                    elementList.add((IGSModel) o);
                }
            }
        } else {
            elementList = Collections.emptyList();
        }
        return elementList;
    }

    /*
     * @see
     * sernet.verinice.iso27k.rcp.action.DropPerformer#validateDrop(java.lang.
     * Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object rawTarget, int operation, TransferData transferData) {
        if (!checkRights()) {
            log.debug("CheckRights() failed, return false");//$NON-NLS-1$
            isActive = false;

        } else {
            if (!getTransfer().isSupportedType(transferData)) {
                log.debug("Unsupported type of TransferData: " + transferData.type);//$NON-NLS-1$
                this.targetElement = null;
            } else {
                this.targetElement = getTargetElement(rawTarget);
            }
            isActive = isTargetElement();
        }
        return isActive;
    }

    /*
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
     */
    @Override
    public boolean isActive() {
        return isActive;
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.BASEPROTECTIONMODELING;
    }

    /*
     * @see
     * sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.
     * lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // nothing to do
    }

    private CnATreeElement getTargetElement(Object target) {
        if (log.isDebugEnabled()) {
            log.debug("Target: " + target); //$NON-NLS-1$
        }
        CnATreeElement element = null;
        if (target instanceof CnATreeElement) {
            element = (CnATreeElement) target;
            if (!supportedDropTypeIds.contains(element.getTypeId())) {
                if (log.isDebugEnabled()) {
                    log.debug("Unsupported type of target element: " + element.getTypeId());//$NON-NLS-1$
                }
                element = null;
            }
        }
        return element;
    }

    protected boolean isTargetElement() {
        return this.targetElement != null;
    }

    protected VeriniceElementTransfer getTransfer() {
        return IGSModelElementTransfer.getInstance();
    }

    private void logParameter(List<IGSModel> draggedElements, CnATreeElement targetElementParam) {
        log.debug("Module(s):");//$NON-NLS-1$
        for (IGSModel module : draggedElements) {
            log.debug(module);
        }
        log.debug("is/are modeled with: " + targetElementParam + "...");//$NON-NLS-1$ //$NON-NLS-2$
    }
}
