/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.service;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;

/**
 * Returns the achieved maturity levels compared to the maximum maturity level. Both values are weighted.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class TotalSecurityFigureCommand extends GenericCommand  {

    private transient Logger log = Logger.getLogger(TotalSecurityFigureCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(TotalSecurityFigureCommand.class);
        }
        return log;
    }
    
    private Integer auditDbId = null;
    private Double totalSecurityFigure;
    

    public TotalSecurityFigureCommand(Integer   samtGroup)
    {
        this.auditDbId = samtGroup;
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            FindSamtGroup command = new sernet.verinice.samt.service.FindSamtGroup(true, auditDbId);
            command = getCommandService().executeCommand(command);
            ControlGroup controlGroup = command.getSelfAssessmentGroup();
            Integer weightedMaturity = getWeightedMaturity(controlGroup);
            Integer weightedThreshold = getWeightedThreshold(controlGroup);
            totalSecurityFigure = 1.0;
            if(weightedThreshold!=0) {
                totalSecurityFigure = (double) weightedMaturity / (double) weightedThreshold;
            }
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
    }
    
    /**
     * As defined by customer: never return more than 100%:
     * 
     * @return the totalSecurityFigure
     */
    public Double getResult() {
        return totalSecurityFigure.doubleValue() > 1.0 ? new Double(1.0) : totalSecurityFigure;
    }

    public Integer getWeightedMaturity(ControlGroup cg) {
        int maturity = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl) child;
                maturity += getWeightedMaturity(control);
            }
            if (child instanceof ControlGroup) {
                ControlGroup control = (ControlGroup) child;
                maturity += getWeightedMaturity(control);
            }
        }
        return maturity;
    }
    
    public Integer getWeightedMaturity(IControl contr) {
        int value = 0;
        int maturity = contr.getMaturity();
        // maturity less than 0 is counted as 0
        if(IControl.IMPLEMENTED_NA_NUMERIC!=maturity
           && IControl.IMPLEMENTED_NOTEDITED_NUMERIC!=maturity) {
            value = contr.getMaturity() * contr.getWeight2();
            // value must never be more than weighted threshold
            Integer weightedThreshold = getWeightedThreshold(contr);
            if (value > weightedThreshold){
                value = weightedThreshold;
            }
        }
        return value;
    }
    
    public Integer getWeightedThreshold(ControlGroup cg) {
        int maturity = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl) child;
                maturity += getWeightedThreshold(control);
            }
            if (child instanceof ControlGroup) {
                ControlGroup control = (ControlGroup) child;
                maturity += getWeightedThreshold(control);
            }
        }
        return maturity;
    }
    
    public Integer getWeightedThreshold(IControl contr) {
        int value = 0;
        int maturity = contr.getMaturity();
        // if maturity is "not applicable" we don't count the threshold 
        if(IControl.IMPLEMENTED_NA_NUMERIC!=maturity) {
            value = contr.getThreshold2() * contr.getWeight2();
        }
        return value;
    }

}
