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
package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.crudcommands.LoadReportElements;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.report.service.impl.TocHelper2;

/**
 *
 */
public class LoadISAQuestionRadarChartData extends GenericCommand implements ICachedCommand{
    
    private static transient Logger log = Logger.getLogger(LoadISAQuestionRadarChartData.class);
    private static final int THRESHOLD_VALUE = 3;
    
    public static final String[] COLUMNS = new String[]{"title", 
                                                  "riskValue",
                                                  "threshold"
                                                 };
    
    private static final int MINIMUM_CHART_ENTRIES = 7;

    private Integer rootElmnt;
    
    private List<List<String>> result;
    
    private boolean resultInjectedFromCache = false;
    
    public LoadISAQuestionRadarChartData(Integer root){
        this.rootElmnt = root;
        result = new ArrayList<List<String>>(0);
    }
    
    public LoadISAQuestionRadarChartData(String root){
        this(Integer.valueOf(Integer.parseInt(root)));
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            try {
                LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, rootElmnt, true);
                command = getCommandService().executeCommand(command);
                List<CnATreeElement> elements = command.getElements();
                for(CnATreeElement e : elements){
                    if(e instanceof SamtTopic){
                        SamtTopic topic = (SamtTopic)e;
                        ArrayList<String> list = new ArrayList<String>(0);
                        list.add(adjustTitle(topic.getTitle()));
                        list.add(String.valueOf(getMaturityByWeight(topic)));
                        list.add(String.valueOf(THRESHOLD_VALUE));
                        list.trimToSize();
                        result.add(list);
                    }
                }
            } catch (CommandException e) {
                getLog().error("Error while determing samt topics", e);
            }
        } 
    }
    
    public List<List<String>> getResult(){
        if(result.size() < MINIMUM_CHART_ENTRIES){
            addPaddingValues();
        }
        return result;
    }
    
    public void addPaddingValues(){
        while(result.size() < MINIMUM_CHART_ENTRIES){
            ArrayList<String> paddingEntry = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < result.size(); i++){
                sb.append(" ");
            }
            paddingEntry.add(sb.toString());
            paddingEntry.add(String.valueOf(0));
            paddingEntry.add(String.valueOf(THRESHOLD_VALUE));
            result.add(paddingEntry);
        }
    }
    
    public Integer getWeights(ControlGroup cg) {
        int weight = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl) child;
                weight += control.getWeight2();
            }
            if (child instanceof ControlGroup) {
                ControlGroup control = (ControlGroup) child;
                weight += getWeights(control);
            }
        }
        return weight;
    }
    
    /**
     * @return
     */
    public Integer getWeightedMaturity(IControl contr) {
        return getMaturity(contr) * contr.getWeight2();
    }
    
    public Double getMaturityByWeight(IControl contr) {
        return ((double)getWeightedMaturity(contr)) / ((double)contr.getWeight2());
    }
    
    public int getMaturity(IControl control) {
        return control.getMaturity();
    }
    
    private String adjustTitle(String title){
        final int maxTitleSize = 50;
        final int halfMaxTitleSize = 25;
        if(TocHelper2.getStringDisplaySize(title) > maxTitleSize){
            StringBuilder sb = new StringBuilder();
            StringTokenizer tokenizer = new StringTokenizer(title); // space is one of the standard delimiters
            while(tokenizer.hasMoreElements()){
                sb.append(tokenizer.nextToken());
                if(TocHelper2.getStringDisplaySize(title) > halfMaxTitleSize){
                    sb.append("\n");
                } else {
                    sb.append(" ");
                }
               
            }
            
        }
        
        return title;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmnt));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.result = (ArrayList<List<String>>)result;
        resultInjectedFromCache = true;
        if(getLog().isDebugEnabled()){
            getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadISAQuestionRadarChartData.class);
        }
        return log;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return result;
    }
    
}
