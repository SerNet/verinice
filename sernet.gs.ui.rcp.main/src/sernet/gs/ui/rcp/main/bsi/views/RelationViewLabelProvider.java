/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.hui.common.connect.IIdentifiableElement;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadElementTitles;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RelationViewLabelProvider extends LabelProvider implements ITableLabelProvider {

    private static final Logger log = Logger.getLogger(RelationViewLabelProvider.class);

    private IRelationTable view;
    private static Map<Integer, String> titleMap = new HashMap<>();

    public RelationViewLabelProvider(IRelationTable view) {
        this.view = view;
    }

    private static String getRisk(CnALink link, String col) {
        String riskValue;
        switch (col) {
        case IRelationTable.COLUMN_RISK_C:
            riskValue = riskValueToString(link.getRiskConfidentiality());
            break;
        case IRelationTable.COLUMN_RISK_C_CONTROLS:
            riskValue = riskValueToString(link.getRiskConfidentialityWithControls());
            break;
        case IRelationTable.COLUMN_RISK_I:
            riskValue = riskValueToString(link.getRiskIntegrity());
            break;
        case IRelationTable.COLUMN_RISK_I_CONTROLS:
            riskValue = riskValueToString(link.getRiskIntegrityWithControls());
            break;
        case IRelationTable.COLUMN_RISK_A:
            riskValue = riskValueToString(link.getRiskAvailability());
            break;
        case IRelationTable.COLUMN_RISK_A_CONTROLS:
            riskValue = riskValueToString(link.getRiskAvailabilityWithControls());
            break;
        case IRelationTable.COLUMN_RISK_TREATMENT:
            if (link.getRiskTreatment() != null) {
                riskValue = CnALink.riskTreatmentLabels.get(link.getRiskTreatment().name());
            } else if (RelationTableViewer.isAssetAndSzenario(link)) {
                riskValue = CnALink.riskTreatmentLabels.get(CnALink.RiskTreatment.UNEDITED.name());
            } else {
                riskValue = StringUtils.EMPTY;
            }
            break;
        default:
            riskValue = StringUtils.EMPTY;
        }
        if (log.isDebugEnabled()) {
            log.debug("Risk values for column: " + col + " is: " + riskValue);
        }
        return riskValue;
    }

    private static String riskValueToString(Integer value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        return value.toString();
    }

    @Override
    public String getColumnText(Object obj, int index) {
        if (obj instanceof PlaceHolder) {
            if (index != 1) {
                return ""; //$NON-NLS-1$
            }
            PlaceHolder pl = (PlaceHolder) obj;
            return pl.getTitle();
        }

        CnALink link = (CnALink) obj;
        HuiRelation relation = HitroUtil.getInstance().getTypeFactory()
                .getRelation(link.getRelationId());

        switch (index) {
        case 0:
            return ""; // image only //$NON-NLS-1$
        case 1:
            // if we can't find a real name for the relation, we just display
            // "depends on" or "necessary for":
            if (CnALink.isDownwardLink(view.getInputElmt(), link)) {
                return (relation != null) ? relation.getName()
                        : Messages.RelationViewLabelProvider_2;
            } else {
                return (relation != null) ? relation.getReversename()
                        : Messages.RelationViewLabelProvider_3;
            }
        case 2:
            return ""; // image only //$NON-NLS-1$
        case 3:
            replaceLinkEntities(link);
            return getLinkTargetTitleIncludingPotentialIdentifier(view.getInputElmt(), link);
        case 4:
            String title = "";
            try {
                CnATreeElement target = getElementOnOtherSide(view.getInputElmt(), link);
                if (!titleMap.containsKey(target.getScopeId())) {
                    title = loadElementsTitles(target);
                } else {
                    title = titleMap.get(target.getScopeId());
                }
            } catch (CommandException e) {
                log.error("Error while getting element properties", e);
            }
            return title; // ScopeTitle from element dependencies
        case 5:
            return link.getComment();
        case 6:
            return getRisk(link, IRelationTable.COLUMN_RISK_TREATMENT);
        case 7:
            return getRisk(link, IRelationTable.COLUMN_RISK_C);
        case 8:
            return getRisk(link, IRelationTable.COLUMN_RISK_I);
        case 9:
            return getRisk(link, IRelationTable.COLUMN_RISK_A);
        case 10:
            return getRisk(link, IRelationTable.COLUMN_RISK_C_CONTROLS);
        case 11:
            return getRisk(link, IRelationTable.COLUMN_RISK_I_CONTROLS);
        case 12:
            return getRisk(link, IRelationTable.COLUMN_RISK_A_CONTROLS);
        default:
            return ""; //$NON-NLS-1$
        }
    }

    private static CnATreeElement getElementOnOtherSide(CnATreeElement elementOnThisSide, CnALink link) {
        CnATreeElement dependency = link.getDependency();
        if (dependency.equals(elementOnThisSide)) {
            return link.getDependant();
        }
        return dependency;
    }

    public static String getLinkTargetTitleIncludingPotentialIdentifier(CnATreeElement linkSource, CnALink link) {
        CnATreeElement linkTarget = getElementOnOtherSide(linkSource, link);
        if (linkTarget instanceof BpRequirement || linkTarget instanceof BpRequirementGroup
                || linkTarget instanceof Safeguard || linkTarget instanceof SafeguardGroup
                || linkTarget instanceof BpThreat) {
            return ((IIdentifiableElement) linkTarget).getFullTitle();
        }
        return CnALink.getRelationObjectTitle(linkSource, link);
    }

    public static void replaceLinkEntities(CnALink link) {
        CnATreeElement dependantWithProperties = Retriever
                .checkRetrieveElement(link.getDependant());
        CnATreeElement dependencyWithProperties = Retriever
                .checkRetrieveElement(link.getDependency());
        link.getDependant().setEntity(dependantWithProperties.getEntity());
        link.getDependency().setEntity(dependencyWithProperties.getEntity());
    }

    @Override
    public Image getColumnImage(Object obj, int index) {
        if (obj instanceof PlaceHolder) {
            return null;
        }
        CnALink link = (CnALink) obj;
        switch (index) {
        case 0:
            if (CnALink.isDownwardLink(view.getInputElmt(), link)) {
                return ImageCache.getInstance().getImage(ImageCache.LINK_DOWN);
            } else {
                return ImageCache.getInstance().getImage(ImageCache.LINK_UP);
            }
        case 2:
            if (CnALink.isDownwardLink(view.getInputElmt(), link)) {
                return getObjTypeImage(link.getDependency());
            } else {
                return getObjTypeImage(link.getDependant());
            }
        default:
            return null;
        }

    }

    private static Image getObjTypeImage(CnATreeElement elmt) {
        return CnAImageProvider.getImage(elmt);

    }

    private static String loadElementsTitles(CnATreeElement elmt) throws CommandException {
        LoadElementTitles scopeCommand;
        scopeCommand = new LoadElementTitles();
        scopeCommand = ServiceFactory.lookupCommandService().executeCommand(scopeCommand);
        titleMap = scopeCommand.getElements();
        return titleMap.get(elmt.getScopeId());
    }

    public CnATreeElement getInputElemt() {
        return view.getInputElmt();
    }

}