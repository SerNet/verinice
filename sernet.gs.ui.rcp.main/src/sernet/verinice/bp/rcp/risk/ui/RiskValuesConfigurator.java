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
import java.util.function.Consumer;

import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;

final class RiskValuesConfigurator extends StackConfigurator<Risk> {

    private static final int MAX_NUMBER_OF_RISKS = 5;

    private RiskConfiguration riskConfiguration;
    private Consumer<RiskConfiguration> updateListener;

    RiskValuesConfigurator(Composite parent,
            Consumer<RiskConfiguration> updateListener) {
        super(parent, MAX_NUMBER_OF_RISKS);
        this.updateListener = updateListener;
    }

    @Override
    protected void addRow(Composite parent, Risk risk) {
        parent.setLayout(
                RowLayoutFactory.createFrom(new RowLayout(SWT.HORIZONTAL)).spacing(10).create());

        Composite leftComposite = new Composite(parent, SWT.NONE);

        leftComposite.setLayout(
                RowLayoutFactory.createFrom(new RowLayout(SWT.VERTICAL)).spacing(5).create());
        Text riskLabel = new Text(leftComposite, SWT.NONE);
        riskLabel.setLayoutData(new RowData(LABLE_SIZE, SWT.DEFAULT));
        riskLabel.setText(risk.getLabel());
        riskLabel.addFocusListener(new LabelFocusListener(updateListener, riskConfiguration, risk));

        Button riskColor = new Button(leftComposite, SWT.NONE);
        RGB rgb = ColorConverter.toRGB(risk.getColor());
        if (rgb != null) {
            riskColor.setBackground(new Color(getDisplay(), rgb));
        }
        riskColor.addSelectionListener(
                new ColorSelectionAdapter(updateListener, riskConfiguration, rgb, risk));

        Text riskDescription = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        riskDescription.setLayoutData(new RowData(450, 80));
        riskDescription.setText(risk.getDescription());
        riskDescription.addFocusListener(
                new DescriptionFocusListener(riskConfiguration, updateListener, risk));
    }

    @Override
    protected void onAddClicked() {
        riskConfiguration = riskConfiguration.withRiskAdded();
        updateListener.accept(riskConfiguration);
    }

    @Override
    protected void onRemoveClicked() {
        riskConfiguration = riskConfiguration.withLastRiskRemoved();
        updateListener.accept(riskConfiguration);
    }

    /**
     * Set the new risk configuration and refresh the composite with the list.
     */
    public void setRiskConfiguration(RiskConfiguration riskConfiguration) {
        this.riskConfiguration = riskConfiguration;
        super.refresh(this.riskConfiguration.getRisks());
    }

    @Override
    protected void refresh(List<Risk> risks) {
        throw new UnsupportedOperationException("call setRiskConfiguration instead"); //$NON-NLS-1$
    }

    private final class DescriptionFocusListener extends FocusAdapter {
        private final RiskConfiguration riskConfiguration;
        private final Consumer<RiskConfiguration> updateListener;
        private final Risk risk;

        private DescriptionFocusListener(RiskConfiguration riskConfiguration,
                Consumer<RiskConfiguration> updateListener, Risk risk) {
            this.riskConfiguration = riskConfiguration;
            this.updateListener = updateListener;
            this.risk = risk;
        }

        @Override
        public void focusLost(FocusEvent event) {
            Text text = (Text) event.widget;
            String newDescription = text.getText();
            if (!Objects.equals(risk.getDescription(), newDescription)) {
                updateListener.accept(riskConfiguration.withRiskDescription(risk, newDescription));
            }
        }
    }

    private final class LabelFocusListener extends FocusAdapter {

        private final Consumer<RiskConfiguration> updateListener;
        private final RiskConfiguration riskConfiguration;
        private final Risk risk;

        private LabelFocusListener(Consumer<RiskConfiguration> updateListener,
                RiskConfiguration riskConfiguration, Risk risk) {
            this.updateListener = updateListener;
            this.riskConfiguration = riskConfiguration;
            this.risk = risk;
        }

        @Override
        public void focusLost(FocusEvent event) {
            Text text = (Text) event.widget;
            String newLabel = text.getText();
            if (!Objects.equals(risk.getLabel(), newLabel)) {
                updateListener.accept(riskConfiguration.withRiskLabel(risk, newLabel));
            }
        }
    }

    private final class ColorSelectionAdapter extends SelectionAdapter {

        private final Consumer<RiskConfiguration> updateListener;
        private final RiskConfiguration riskConfiguration;
        private final RGB rgb;
        private final Risk risk;

        private ColorSelectionAdapter(Consumer<RiskConfiguration> updateListener,
                RiskConfiguration riskConfiguration, RGB rgb, Risk risk) {
            this.updateListener = updateListener;
            this.riskConfiguration = riskConfiguration;
            this.rgb = rgb;
            this.risk = risk;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            ColorDialog dlg = new ColorDialog(getDisplay().getActiveShell());

            dlg.setRGB(rgb);
            dlg.setText(Messages.RiskValuesConfigurator_chooseColor);

            RGB newColor = dlg.open();
            if (!Objects.equals(rgb, newColor)) {
                updateListener.accept(riskConfiguration.withRiskColor(risk,
                        ColorConverter.toRiskColor(newColor)));
            }
        }
    }
}