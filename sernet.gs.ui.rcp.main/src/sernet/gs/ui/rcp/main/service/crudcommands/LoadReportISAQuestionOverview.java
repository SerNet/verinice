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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportISAQuestionOverview extends GenericCommand implements ICachedCommand {
    
    private static final Logger LOG = Logger.getLogger(LoadReportISAQuestionOverview.class);
    private static final String PROP_REL_SAMTTOPIC_PERSONISO_RESP = "rel_samttopic_person-iso_resp";
    private static final String PROP_SAMT_MATURITY = "samt_topic_maturity";
    private static final String DUMMY_VALUE = "value indeterminable";
    private static final String PROP_SAMT_RISK = "samt_topic_audit_ra";
    private static final String OVERVIEW_PROPERTY = "controlgroup_is_NoIso_group";
    private static final int OVERVIEW_PROPERTY_TARGET = 0;
    
    private boolean resultInjectedFromCache = false;

    
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
        if(!resultInjectedFromCache){
            result = new ArrayList<List<String>>(0);
            try {
                for(ControlGroup cg : getControlGroups(rootElmt)){
                    LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, cg.getDbId(), true);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    for(CnATreeElement c : command.getElements()){
                        if(c instanceof SamtTopic){
                            ArrayList<String> list = new ArrayList<String>(0);
                            SamtTopic t = (SamtTopic)c;
                            String[] splittedTitle = splitTopicTitle(t.getTitle());
                            String maturity = String.valueOf(Integer.parseInt(t.getEntity().getValue(SamtTopic.PROP_MATURITY)));
                            String riskValue = String.valueOf(Integer.parseInt(t.getEntity().getValue(PROP_SAMT_RISK)));
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
        
    }
    
    private String[] splitTopicTitle(String title){
        String patternString = ".*(\\d+)\\.?(\\d+)?";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(title);
        String[] result = new String[2];
        if(matcher.find()){
            result[0] = matcher.group();
            result[1] = title.substring(title.indexOf(result[0]) + result[0].length()).trim();
        } else {
            result[0] = "";
            result[1] = title;
        }
        return result;
    }
    
    public List<List<String>> getResult(){
        return result;
    }
    
    private List<ControlGroup> getControlGroups(Integer root){
        ArrayList<ControlGroup> retList = new ArrayList<ControlGroup>(0);
        Set<ControlGroup> alreadySeen = new HashSet<ControlGroup>(0);
        try {
            LoadReportElements command = new LoadReportElements(ControlGroup.TYPE_ID, root, true);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            List<CnATreeElement> groups = command.getElements();
            if(groups.size() == 1 && groups.get(0).getDbId().equals(root)){
                ControlGroup rootGroup = (ControlGroup)groups.get(0);
                groups.clear();
                groups.addAll(command.getElements(ControlGroup.TYPE_ID, rootGroup));
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
        return this.result;
    }

}
