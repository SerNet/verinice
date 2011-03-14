package sernet.verinice.samt.rcp;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;

@SuppressWarnings("restriction")
public class TopicGroupDecorator extends LabelProvider implements ILightweightLabelDecorator {

	private static final Logger LOG = Logger.getLogger(TopicGroupDecorator.class);
	
	ControlMaturityService maturityService = new ControlMaturityService();
	
	@Override
	public void decorate(Object element, IDecoration decoration) {
		ControlGroup group = null;
		try {
			boolean isActive = Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.ISA_RESULTS); 
			if(isActive) {
			    sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();
			    if(element instanceof Audit) {
			        Audit audit = (Audit) element;
			        group = (ControlGroup) audit.getGroup(ControlGroup.TYPE_ID);
			        group = (ControlGroup) Retriever.checkRetrieveChildren(group);
			    }
			    if(element instanceof ControlGroup) {	
    				group = (ControlGroup) element;	
			    }
				// add a decorator if at least one isa topic child exists
				boolean addDecorator = true;
				retrieveChildren(group);
				if(addDecorator) {
					String state = maturityService.getImplementationState(group);
					if(IControl.IMPLEMENTED_NO.equals(state)) {
						decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(TopicDecorator.IMAGE_NO));
					}
					if(IControl.IMPLEMENTED_PARTLY.equals(state)) {
						decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(TopicDecorator.IMAGE_PARTLY));
					}
					if(IControl.IMPLEMENTED_YES.equals(state)) {
						decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(TopicDecorator.IMAGE_YES));
					}
				}
			}		
		} catch(Throwable t) {
			LOG.error("Error while loading maturity value", t);
		}
	}
	
	private void retrieveChildren(/*not final*/ControlGroup group) {
	    Set<CnATreeElement> children = group.getChildren();
	    Set<CnATreeElement> childrenRetrieved = new HashSet<CnATreeElement>(children.size());
        for (CnATreeElement child : children) {
            if(child instanceof IControl) {
                child = Retriever.checkRetrieveElement(child);
            }
            if(child instanceof ControlGroup) {
                child = Retriever.checkRetrieveChildren(child);
                retrieveChildren((ControlGroup) child);
            }
            childrenRetrieved.add(child);               
        }
        group.setChildren(childrenRetrieved);
	}

}
