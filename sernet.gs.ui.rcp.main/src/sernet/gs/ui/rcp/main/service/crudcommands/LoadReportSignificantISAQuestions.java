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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportSignificantISAQuestions extends GenericCommand {

    private static final Logger LOG = Logger.getLogger(LoadReportSignificantISAQuestions.class);

    private static final String PROP_CG_ISISAELMNT = "controlgroup_is_NoIso_group";
    
    private static final String PROP_ISATOPIC_RISK = "samt_topic_audit_ra";
    
    private static final int PROP_ISATOPIC_RISK_THRESHOLD = 2;
    
    public static final String[] COLUMNS = new String[] { 
        "TITLE",
        "RISK_DESCRIPTION",
        "CONTROL_DESCRIPTION"
        };
    
    private Integer rootElmt;

    public LoadReportSignificantISAQuestions(Integer root){
        this.rootElmt = root;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        for(SamtTopic topic : getSamtTopics(rootElmt)){
            ArrayList<String> result = new ArrayList<String>(0);
            result.add(topic.getTitle());
            // add risk
            // add control
        }
    }
    
    private List<SamtTopic> getSamtTopics(Integer root){
        ArrayList<ControlGroup> cgList = new ArrayList<ControlGroup>(0);
        ArrayList<SamtTopic> samtTopicList = new ArrayList<SamtTopic>(0);
        Set<ControlGroup> alreadySeen = new HashSet<ControlGroup>(0);
        try {
            LoadReportElements command = new LoadReportElements(ControlGroup.TYPE_ID, root);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            List<CnATreeElement> groups = command.getElements();
            if(groups.size() == 1 && groups.get(0).getDbId().equals(root)){
                groups.clear();
                groups.addAll(command.getElements(ControlGroup.TYPE_ID, groups.get(0)));
            }
            //get all relevant controlgroups
            for(CnATreeElement e : groups){
                if(e instanceof ControlGroup){
                    ControlGroup c = (ControlGroup)e;
                    if(!alreadySeen.contains(c)){
                        alreadySeen.add(c);
                        if(e.getParent() instanceof ControlGroup &&
                                c.getEntity().getSimpleValue(PROP_CG_ISISAELMNT)
                                .equals("0")
                                && containsSamtTopicsOnly(c)){// avoids rootControlGroup
                                
                            cgList.add(c);
                        }
                    }
                }
            }
            //get all relevant topics
            for(ControlGroup cg : cgList){
                LoadReportElements stLoader = new LoadReportElements(SamtTopic.TYPE_ID, cg.getDbId());
                stLoader = ServiceFactory.lookupCommandService().executeCommand(stLoader);
                for(CnATreeElement c : stLoader.getElements()){
                    SamtTopic st = (SamtTopic)c;
                    if(st.getNumericProperty(PROP_ISATOPIC_RISK) >= PROP_ISATOPIC_RISK_THRESHOLD){
                        samtTopicList.add(st);
                    }
                }
            }
        } catch (CommandException e) {
            LOG.error("Error while determing controlgroups");
        }
        samtTopicList.trimToSize();
        Collections.sort(samtTopicList, new Comparator<SamtTopic>() {

            @Override
            public int compare(SamtTopic o1, SamtTopic o2) {
                NumericStringComparator comp = new NumericStringComparator();
                return comp.compare(o1.getTitle(), o2.getTitle());
            }
        });
        return samtTopicList;
    }
    
    /**
     * if group has a child that is not a samttopic, return false (recursivly)
     * @param group
     * @return
     */
    private boolean containsSamtTopicsOnly(ControlGroup group){
        for(CnATreeElement child : group.getChildren()){
            if(!(child instanceof SamtTopic)){
                return false;
            } 
        }
        return true;
    }
    

}
