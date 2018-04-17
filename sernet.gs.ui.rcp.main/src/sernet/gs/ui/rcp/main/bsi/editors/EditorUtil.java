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

import java.util.Set;

import sernet.gs.service.StringUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.hui.common.connect.IIdentifiableElement;
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

    private static final int MAX_TITLE_LENGTH = 20;

    private EditorUtil() {
        super();
    }

    /**
     * Some {@link CnATreeElement} can change the state of linked
     * {@link CnATreeElement}. This method is the main handler. As long as we
     * don't have any eventing active for the {@link CnATreeElement}.
     */
    public static void updateDependentObjects(CnATreeElement cnAElement) {
        if (cnAElement == null) {
            return;
        }
        if (Safeguard.TYPE_ID.equals(cnAElement.getTypeId())) {
            updateImplementationStatusBySafeguard(cnAElement);
        } else if (BpRequirement.TYPE_ID.equals(cnAElement.getTypeId())) {
            updateRequirementImplementationStatus(cnAElement);
        }
    }

    /**
     * When the deduction of the {@link BpRequirement} is set to active it needs
     * to apply the implementation status from the {@link Safeguard}. As the
     * changed object is not returned by the call to
     * {@link sernet.gs.ui.rcp.main.common.model.CnAElementHome#updateEntity(CnATreeElement)} in the editor (see
     * {@link sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditor#save()}), we
     * need to change the state in our local copy.
     */
    private static void updateRequirementImplementationStatus(CnATreeElement cnAElement) {
        Set<CnALink> linksDown = cnAElement.getLinksDown();
        for (CnALink cnALink : linksDown) {
            CnATreeElement dependency = cnALink.getDependency();
            if (Safeguard.TYPE_ID.equals(dependency.getTypeId())) {
                DeductionImplementationUtil.setImplementationStausToRequirement(dependency,
                        cnAElement);
            }
        }
    }

    /**
     * A {@link Safeguard} can change the state of a requirement so we need to
     * handle them here. As the state of the {@link BpRequirement} is changed on
     * the server and we do not want to reload the object from remote we simply
     * change the state in the client also. Currently the implementation state
     * can be shared between the {@link Safeguard} and the
     * {@link BpRequirement}.
     */
    private static void updateImplementationStatusBySafeguard(CnATreeElement cnAElement) {
        Set<CnALink> linksUp = cnAElement.getLinksUp();
        for (CnALink cnALink : linksUp) {
            CnATreeElement dependency = cnALink.getDependant();
            if (BpRequirement.TYPE_ID.equals(dependency.getTypeId()) && DeductionImplementationUtil
                    .setImplementationStausToRequirement(cnAElement, dependency)) {
                CnAElementFactory.getModel(dependency).childChanged(dependency);
            }
        }
    }

    public static String getEditorName(CnATreeElement cnATreeElement) {
        String elementTitle = getElementTitle(cnATreeElement);
        return StringUtil.truncate(elementTitle, MAX_TITLE_LENGTH);
    }

    public static String getEditorToolTipText(CnATreeElement cnATreeElement) {
        return getElementTitle(cnATreeElement);
    }

    private static String getElementTitle(CnATreeElement cnATreeElement) {
        if (cnATreeElement instanceof IIdentifiableElement) {
            return ((IIdentifiableElement) cnATreeElement).getFullTitle();
        }
        return cnATreeElement.getTitle();
    }

}
