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
    
    private int risk_0 = 0;
    private int risk_1 = 0;
    private int risk_2 = 0;
    private int risk_3 = 0;
    private int risk_4 = 0;
    private int risk_sum = 0;
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
                                    case -1: risk_0++; risk_sum++;
                                    break;

                                    case 0: risk_1++; risk_sum++;
                                    break;

                                    case 1: risk_2++; risk_sum++;
                                    break;

                                    case 2: risk_3++; risk_sum++;
                                    break;

                                    case 3: risk_4++; risk_sum++;
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
            if(risk_sum > 0){
                p0 = computePercentage(risk_0);
                p1 = computePercentage(risk_1);
                p2 = computePercentage(risk_2);
                p3 = computePercentage(risk_3);
                p4 = computePercentage(risk_4);
                pSum = new Double(p0) + new Double(p1) + new Double(p2) + new Double(p3) + new Double(p4);
            }
            List<String> asList = Arrays.asList(Integer.toString(risk_0), 
                    Integer.toString(risk_1), 
                    Integer.toString(risk_2),
                    Integer.toString(risk_3),
                    Integer.toString(risk_4),
                    Integer.toString(risk_sum),
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
        Double d = new Double(riskValue).doubleValue() / new Double(risk_sum).doubleValue();
        return d * 100.00;
    }
    
    private String adjustPercentageString(Double value){
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(value.doubleValue()) + "%";
    }
    
    private ArrayList<String> getArrayList(int input){
        ArrayList<String> list = new ArrayList<String>(0);
        list.add(String.valueOf(input));
        return list;
    }
    
    public List<List<String>> getResult(){
        result.remove(null);
        return result;
    }

    private CnATreeElement loadChildren(CnATreeElement el) {
        if (el.isChildrenLoaded()) {
            return el;
        } 
        LoadChildrenForExpansion command;
        command = new LoadChildrenForExpansion(el);
        try {
            command = getCommandService().executeCommand(command);
            CnATreeElement newElement = command.getElementWithChildren();
            newElement.setChildrenLoaded(true);
            return newElement;
        } catch (CommandException e) {
            LOG.error("error while loading children of CnaTreeElment", e);
        }
        return null;
    }
    
    /**
     * sets all result values to 0, to be ready for next iteration (controlgroup)
     */
    private void resetValues(){
        risk_0 = 0;
        risk_1 = 0;
        risk_2 = 0;
        risk_3 = 0;
        risk_4 = 0;
        risk_sum = 0;
        p0 = 0.0;
        p1 = 0.0;
        p2 = 0.0;
        p3 = 0.0;
        p4 = 0.0;
        pSum = 0.0;
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
