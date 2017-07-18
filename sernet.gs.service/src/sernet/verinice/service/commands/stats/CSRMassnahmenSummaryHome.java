/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.stats;

import java.util.Map;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 *
 */
public class CSRMassnahmenSummaryHome extends MassnahmenSummaryHome {
    @Override
    public Map<String, Double> getControlMaxGroups(ControlGroup elmt) throws CommandException {
        CSRMaturitySummary command = new CSRMaturitySummary(elmt.getEntity().getEntityType(), elmt.getEntity().getDbId(), MaturitySummary.TYPE_MAX);
        command = getCommandService().executeCommand(command);
        return command.getSummary();
    }

    /**
     * @param elmt
     * @return
     * @throws CommandException 
     */
    @Override
    public Map<String, Double> getControlGoal1Groups(ControlGroup elmt) throws CommandException {
        CSRMaturitySummary command = new CSRMaturitySummary(elmt.getEntity().getEntityType(), elmt.getEntity().getDbId(), MaturitySummary.TYPE_THRESHOLD1);
        command = getCommandService().executeCommand(command);
        return command.getSummary();

    }
    
    /**
     * @param elmt
     * @return
     * @throws CommandException 
     */
    @Override
    public Map<String, Double> getControlGoal2Groups(ControlGroup elmt) throws CommandException {
        CSRMaturitySummary command = new CSRMaturitySummary(elmt.getEntity().getEntityType(), elmt.getEntity().getDbId(), MaturitySummary.TYPE_THRESHOLD2);
        command = getCommandService().executeCommand(command);
        return command.getSummary();
        
    }
    
    /**
     * @param elmt 
     * @param level 
     * @return
     * @throws CommandException 
     */
    @Override
    public Map<String, Double> getControlGroups(ControlGroup elmt) throws CommandException {
        CSRMaturitySummary command = new CSRMaturitySummary(elmt.getEntity().getEntityType(), elmt.getEntity().getDbId(), MaturitySummary.TYPE_IMPLEMENTATION);
        command = getCommandService().executeCommand(command);
        return command.getSummary();
    }
    
    public Map<String, Double> getControlGroupsWithoutWeight(ControlGroup elmt) throws CommandException{
        CSRMaturitySummary command = new CSRMaturitySummary(elmt.getEntity().getEntityType(), elmt.getEntity().getDbId(), MaturitySummary.TYPE_MAT_WITHOUT_WEIGHT);
        command = getCommandService().executeCommand(command);
        return command.getSummary();
    }
    
}
