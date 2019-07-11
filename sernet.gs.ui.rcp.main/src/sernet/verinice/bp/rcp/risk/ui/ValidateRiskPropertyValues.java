/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
package sernet.verinice.bp.rcp.risk.ui;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

import sernet.gs.service.StringUtil;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.verinice.model.bp.elements.BpThreat;

public final class ValidateRiskPropertyValues extends SelectionAdapter {

    private final BpThreat element;
    private final HitroUIComposite huiComposite;
    private List<Runnable> validators;

    public ValidateRiskPropertyValues(HitroUIComposite huiComposite, BpThreat threat) {
        this.huiComposite = huiComposite;
        this.element = threat;
        validators = Stream
                .of(createValidator(BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS,
                        BpThreat.PROP_FREQUENCY_WITHOUT_SAFEGUARDS),
                        createValidator(BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS,
                                BpThreat.PROP_IMPACT_WITHOUT_SAFEGUARDS),
                        createValidator(BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS,
                                BpThreat.PROP_RISK_WITHOUT_SAFEGUARDS),
                        createValidator(BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS,
                                BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS),
                        createValidator(BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS,
                                BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS),
                        createValidator(BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS,
                                BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS))
                .collect(Collectors.toList());
        validators.forEach(Runnable::run);
    }

    private Runnable createValidator(String property, String compareProperty) {
        Control control = huiComposite.getField(property);
        String comparePropertyGroupLabel = HUITypeFactory.getInstance()
                .getPropertyGroup(BpThreat.TYPE_ID, compareProperty).getName();
        ControlDecoration decorator = createLabelFieldDecoration(control,
                Messages.bind(Messages.warningRiskPropertyExceedance, comparePropertyGroupLabel));
        return () -> updateDecoratorVisibility(decorator, property, compareProperty);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        validators.forEach(Runnable::run);
    }

    private void updateDecoratorVisibility(ControlDecoration decorator, String property,
            String compareProperty) {
        String value = StringUtil
                .replaceEmptyStringByNull(element.getEntity().getRawPropertyValue(property));
        String compareValue = StringUtil
                .replaceEmptyStringByNull(element.getEntity().getRawPropertyValue(compareProperty));
        if (value != null && compareValue != null && value.compareTo(compareValue) > 0) {
            decorator.show();
        } else {
            decorator.hide();
        }
    }

    private static ControlDecoration createLabelFieldDecoration(Control contol,
            String descriptionText) {
        ControlDecoration txtDecorator = new ControlDecoration(contol, SWT.TOP | SWT.RIGHT);
        FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);
        Image img = fieldDecoration.getImage();
        txtDecorator.setImage(img);
        txtDecorator.setDescriptionText(descriptionText);
        return txtDecorator;
    }
}