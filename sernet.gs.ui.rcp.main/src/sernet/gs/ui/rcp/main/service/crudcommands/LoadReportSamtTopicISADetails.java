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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.samt.SamtTopic;

/**
 *
 */
public class LoadReportSamtTopicISADetails extends GenericCommand {
    
    private static final Logger LOG = Logger.getLogger(LoadReportIsaQuestionDetails.class);
    private static final String PROP_SAMT_RISK = "samt_topic_audit_ra";
    private static final String REL_FINDING_CONTROL_ACTION = "rel_finding_control_action_name"; // vorgeschlagenes Control
    private static final String REL_FINDING_CONTROL_NC = "rel_finding_samt_topic_nc_name"; // versagen in
    private static final String REL_SAMT_TOPIC_PERSON_ISO = "rel_samttopic_person-iso_resp_name"; // linktype samttopic <-> person in charge
    private static final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
    
    public static final String[] COLUMNS = new String[] { 
        "TITLE",
        "FINDINGS",
        "MATURITY",
        "RISK",
        "DEDUCED_CONTROLS",
        "QUESTION_TEXT",
        "PERSON_IN_CHARGE",
        "DEADLINE", 
        "DBID"
        };
    private Integer rootElmt;
    
    private List<List<String>> result;
    
    public LoadReportSamtTopicISADetails(Integer root){
        this.rootElmt = root;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    public void execute() {
        try {
            result = new ArrayList<List<String>>(0);
            ControlGroup cg = (ControlGroup)getDaoFactory().getDAO(ControlGroup.TYPE_ID).findById(rootElmt);
            if(cg == null){
                RetrieveCnATreeElement command = new RetrieveCnATreeElement("cnatreeelement", rootElmt);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                CnATreeElement elmt = command.getElement();
                elmt.hashCode();
                
            }
            LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, cg.getDbId());
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            for(CnATreeElement c : command.getElements()){
                ArrayList<String> list = new ArrayList<String>(0);
                if(SamtTopic.class.isInstance(c)){
                    SamtTopic st = (SamtTopic)c;
                    list.add(st.getTitle()); // add title
                    StringBuilder findingBuilder = new StringBuilder();
                    ArrayList<Finding> findingList = new ArrayList<Finding>(0);
                    // compute linked findings
                    for(Entry<CnATreeElement, CnALink> entry : CnALink.getLinkedElements(st, Finding.TYPE_ID).entrySet()){
                        if(entry.getValue().getRelationId().equals(REL_FINDING_CONTROL_NC)){
                            Finding f = (Finding)getDaoFactory().getDAO(Finding.TYPE_ID).initializeAndUnproxy(entry.getKey());
                            findingBuilder.append(f.getTitle());
                            findingBuilder.append("\n");
                            findingList.add(f);
                        }
                    }
                    list.add(removeLastCR(findingBuilder.toString())); // add findings
                    PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(SamtTopic.TYPE_ID, SamtTopic.PROP_MATURITY);
                    String maturity = type.getNameForValue(Integer.parseInt(st.getEntity().getValue(SamtTopic.PROP_MATURITY)));
                    list.add(maturity); // add maturity
                    type = HitroUtil.getInstance().getTypeFactory().getPropertyType(SamtTopic.TYPE_ID, SamtTopic.PROP_MATURITY);
                    String riskValue = type.getNameForValue(Integer.parseInt(st.getEntity().getValue(PROP_SAMT_RISK)));
                    list.add(riskValue); // add risk
                    StringBuilder controlBuilder = new StringBuilder();
                    for(Finding f : findingList){
                        // compute deduced controls
                        for(Entry<CnATreeElement, CnALink> entry : CnALink.getLinkedElements(f, Control.TYPE_ID).entrySet()){
                            if(CnALink.isDownwardLink(f, entry.getValue()) &&
                                    entry.getValue().getRelationId().equals(REL_FINDING_CONTROL_ACTION)){
                                Control control = (Control)getDaoFactory().getDAO(Control.TYPE_ID).initializeAndUnproxy(entry.getKey());
                                controlBuilder.append(control.getTitle());
                                controlBuilder.append("\n");
                            }
                        }
                    }
                    for(Entry<CnATreeElement, CnALink> entry : CnALink.getLinkedElements(st, Control.TYPE_ID).entrySet()){
                        boolean hasSpecificMeasures = false;
                        for(Entry<CnATreeElement, CnALink> sEntry : CnALink.getLinkedElements(entry.getKey(), Control.TYPE_ID).entrySet()){
                            
                        }
                    }
                    list.add(removeLastCR(controlBuilder.toString())); // add deduced controls
                    list.add(st.getDescription()); // add description (isa question text)
                    // compute person in charge
                    StringBuilder personInChargeBuilder = new StringBuilder();
                    for(Entry<CnATreeElement, CnALink> entry : CnALink.getLinkedElements(st, PersonIso.TYPE_ID).entrySet()){
                        if(entry.getValue().getRelationId().equals(REL_SAMT_TOPIC_PERSON_ISO)){
                            PersonIso personIso = (PersonIso)getDaoFactory().getDAO(PersonIso.TYPE_ID).initializeAndUnproxy(entry.getKey());
                            personInChargeBuilder.append(personIso.getSurname() + ", " + personIso.getName());
                            personInChargeBuilder.append("\n");
                        }
                    }
                    list.add(removeLastCR(personInChargeBuilder.toString()));
                    //add deadline
                    DateFormat fmt = new SimpleDateFormat("dd.MM.yy");
                    list.add(fmt.format(st.getCompleteUntil()));
                    // add dbid
                    list.add(String.valueOf(st.getDbId()));
                }
                
                // add results for samttopic to resultlist
                result.add(list);
            }

        } catch (CommandException e) {
            LOG.error("");
        }
    }
    
    private String removeLastCR(String multiLineString){
        if(multiLineString.endsWith("\n")){
            multiLineString = multiLineString.substring(0, multiLineString.lastIndexOf("\n"));
        }
        return multiLineString;
    }

    public List<List<String>> getResult(){
        return result;
    }
    
    private String reduceDescriptionToHeadline(String description){
        if(description.contains("<h1>")){
            description = description.substring(description.indexOf("<h1>") + 4, description.indexOf("</h1>"));
        } else {
            description = description.substring(0, 80);
        }
        return description;
    }
    
    private String removeTags(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }

        Matcher m = REMOVE_TAGS.matcher(string);
        return m.replaceAll("");
    }

}
