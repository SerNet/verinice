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
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.ITargetObject;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.verinice.model.bp.BCMUtils;
import sernet.verinice.model.bp.BCMUtils.BCMProperties;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.elements.ItNetwork;
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
            enableMinMtpdDeduction(huiComposite, element);
            enableMtpdDeduction(huiComposite, element);
        }
    }

    private static void enableMtpdDeduction(HitroUIComposite huiComposite, CnATreeElement element) {
        BCMProperties properties = BCMUtils.getPropertiesForElement(element);

        PropertyType prop = HUITypeFactory.getInstance().getEntityType(ItNetwork.TYPE_ID)
                .getPropertyType(ItNetwork.PROP_UNTRAGBARKEITSNIVEAU);
        int numberOfDamagePotentialOptions = prop.getOptions().size();

        if (checkIsCombo(huiComposite, properties.propertyImpact24h, numberOfDamagePotentialOptions)
                && checkIsCombo(huiComposite, properties.propertyImpact3d,
                        numberOfDamagePotentialOptions)
                && checkIsCombo(huiComposite, properties.propertyImpact7d,
                        numberOfDamagePotentialOptions)
                && checkIsCombo(huiComposite, properties.propertyImpact14d,
                        numberOfDamagePotentialOptions)
                && checkIsCombo(huiComposite, properties.propertyImpact30d,
                        numberOfDamagePotentialOptions)
                // 5 intervals plus "Unedited plus "No MTPD"
                && checkIsCombo(huiComposite, properties.propertyMtpd, 7)) {

            CalculateMtpd listener = new CalculateMtpd(element, properties);

            element.getEntity().addChangeListener(listener);
        }
    }

    private static void enableMinMtpdDeduction(HitroUIComposite huiComposite,
            CnATreeElement element) {

        BCMProperties properties = BCMUtils.getPropertiesForElement(element);

        String targetProperty = properties.propertyMtpdMin;
        String sourceProperty = properties.propertyMtpd;
        String overrideProperty = properties.propertyMtpdOverride;

        Control sourceField = huiComposite.getField(sourceProperty);

        if (!(sourceField instanceof Combo)) {
            LOG.warn("Illegal field for " + sourceProperty + ", requiring a combo but found "
                    + sourceField);
        } else {
            Combo sourceCombo = (Combo) sourceField;

            if (checkIsCombo(huiComposite, targetProperty, sourceCombo.getItemCount())
                    && checkIsCombo(huiComposite, overrideProperty,
                            sourceCombo.getItemCount() + 1)) {

                CalculateMinMtpd listener = new CalculateMinMtpd(element, sourceProperty,
                        overrideProperty);
                element.getEntity().addChangeListener(listener);
            }
        }
    }

    private static boolean checkIsCombo(HitroUIComposite huiComposite, String propertyName,
            int requiredItemCount) {
        Control field = huiComposite.getField(propertyName);
        if (field == null) {
            LOG.warn("Field for " + propertyName + " not found in editor");
            return false;
        }
        if (!(field instanceof Combo)) {
            LOG.warn(
                    "Illegal field for " + propertyName + ", requiring a combo but found " + field);
            return false;
        }
        Combo combo = (Combo) field;
        int itemCount = combo.getItemCount();
        if (itemCount != requiredItemCount) {
            LOG.warn("Illegal number of items for " + propertyName + ", requiring "
                    + requiredItemCount + "but found " + itemCount);
            return false;
        }
        return true;
    }

    public static void addControlHints(HitroUIComposite huiComposite, CnATreeElement element) {
        if (element instanceof ITargetObject && element instanceof IBpElement
                && !(element.isScope())) {
            String typeId = element.getTypeId();
            String property = typeId + "__rto";
            Control control = huiComposite.getField(property);
            if (control == null) {
                LOG.warn("Field for " + property + " not found in editor");

            } else {
                ControlDecoration txtDecorator = new ControlDecoration(control, SWT.TOP | SWT.LEFT);
                FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
                        .getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION);
                Image img = fieldDecoration.getImage();
                txtDecorator.setImage(img);
                txtDecorator.setDescriptionText(Messages.rtoMustBeSmallerThanMinMtpd);
            }
        }

    }
}