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
	
	public static final String IMAGE_NO = "overlays/no.png";
	public static final String IMAGE_PARTLY = "overlays/partly.png";
	public static final String IMAGE_YES = "overlays/yes.png";
	
	private ControlMaturityService maturityService = new ControlMaturityService();
	
	@Override
	public void decorate(Object element, IDecoration decoration) {
		try {
			boolean isActive = Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.ISA_RESULTS); 
			if(isActive && element instanceof IControl) {
				sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();
				IControl control = (IControl)element;
				String state = maturityService.getImplementationState(control);
				if(IControl.IMPLEMENTED_NO.equals(state)) {
					decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_NO));
				}
				if(IControl.IMPLEMENTED_PARTLY.equals(state)) {
					decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_PARTLY));
				}
				if(IControl.IMPLEMENTED_YES.equals(state)) {
					decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(IMAGE_YES));
				}
			}
		} catch(Exception t) {
			LOG.error("Error while loading maturity value", t);
		}
	}

}
