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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import sernet.gs.ui.rcp.main.common.model.CnATreeElementScopeUtils;
import sernet.hui.swt.SWTResourceManager;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnATreeElement;

public class RiskMatrixConfigurator extends Composite {

    private static final int SELECTOR_SIZE = 100;

    private static final RGB WHITE = new RGB(255, 255, 255);
    private static final RGB BLACK = new RGB(0, 0, 0);

    private Composite pane;

    private RiskConfiguration editorState;
    private final Runnable firePropertyChanged;

    public RiskMatrixConfigurator(Composite parent, RiskConfiguration riskConfiguration,
            Runnable firePropertyChanged) {
        super(parent, SWT.NONE);
        this.firePropertyChanged = firePropertyChanged;
        editorState = riskConfiguration;
        refresh();
    }

    public static RiskConfiguration getRiskConfiguration(CnATreeElement element) {
        ItNetwork itNetwork = (ItNetwork) CnATreeElementScopeUtils.getScope(element);
        if (!(itNetwork instanceof ItNetwork)) {
            throw new IllegalArgumentException("Cannot retrieve risk configuration for " + element
                    + " as it is not part of an IT network");
        }
        return itNetwork.getRiskConfigurationOrDefault();
    }

    public void setEditorState(RiskConfiguration riskConfiguration) {
        this.editorState = riskConfiguration;
    }

    public RiskConfiguration getEditorState() {
        return editorState;
    }

    public void refresh() {
        if (pane != null) {
            pane.dispose();
        }

        List<Impact> impactValues = editorState.getImpacts();
        List<Impact> impactValuesReverse = new ArrayList<>(impactValues);
        Collections.reverse(impactValuesReverse);
        List<Frequency> frequencyValues = editorState.getFrequencies();

        setLayout(new GridLayout(frequencyValues.size() + 1, true));
        pane = new Composite(this, SWT.NONE);
        pane.setLayout(new GridLayout(frequencyValues.size() + 1, false));

        Label labels = new Label(pane, SWT.NONE);
        labels.setLayoutData(
                GridDataFactory.swtDefaults().span(frequencyValues.size() + 1, 1).create());
        labels.setText(Messages.riskConfigurationMatrixImpactAxis);

        impactValuesReverse.forEach(impact -> {
            Label label = new Label(pane, SWT.NONE);
            GridData layoutData = new GridData();
            layoutData.widthHint = StackConfigurator.LABEL_WIDTH;
            label.setLayoutData(layoutData);
            String text = cutLabel(impact.getLabel(), label,
                    StackConfigurator.LABEL_WIDTH + StackConfigurator.ELEMENT_MARGINS);
            label.setText(text);
            frequencyValues.forEach(frequency -> addRiskSelector(impact, frequency));
        });
        new Label(pane, SWT.NONE);
        frequencyValues.forEach(frequency -> {
            Label label = new Label(pane, SWT.NONE);
            String text = cutLabel(frequency.getLabel(), label, SELECTOR_SIZE);
            label.setText(text);
        });
        new Label(pane, SWT.NONE);
        Label axisLabel = new Label(pane, SWT.NONE);
        axisLabel.setLayoutData(GridDataFactory.swtDefaults()//
                .align(SWT.CENTER, SWT.BEGINNING)//
                .span(frequencyValues.size(), 1).create());
        axisLabel.setText(Messages.riskConfigurationMatrixFrequencyAxis);

        new Label(pane, SWT.NONE).setLayoutData(
                GridDataFactory.swtDefaults().span(frequencyValues.size() + 1, 1).create());
        Label helpText = new Label(pane, SWT.NONE);
        helpText.setLayoutData(
                GridDataFactory.swtDefaults().span(frequencyValues.size() + 1, 1).create());
        helpText.setText(Messages.riskConfigurationMatrixUsage);

        pack(true);
        if (getParent() instanceof ScrolledComposite) {
            ((ScrolledComposite) getParent()).setMinSize(getClientArea().width,
                    getClientArea().height);
        }

        pane.requestLayout();
        redraw();
    }

    private void addRiskSelector(Impact impact, Frequency frequency) {
        CLabel selector = new CLabel(pane, SWT.SHADOW_OUT | SWT.CENTER);

        selector.setLayoutData(new GridData(SELECTOR_SIZE, SELECTOR_SIZE));
        selector.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                boolean modifierPressed = (e.stateMask & SWT.MODIFIER_MASK) != 0;
                int numberOfRisks = editorState.getRisks().size();

                Risk currentRisk = editorState.getRisk(frequency, impact);
                Risk newRisk;
                int index = editorState.getRisks().indexOf(currentRisk);

                int newIndex = index + (modifierPressed ? -1 : 1);
                if (newIndex >= numberOfRisks || newIndex == -1) {
                    newRisk = null;
                } else if (newIndex < -1) {
                    newRisk = editorState.getRisks().get(numberOfRisks - 1);
                } else {
                    newRisk = editorState.getRisks().get(newIndex);
                }
                fillSelector(selector, newRisk);
                editorState = editorState.withRisk(frequency, impact, newRisk);
                firePropertyChanged.run();
            }
        });

        fillSelector(selector, editorState.getRisk(frequency, impact));
    }

    private void fillSelector(CLabel selector, Risk risk) {
        String text;
        String longText;
        Color backgroundColor = null;
        Color textColor = null;
        if (risk == null) {
            text = sernet.hui.swt.widgets.Messages.getString("SingleSelectDummyValue");
            longText = text;
            textColor = SWTResourceManager.getColor(BLACK);
        } else {
            text = cutLabel(risk.getLabel(), selector,
                    SELECTOR_SIZE - StackConfigurator.ELEMENT_MARGINS);
            longText = risk.getLabel();
            RGB riskColor = ColorConverter.toRGB(risk.getColor());
            if (riskColor != null) {
                backgroundColor = SWTResourceManager.getColor(riskColor);
                textColor = SWTResourceManager
                        .getColor(determineOptimalTextColor(riskColor, WHITE, BLACK));
            }
        }

        selector.setText(text);
        selector.setToolTipText(longText);
        selector.getBackground().dispose();
        selector.setBackground(backgroundColor);
        selector.getForeground().dispose();
        selector.setForeground(textColor);
    }

    private String cutLabel(String text, Control control, int size) {
        GC gc = new GC(control);
        int stringWidth = gc.stringExtent(text).x;
        String newText = text;
        int x = newText.length();
        while (stringWidth > size) {
            newText = text.substring(0, x) + "...";
            stringWidth = gc.stringExtent(newText).x;
            x--;
        }
        gc.dispose();
        return newText;
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
}