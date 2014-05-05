package sernet.verinice.samt.rcp;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.samt.service.TotalSecurityFigureCommand;

@SuppressWarnings("restriction")
public class TopicGroupDecorator extends LabelProvider implements ILightweightLabelDecorator {

	private static final Logger LOG = Logger.getLogger(TopicGroupDecorator.class);
	
	private ICommandService commandService;
	
	private ControlMaturityService maturityService = new ControlMaturityService();
	
	@Override
	public void decorate(Object element, IDecoration decoration) {
		ControlGroup group = null;
		try {
			boolean isActive = Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.ISA_RESULTS); 
			if(isActive) {
			    sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();
			    Double securityFigure = null;
			    if(element instanceof Audit) {
			        Audit audit = (Audit) element;
			        audit = (Audit) Retriever.checkRetrieveChildren(audit);
			        group = (ControlGroup) audit.getGroup(ControlGroup.TYPE_ID);
			        group = (ControlGroup) Retriever.checkRetrieveChildren(group);
			        TotalSecurityFigureCommand command = new TotalSecurityFigureCommand(audit.getDbId());
			        command = getCommandService().executeCommand(command);
			        securityFigure = command.getResult();
			        if (LOG.isDebugEnabled()) {
                        LOG.debug("Security figure: " + securityFigure + ", audit uuid: " + audit.getUuid());
                    }
			    }
			    if(element instanceof ControlGroup) {	
    				group = (ControlGroup) Retriever.checkRetrieveChildren((CnATreeElement) element);	
			    }
				// add a decorator if at least one isa topic child exists
				boolean addDecorator = retrieveChildren(group);
				if(addDecorator) {
					String state = maturityService.getImplementationState(group);
					addOverlayAndSuffix(decoration, securityFigure, state);
				}
			}		
		} catch(CommandException t) {
			LOG.error("Error while loading maturity value", t);
		}
	}

    private void addOverlayAndSuffix(IDecoration decoration, Double securityFigure, String state) {
        if(IControl.IMPLEMENTED_NO.equals(state)) {
        	decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(TopicDecorator.IMAGE_NO));
        }
        if(IControl.IMPLEMENTED_PARTLY.equals(state)) {
        	decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(TopicDecorator.IMAGE_PARTLY));
        }
        if(IControl.IMPLEMENTED_YES.equals(state)) {
        	decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(TopicDecorator.IMAGE_YES));
        }
        if(securityFigure!=null) {
            StringBuilder sb = new StringBuilder();
            sb.append(" [").append(getPercent(securityFigure)).append(" %") .append("]");
            decoration.addSuffix(sb.toString());
        }
    }
	
	/**
	 * Retrieves all children of a ControlGroup.
	 * Returns true if at least one {@link IControl} child exists.
	 * 
	 * @param group a ControlGroup
	 * @return true if at least one {@link IControl} child exists
	 */
	private boolean retrieveChildren(/*not final*/ControlGroup group) {
	    boolean isIsa = false;
	    Set<CnATreeElement> children = group.getChildren();
	    Set<CnATreeElement> childrenRetrieved = new HashSet<CnATreeElement>(children.size());
        for (CnATreeElement child : children) {
            if(child instanceof IControl) {
                child = Retriever.checkRetrieveElement(child);
                isIsa = true;
            }
            if(child instanceof ControlGroup) {
                child = Retriever.checkRetrieveChildren(child);
                boolean isIsaRecursiv = retrieveChildren((ControlGroup) child);
                if(isIsaRecursiv) {
                    isIsa = true; 
                }
            }
            childrenRetrieved.add(child);               
        }
        group.setChildren(childrenRetrieved);
        return isIsa;
	}
	
	private Double getPercent(Double d) {
	    final double factor = 10000.0;
	    final double divisor = 100.0;
	    return Math.round(d*factor) / divisor;
	}
	
	private ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

}
