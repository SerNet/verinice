
package sernet.verinice.samt.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.samt.rcp.IsaDecoratorUtil.DecoratorColor;

import java.math.BigDecimal;

@SuppressWarnings("restriction")
public class IsaDecoratorForAudit extends LabelProvider implements ILightweightLabelDecorator {

    private static final Logger logger = Logger.getLogger(IsaDecoratorForAudit.class);

    @Override
    public void decorate(Object element, IDecoration decoration) {

        boolean preferenceEnabled = Activator.getDefault().getPreferenceStore()
                .getBoolean(SamtPreferencePage.ISA_RESULTS);

        if (!preferenceEnabled || !(element instanceof Audit)) {
            return;
        }

        // This Activator has to be addressed with the fully qualified name because otherwise it
        // would collide with the Activator used earlier
        sernet.gs.ui.rcp.main.Activator.inheritVeriniceContextState();

        Audit audit = (Audit) Retriever.checkRetrieveChildren((CnATreeElement) element);

        if (IsaDecoratorUtil.hasIsaControlChild(audit)) {

            DecoratorColor color = IsaDecoratorUtil.decoratorColor(audit);
            IsaDecoratorUtil.addOverlay(color, decoration);

            BigDecimal score = IsaDecoratorUtil.resultScore(audit);

            StringBuilder sb = new StringBuilder();
            sb.append(" [").append(String.format("%.2f", score)).append("]");
            decoration.addSuffix(sb.toString());

            logger.debug("Score: " + score + ", audit uuid: " + audit.getUuid());
        }
    }
}
