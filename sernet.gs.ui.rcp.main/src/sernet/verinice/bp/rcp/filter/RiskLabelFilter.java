package sernet.verinice.bp.rcp.filter;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.hui.common.connect.ITargetObject;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

public class RiskLabelFilter extends ViewerFilter {
    private Set<String> risklabels;

    public RiskLabelFilter(Set<String> riskLabels) {
        this.risklabels = riskLabels;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof BpThreat) {
            return matchesRisks((BpThreat) element);
        } else if (element instanceof CnATreeElement && element instanceof ITargetObject
                && !(element instanceof ItNetwork)) {
            return Retriever
                    .retrieveElement(((CnATreeElement) element),
                            new RetrieveInfo().setLinksUpProperties(true))
                    .getLinksUp().stream().map(CnALink::getDependant)
                    .filter(d -> d instanceof BpThreat).anyMatch(t -> matchesRisks((BpThreat) t));
        }
        return true;
    }

    private Boolean matchesRisks(@NonNull BpThreat threat) {
        return risklabels.contains(threat.getRiskLabel());
    }
}
