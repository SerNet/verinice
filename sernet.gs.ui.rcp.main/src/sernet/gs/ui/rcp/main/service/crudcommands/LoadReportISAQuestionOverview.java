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
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportISAQuestionOverview extends GenericCommand {
    
    private static final Logger LOG = Logger.getLogger(LoadReportISAQuestionOverview.class);
    private static final String PROP_REL_SAMTTOPIC_PERSONISO_RESP = "rel_samttopic_person-iso_resp";
    private static final String PROP_SAMT_MATURITY = "samt_topic_maturity";
    private static final String DUMMY_VALUE = "value indeterminable";
    private static final String PROP_SAMT_RISK = "samt_topic_audit_ra";
    private static final String OVERVIEW_PROPERTY = "controlgroup_is_NoIso_group";
    private static final int OVERVIEW_PROPERTY_TARGET = 0;
    
    public static final String[] COLUMNS = new String[] { 
        "TITLE",
        "DESCRIPTION",
        "MATURITY",
        "RISK",
        "RESPONSIBLE_PERSON",
        "DUEDATE"
        };
    private Integer rootElmt;
    
    private List<List<String>> result;
    
    public LoadReportISAQuestionOverview(Integer root){
        this.rootElmt = root;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        result = new ArrayList<List<String>>(0);
        try {
            for(ControlGroup cg : getControlGroups(rootElmt)){
                LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, cg.getDbId());
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                for(CnATreeElement c : command.getElements()){
                    if(c instanceof SamtTopic){
                        ArrayList<String> list = new ArrayList<String>(0);
                        SamtTopic t = (SamtTopic)c;
                        String[] splittedTitle = splitTopicTitle(t.getTitle());
                        PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(SamtTopic.TYPE_ID, SamtTopic.PROP_MATURITY);
                        String maturity = type.getNameForValue(Integer.parseInt(t.getEntity().getValue(SamtTopic.PROP_MATURITY)));
                        type = HitroUtil.getInstance().getTypeFactory().getPropertyType(SamtTopic.TYPE_ID, SamtTopic.PROP_MATURITY);
                        String riskValue = type.getNameForValue(Integer.parseInt(t.getEntity().getValue(PROP_SAMT_RISK)));
                        StringBuilder sb = new StringBuilder();
                        for(Entry<CnATreeElement, CnALink> entry : CnALink.getLinkedElements(t, PersonIso.TYPE_ID).entrySet()){
                            if(CnALink.isDownwardLink(t, entry.getValue()) && entry.getValue().getRelationId().equals(PROP_REL_SAMTTOPIC_PERSONISO_RESP)){
                                PersonIso e = (PersonIso)getDaoFactory().getDAO(PersonIso.TYPE_ID).initializeAndUnproxy(entry.getKey());
                                sb.append(e.getSurname());
                                sb.append(", ");
                                sb.append(e.getName());
                                sb.append("\n"); // newline, to enlist more than one person
                            }
                        }
                        String persons = sb.toString();
                        String dueDate = t.getEntity().getSimpleValue(SamtTopic.PROP_COMPLETE_UNTIL);
                        if(dueDate == null){
                            dueDate = DUMMY_VALUE;
                        }
                        if(persons == null ){
                            persons = DUMMY_VALUE;
                        }
                        if(riskValue == null){
                            riskValue = DUMMY_VALUE;
                        }
                        if(maturity == null){
                            maturity = DUMMY_VALUE;
                        }
                        list.add(splittedTitle[0]);
                        list.add(splittedTitle[1]);
                        list.add(maturity);
                        list.add(riskValue);
                        list.add(persons);
                        list.add(dueDate);
                        
                        result.add(list);
                    }
                }

            }
        } catch (CommandException e) {
            LOG.error("Error while computing details for SamtTopic", e);
        }
        
    }
    
    private String[] splitTopicTitle(String title){
        String[] result = new String[2];
        if(title.contains(" ")){
            String partOne = title.substring(0, title.indexOf(" "));
            String partTwo = title.substring(title.indexOf(" "));
            if(!Float.isNaN(Float.parseFloat(partOne))){
                result[0] = partOne;
                result[1] = partTwo;
            }
        }
        if(result[0] == null){
            result[0] = DUMMY_VALUE;
            result[1] = result[0];
        }
        return result;
    }
    
    /**
     * just for generating test data
     * @param max
     * @return
     */
    private int getRandomInt(int max){
        Random random = new Random();
        return random.nextInt(max); 
    }
    
    /**
     * just for generating test data
     * @param length
     * @return
     */
    private String getRandomString(int length){
        return UUID.randomUUID().toString().substring(0, length);
    }
    
    public List<List<String>> getResult(){
        return result;
    }
    
//    private List<ControlGroup> getControlGroups(Integer root){
//        LoadISAR command = new LoadReportISARiskChapter(root);
//        command = ServiceFactory.lookupCommandService().executeCommand(command);
//        
//        ArrayList<ControlGroup> retList = new ArrayList<ControlGroup>(0);
//        try {
//            LoadReportElements command = new LoadReportElements(ControlGroup.TYPE_ID, root);
//            command = ServiceFactory.lookupCommandService().executeCommand(command);
//            List<CnATreeElement> groups = command.getElements();
//            if(groups.size() == 1 && groups.get(0).getDbId().equals(root)){
//                groups.clear();
//                command.getElements(ControlGroup.TYPE_ID, groups, groups.get(0));
//            }
//            for(CnATreeElement e : groups){
//                if(e instanceof ControlGroup){
//                    ControlGroup c = (ControlGroup)e;
//                    if(e.getParent() instanceof ControlGroup &&
//                            c.getEntity().getSimpleValue(PROP_CONTROLGROUP_ISELEMENT)
//                            .equals(VALUE_CONTROLGROUP_ISELEMENT)){ // avoids rootControlGroup
//                        retList.add((ControlGroup)e);
//                    }
//                }
//            }
//        } catch (CommandException e) {
//            LOG.error("Error while determing controlgroups");
//        }
//        retList.trimToSize();
//        Collections.sort(retList, new Comparator<ControlGroup>() {
//
//            @Override
//            public int compare(ControlGroup o1, ControlGroup o2) {
//                NumericStringComparator comp = new NumericStringComparator();
//                return comp.compare(o1.getTitle(), o2.getTitle());
//            }
//        });
//        return retList;
//    }
    
    private List<ControlGroup> getControlGroups(Integer root){
        ArrayList<ControlGroup> retList = new ArrayList<ControlGroup>(0);
        Set<ControlGroup> alreadySeen = new HashSet<ControlGroup>(0);
        try {
            LoadReportElements command = new LoadReportElements(ControlGroup.TYPE_ID, root);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            List<CnATreeElement> groups = command.getElements();
            if(groups.size() == 1 && groups.get(0).getDbId().equals(root)){
                ControlGroup rootGroup = (ControlGroup)groups.get(0);
                groups.clear();
                command.getElements(ControlGroup.TYPE_ID, groups, rootGroup);
            }
            for(CnATreeElement e : groups){
                if(e instanceof ControlGroup){
                    ControlGroup c = (ControlGroup)e;
                    if(!alreadySeen.contains(c)){
                        alreadySeen.add(c);
                        if(e.getParent() instanceof ControlGroup &&
                                c.getEntity().getSimpleValue(OVERVIEW_PROPERTY)
                                .equals(String.valueOf(OVERVIEW_PROPERTY_TARGET))
                                && containsSamtTopicsOnly(c)){ // avoids rootControlGroup
                            retList.add(c);
                        }
                    }
                }
            }
        } catch (CommandException e) {
            LOG.error("Error while determing controlgroups");
        }
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
