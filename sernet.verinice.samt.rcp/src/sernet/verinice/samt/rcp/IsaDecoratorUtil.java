package sernet.verinice.samt.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.service.commands.LoadAncestors;

@SuppressWarnings("restriction")
public final class IsaDecoratorUtil {
     
    private static final Logger LOG = Logger.getLogger(IsaDecoratorUtil.class);
    
    private static enum IconOverlay {
        EMPTY("overlays/empty.png"),
        GREEN("overlays/dot_green.png"),
        YELLOW("overlays/dot_yellow.png"),
        RED("overlays/dot_red.png");
        
        private String path;
        
        IconOverlay(String path) {
            this.path = path;
        }
        
        public String getPath() {
            return path;
        }
    }
    
    protected static boolean isAuditGroupDescendant(CnATreeElement element) {
        try {
            LoadAncestors command = new LoadAncestors(element.getUuid(), new RetrieveInfo());
            command = ServiceFactory.lookupCommandService().executeCommand(command);          
            element = command.getElement();
            
            while (element.getParent() != null) {
                if (element instanceof AuditGroup) {
                    return true;
                }
                element = element.getParent();
            }
        } catch (CommandException e) {
            LOG.error("Error while loading and checking ancestor elements", e);
        }
        return false;
    }
    
    protected static void addOverlay(ControlMaturityService.DecoratorColor color, IDecoration decoration) {
        switch (color) {
        case NULL:
            decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IconOverlay.EMPTY.getPath()));
            break;
        case GREEN:
            decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IconOverlay.GREEN.getPath()));
            break;
        case YELLOW:
            decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IconOverlay.YELLOW.getPath()));
            break;
        case RED:
            decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IconOverlay.RED.getPath()));
            break;
        default:
            decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IconOverlay.EMPTY.getPath()));
        }
    }
}
