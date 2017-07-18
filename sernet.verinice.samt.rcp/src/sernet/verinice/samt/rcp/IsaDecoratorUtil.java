/**
 * Copyright 2016 Moritz Reiter.
 *
 * <p>This file is part of Verinice.
 *
 * <p>Verinice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * <p>Verinice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License
 * along with Verinice. If not, see http://www.gnu.org/licenses/.
 */

package sernet.verinice.samt.rcp;

import org.eclipse.jface.viewers.IDecoration;

import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

/**
 * A service for computations needed to show decorators for a "Security Assessment".
 * 
 * <p>The computations are based on the Information Security Assessment (ISA) method published by
 * "Verband der Automobilindustrie" (VDA) here:
 * https://www.vda.de/dam/vda/publications/2015/information-security-assessment-isa-en.xlsx
 * 
 * <p>This service is based on version 2.1.3 of the document issued by the VDA.
 * 
 * @author Moritz Reiter
 */
@SuppressWarnings("restriction")
public class IsaDecoratorUtil {

    enum DecoratorColor {
        NULL, GREEN, YELLOW, RED
    }

    private static final BigDecimal MAX_SCORE = new BigDecimal("3.0");
    private static final BigDecimal GREEN_SCORE_COEFFICIENT = new BigDecimal("0.9");
    private static final BigDecimal YELLOW_SCORE_COEFFICIENT = new BigDecimal("0.7");

    private static final int SCALE = 2;

    /**
     * Returns the decorator color for a control object.
     * 
     * @param  isaControl the control for which the decorator color is requested
     * @return the decorator color for the control that was passed in
     */
    static DecoratorColor decoratorColor(SamtTopic isaControl) {

        int maturity = getMaturity(isaControl);
        int targetMaturity = getTargetMaturity(isaControl);

        if (targetMaturity < TargetMaturity.MIN.value()) {
            return DecoratorColor.NULL;
        }

        return computeDecoratorColor(maturity, targetMaturity);
    }

    /**
     * Returns the decorator color for a control group object.
     * 
     * @param  controlGroup the control group for which the decorator color is requested
     * @return the decorator color for the control group that was passed in
     */
    static DecoratorColor decoratorColor(ControlGroup controlGroup) {

        ControlGroup hydratedControlGroup = (ControlGroup) Retriever
                .checkRetrieveElement(controlGroup);

        int averageMaturity = getAverageMaturity(hydratedControlGroup);
        int averageTargetMaturity = getAverageTargetMaturity(hydratedControlGroup);

        if (averageTargetMaturity < TargetMaturity.MIN.value()) {
            return DecoratorColor.NULL;
        }

        return computeDecoratorColor(averageMaturity, averageTargetMaturity);
    }

    /**
     * Returns the decorator color for an audit object according to the following rule.
     * 
     * <pre>{@code
     * if (score > greenScoreCoefficient * maxScore):
     *   return "green"
     * else if (score > yellowScoreCoefficient * maxScore):
     *   return "yellow"
     * else:
     *   return "red"
     * }</pre>
     * 
     * @param  audit the audit for which the decorator color is requested
     * @return the decorator color for the audit that was passed in
     */
    static DecoratorColor decoratorColor(Audit audit) {

        BigDecimal score = resultScore(audit);

        if (score.compareTo(GREEN_SCORE_COEFFICIENT.multiply(MAX_SCORE)) > 0) {
            return DecoratorColor.GREEN;
        } else if (score.compareTo(YELLOW_SCORE_COEFFICIENT.multiply(MAX_SCORE)) > 0) {
            return DecoratorColor.YELLOW;
        } else {
            return DecoratorColor.RED;
        }
    }

    private static DecoratorColor computeDecoratorColor(int maturity, int targetMaturity) {

        assert maturity == Maturity.NOT_EDITED.value()
                || maturity == Maturity.NOT_APPLICABLE.value()
                || (maturity >= Maturity.MIN.value() && maturity <= Maturity.MAX.value());

        assert targetMaturity >= TargetMaturity.MIN.value()
                && targetMaturity <= TargetMaturity.MAX.value();

        DecoratorColor decoratorColor;

        if (maturity == Maturity.NOT_EDITED.value) {
            decoratorColor = DecoratorColor.RED;
        } else if (maturity == Maturity.NOT_APPLICABLE.value()) {
            decoratorColor = DecoratorColor.NULL;
        } else if (maturity <= targetMaturity - 2) {
            decoratorColor = DecoratorColor.RED;
        } else if (maturity == targetMaturity - 1) {
            decoratorColor = DecoratorColor.YELLOW;
        } else if (maturity >= targetMaturity) {
            decoratorColor = DecoratorColor.GREEN;
        } else {
            decoratorColor = DecoratorColor.NULL;
        }

        return decoratorColor;
    }

    /**
     * Returns the result score with a cut back to the target maturity levels for the VDA ISA.
     * 
     * <p>This method assumes the audit object parameter to be a "Security Assessment" with its tree
     * of descendants to have a certain structure. If this structure changes, the algorithm in this
     * method must be adapted accordingly.
     * 
     * @param  audit the Audit object ("Security Assessment") for which the result score is
     *         requested
     * @return the result score for the given audit
     */
    static BigDecimal resultScore(Audit audit) {

        Audit hydratedAudit = (Audit) Retriever.checkRetrieveChildren(audit);
        ControlGroup topLevelControlGroup = hydratedAudit.getControlGroup();
        topLevelControlGroup = (ControlGroup) Retriever.checkRetrieveChildren(topLevelControlGroup);

        Set<SamtTopic> isaControls = new HashSet<>();

        for (CnATreeElement element : topLevelControlGroup.getChildren()) {
            if (element instanceof ControlGroup) {
                ControlGroup controlGroup = (ControlGroup) element;

                controlGroup = (ControlGroup) Retriever.checkRetrieveChildren(controlGroup);
                isaControls.addAll(getChildrenOfTypeIsaControl(controlGroup));
            }
        }

        int accumulatedMaturity = 0;
        int count = 0;
        for (SamtTopic isaControl : isaControls) {
            isaControl = (SamtTopic) Retriever.checkRetrieveElement(isaControl);
            int maturity = getMaturity(isaControl);
            int targetMaturity = getTargetMaturity(isaControl);
            if (maturity != Maturity.NOT_APPLICABLE.value()) {
                if (maturity == Maturity.NOT_EDITED.value()) {
                    maturity = 0;
                }
                if (maturity > targetMaturity) {
                    maturity = targetMaturity;
                }
                accumulatedMaturity += maturity;
                count++;
            }
        }

        if (count == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(accumulatedMaturity).divide(BigDecimal.valueOf(count), SCALE,
                RoundingMode.HALF_UP);
    }

    /**
     * Returns {@code true} if the given Control Group is the grandchild of an Audit object.
     * 
     * @param  controlGroup the Control Group to be tested
     * @return {@code true} if {@code controlGroup} is the grandchild of an Audit object, {@code
     *         false} otherwise
     */
    static boolean isGrandchildOfAudit(ControlGroup controlGroup) {

        CnATreeElement parent = controlGroup.getParent();
        CnATreeElement grandparent = parent.getParent();

        if (grandparent instanceof Audit) {
            return true;
        }

        return false;
    }

    /**
     * Returns {@code true} if the given ISA Control is the great-grandchild of an Audit object.
     * 
     * @param  isaControl the ISA Control to be tested
     * @return {@code true} if {@code isaControl} is the grandchild of an Audit object, {@code
     *         false} otherwise
     */
    static boolean isGreatGrandchildOfAudit(SamtTopic isaControl) {

        CnATreeElement parent = isaControl.getParent();
        CnATreeElement grandparent = parent.getParent();
        CnATreeElement greatGrandparent = grandparent.getParent();

        if (greatGrandparent instanceof Audit) {
            return true;
        }

        return false;
    }

    /**
     * Returns {@code true}, if the given {@code audit} has an ISA Control ({@code SamtTopic})
     * in the subtree.
     * 
     * @param  audit the Audit object to be tested
     * @return {@code true}, if the given Audit object has a ISA Control child, {@code false} if
     *         not.
     */
    static boolean hasIsaControlChild(Audit audit) {
        ControlGroup topLevelControlGroup = audit.getControlGroup();
        return hasIsaControl(topLevelControlGroup);
    }

    /**
     * Checks all children for an ISA control, and then their children for an
     * ISA Control. As retrieving of the children is expensive (db call) we
     * check all of them first for being an instance of ISA Control and check their
     * children after.
     * 
     * @param element
     *            the element to check
     * @return {@code true}, if the given Audit object has a ISA Control child,
     *         {@code false} if not.
     */
    private static boolean hasIsaControl(CnATreeElement element) {
        CnATreeElement whithChilds = Retriever.checkRetrieveChildren(element);
        for (CnATreeElement child : whithChilds.getChildren()) {
            if (child instanceof SamtTopic) {
                return true;
            }
        }
        for (CnATreeElement child : whithChilds.getChildren()) {
            if (hasIsaControl(child)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Chooses the path to an overlay icon based on the specified {@code DecoratorColor} and calls 
     * {@code addOverlay()} with that path on the specified {@code IDecoration}.
     * 
     * @param color the {@code DecoratorColor} that the {@code IDecoration} object should get
     * @param decoration the {@code IDecoration} on which {@code addOverlay()} should be called
     */
    static void addOverlay(DecoratorColor color, IDecoration decoration) {

        ImageCache cache = ImageCache.getInstance();

        switch (color) {
            case NULL:
                decoration.addOverlay(cache.getImageDescriptor(IconOverlay.EMPTY.getPath()));
                break;
            case GREEN:
                decoration.addOverlay(cache.getImageDescriptor(IconOverlay.GREEN.getPath()));
                break;
            case YELLOW:
                decoration.addOverlay(cache.getImageDescriptor(IconOverlay.YELLOW.getPath()));
                break;
            case RED:
                decoration.addOverlay(cache.getImageDescriptor(IconOverlay.RED.getPath()));
                break;
            default:
                decoration.addOverlay(cache.getImageDescriptor(IconOverlay.EMPTY.getPath()));
        }
    }

    private static Set<SamtTopic> getChildrenOfTypeIsaControl(ControlGroup controlGroup) {

        Set<SamtTopic> isaControls = new HashSet<>();

        for (CnATreeElement element : controlGroup.getChildren()) {
            if (element instanceof SamtTopic) {
                isaControls.add((SamtTopic) element);
            }
        }

        return isaControls;
    }

    private static int getMaturity(SamtTopic isaControl) {

        SamtTopic hydratedControl = (SamtTopic) Retriever
                .checkRetrieveElement((CnATreeElement) isaControl);

        return hydratedControl.getMaturity();
    }

    private static int getTargetMaturity(SamtTopic isaControl) {

        SamtTopic hydratedControl = (SamtTopic) Retriever
                .checkRetrieveElement((CnATreeElement) isaControl);

        return hydratedControl.getThreshold2();
    }

    /**
     * Returns the rounded average maturity of the given control group defined as the arithmetic
     * mean of its children of type control.
     * 
     * @param  controlGroup the control group for which the average maturity is requested
     * @return the average maturity of the given control group
     */
    private static int getAverageMaturity(ControlGroup controlGroup) {

        int accumulativeMaturity = 0;
        int count = 0;

        for (CnATreeElement child : controlGroup.getChildren()) {
            if (child instanceof SamtTopic) {
                SamtTopic isaControl = (SamtTopic) child;
                int maturity = getMaturity(isaControl);
                int targetMaturity = getTargetMaturity(isaControl);
                if (maturity == Maturity.NOT_EDITED.value()) {
                    maturity = 0;
                }
                if (maturity > targetMaturity) {
                    maturity = targetMaturity;
                }
                if (maturity != Maturity.NOT_APPLICABLE.value()) {
                    accumulativeMaturity += maturity;
                    count += 1;
                }
            }
        }

        if (count == 0) {
            return 0;
        } else {
            return accumulativeMaturity / count;
        }
    }

    /**
     * Returns the rounded average target maturity of the given control group defined as the
     * arithmetic mean of its children of type control.
     * 
     * @param  controlGroup the control group for which the average target maturity is requested
     * @return the average target maturity of the given control group
     */
    private static int getAverageTargetMaturity(ControlGroup controlGroup) {

        int accumulativeTargetMaturity = 0;
        int count = 0;

        for (CnATreeElement child : controlGroup.getChildren()) {
            if (child instanceof SamtTopic) {
                accumulativeTargetMaturity += getTargetMaturity((SamtTopic) child);
                count += 1;
            }
        }

        if (count == 0) {
            return 0;
        } else {
            return accumulativeTargetMaturity / count;
        }
    }

    private enum Maturity {
        NOT_EDITED(-2), NOT_APPLICABLE(-1), MIN(0), MAX(5);

        private int value;

        Maturity(int value) {
            this.value = value;
        }

        public int value() {

            return value;
        }
    }

    private enum TargetMaturity {
        MIN(2), MAX(4);

        private int value;

        TargetMaturity(int value) {
            this.value = value;
        }

        public int value() {

            return value;
        }
    }

    private enum IconOverlay {
        EMPTY("overlays/empty.png"), GREEN("overlays/dot_green.png"), YELLOW(
                "overlays/dot_yellow.png"), RED("overlays/dot_red.png");

        private String path;

        IconOverlay(String path) {
            this.path = path;
        }

        public String getPath() {

            return path;
        }
    }

}
