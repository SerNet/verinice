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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import sernet.gs.ui.rcp.main.common.model.CnATreeElementScopeUtils;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnATreeElement;

public class RiskMatrixConfigurator extends Composite {

    private static final int BUTTON_SIZE = 100;

    private static final RGB WHITE = new RGB(255, 255, 255);
    private static final RGB BLACK = new RGB(0, 0, 0);

    private final RiskConfiguration riskConfiguration;

    public RiskMatrixConfigurator(Composite parent, RiskConfiguration riskConfiguration,
            Consumer<RiskConfiguration> updateListener) {
        super(parent, SWT.NONE);
        this.riskConfiguration = riskConfiguration;

        List<Impact> impactValues = riskConfiguration.getImpacts();
        List<Impact> impactValuesReverse = new ArrayList<>(impactValues);
        Collections.reverse(impactValuesReverse);

        List<Frequency> frequencyValues = riskConfiguration.getFrequencies();
        setLayout(new GridLayout(frequencyValues.size() + 1, false));
        
        Label labels = new Label(this, SWT.NONE);
        labels.setLayoutData(GridDataFactory.swtDefaults().span(frequencyValues.size()+1, 1).create());
        labels.setText(Messages.riskConfigurationMatrixImpactAxis);
        
        impactValuesReverse.forEach(impact -> {
            Label label = new Label(this, SWT.NONE);
            GridData layoutData = new GridData();
            layoutData.widthHint = StackConfigurator.LABLE_SIZE;
			label.setLayoutData(layoutData);
            String text = cutLable(impact.getLabel(), label, StackConfigurator.LABLE_SIZE + StackConfigurator.ELEMENT_MARGINS);
            label.setText(text);
            frequencyValues.forEach(frequency -> addRiskButton(riskConfiguration, updateListener,
                    impact, frequency));
        });
        new Label(this, SWT.NONE);
        frequencyValues.forEach(frequency -> {
            Label label = new Label(this, SWT.NONE);
            String text = cutLable(frequency.getLabel(), label, BUTTON_SIZE);
            label.setText(text);
        });
        new Label(this, SWT.NONE);
        Label axisLabel = new Label(this, SWT.NONE);
        axisLabel.setLayoutData(GridDataFactory.swtDefaults()//
        		.align(SWT.CENTER, SWT.BEGINNING)//
        		.span(frequencyValues.size(), 1).create());
        axisLabel.setText(Messages.riskConfigurationMatrixFrequencyAxis);

		new Label(this, SWT.NONE)
		.setLayoutData(GridDataFactory.swtDefaults().span(frequencyValues.size() + 1, 1).create());
        Label helpText = new Label(this, SWT.NONE);
        helpText.setLayoutData(
                GridDataFactory.swtDefaults().span(frequencyValues.size()+1, 1).create());
        helpText.setText(Messages.riskConfigurationMatrixUsage);
    }

    private void addRiskButton(RiskConfiguration riskConfiguration,
            Consumer<RiskConfiguration> updateListener, Impact impact,
            Frequency frequency) {
        Button button = new Button(this, SWT.BORDER);

        button.setLayoutData(new GridData(BUTTON_SIZE, BUTTON_SIZE));
        button.addSelectionListener(new RiskButtonSelectionListener(impact, updateListener, riskConfiguration, frequency));
        Risk risk = riskConfiguration.getRisk(frequency, impact);
        String text;
        Color backgroundColor = null;
        Color textColor = null;
        if (risk == null) {
            text = sernet.hui.swt.widgets.Messages.getString("SingleSelectDummyValue");
            textColor = new Color(button.getDisplay(), BLACK);
        } else {
			text = cutLable(risk.getLabel(), button, BUTTON_SIZE - StackConfigurator.ELEMENT_MARGINS);
			RGB riskColor = ColorConverter.toRGB(risk.getColor());
			if (riskColor != null) {
				backgroundColor = new Color(button.getDisplay(), riskColor);
				textColor = new Color(button.getDisplay(), determineOptimalTextColor(riskColor, WHITE, BLACK));
			}
        }

        button.setText(text);
        button.getBackground().dispose();
        button.setBackground(backgroundColor);
        button.getForeground().dispose();
        button.setForeground(textColor);
    }

	private String cutLable(String text, Control button, int size) {
		GC gc = new GC(button);
		int stringWidth = gc.stringExtent(text).x;
		String newText = text;
		int x = newText.length();
		while (stringWidth > size) {
			newText = text.substring(0, x)+ "...";
			stringWidth = gc.stringExtent(newText).x;
			x--;
		}
		gc.dispose();
		return newText;
	}

	public RiskConfiguration getRiskConfiguration() {
        return riskConfiguration;
    }

    public static RiskConfiguration getRiskConfiguration(CnATreeElement element) {
        ItNetwork itNetwork = (ItNetwork) CnATreeElementScopeUtils.getScope(element);
        if (!(itNetwork instanceof ItNetwork)) {
            throw new IllegalArgumentException("Cannot retrieve risk configuration for " + element
                    + " as it is not part of an IT network");
        }
        return Optional.ofNullable(itNetwork.getRiskConfiguration())
                .orElseGet(DefaultRiskConfiguration::getInstance);
    }

    private static RGB determineOptimalTextColor(RGB bgColor, RGB lightColor, RGB darkColor) {
        double l = getRelativeLuminance(bgColor);
        double lLightColor = getRelativeLuminance(lightColor);
        double lDarkColor = getRelativeLuminance(darkColor);

        double contrastRatioToLightColor = (lLightColor + 0.05) / (l + 0.05);
        double contrastRatioToDarkColor = (l + 0.05) / (lDarkColor + 0.05);

        if (contrastRatioToDarkColor > contrastRatioToLightColor) {
            return darkColor;
        } else {
            return lightColor;
        }
    }

    private static double getRelativeLuminance(RGB color) {
        double[] c = new double[] { color.red, color.green, color.blue };
        for (int i = 0; i <= 2; i++) {
            double col = c[i] / 255d;
            if (col <= 0.03928) {
                col = col / 12.92;
            } else {
                col = Math.pow((col + 0.055) / 1.055, 2.4);
            }
            c[i] = col;
        }
        return (0.2126 * c[0]) + (0.7152 * c[1]) + (0.0722 * c[2]);
    }

    /**
     * TODO meaningful comment
     */
    private final class RiskButtonSelectionListener extends SelectionAdapter {
        private final Impact impact;
        private final Consumer<RiskConfiguration> updateListener;
        private final RiskConfiguration riskConfiguration;
        private final Frequency frequency;

        private RiskButtonSelectionListener(Impact impact,
                Consumer<RiskConfiguration> updateListener, RiskConfiguration riskConfiguration,
                Frequency frequency) {
            this.impact = impact;
            this.updateListener = updateListener;
            this.riskConfiguration = riskConfiguration;
            this.frequency = frequency;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            boolean modifierPressed = (e.stateMask & SWT.MODIFIER_MASK) != 0;
            int numberOfRisks = riskConfiguration.getRisks().size();

            Risk currentRisk = riskConfiguration.getRisk(frequency, impact);
            Risk newRisk;
            int index = riskConfiguration.getRisks().indexOf(currentRisk);

            int newIndex = index + (modifierPressed ? -1 : 1);
            if (newIndex >= numberOfRisks || newIndex == -1) {
                newRisk = null;
            } else if (newIndex < -1) {
                newRisk = riskConfiguration.getRisks()
                        .get(numberOfRisks - 1);
            } else {
                newRisk = riskConfiguration.getRisks().get(newIndex);
            }
            updateListener
                    .accept(riskConfiguration.withRisk(frequency, impact, newRisk));
            super.widgetSelected(e);

        }
    }
}
