package sernet.verinice.samt.rcp;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;
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
    
    /**
     * Retrieves all children of a ControlGroup.
     * Returns true if at least one {@link IControl} child exists.
     * 
     * @param controlGroup a ControlGroup
     * @return true if at least one {@link IControl} child exists
     */
    protected static boolean retrieveChildrenAndCheckForIControl(/*not final*/ControlGroup controlGroup) {
        boolean isIsa = false;
        Set<CnATreeElement> children = controlGroup.getChildren();
        Set<CnATreeElement> childrenRetrieved = new HashSet<CnATreeElement>(children.size());
        for (CnATreeElement child : children) {
            if(child instanceof IControl) {
                child = Retriever.checkRetrieveElement(child);
                isIsa = true;
            }
            if(child instanceof ControlGroup) {
                child = Retriever.checkRetrieveChildren(child);
                boolean isIsaRecursiv = retrieveChildrenAndCheckForIControl((ControlGroup) child);
                if(isIsaRecursiv) {
                    isIsa = true; 
                }
            }
            childrenRetrieved.add(child);               
        }
        controlGroup.setChildren(childrenRetrieved);
        return isIsa;
    }
}
