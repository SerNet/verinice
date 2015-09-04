package sernet.verinice.samt.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.iso27k.service.ControlMaturityService.DecoratorColor;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.samt.service.TotalSecurityFigureISA2Command;

@SuppressWarnings("restriction")
public class IsaDecoratorForAudit extends LabelProvider implements ILightweightLabelDecorator {

    private static final Logger LOG = Logger.getLogger(IsaDecoratorForControlGroup.class);

    @Override
    public void decorate(Object element, IDecoration decoration) {
        boolean prefEnabled = Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.ISA_RESULTS);
        if (!prefEnabled || !(element instanceof Audit)) {
            return;
        }

        Audit audit = (Audit) Retriever.checkRetrieveChildren((Audit) element);
        
        ControlGroup controlGroup = (ControlGroup) audit.getGroup(ControlGroup.TYPE_ID);
        controlGroup = (ControlGroup) Retriever.checkRetrieveChildren(controlGroup);
             
        boolean hasIControlChild = IsaDecoratorUtil.retrieveChildrenAndCheckForIControl(controlGroup);
        if (hasIControlChild) {
            DecoratorColor color = (new ControlMaturityService()).getDecoratorColor(controlGroup);
            IsaDecoratorUtil.addOverlay(color, decoration);
            
            Double securityFigure = getSecurityFigure(audit);
            addSuffix(securityFigure, decoration);

            LOG.debug("Security figure: " + securityFigure + ", audit uuid: " + audit.getUuid());
        }
    }
    
    private Double getSecurityFigure(Audit audit) {
        TotalSecurityFigureISA2Command command = null;
        try {
            command = new TotalSecurityFigureISA2Command(audit.getDbId());
            command = ServiceFactory.lookupCommandService().executeCommand(command);
        } catch (CommandException e) {
            LOG.error("Error computing security figure for Audit.", e);
        }
        return command.getResult();
    }

    private void addSuffix(double securityFigure, IDecoration decoration) {
        StringBuilder sb = new StringBuilder();
        sb.append(" [").append(String.format("%.2f", securityFigure)).append("]");
        decoration.addSuffix(sb.toString());
    }
}
