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
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.samt.SamtTopic;

/**
 * takes a controlgroup as input, computes details to samtTopics contained in that group
 */
public class LoadReportIsaQuestionDetails extends GenericCommand {
    
    private static final Logger LOG = Logger.getLogger(LoadReportIsaQuestionDetails.class);
    private static final String PROP_SAMT_RISK = "samt_topic_audit_ra";
    private static final String PROP_CONTROLGROUP_ISELEMENT = "controlgroup_is_NoIso_group";
    private static final String VALUE_CONTROLGROUP_ISELEMENT = "0";
    private static final String REL_FINDING_CONTROL_ACTION = "rel_finding_control_action_name"; // vorgeschlagenes Control
    private static final String REL_FINDING_CONTROL_NC = "rel_finding_control_nc_name"; // versagen in
    
    public static final String[] COLUMNS = new String[] { 
        "TITLE",
        "FINDINGS",
        "MATURITY",
        "RISK",
        "DEDUCED_CONTROLS"
        };
    private Integer rootElmt;
    
    private List<List<String>> result;
    
    public LoadReportIsaQuestionDetails(Integer root){
        this.rootElmt = root;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            result = new ArrayList<List<String>>(0);
            ControlGroup cg = (ControlGroup)getDaoFactory().getDAO(ControlGroup.TYPE_ID).findById(rootElmt);
            LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, cg.getDbId());
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            for(CnATreeElement c : command.getElements()){
                ArrayList<String> list = new ArrayList<String>(0);
                if(SamtTopic.class.isInstance(c)){
                    SamtTopic st = (SamtTopic)c;
                    list.add(st.getTitle());
                    StringBuilder findingBuilder = new StringBuilder();
                    ArrayList<Finding> findingList = new ArrayList<Finding>(0);
                    for(Entry<CnATreeElement, CnALink> entry : CnALink.getLinkedElements(st, Finding.TYPE_ID).entrySet()){
                        if(entry.getValue().getRelationId().equals(REL_FINDING_CONTROL_NC)){
                            Finding f = (Finding)getDaoFactory().getDAO(Finding.TYPE_ID).initializeAndUnproxy(entry.getKey());
                            findingBuilder.append(f.getTitle());
                            findingBuilder.append("\n");
                            findingList.add(f);
                        }
                    }
                    list.add(findingBuilder.toString());
                    PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(SamtTopic.TYPE_ID, SamtTopic.PROP_MATURITY);
                    String maturity = type.getNameForValue(Integer.parseInt(st.getEntity().getValue(SamtTopic.PROP_MATURITY)));
                    list.add(maturity);
                    type = HitroUtil.getInstance().getTypeFactory().getPropertyType(SamtTopic.TYPE_ID, SamtTopic.PROP_MATURITY);
                    String riskValue = type.getNameForValue(Integer.parseInt(st.getEntity().getValue(PROP_SAMT_RISK)));
                    list.add(riskValue);
                    StringBuilder controlBuilder = new StringBuilder();
                    for(Finding f : findingList){
                        for(Entry<CnATreeElement, CnALink> entry : CnALink.getLinkedElements(f, Control.TYPE_ID).entrySet()){
                            if(CnALink.isDownwardLink(f, entry.getValue()) &&
                                    entry.getValue().getRelationId().equals(REL_FINDING_CONTROL_ACTION)){
                                Control control = (Control)getDaoFactory().getDAO(Control.TYPE_ID).initializeAndUnproxy(entry.getKey());
                                controlBuilder.append(control.getTitle());
                                controlBuilder.append("\n");
                            }
                        }
                    }
                    list.add(controlBuilder.toString());
                }
                result.add(list);
            }

        } catch (CommandException e) {
            LOG.error("");
        }
    }
    
    public List<List<String>> getResult(){
        return result;
    }
    
    private List<ControlGroup> getControlGroups(Integer root){
        ArrayList<ControlGroup> retList = new ArrayList<ControlGroup>(0);
        Set<ControlGroup> retSet = new HashSet<ControlGroup>();
        try {
            LoadReportElements command = new LoadReportElements(ControlGroup.TYPE_ID, root);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            List<CnATreeElement> groups = command.getElements();
            if(groups.size() == 1 && groups.get(0).getDbId().equals(root)){
                groups.clear();
                groups.addAll(command.getElements(ControlGroup.TYPE_ID, groups.get(0)));
            }
            for(CnATreeElement e : groups){
                if(e instanceof ControlGroup){
                    ControlGroup c = (ControlGroup)e;
                    if(e.getParent() instanceof ControlGroup &&
                            c.getEntity().getSimpleValue(PROP_CONTROLGROUP_ISELEMENT)
                            .equals(VALUE_CONTROLGROUP_ISELEMENT)){ // avoids rootControlGroup
                        retSet.add((ControlGroup)e);
                    }
                }
            }
        } catch (CommandException e) {
            LOG.error("Error while determing controlgroups");
        }
        retList.addAll(retSet);
        retList.trimToSize();
        Collections.sort(retList, new Comparator<ControlGroup>() {

            @Override
            public int compare(ControlGroup o1, ControlGroup o2) {
                NumericStringComparator comp = new NumericStringComparator();
                return comp.compare(o1.getTitle(), o2.getTitle());
            }
        });
        return retList;
    }

}
