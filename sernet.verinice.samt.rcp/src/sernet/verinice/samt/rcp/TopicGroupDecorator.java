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
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;

@SuppressWarnings("restriction")
public class TopicGroupDecorator extends LabelProvider implements ILightweightLabelDecorator {

	private static final Logger LOG = Logger.getLogger(TopicGroupDecorator.class);
	
	ControlMaturityService maturityService = new ControlMaturityService();
	
	@Override
	public void decorate(Object element, IDecoration decoration) {
		ControlGroup group = null;
		Set<CnATreeElement> childrenRetrieved = null;
		try {
			boolean isActive = Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.ISA_RESULTS); 
			if(isActive && element instanceof ControlGroup) {
				sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();
				group = (ControlGroup) element;
				
				// add a decorator if at least one isa topic child exists
				boolean addDecorator = false;
				Set<CnATreeElement> 
				children = group.getChildren();
				childrenRetrieved = new HashSet<CnATreeElement>(children.size());
				for (CnATreeElement child : children) {
					if(child instanceof IControl) {
						addDecorator = true;
						child = Retriever.checkRetrieveElement(child);
					}
					childrenRetrieved.add(child);				
				}
				if(addDecorator) {
					group.setChildren(childrenRetrieved);
					
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
					
					
					//decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_PATH));
		            decoration.addSuffix( new StringBuilder().append(" [")
		                    .append(maturityService.getWeightedMaturity(group))
		                    .append("]").toString() );
				}
			}
		} catch(Throwable t) {
			LOG.error("Error while loading maturity value", t);
			decoration.addSuffix( new StringBuilder().append(" [?]").toString() );
		}
	}

}
