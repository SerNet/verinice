/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.bpm.TodoViewItem;
import sernet.verinice.model.bsi.CnAPlaceholder;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.iso27k.ControlMaturityService;

public final class CnAImageProvider {

    private static final Map<String, String> IMAGE_NAME_BY_STATE;

    private static final ControlMaturityService CONTROL_MATURITY_SERVICE = new ControlMaturityService();

    static {
        Map<String, String> m = new HashMap<>();
        m.put(MassnahmenUmsetzung.P_UMSETZUNG_NEIN, ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
        m.put(MassnahmenUmsetzung.P_UMSETZUNG_JA, ImageCache.MASSNAHMEN_UMSETZUNG_JA);
        m.put(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
        m.put(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH,
                ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_NO, ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_YES, ImageCache.MASSNAHMEN_UMSETZUNG_JA);
        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_PARTIALLY,
                ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_NOT_APPLICABLE,
                ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_NO, ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_YES, ImageCache.MASSNAHMEN_UMSETZUNG_JA);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_PARTIALLY,
                ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_NOT_APPLICABLE,
                ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
        IMAGE_NAME_BY_STATE = Collections.unmodifiableMap(m);
    }

    public static Image getImage(TodoViewItem elmt) {
        return getImageByImplementationState(elmt.getUmsetzung());
    }

    /**
     * Obtain an image for the given element.
     * 
     * This method returns a custom image if there is one configured for the
     * image. For elements that have an implementation state, it will return an
     * image that represents the element's state. For groups, it will return the
     * respective element's icon. Otherwise, it will return an appropriate image
     * according to the element's type.
     * 
     */
    public static Image getImage(CnATreeElement cnATreeElement) {
        return getImage(cnATreeElement, false);
    }

    /**
     * Obtain an image for the given element.
     * 
     * This method returns a custom image if there is one configured for the
     * image. For elements that have an implementation state, it will return an
     * image that represents the element's state (see parameter
     * {@code useGenericIconForISAControl}). For groups, it will return the
     * respective element's icon. Otherwise, it will return an appropriate image
     * according to the element's type.
     * 
     * @param useGenericIconForISAControl
     *            if set to <code>true</code>, a generic icon will be used for
     *            ISA Controls ({@link SamtTopic}} instead of one that
     *            represent's the contol's implementation state.
     */
    public static Image getImage(CnATreeElement cnATreeElement,
            boolean useGenericIconForISAControl) {
        String customIconPath = cnATreeElement.getIconPath();
        ImageCache imageCache = ImageCache.getInstance();
        if (customIconPath != null) {
            Image customIcon = imageCache.getCustomImage(customIconPath);
            if (customIcon != null) {
                return customIcon;
            }
            // what should we do if we cannot find the icon? Ignore it and
            // return a default icon, throw an exception, return a special
            // "404 icon"?
        }
        return getDefaultImage(cnATreeElement, useGenericIconForISAControl);
    }

    private static Image getDefaultImage(CnATreeElement element,
            boolean useGenericIconForISAControl) {
        ImageCache imageCache = ImageCache.getInstance();
        Image image = null;

        // the BSIElement editor icon for an ISA control (SamtTopic) is not
        // supposed to be chosen according to its implementation state
        if (!(element instanceof SamtTopic && useGenericIconForISAControl)) {
            image = findImageByImplementationState(element);
        }
        if (image != null) {
            return image;
        }

        if (imageCache.isBSITypeElement(element.getTypeId())) {
            return imageCache.getBSITypeImage(element.getTypeId());
        }

        // special cases for some old BP elements
        if (element instanceof FinishedRiskAnalysis) {
            return imageCache.getImage(ImageCache.RISIKO_MASSNAHMEN_UMSETZUNG);
        }
        if (element instanceof GefaehrdungsUmsetzung) {
            return imageCache.getImage(ImageCache.GEFAEHRDUNG);
        }
        if (element instanceof ImportBpGroup || element instanceof ImportIsoGroup
                || element instanceof ImportBsiGroup) {
            return imageCache.getImage(ImageCache.ISO27K_IMPORT);
        }

        if (element instanceof CnAPlaceholder) {
            return imageCache.getImage(ImageCache.EXPLORER);
        }

        if (element instanceof Group<?>) {
            // TODO - getChildTypes()[0] might be a problem for more than one
            // type
            String elementType = ((Group<?>) element).getChildTypes()[0];
            return imageCache.getImageForTypeId(elementType);
        }

        return imageCache.getImageForTypeId(element.getTypeId());
    }

    private static Image findImageByImplementationState(CnATreeElement element) {
        if (element instanceof Safeguard) {
            Safeguard safeguard = (Safeguard) element;
            safeguard = (Safeguard) Retriever.checkRetrieveElement(safeguard);
            String state = safeguard.getImplementationStatus();

            final Image implementationStateImage = getImageByImplementationState(state);
            final Image typeImage = ImageCache.getInstance().getImage(ImageCache.BP_SAFEGUARD);

            return createImageWithOverlay(implementationStateImage, typeImage);
        }
        if (element instanceof BpRequirement) {
            BpRequirement requirement = (BpRequirement) element;
            requirement = (BpRequirement) Retriever.checkRetrieveElement(requirement);
            String state = requirement.getImplementationStatus();
            final Image implementationStateImage = getImageByImplementationState(state);
            final Image typeImage = ImageCache.getInstance().getImage(ImageCache.BP_REQUIREMENT);

            return createImageWithOverlay(implementationStateImage, typeImage);
        }
        if (element instanceof Control) {
            Control control = (Control) element;
            return ImageCache.getInstance()
                    .getControlImplementationImage(control.getImplementation());
        } else if (element instanceof SamtTopic) {
            SamtTopic topic = (SamtTopic) element;
            return ImageCache.getInstance()
                    .getControlImplementationImage(CONTROL_MATURITY_SERVICE.getIsaState(topic));
        }
        if (element instanceof MassnahmenUmsetzung) {
            MassnahmenUmsetzung mn = (MassnahmenUmsetzung) element;
            mn = (MassnahmenUmsetzung) Retriever.checkRetrieveElement(mn);
            String state = mn.getUmsetzung();
            return getImageByImplementationState(state);
        }
        return null;
    }

    private static Image createImageWithOverlay(final Image baseImage, final Image overlayImage) {
        CompositeImageDescriptor imageWithOverlayDescriptor = new ImageWithOverlayDescriptor(
                baseImage, overlayImage);
        return imageWithOverlayDescriptor.createImage();
    }

    private static Image getImageByImplementationState(String state) {
        String imageName = IMAGE_NAME_BY_STATE.get(state);
        if (imageName == null) {
            imageName = ImageCache.MASSNAHMEN_UMSETZUNG_UNBEARBEITET;
        }
        return ImageCache.getInstance().getImage(imageName);
    }

    private static final class ImageWithOverlayDescriptor extends CompositeImageDescriptor {
        private final Image baseImage;
        private final Image overlayImage;

        private ImageWithOverlayDescriptor(Image baseImage, Image overlayImage) {
            this.baseImage = baseImage;
            this.overlayImage = overlayImage;
        }

        @Override
        protected Point getSize() {
            return new Point(16, 16);
        }

        @Override
        protected void drawCompositeImage(int width, int height) {
            drawImage(baseImage.getImageData(), 0, 0);
            drawImage(overlayImage.getImageData().scaledTo(8, 8), 0, 0);
        }
    }

    private CnAImageProvider() {

    }
}
