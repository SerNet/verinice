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

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByEntityId;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
//import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.IControl;
import sernet.verinice.iso27k.service.ControlMaturityService;

@SuppressWarnings("serial")
public class MaturitySummary extends GenericCommand {

    public static final int TYPE_IMPLEMENTATION = 0;
    public static final int TYPE_THRESHOLD1 = 1;
    public static final int TYPE_THRESHOLD2 = 2;
    public static final int TYPE_MAX = 3;
    
    Map<String, Double> maturity = new HashMap<String, Double>();
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
        if (! entityType.equals(ControlGroup.TYPE_ID))
            return;
        
        try {
            LoadCnAElementByEntityId command = new LoadCnAElementByEntityId(dbId);
            command = getCommandService().executeCommand(command);
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
            if (group instanceof ControlGroup)
                getSummary((ControlGroup)group);
        }
    }

    /**
     * @param group
     */
    private void getSummary(ControlGroup group) {
        ControlMaturityService maturityService = new ControlMaturityService();
        if (type == TYPE_MAX)
            maturity.put(group.getTitle(), maturityService.getMaxMaturityValue(group));
        else if (type == TYPE_IMPLEMENTATION)
            maturity.put(group.getTitle(), maturityService.getMaturityByWeight(group));
        else if (type == TYPE_THRESHOLD1)
            maturity.put(group.getTitle(), getThreshold(group, TYPE_THRESHOLD1));
        else if (type == TYPE_THRESHOLD2)
            maturity.put(group.getTitle(), getThreshold(group, TYPE_THRESHOLD2));
    }

    /**
     * @param controlGroup
     * @param type_threshold22 
     * @return
     */
    private Double getThreshold(ControlGroup controlGroup, int typeThreshold) {
        Set<CnATreeElement> children = controlGroup.getChildren();
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            CnATreeElement cnATreeElement = (CnATreeElement) iterator.next();
            if (cnATreeElement instanceof IControl) {
                IControl control = (IControl) cnATreeElement;
                if (typeThreshold == TYPE_THRESHOLD1)
                    return (double)control.getThreshold1();
                else 
                    return (double)control.getThreshold2();
            } else if (cnATreeElement instanceof ControlGroup) {
               return getThreshold((ControlGroup) cnATreeElement, typeThreshold);
            }
        }
        return (double)0;
    }

    /**
     * @return
     */
    private Double getMaxMaturityValue() {
        HUITypeFactory hui = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
        PropertyType propertyType = hui.getPropertyType(Control.TYPE_ID, Control.PROP_MATURITY);
        return Double.valueOf(propertyType.getMaxValue());
    }

    /**
     * @return
     */
    public Map<String, Double> getSummary() {
        return maturity;
    }
	

}
