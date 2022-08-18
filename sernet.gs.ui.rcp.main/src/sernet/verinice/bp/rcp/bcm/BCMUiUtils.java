/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.bcm;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

import sernet.hui.common.connect.ITargetObject;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.common.CnATreeElement;

public final class BCMUiUtils {

    private static final Logger LOG = Logger.getLogger(BCMUiUtils.class);

    private BCMUiUtils() {
        // Do not instantiate this class.
    }

    /**
     * Add selection listeners to a HUI composite for a given element.
     */
    public static void addSelectionListener(HitroUIComposite huiComposite, CnATreeElement element) {

        if (element instanceof ITargetObject && element instanceof IBpElement
                && !(element.isScope())) {
            String typeId = element.getTypeId();

            String targetProperty = typeId + "_bcm_mtpdMIN";
            String sourceProperty = typeId + "_bcm_mtpd1";
            String overrideProperty = typeId + "_bcm_mtpd2";

            Control overrideField = huiComposite.getField(overrideProperty);
            Control sourceField = huiComposite.getField(sourceProperty);
            Control targetField = huiComposite.getField(targetProperty);

            if (!(sourceField instanceof Combo && overrideField instanceof Combo
                    && targetField instanceof Combo)) {
                LOG.warn("Illegal fields, requiring combos for " + sourceProperty + ", "
                        + overrideProperty + ", and " + targetProperty);

            } else {

                Combo overrideCombo = (Combo) overrideField;
                Combo sourceCombo = (Combo) sourceField;
                Combo targetCombo = (Combo) targetField;

                if (targetCombo.getItemCount() != sourceCombo.getItemCount()
                        || overrideCombo.getItemCount() != sourceCombo.getItemCount() + 1) {
                    LOG.warn("Illegal fields, required item counts do not match for "
                            + sourceProperty + ", " + overrideProperty + ", and " + targetProperty);
                } else {
                    CalculateMinMtpd listener = new CalculateMinMtpd(element, sourceProperty,
                            targetProperty, overrideProperty);
                    element.getEntity().addChangeListener(listener);
                }
            }
        }
    }
}
