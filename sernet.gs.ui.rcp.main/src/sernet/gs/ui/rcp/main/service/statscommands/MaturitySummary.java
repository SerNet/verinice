/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - use custom SQL query
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.statscommands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByEntityId;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;

@SuppressWarnings("serial")
public class MaturitySummary extends GenericCommand {

    public static final int TYPE_IMPLEMENTATION = 0;
    public static final int TYPE_THRESHOLD1 = 1;
    public static final int TYPE_THRESHOLD2 = 2;
    public static final int TYPE_MAX = 3;

    public static final int ISR_TYPE_IMPLEMENTATION = 4;
    public static final int ISR_TYPE_MAX = 7;
    
    protected Map<String, Double> maturity = new HashMap<String, Double>();
    private Integer dbId;
    private String entityType;
    private int type;
    

    /**
     * @param entityType
     * @param dbId
     * @param maxValues 
     */
    public MaturitySummary(String entityType, Integer dbId, int type) {
        this.entityType = entityType;
        this.dbId = dbId;
        this.type = type;
    }

    public void execute() {
        if (! entityType.equals(ControlGroup.TYPE_ID)){
            return;
        }
        try {
            LoadCnAElementByEntityId command = new LoadCnAElementByEntityId(dbId);
            command = getCommandService().executeCommand(command);
            if (command.getElements().size()==0) {
                return;
            }
            CnATreeElement cnATreeElement = command.getElements().get(0);
            getItems((ControlGroup)cnATreeElement);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
        
	}

    /**
     * @param topGroup
     */
    private void getItems(ControlGroup topGroup) {
        Set<CnATreeElement> groups = topGroup.getChildren();
        for (CnATreeElement group : groups) {
            if (group instanceof ControlGroup){
                getSummary((ControlGroup)group);
            }
        }
    }

    /**
     * @param group
     */
    private void getSummary(ControlGroup group) {
        ControlMaturityService maturityService = new ControlMaturityService(ControlMaturityService.TYPE_MATURITY);
        ControlMaturityService isrService = new ControlMaturityService(ControlMaturityService.TYPE_ISR);
        
        if (type == TYPE_MAX){
            maturity.put(group.getTitle(), maturityService.getMaxMaturityValue(group));
        } else if (type == TYPE_IMPLEMENTATION) {
            maturity.put(group.getTitle(), maturityService.getMaturityByWeight(group));
        } else if (type == TYPE_THRESHOLD1) {
            maturity.put(group.getTitle(), getThreshold(group, TYPE_THRESHOLD1));
        } else if (type == TYPE_THRESHOLD2) {
            maturity.put(group.getTitle(), getThreshold(group, TYPE_THRESHOLD2));
        } else if (type == ISR_TYPE_MAX) { 
            maturity.put(group.getTitle(), isrService.getMaxMaturityValue(group));
        } else if (type == ISR_TYPE_IMPLEMENTATION) { 
            maturity.put(group.getTitle(), isrService.getMaturityByWeight(group));
        }
    }

    
    /**
     * Gibt den maximalen Schwellenwert (threshold)
     * 
     * @param controlGroup
     * @param typeThreshold
     * @return
     */
    private Double getThreshold(ControlGroup controlGroup, int typeThreshold) {
        Double result = Double.valueOf(0);
        Set<CnATreeElement> children = controlGroup.getChildren();
        for (Iterator<CnATreeElement> iterator = children.iterator(); iterator.hasNext();) {
            CnATreeElement cnATreeElement = iterator.next();
            if (cnATreeElement instanceof IControl) {
                IControl control = (IControl) cnATreeElement;
                switch (typeThreshold) {
                case TYPE_THRESHOLD1:
                    if (control.getThreshold1()>result) {
                        result = (double)control.getThreshold1(); 
                    }
                    break;
                case TYPE_THRESHOLD2:
                    if (control.getThreshold2()>result) {
                        result = (double)control.getThreshold2(); 
                    }
                    break;
                default:
                    break;
                }
            } else if (cnATreeElement instanceof ControlGroup) {
                Double recursiveResult = getThreshold((ControlGroup) cnATreeElement, typeThreshold);
                if(recursiveResult>result) {
                    result = recursiveResult;
                }
            }
        }
        return result;
    }

    /**
     * @return
     */
    public Map<String, Double> getSummary() {
        return maturity;
    }
	

}
