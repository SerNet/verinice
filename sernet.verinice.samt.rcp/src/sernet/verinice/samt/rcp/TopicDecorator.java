package sernet.verinice.samt.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.model.iso27k.IControl;

@SuppressWarnings("restriction")
public class TopicDecorator extends LabelProvider implements ILightweightLabelDecorator {

    private static final Logger LOG = Logger.getLogger(TopicDecorator.class);

    public static final String ICON_OVERLAY_EMTPY = "overlays/empty.png";
    public static final String ICON_OVERLAY_GREEN = "overlays/dot_green.png";
    public static final String ICON_OVERLAY_YELLOW = "overlays/dot_yellow.png";
    public static final String ICON_OVERLAY_RED = "overlays/dot_red.png";

    private ControlMaturityService maturityService = new ControlMaturityService();
    private ImageCache imageCache = ImageCache.getInstance();
    private IDecoration decoration;

    @Override
    public void decorate(Object element, IDecoration decoration) {
        boolean isActive = Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.ISA_RESULTS);
        if (!isActive) {
            return;
        }
        
        try {
            if (element instanceof IControl) {
                sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();
                IControl control = (IControl) element;
                this.decoration = decoration;
                addOverlay(maturityService.getDecoratorColor(control));
            }
        } catch (Exception t) {
            LOG.error("Error while using ControlMaturityService to determine decorator color.", t);
        }
    }

    void addOverlay(ControlMaturityService.DecoratorColor color) {
        switch (color) {
        case NULL:
            decoration.addOverlay(imageCache.getImageDescriptor(ICON_OVERLAY_EMTPY));
            break;
        case GREEN:
            decoration.addOverlay(imageCache.getImageDescriptor(ICON_OVERLAY_GREEN));
            break;
        case YELLOW:
            decoration.addOverlay(imageCache.getImageDescriptor(ICON_OVERLAY_YELLOW));
            break;
        case RED:
            decoration.addOverlay(imageCache.getImageDescriptor(ICON_OVERLAY_RED));
            break;
        default:
            decoration.addOverlay(imageCache.getImageDescriptor(ICON_OVERLAY_EMTPY));
        }
    }
}
