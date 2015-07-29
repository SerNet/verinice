package sernet.verinice.samt.rcp;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.iso27k.service.ControlMaturityService.DecoratorColor;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.samt.service.TotalSecurityFigureISA2Command;

@SuppressWarnings("restriction")
public class TopicGroupDecorator extends LabelProvider implements ILightweightLabelDecorator {

	private static final Logger LOG = Logger.getLogger(TopicGroupDecorator.class);
		
    @Override
    public void decorate(Object element, IDecoration decoration) {
        boolean prefEnabled = Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.ISA_RESULTS);
        if (!prefEnabled ||
            !(element instanceof Audit || element instanceof ControlGroup) ||
            !IsaDecoratorUtil.isAuditGroupDescendant((CnATreeElement) element)) {
            return;
        }

        ControlGroup controlGroup = null;
        
        try {
            sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();
            Double securityFigure = null;
            if (element instanceof Audit) {
                Audit audit = (Audit) element;
                audit = (Audit) Retriever.checkRetrieveChildren(audit);
                controlGroup = (ControlGroup) audit.getGroup(ControlGroup.TYPE_ID);
                controlGroup = (ControlGroup) Retriever.checkRetrieveChildren(controlGroup);
                TotalSecurityFigureISA2Command command = new TotalSecurityFigureISA2Command(audit.getDbId());
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                securityFigure = command.getResult();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Security figure: " + securityFigure + ", audit uuid: " + audit.getUuid());
                }
            }
            if (element instanceof ControlGroup) {
                controlGroup = (ControlGroup) Retriever.checkRetrieveChildren((CnATreeElement) element);
            }
            // add a decorator if at least one isa topic child exists
            boolean addDecorator = retrieveChildrenAndCheckForIControl(controlGroup);
            if (addDecorator) {
                DecoratorColor color = (new ControlMaturityService()).getDecoratorColor(controlGroup);
                IsaDecoratorUtil.addOverlay(color, decoration);

                if (securityFigure != null) {
                    addSuffix(securityFigure, decoration);
                }
            }
        } catch (CommandException t) {
            LOG.error("Error computing decorators for ISA Controls and Control Groups", t);
        }
    }
    
    /**
     * Retrieves all children of a ControlGroup.
     * Returns true if at least one {@link IControl} child exists.
     * 
     * @param group a ControlGroup
     * @return true if at least one {@link IControl} child exists
     */
    private boolean retrieveChildrenAndCheckForIControl(/*not final*/ControlGroup group) {
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
                boolean isIsaRecursiv = retrieveChildrenAndCheckForIControl((ControlGroup) child);
                if(isIsaRecursiv) {
                    isIsa = true; 
                }
            }
            childrenRetrieved.add(child);               
        }
        group.setChildren(childrenRetrieved);
        return isIsa;
    }
    
    private void addSuffix(double securityFigure, IDecoration decoration) {
        StringBuilder sb = new StringBuilder();
        sb.append(" [").append(String.format("%.2f", securityFigure)).append("]");
        decoration.addSuffix(sb.toString());
    }	
}