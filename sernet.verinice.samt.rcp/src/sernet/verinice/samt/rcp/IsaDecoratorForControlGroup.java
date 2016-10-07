
package sernet.verinice.samt.rcp;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.samt.rcp.IsaDecoratorUtil.DecoratorColor;

@SuppressWarnings("restriction")
public class IsaDecoratorForControlGroup extends LabelProvider
        implements ILightweightLabelDecorator {

    @Override
    public void decorate(Object element, IDecoration decoration) {

        boolean preferenceEnabled = Activator.getDefault().getPreferenceStore()
                .getBoolean(SamtPreferencePage.ISA_RESULTS);
        boolean isControlGroup = element instanceof ControlGroup;

        if (!preferenceEnabled || !isControlGroup) {
            return;
        }

        // This Activator has to be addressed with the fully qualified name because otherwise it
        // would collide with the Activator used earlier
        sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();

        ControlGroup controlGroup = (ControlGroup) Retriever
                .checkRetrieveChildren((CnATreeElement) element);

        if (IsaDecoratorUtil.isGrandchildOfAudit(controlGroup)) {
            DecoratorColor color = IsaDecoratorUtil.decoratorColor(controlGroup);
            IsaDecoratorUtil.addOverlay(color, decoration);
        }
    }
}