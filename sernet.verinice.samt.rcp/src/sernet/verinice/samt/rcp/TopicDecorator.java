package sernet.verinice.samt.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IControl;

@SuppressWarnings("restriction")
public class TopicDecorator extends LabelProvider implements ILightweightLabelDecorator {

    private static final Logger LOG = Logger.getLogger(TopicDecorator.class);

    private ControlMaturityService maturityService = new ControlMaturityService();
    
    @Override
    public void decorate(Object element, IDecoration decoration) {
        boolean prefEnabled = Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.ISA_RESULTS);
        if (!prefEnabled ||
            !(element instanceof IControl) ||
            !IsaDecoratorUtil.isAuditGroupDescendant((CnATreeElement) element)) {
            return;
        }

        try {
            sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();
            IsaDecoratorUtil.addOverlay(maturityService.getDecoratorColor((IControl) element), decoration);
        } catch (Exception e) {
            LOG.error("Error while using ControlMaturityService to determine decorator color.", e);
        }
    }
}
