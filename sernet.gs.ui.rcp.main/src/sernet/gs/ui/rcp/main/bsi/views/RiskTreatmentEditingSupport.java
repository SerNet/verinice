/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.service.commands.UpdateElement;

/**
 * This class provides the editing support for the risk treatment column in the
 * relations table used in the LinkMaker and in the RelationTableViewer.
 *
 * @author dm[at]sernet[dot]de
 */
public class RiskTreatmentEditingSupport extends EditingSupport {

    private static final Logger LOG = Logger.getLogger(RiskTreatmentEditingSupport.class);
    
    private static final String[] RISK_TREATMENT_LABELS;
    static {
        List<String> riskTreatmentLabelList = new LinkedList<>(CnALink.riskTreatmentLabels.values());
        Collections.sort(riskTreatmentLabelList);
        RISK_TREATMENT_LABELS = riskTreatmentLabelList.toArray(new String[riskTreatmentLabelList.size()]);
    }
    
    private IRelationTable view;
    private TableViewer viewer;
    
    private ICommandService commandService;

    public RiskTreatmentEditingSupport(IRelationTable view, TableViewer viewer) {
        super(viewer);
        this.view = view;
        this.viewer = viewer;
    }

    @Override
    protected boolean canEdit(Object element) {
        boolean canEdit = element instanceof CnALink;
        if (canEdit) {
            CnALink link = (CnALink) element;
            canEdit = RelationTableViewer.isAssetAndSzenario(link);
        }
        return canEdit;

    }  
    
    @Override
    protected CellEditor getCellEditor(Object element) {
        if (!(element instanceof CnALink)) {
            return null;
        } 
        ComboBoxCellEditor cellEditor = new ComboBoxCellEditor(viewer.getTable(),
                RISK_TREATMENT_LABELS, SWT.READ_ONLY);
        cellEditor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
        return cellEditor;
    }

    @Override
    protected Object getValue(Object element) {
        if (!(element instanceof CnALink)) {
            return null;
        }
        CnALink cnaLink = (CnALink) element;
        CnALink.RiskTreatment riskTreatmentValue = cnaLink.getRiskTreatment();
        return getIndexOfRiskTreatment(riskTreatmentValue);
    }

    public int getIndexOfRiskTreatment(CnALink.RiskTreatment riskTreatment) {
        if (riskTreatment == null) {
            riskTreatment = CnALink.RiskTreatment.UNEDITED;
        }
        int i = 0;
        int index = -1;
        String selectedLabel = CnALink.riskTreatmentLabels.get(riskTreatment.name());
        for (String label : RISK_TREATMENT_LABELS) {
            if(selectedLabel.equals(label)) {
                index = i;            
                break;
            }
            i++;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Index of selected risk treatment is: " + index);
        }
        return index;
    }

    @Override
    protected void setValue(Object element, Object selectedIndex) {
        try {
            if (!(element instanceof CnALink)) {
                return;
            }
            CnALink cnaLink = (CnALink) element;
            String selectedLabel = RISK_TREATMENT_LABELS[(Integer) selectedIndex];         
            selectRiskTreatment(cnaLink, selectedLabel);
            CnALink newLink = updateLink(cnaLink);
            CnAElementFactory.getModel(cnaLink.getDependant()).linkChanged(cnaLink, newLink, view);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Risk treatment of cnalink saved: " + newLink.getRiskTreatment());
            }
        } catch(Exception e) {
            LOG.error("Error while setting risk treatment.", e);
        }
    }

    public void selectRiskTreatment(CnALink cnaLink, String selectedLabel) {
        for (String key : CnALink.riskTreatmentLabels.keySet()) {
            if (selectedLabel.equals(CnALink.riskTreatmentLabels.get(key))) {
                cnaLink.setRiskTreatment(CnALink.RiskTreatment.valueOf(key));                 
                break;                  
            }
        }
    }

    public CnALink updateLink(CnALink cnaLink) throws CommandException {
        UpdateElement<CnALink> command = new UpdateElement<>(cnaLink, true, ChangeLogEntry.STATION_ID);
        command = getCommandService().executeCommand(command);
        return command.getElement();
    }
    
    private ICommandService getCommandService() {
        if(commandService == null) {
            commandService = ServiceFactory.lookupCommandService();
        }
        return commandService;
    }

}
