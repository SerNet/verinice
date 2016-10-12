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
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.LoadElementTitles;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RelationViewLabelProvider extends LabelProvider implements ITableLabelProvider {
    
    private static final Logger LOG = Logger.getLogger(RelationViewLabelProvider.class);
    
    public static final Map<String, String> RISK_TREATMENT_LABELS;
    static {
        RISK_TREATMENT_LABELS = new Hashtable<>();
        RISK_TREATMENT_LABELS.put(CnALink.RiskTreatment.ACCEPT.name(), Messages.RelationViewLabelProvider_ACCEPT);
        RISK_TREATMENT_LABELS.put(CnALink.RiskTreatment.AVOID.name(), Messages.RelationViewLabelProvider_AVOID);
        RISK_TREATMENT_LABELS.put(CnALink.RiskTreatment.MODIFY.name(), Messages.RelationViewLabelProvider_MODIFY);
        RISK_TREATMENT_LABELS.put(CnALink.RiskTreatment.TRANSFER.name(), Messages.RelationViewLabelProvider_TRANSFER);
    }
	private IRelationTable view;
	private static HashMap<Integer, String> titleMap = new HashMap<>();
	  
	public RelationViewLabelProvider(IRelationTable view) {
        this.view = view;
    }
	
	private String getRisk(CnALink link, String col) {
	    String riskValue = "";
	    switch (col) {
            case IRelationTable.COLUMN_RISK_C:
                if (link.getRiskConfidentiality() != null){
                    riskValue = link.getRiskConfidentiality().toString();
                }
                break;
            case IRelationTable.COLUMN_RISK_C_CONTROLS:
                if (link.getRiskConfidentialityWithControls() != null){
                    riskValue = link.getRiskConfidentialityWithControls().toString();
                }
                break;
            case IRelationTable.COLUMN_RISK_I:
                if (link.getRiskIntegrity() != null){
                    riskValue = link.getRiskIntegrity().toString();
                }
                break;
            case IRelationTable.COLUMN_RISK_I_CONTROLS:
                if (link.getRiskIntegrityWithControls() != null){
                    riskValue = link.getRiskIntegrityWithControls().toString();
                }
                break;
            case IRelationTable.COLUMN_RISK_A:
                if (link.getRiskAvailability() != null){
                    riskValue = link.getRiskAvailability().toString();
                }
                break;
            case IRelationTable.COLUMN_RISK_A_CONTROLS:
                if (link.getRiskAvailabilityWithControls() != null){
                    riskValue = link.getRiskAvailabilityWithControls().toString();
                }
                break;
            case IRelationTable.COLUMN_RISK_TREATMENT:
                if (link.getRiskTreatment() != null){
                    riskValue = RISK_TREATMENT_LABELS.get(link.getRiskTreatment().name());
                }
        }
	    if (LOG.isDebugEnabled()) {
            LOG.debug("Risk values for column: " + col + " is: " + riskValue);
        }
	    return riskValue;
	}

	@Override
    public String getColumnText(Object obj, int index) {
	    if (obj instanceof PlaceHolder) {
	        if (index != 1){
	            return ""; //$NON-NLS-1$
	        }
	        PlaceHolder pl = (PlaceHolder) obj;
	        return pl.getTitle();
	    }

	    CnALink link = (CnALink) obj;
	    HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(link.getRelationId());

	    switch (index) {
	    case 0:
	        return ""; // image only //$NON-NLS-1$
	    case 1:
	        // if we can't find a real name for the relation, we just display
	        // "depends on" or "necessary for":
	        if (CnALink.isDownwardLink(view.getInputElmt(), link)){
	            return (relation != null) ? relation.getName() : Messages.RelationViewLabelProvider_2;
	        } else {
	            return (relation != null) ? relation.getReversename() : Messages.RelationViewLabelProvider_3;
	        }
	    case 2:
	        return ""; // image only //$NON-NLS-1$
	    case 3:
	        replaceLinkEntities(link);
	        return CnALink.getRelationObjectTitle(view.getInputElmt(), link);      
	    case 4:
	        String title  = "";
            try {
                CnATreeElement target = link.getDependency();
                if(target.equals(view.getInputElmt())) {
                    target = link.getDependant();
                }
                if(!titleMap.containsKey(target.getScopeId())){
                    title = loadElementsTitles(target);
                } else {
                    title = titleMap.get(target.getScopeId());
                }
            } catch (CommandException e) {
                LOG.error("Error while getting element properties", e);
            }
            return title; //ScopeTitle from element dependencies         
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

    public void replaceLinkEntities(CnALink link) {
        CnATreeElement dependantWithProperties = Retriever.checkRetrieveElement(link.getDependant());
        CnATreeElement dependencyWithProperties = Retriever.checkRetrieveElement(link.getDependency());        
        link.getDependant().setEntity(dependantWithProperties.getEntity());
        link.getDependency().setEntity(dependencyWithProperties.getEntity());
    }

	@Override
    public Image getColumnImage(Object obj, int index) {
		if (obj instanceof PlaceHolder){
			return null;
		}
		CnALink link = (CnALink) obj;
		switch (index) {
		case 0:
			if (CnALink.isDownwardLink(view.getInputElmt(), link)){
				return ImageCache.getInstance().getImage(ImageCache.LINK_DOWN);
			} else {
				return ImageCache.getInstance().getImage(ImageCache.LINK_UP);
			}
		case 2:
			if (CnALink.isDownwardLink(view.getInputElmt(), link)){
				return getObjTypeImage(link.getDependency());
			} else { 
				return getObjTypeImage(link.getDependant());
			}
		default:
			return null;
		}

	}

	private Image getObjTypeImage(CnATreeElement elmt) {
	    Image image = CnAImageProvider.getCustomImage(elmt);
        if(image!=null) {
            return image;
        }
	    
		String typeId = elmt.getTypeId();
		
	    if (typeId.equals(Control.TYPE_ID) || typeId.equals(SamtTopic.TYPE_ID) ) {
	        String impl = Control.getImplementation(elmt.getEntity());
	        return ImageCache.getInstance().getControlImplementationImage(impl);
	    }if (elmt instanceof Group && !(elmt instanceof ImportIsoGroup)) {
			Group group = (Group) elmt;
			// TODO - getChildTypes()[0] might be a problem for more than one type
			typeId = group.getChildTypes()[0];
	    }
		return ImageCache.getInstance().getObjectTypeImage(typeId);
	}
	
	private static String loadElementsTitles(CnATreeElement elmt ) throws CommandException {
	    LoadElementTitles  scopeCommand;
	    scopeCommand = new LoadElementTitles();
	    scopeCommand = ServiceFactory.lookupCommandService().executeCommand(scopeCommand);
	    titleMap = scopeCommand.getElements();
	    return titleMap.get(elmt.getScopeId());
	}	
	
	public CnATreeElement getInputElemt()
	{
	    return view.getInputElmt();
	}
	
}