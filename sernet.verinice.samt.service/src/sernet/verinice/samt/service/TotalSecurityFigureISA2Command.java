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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.samt.SamtTopic;

/**
 * computes key figures for isa report templates
 * - samt-report-compliance.rptdesign
 * - ISA-ActionReport.rptdesign
 */

/**
 * Computes ISA 2.0 key figures (total security figure and Result with/-out cutback
 * to target maturity levels), by summing up (and
 * building the average of) maturity and targetmaturity (threshold2) of {@link SamtTopic}
 * children of a {@link ControlGroup}. root element should be an {@link Audit}.
 **/
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
    private Double targetMaturity;
    private Double averageTargetMaturity;

    /**
     * @param samtGroup
     */
    public TotalSecurityFigureISA2Command(Integer samtGroup) {
        this.auditDbId = samtGroup;
        targetMaturity = 0.0;
        averageTargetMaturity = 0.0;
        
    }
    
    @Override
    public void execute() {
        try {
            if(!resultInjectedFromCache){
                FindSamtGroup command = new FindSamtGroup(true, auditDbId);
                command = getCommandService().executeCommand(command);
                ControlGroup controlGroup = command.getSelfAssessmentGroup();
                int matSum = getMaturitySum(controlGroup);
                if(controlCount != 0){
                    totalSecurityFigure = (double) matSum / (double) controlCount;
                    targetMaturity = (double) targetMaturity / (double) controlCount;
                    averageTargetMaturity = (double) averageTargetMaturity / (double) controlCount;
                } else {
                    totalSecurityFigure = Double.valueOf(0.0);
                    targetMaturity = Double.valueOf(0.0);
                    averageTargetMaturity = Double.valueOf(0.0);
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
                if(maturity > -1 && getTargetMaturity(control) > 0){
                    maturitySum += maturity;
                    targetMaturity += Double.valueOf(String.valueOf(reduceToTargetMaturity(control)));
                    controlCount += 1;
                    averageTargetMaturity += getTargetMaturity(control);
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
    
    private int reduceToTargetMaturity(IControl control){
        int target = getTargetMaturity(control);
        if(control.getMaturity() > target){
            if(LOG.isDebugEnabled()){
                LOG.debug("Reducing " + control.getTitle()+ "(" + control.getMaturity() + ") to" + control.getThreshold2());
            }
            return target;
        }
        return control.getMaturity();
    }
    
    @Deprecated
    public Double getResult() {
        return getRoundedValue(totalSecurityFigure);
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
        if(result instanceof Object[]){
            Object[] arr = (Object[])result;
            this.totalSecurityFigure = (Double)arr[0];
            this.targetMaturity = (Double)arr[1];
            this.averageTargetMaturity = (Double)arr[2];
            this.resultInjectedFromCache = true;
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return new Object[]{this.totalSecurityFigure.doubleValue(), this.targetMaturity.doubleValue(), this.averageTargetMaturity.doubleValue()};
    }

    /**
     * @return the targetMaturity
     */
    @Deprecated
    public Double getTargetMaturity() {
        return getRoundedValue(targetMaturity);
    }
    
    @Deprecated
    public Double getAverageMaturity(){
        return getRoundedValue(averageTargetMaturity);
    }
    
    // target maturity is threshold2
    private int getTargetMaturity(IControl control){
        return control.getThreshold2();
    }
    
    private double getRoundedValue(double d){
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * returns a value used in ISA 2.0 called total Security Figure
     * ("Result without cutback to target maturity levels")
     * 
     * the totalSecurityFigure sums up the maturity of all instances of
     * {@link SamtTopic} which are children of a given {@link ControlGroup},
     * computed recursively for all children of type {@link ControlGroup} of the
     * root (given) {@link ControlGroup}. Once summed up, the value is divided
     * by the count of for computation considered {@link SamtTopic} (
     * {@link SamtTopic} with an maturity value of 0 or below are ignored )
     * 
     * to ensure downwards compatibility with old (custom) reports, the method
     * getResult() needs to be kept here despite it is marked deprecated
     **/
    public Double getTotalSecurityFigure() {
        return getResult();
    }

    /**
     * returns a value used in ISA 2.0 called Reduced Target Maturity 
     * ("Result with cutback to target maturity levels") 
     * 
     * the reducedTargetMaturity sums up maturity of all {@link SamtTopic} which are
     * children of a given {@link ControlGroup}, computed recursively for all
     * children of type {@link ControlGroup} of the root (given)
     * {@link ControlGroup}. For every {@link SamtTopic} contained in an ISA 2.0
     * {@link Audit} there is a property defined called target Maturity (for
     * historical reasons this is stored in the attribute SamtTopic.PROP_MIN2
     * (SamtTopic.getThreshold2()))
     * 
     * if the user defined maturity of the {@link SamtTopic} is greater as the
     * predefined target maturity the predefined value instead of the user
     * defined maturity gets summed up.
     * 
     * after summing up (reduced) maturity over all relevant ( maturity <= 0 are
     * ignored) {@link SamtTopic} (recursively), the sum is divided by count of
     * considered {@link SamtTopic}
     * 
     * to ensure downwards compatibility with old (custom) reports, the method
     * getTargetMaturity() needs to be kept here despite it is marked deprecated
     */
    public Double getReducedTargetMaturity() {
        return getTargetMaturity();
    }

    /**
     * returns a value used in ISA 2.0 called maximum Score 
     * this value represents the average of target maturity, 
     * computed over all considered {@link SamtTopic},
     * recursively on the given root {@link ControlGroup}
     *
     * to ensure downwards compatibility with old (custom) reports, the method
     * getAverageMaturity() needs to be kept here despite it is marked
     * deprecated
     */
    public Double getAverageMaximumAchieveableMaturity() {
        return getAverageMaturity();
    }


}
