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
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 *
 */
@Deprecated
public class LoadReportISANonOverviewGroups extends GenericCommand {

    private Integer rootElmt;
    private Integer sgdbid;
    
    private static final Logger LOG = Logger.getLogger(LoadReportISANonOverviewGroups.class);
    private static final String OVERVIEW_PROPERTY = "controlgroup_is_NoIso_group";
    
    public static final String[] COLUMNS = new String[] { 
                                            "TITLE",
                                            "BEGEHUNGSDATUM",
                                            "BEGLEITPERSONEN",
                                            "INSPEKTEUR",
                                            "EINSTUFUNG",
                                            
    };
    
    private List<List<String>> results;
    
    public LoadReportISANonOverviewGroups(Integer root){
        this.rootElmt = root;
    }
    
    public LoadReportISANonOverviewGroups(Integer root, Integer sgdbid){
        this(root);
        this.sgdbid = sgdbid;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        results = new ArrayList<List<String>>(0);
        try{
            ControlGroup samtGroup = null;
            if(sgdbid != null){
                samtGroup = (ControlGroup)getDaoFactory().getDAO(ControlGroup.TYPE_ID).findById(sgdbid);
            } else {
                FindSGCommand c1 = new FindSGCommand(true, rootElmt);
                c1 = ServiceFactory.lookupCommandService().executeCommand(c1);
                samtGroup = c1.getSelfAssessmentGroup();
            }
            LoadReportElements c2 = new LoadReportElements(ControlGroup.TYPE_ID, rootElmt);
            c2 = ServiceFactory.lookupCommandService().executeCommand(c2);
            for(CnATreeElement e : c2.getElements()){
                if(e instanceof ControlGroup){
                    ControlGroup group = (ControlGroup)e;
                    if(Boolean.parseBoolean(group.getEntity().getSimpleValue(OVERVIEW_PROPERTY))){
                        // TBD;
                    }
                }
            }
        } catch (CommandException e){
            LOG.error("Error while determing nonOverviewGroups for report");
        }
    }

}
