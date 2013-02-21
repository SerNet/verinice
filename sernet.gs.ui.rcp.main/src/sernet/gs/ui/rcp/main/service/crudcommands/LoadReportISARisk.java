/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportISARisk extends GenericCommand implements ICachedCommand{
    
    private static final Logger LOG = Logger.getLogger(LoadReportISARisk.class);

    private static final String PROP_CG_ISISAELMNT = "controlgroup_is_NoIso_group";
    
    private static final String PROP_ISATOPIC_RISK = "samt_topic_audit_ra";
    
    private boolean resultInjectedFromCache = false;
    
    public static final String[] COLUMNS = new String[] { 
        "COUNT_RISK_NO",
        "COUNT_RISK_LOW",
        "COUNT_RISK_MEDIUM",
        "COUNT_RISK_HIGH",
        "COUNT_RISK_VERYHIGH",
        "RISK_SUM",
        "PERCENTAGE_NO",
        "PERCENTAGE_LOW",
        "PERCENTAGE_MEDIUM",
        "PERCENTAGE_HIGH",
        "PERCENTAGE_VERYHIGH",
        "PERCENTAGE_SUM"
        };
    private Integer rootElmt;
    
    private int risk0 = 0;
    private int risk1 = 0;
    private int risk2 = 0;
    private int risk3 = 0;
    private int risk4 = 0;
    private int riskSum = 0;
    private double p0 = 0.0;
    private double p1 = 0.0;
    private double p2 = 0.0;
    private double p3 = 0.0;
    private double p4 = 0.0;
    private double pSum = 0.0;
    
    private List<List<String>> result;
    
    public LoadReportISARisk(Integer root){
        this.rootElmt = root;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            LoadReportElements command = new LoadReportElements(ControlGroup.TYPE_ID, rootElmt, true);
            try {
                command = getCommandService().executeCommand(command);
            } catch (CommandException e) {
                LOG.error("Error while loading controlgroups", e);
            }

            Set<ControlGroup> groups = new HashSet<ControlGroup>();
            for(CnATreeElement elmt : command.getElements()){
                if(elmt instanceof ControlGroup){
                    ControlGroup g = (ControlGroup)elmt;
                    if(!Boolean.parseBoolean(g.getEntity().getSimpleValue(PROP_CG_ISISAELMNT))){
                        groups.add(g);
                    }
                }
            }
            // ensuring that every samtTopic is counted only one
            // because of (sub-)root-Controlgroups are enlisted here also, SamtTopics could be iterated more than once here
            HashSet<String> alreadySeenCG = new HashSet<String>();
            HashSet<String> alreadySeenST = new HashSet<String>();
            for(ControlGroup g : groups){
                if(!alreadySeenCG.contains(g.getUuid())){
                    command = new LoadReportElements(SamtTopic.TYPE_ID, g.getDbId(), true);
                    try {
                        command = getCommandService().executeCommand(command);
                        for(CnATreeElement c : command.getElements()){
                            if(c instanceof SamtTopic){
                                SamtTopic t  = (SamtTopic)c;
                                if(!alreadySeenST.contains(t.getUuid())){
                                    int riskValue = Integer.parseInt(t.getEntity().getSimpleValue(PROP_ISATOPIC_RISK));
                                    switch(riskValue){
                                    case -1: risk0++; riskSum++;
                                    break;

                                    case 0: risk1++; riskSum++;
                                    break;

                                    case 1: risk2++; riskSum++;
                                    break;

                                    case 2: risk3++; riskSum++;
                                    break;

                                    case 3: risk4++; riskSum++;
                                    break;

                                    default:
                                        break;
                                    }
                                    alreadySeenST.add(t.getUuid());
                                }
                            }
                        }
                    } catch (CommandException e) {
                        LOG.error("Error while loading samtTopics", e);
                    }
                }
                alreadySeenCG.add(g.getUuid());
            }
            if(riskSum > 0){
                p0 = computePercentage(risk0);
                p1 = computePercentage(risk1);
                p2 = computePercentage(risk2);
                p3 = computePercentage(risk3);
                p4 = computePercentage(risk4);
                pSum = p0 + p1 + p2 + p3 + p4;
            }
            List<String> asList = Arrays.asList(Integer.toString(risk0), 
                    Integer.toString(risk1), 
                    Integer.toString(risk2),
                    Integer.toString(risk3),
                    Integer.toString(risk4),
                    Integer.toString(riskSum),
                    adjustPercentageString(p0),
                    adjustPercentageString(p1),
                    adjustPercentageString(p2),
                    adjustPercentageString(p3),
                    adjustPercentageString(p4),
                    adjustPercentageString(pSum));
            ArrayList<String> aList = new ArrayList<String>();
            aList.addAll(asList);
            result = new ArrayList<List<String>>();
            result.add(aList);
        }
    }
    
    private double computePercentage(Integer riskValue){
        final double maxPercent = 100.00;
        double d = (double)riskValue.intValue() / (double)riskSum;
        return d * maxPercent;
    }
    
    private String adjustPercentageString(Double value){
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(value.doubleValue()) + "%";
    }
    
    public List<List<String>> getResult(){
        result.remove(null);
        return result;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.result = (ArrayList<List<String>>)result;
        resultInjectedFromCache = true;
        if(LOG.isDebugEnabled()){
            LOG.debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return result;
    }
}
