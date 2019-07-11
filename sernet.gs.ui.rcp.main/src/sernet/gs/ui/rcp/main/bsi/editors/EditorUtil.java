/*******************************************************************************
 * Copyright (c) 2018 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import sernet.gs.service.StringUtil;
import sernet.gs.ui.rcp.main.common.model.CnATreeElementLabelGenerator;
import sernet.verinice.model.bp.DeductionImplementationUtil;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A collection of useful editor related method. Like calling update on
 * dependent objects
 */
public final class EditorUtil {

    private static final Logger logger = Logger.getLogger(EditorUtil.class);

    private static final int MAX_TITLE_LENGTH = 20;
    public static final String EMPTY_EDITOR_ID = "org.eclipse.ui.internal.emptyEditorTab"; //$NON-NLS-1$

    private EditorUtil() {
        super();
    }

    /**
     * Closes the editor for a given element by its uuid.
     */
    public static void closeEditorForElement(String uuid) {
        Stream.of(PlatformUI.getWorkbench().getWorkbenchWindows())
                .forEach(window -> Stream.of(window.getPages()).forEach(
                        page -> Stream.of(page.getEditorReferences()).forEach(editorReference -> {
                            try {
                                if (editorReference
                                        .getEditorInput() instanceof BSIElementEditorInput) {
                                    CnATreeElement element = ((BSIElementEditorInput) editorReference
                                            .getEditorInput()).getCnAElement();
                                    if (uuid.equals(element.getUuid())) {
                                        page.closeEditors(
                                                new IEditorReference[] { editorReference }, true);
                                    }
                                }

                            } catch (PartInitException e) {
                                logger.error("Error while closing element editor.", e);
                            }
                        })));
    }

    /**
     * Returns all related objects for the given type. The editor don't have the
     * information which objects depends from another, for example in the case
     * of deduction of implementation. All special cases of indirections need to
     * be handled here when the editor need to update other objects as the
     * edited object. <br>
     * The current cases are: {@link Safeguard}-> a list of linked
     * {@link BpRequirement}.<br>
     * {@link BpRequirement}-> a list of linked {@link Safeguard}.
     */
    public static List<CnATreeElement> getRelatedObjects(CnATreeElement cnAElement) {
        if (Safeguard.TYPE_ID.equals(cnAElement.getTypeId())) {
            return cnAElement.getLinksUp().stream().filter(
                    DeductionImplementationUtil::isRelevantLinkForImplementationStateDeduction)
                    .map(CnALink::getDependant).collect(Collectors.toList());
        } else if (BpRequirement.TYPE_ID.equals(cnAElement.getTypeId())) {
            return DeductionImplementationUtil.getSafeguardsFromRequirement(cnAElement);
        }
        return Collections.emptyList();
    }

    /**
     * Some {@link CnATreeElement} can change the state of linked
     * {@link CnATreeElement}. This method is the main handler. As long as we
     * don't have any eventing active for the {@link CnATreeElement}. This
     * method is used to synchronize the state of the client objects in the case
     * of a change occurring while save the object.
     */
    public static void updateDependentObjects(CnATreeElement cnAElement) {
        if (cnAElement == null) {
            return;
        }
        if (BpRequirement.TYPE_ID.equals(cnAElement.getTypeId())) {
            updateRequirementImplementationStatus(cnAElement);
        }
    }

    /**
     * When the deduction of the {@link BpRequirement} is set to active it needs
     * to apply the implementation status from the {@link Safeguard}. As the
     * changed object is not returned by the call to
     * {@link sernet.gs.ui.rcp.main.common.model.CnAElementHome#updateEntity(CnATreeElement)}
     * in the editor (see
     * {@link sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditor#save()}), we
     * need to change the state in our local copy.
     */
    private static void updateRequirementImplementationStatus(CnATreeElement cnAElement) {
        DeductionImplementationUtil.setImplementationStatusToRequirement(cnAElement);
    }

    public static String getEditorName(CnATreeElement cnATreeElement) {
        String elementTitle = getElementTitle(cnATreeElement);
        return StringUtil.truncate(elementTitle, MAX_TITLE_LENGTH);
    }

    public static String getEditorToolTipText(CnATreeElement cnATreeElement) {
        return getElementTitle(cnATreeElement);
    }

    private static String getElementTitle(CnATreeElement cnATreeElement) {
        return CnATreeElementLabelGenerator.getElementTitle(cnATreeElement);
    }

    /**
     * Cleans the old editorref. A patch for
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=386648 .
     */
    public static void cleanOldEditors() {
        try {
            IWorkbench wb = PlatformUI.getWorkbench();
            IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
            if (win == null)
                return;

            IWorkbenchWindow[] workbenchWindows = wb.getWorkbenchWindows();
            Arrays.stream(workbenchWindows).forEach(ww -> {
                IWorkbenchPage page = ww.getActivePage();
                Arrays.stream(page.getEditorReferences()).forEach(ref -> {
                    String editorId = ref.getId();
                    if (EMPTY_EDITOR_ID.equals(editorId)) {
                        page.closeEditors(new IEditorReference[] { ref }, false);
                    }
                });
            });
        } catch (Exception e) {
            logger.error("Error closing editors", e);
        }
    }

}
