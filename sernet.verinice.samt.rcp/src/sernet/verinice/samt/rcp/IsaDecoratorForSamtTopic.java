
package sernet.verinice.samt.rcp;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.samt.rcp.IsaDecoratorUtil.DecoratorColor;

@SuppressWarnings("restriction")
public class IsaDecoratorForSamtTopic extends LabelProvider implements ILightweightLabelDecorator {

    @Override
    public void decorate(Object element, IDecoration decoration) {

        boolean preferenceEnabled = Activator.getDefault().getPreferenceStore()
                .getBoolean(SamtPreferencePage.ISA_RESULTS);
        boolean isIsaControl = element instanceof SamtTopic;

        if (!preferenceEnabled || !isIsaControl) {
            return;
        }

        sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();
        SamtTopic isaControl = (SamtTopic) element;

        boolean isGreatGrandchildOfAudit = IsaDecoratorUtil.isGreatGrandchildOfAudit(isaControl);

        if (!isGreatGrandchildOfAudit) {
            return;
        }

        DecoratorColor color = IsaDecoratorUtil.decoratorColor(isaControl);
        IsaDecoratorUtil.addOverlay(color, decoration);
    }
}
