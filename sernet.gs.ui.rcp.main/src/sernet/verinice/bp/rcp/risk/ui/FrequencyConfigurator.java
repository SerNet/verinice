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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;

final class FrequencyConfigurator extends StackConfigurator<Frequency> {

    private static final int MAX_NUMBER_OF_FREQUENCIES = 10;

    private RiskConfiguration riskConfiguration;
    private Consumer<RiskConfiguration> updateListener;

    FrequencyConfigurator(Composite parent,
            Consumer<RiskConfiguration> updateListener) {
        super(parent, MAX_NUMBER_OF_FREQUENCIES);
        this.updateListener = updateListener;
    }

    @Override
    protected void addRow(Composite parent, Frequency frequency) {
        parent.setLayout(
                RowLayoutFactory.createFrom(new RowLayout(SWT.HORIZONTAL)).spacing(10).create());

        Composite currentItemLeft = new Composite(parent, SWT.NONE);

        currentItemLeft.setLayout(
                RowLayoutFactory.createFrom(new RowLayout(SWT.VERTICAL)).spacing(5).create());
        Text labelField = new Text(currentItemLeft, SWT.BORDER);
        labelField.setLayoutData(new RowData(LABEL_WIDTH, SWT.DEFAULT));
        labelField.setText(frequency.getLabel());
        labelField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent event) {
                Text text = (Text) event.widget;
                String newLabel = text.getText();
                if (!Objects.equals(frequency.getLabel(), newLabel)) {
                    updateListener
                            .accept(riskConfiguration.withFrequencyLabel(frequency, newLabel));
                }
            }
        });

        Text descriptionField = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        descriptionField.setLayoutData(new RowData(450, 80));
        descriptionField.setText(frequency.getDescription());
        descriptionField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent event) {
                Text text = (Text) event.widget;
                String newDescription = text.getText();
                if (!Objects.equals(frequency.getDescription(), newDescription)) {
                    updateListener.accept(
                            riskConfiguration.withFrequencyDescription(frequency, newDescription));
                }
            }
        });
    }

    @Override
    protected void onAddClicked() {
        riskConfiguration = riskConfiguration.withFrequencyAdded();
        updateListener.accept(riskConfiguration);
    }

    @Override
    protected void onRemoveClicked() {
        riskConfiguration = riskConfiguration.withLastFrequencyRemoved();
        updateListener.accept(riskConfiguration);
    }

    /**
     * Set the new risk configuration an refreshes the risk list composite.s
     */
    public void setRiskConfiguration(RiskConfiguration riskConfiguration) {
        this.riskConfiguration = riskConfiguration;
        super.refresh(this.riskConfiguration.getFrequencies());
    }

    @Override
    protected void refresh(List<Frequency> frequencies) {
        throw new UnsupportedOperationException("call setRiskConfiguration instead");
    }
}