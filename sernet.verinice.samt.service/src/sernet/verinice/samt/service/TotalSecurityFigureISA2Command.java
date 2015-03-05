/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
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
package sernet.verinice.samt.service;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;

/**
 *
 */
public class TotalSecurityFigureISA2Command extends GenericCommand implements ICachedCommand {
    
    private final static Logger LOG = Logger.getLogger(TotalSecurityFigureISA2Command.class);

    private boolean resultInjectedFromCache = false;
    
    /**
     * 
     */
    private static final long serialVersionUID = 201502111531L;
    
    private Integer auditDbId = null;
    private Double totalSecurityFigure;
    private int controlCount = 0;

    /**
     * @param samtGroup
     */
    public TotalSecurityFigureISA2Command(Integer samtGroup) {
        this.auditDbId = samtGroup;
    }
    
    @Override
    public void execute() {
        try {
            if(!resultInjectedFromCache){
                FindSamtGroup command = new sernet.verinice.samt.service.FindSamtGroup(true, auditDbId);
                command = getCommandService().executeCommand(command);
                ControlGroup controlGroup = command.getSelfAssessmentGroup();
                int matSum = getMaturitySum(controlGroup);
                if(controlCount != 0){
                    totalSecurityFigure = (double) matSum / (double) controlCount;
                } else {
                    totalSecurityFigure = Double.valueOf(0.0);
                }
            }

        }catch (CommandException e){
            throw new RuntimeCommandException(e);
        }
        
    }
    
    public int getMaturitySum(ControlGroup controlgroup){
        int maturitySum = 0;
        for (CnATreeElement child : controlgroup.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl) child;
                int maturity = control.getMaturity();
                // if maturity is not edited yet, add 0 (do nothing but count the control for average value)
                if(maturity > 0){
                    maturitySum += maturity;
                }
                if(IControl.IMPLEMENTED_NA_NUMERIC != maturity){
                    controlCount += 1;
                }
                if(IControl.IMPLEMENTED_NOTEDITED_NUMERIC!=maturity){
                    if(LOG.isDebugEnabled()){
                        LOG.debug("Adding maturity " + String.valueOf(maturity) + "from Control:\t" + control.getTitle());
                    }
                }
            }
            if (child instanceof ControlGroup) {
                ControlGroup cg = (ControlGroup) child;
                maturitySum += getMaturitySum(cg);
            }
        }
        return maturitySum;
    }
    
    public Double getResult() {
        return totalSecurityFigure.doubleValue();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(auditDbId));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.totalSecurityFigure = (Double)result;
        this.resultInjectedFromCache = true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return totalSecurityFigure.doubleValue();
    }

}
