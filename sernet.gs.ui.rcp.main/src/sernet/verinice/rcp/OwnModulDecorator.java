package sernet.verinice.rcp;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.bsi.BausteinUmsetzung;

/**Decorator for a new own module
 * @author Julia Haas <jh[at]sernet[dot]de>
 * 
 */
public class OwnModulDecorator extends LabelProvider implements ILightweightLabelDecorator {

    public static final String IMAGE_PATH = "overlays/owned_ovr.gif";

    public void decorate(Object o, IDecoration decoration) {
        if (o instanceof BausteinUmsetzung) {
            BausteinUmsetzung baustein = (BausteinUmsetzung) o;
            if (baustein.getUrl() == null || baustein.getUrl().isEmpty() || baustein.getUrl().equals("null")) {

                decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_PATH));
            }

        }

    }
}
