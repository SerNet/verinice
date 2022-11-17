/*******************************************************************************
 * Copyright (c) 2020 Jochen Kemnade.
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.ITargetObject;
import sernet.hui.swt.SWTResourceManager;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.bp.risk.RiskService;

/**
 * Decorates threats, threat groups and target objects according to their risks
 */
public class RiskDecorator extends LabelProvider implements ILightweightLabelDecorator {

    private static final Logger logger = Logger.getLogger(RiskDecorator.class);

    private static final RGB GRAY = new RGB(180, 180, 180);
    private static final RGB BLACK = new RGB(0, 0, 0);
    private static final RGB WHITE = new RGB(255, 255, 255);

    private boolean decoratorEnabled;

    private static final boolean ADD_COLOR_SUFFIX = Boolean
            .parseBoolean(System.getProperty("verinice.debug_bp_risk_analysis_decorator"));

    private static final Map<RGB, ImageDescriptor> CACHED_OVERLAYS_PER_COLOR = new LinkedHashMap<RGB, ImageDescriptor>() {

        private static final long serialVersionUID = 3645241081387305815L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<RGB, ImageDescriptor> eldest) {
            return size() > 500;
        }
    };

    public RiskDecorator() {
        decoratorEnabled = Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.SHOW_BP_RISK_ANALYSIS_DECORATOR);
        logger.info("Initializing, decoratorEnabled = " + decoratorEnabled);
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener(event -> {
            if (PreferenceConstants.SHOW_BP_RISK_ANALYSIS_DECORATOR.equals(event.getProperty())) {
                decoratorEnabled = Activator.getDefault().getPreferenceStore()
                        .getBoolean(PreferenceConstants.SHOW_BP_RISK_ANALYSIS_DECORATOR);
                logger.info("Activation state changed, decoratorEnabled = " + decoratorEnabled);
                updateDecorations();
            }
        });

    }

    private void updateDecorations() {
        fireLabelProviderChanged(new LabelProviderChangedEvent(this));
    }

    @Override
    public void decorate(Object element, IDecoration decoration) {
        if (decoratorEnabled) {
            RGB color = null;
            if (element instanceof BpThreat) {
                BpThreat threat = (BpThreat) element;
                EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(threat);
                color = getColorForThreatRisk(threat, risk);
            } else if (element instanceof BpThreatGroup) {
                color = getColorForThreatGroup((BpThreatGroup) element);
            } else if (element instanceof ITargetObject) {
                color = getColorForTargetObject((CnATreeElement) element);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Color for " + element + " = " + element);
            }
            if (color != null) {
                decoration.addOverlay(CACHED_OVERLAYS_PER_COLOR.computeIfAbsent(color,
                        RiskDecorator::createOverlay));
                if (ADD_COLOR_SUFFIX) {
                    String colorString = String.format("#%02x%02x%02x", color.red, color.green,
                            color.blue);
                    decoration.addSuffix(" - " + colorString);
                }

            }
        }
    }

    private static RGB getColorForTargetObject(CnATreeElement element) {
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(element);
        return getColorForThreatRisk(element, risk);
    }

    private RGB getColorForThreatGroup(BpThreatGroup threatGroup) {
        EffectiveRisk risk = RiskUiUtils.getEffectiveRisk(threatGroup);
        return getColorForThreatRisk(threatGroup, risk);
    }

    private static RGB getColorForThreatRisk(CnATreeElement element, EffectiveRisk risk) {
        if (risk == null) {
            return null;
        } else if (EffectiveRisk.TREATED.equals(risk)) {
            return GRAY;
        } else if (EffectiveRisk.UNKNOWN.equals(risk)) {
            return BLACK;
        }
        RiskService riskService = (RiskService) VeriniceContext
                .get(VeriniceContext.ITBP_RISK_SERVICE);
        RiskConfiguration riskConfiguration = Optional
                .ofNullable(riskService.findRiskConfiguration(element.getScopeId()))
                .orElseGet(DefaultRiskConfiguration::getInstance);
        return riskConfiguration.getRisks().stream()
                .filter(item -> item.getId().equals(risk.getRiskId())).map(Risk::getColor)
                .findFirst().map(ColorConverter::toRGB).orElse(null);
    }

    private static ImageDescriptor createOverlay(RGB rgb) {

        final int baseSize = 12;
        return ImageDescriptor.createFromImageDataProvider(zoom -> {
            int size = baseSize * zoom / 100;
            Image image = new Image(Display.getDefault(), size, size);
            GC gc = new GC(image);
            gc.setForeground(SWTResourceManager.getColor(BLACK));
            gc.setBackground(SWTResourceManager.getColor(rgb));
            int[] xAndYValues = new int[] { 0, 0, size, 0, size, size };
            gc.drawPolygon(xAndYValues);
            gc.fillPolygon(xAndYValues);
            gc.dispose();
            ImageData imageData = image.getImageData();
            int whitePixel = imageData.palette.getPixel(WHITE);
            imageData.transparentPixel = whitePixel;
            image.dispose();

            return imageData;
        });
    }

}