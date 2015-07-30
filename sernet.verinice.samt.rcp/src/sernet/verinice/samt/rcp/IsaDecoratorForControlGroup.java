package sernet.verinice.samt.rcp;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.iso27k.service.ControlMaturityService.DecoratorColor;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;

@SuppressWarnings("restriction")
public class IsaDecoratorForControlGroup extends LabelProvider implements ILightweightLabelDecorator {

//    private static final Logger LOG = Logger.getLogger(IsaDecoratorForControlGroup.class);

    @Override
    public void decorate(Object element, IDecoration decoration) {
        boolean prefEnabled = Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.ISA_RESULTS);
        if (!prefEnabled ||
            !(element instanceof ControlGroup) ||
            !IsaDecoratorUtil.isAuditGroupDescendant((CnATreeElement) element)) {
            return;
        }

        sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();
        ControlGroup controlGroup = (ControlGroup) Retriever.checkRetrieveChildren((CnATreeElement) element);;

        boolean hasIControlChild = IsaDecoratorUtil.retrieveChildrenAndCheckForIControl(controlGroup);
        if (hasIControlChild) {
            DecoratorColor color = (new ControlMaturityService()).getDecoratorColor(controlGroup);
            IsaDecoratorUtil.addOverlay(color, decoration);
        }
    }
}