/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.model.bp.risk.Risk;

public final class RiskValuesConfigurator extends StackConfigurator<Risk> {

    private static final int MAX_NUMBER_OF_RISKS = 5;
    private static final int COLOR_BUTTON_WIDTH = 30;

    public RiskValuesConfigurator(Composite parent, List<Risk> editorState,
            Runnable fireProperyChange) {
        super(parent, MAX_NUMBER_OF_RISKS, editorState, fireProperyChange);
    }

    @Override
    protected @NonNull Risk generateNewData(int index) {
        return new Risk(Risk.getPropertyKeyForIndex(index), "", "", null);
    }

    @Override
    protected void addRow(Composite parent, Risk risk) {
        parent.setLayout(
                RowLayoutFactory.createFrom(new RowLayout(SWT.HORIZONTAL)).spacing(10).create());

        Composite leftComposite = new Composite(parent, SWT.NONE);

        leftComposite.setLayout(
                RowLayoutFactory.createFrom(new RowLayout(SWT.VERTICAL)).spacing(5).create());
        Text riskLabel = new Text(leftComposite, SWT.BORDER);
        riskLabel.setLayoutData(new RowData(LABEL_WIDTH, SWT.DEFAULT));
        riskLabel.setText(risk.getLabel());

        riskLabel.addModifyListener(e -> {
            if (e.getSource() instanceof Text) {
                String newLabel = ((Text) e.getSource()).getText();
                updateValue(
                        new Risk(risk.getId(), newLabel, risk.getDescription(), risk.getColor()));
            }
        });

        CLabel riskColor = new CLabel(leftComposite, SWT.SHADOW_OUT | SWT.CENTER);
        riskColor.setLayoutData(new RowData(COLOR_BUTTON_WIDTH, SWT.DEFAULT));
        RGB rgb = ColorConverter.toRGB(risk.getColor());
        if (rgb != null) {
            riskColor.setBackground(new org.eclipse.swt.graphics.Color(getDisplay(), rgb));
        }
        riskColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                ColorDialog dlg = new ColorDialog(getDisplay().getActiveShell());

                dlg.setRGB(rgb);
                dlg.setText(Messages.RiskValuesConfigurator_chooseColor);

                RGB newColor = dlg.open();
                if (!Objects.equals(rgb, newColor)) {
                    updateValue(new Risk(risk.getId(), risk.getLabel(), risk.getDescription(),
                            ColorConverter.toRiskColor(newColor)));
                    riskColor.setBackground(
                            new org.eclipse.swt.graphics.Color(getDisplay(), newColor));
                }
            }
        });

        Text riskDescription = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        riskDescription.setLayoutData(new RowData(450, 80));
        riskDescription.setText(risk.getDescription());

        riskDescription.addModifyListener(e -> {
            if (e.getSource() instanceof Text) {
                String newDescription = ((Text) e.getSource()).getText();
                updateValue(
                        new Risk(risk.getId(), risk.getLabel(), newDescription, risk.getColor()));
            }
        });
    }

}
