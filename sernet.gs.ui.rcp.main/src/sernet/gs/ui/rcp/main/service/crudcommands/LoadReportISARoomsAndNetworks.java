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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.PersonIso;

/**
 *
 */
public class LoadReportISARoomsAndNetworks extends GenericCommand implements ICachedCommand{
    
    private Integer rootElmt;
    private Integer sgdbid;
    
    private static final Logger LOG = Logger.getLogger(LoadReportISARoomsAndNetworks.class);
    private static final String OVERVIEW_PROPERTY = "controlgroup_is_NoIso_group";
    
    public static final String[] ROOMCOLUMNS = new String[] { 
                                            "RAUMNAME",
                                            "BEGEHUNGSDATUM",
                                            "BEGLEITPERSONEN",
                                            "INSPEKTEUR",
                                            "EINSTUFUNG",
                                            "HATKONZEPT",
                                            "ROOMDBID"
    };
    
    public static final String[] NETWORKCOLUMNS = new String[] {
        
        
    };
    
    private List<List<String>> roomResults;
    private List<List<String>> networkResults;
    
    private boolean resultInjectedFromCache = false;
    
    public LoadReportISARoomsAndNetworks(Integer root){
        this.rootElmt = root;
    }
    
    public LoadReportISARoomsAndNetworks(Integer root, Integer sgdbid){
        this(root);
        this.sgdbid = sgdbid;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            roomResults = new ArrayList<List<String>>(0);
            networkResults = new ArrayList<List<String>>(0);
            ControlGroup rootControlGroup = null;
            try {
                if(sgdbid != null){
                    rootControlGroup = (ControlGroup)getDaoFactory().getDAO(ControlGroup.TYPE_ID).findById(sgdbid);
                } else {
                    FindSGCommand command = new FindSGCommand(true, rootElmt);
                    command = getCommandService().executeCommand(command);
                    rootControlGroup = command.getSelfAssessmentGroup();
                }
                LoadReportElements cgFinder = new LoadReportElements(ControlGroup.TYPE_ID, rootControlGroup.getDbId(), true);
                cgFinder = getCommandService().executeCommand(cgFinder);
                List<CnATreeElement> cList = new ArrayList<CnATreeElement>();
                cList.addAll(cgFinder.getElements(ControlGroup.TYPE_ID, rootControlGroup));
                for(CnATreeElement c : cList){
                    if(c instanceof ControlGroup){
                        ControlGroup group = (ControlGroup)c;
                        if(group.getEntity().getSimpleValue(OVERVIEW_PROPERTY).equals("1") &&
                                group.getEntity().getSimpleValue("controlgroup_is_room").equals("1")){
                            List<String> roomResult = new ArrayList<String>();
                            // adding title
                            roomResult.add(group.getTitle());
                            // adding date of inspection
                            roomResult.add(group.getEntity().getSimpleValue("controlgroup_isroom_dateofinspection"));
                            // adding inspecteur and companions
                            Map<CnATreeElement,CnALink> linkedPersonsMap = CnALink.getLinkedElements(group, PersonIso.TYPE_ID);
                            StringBuilder inspectorBuilder = new StringBuilder();
                            StringBuilder companionBuilder = new StringBuilder();
                            for(Entry<CnATreeElement, CnALink> entry : linkedPersonsMap.entrySet()){
                                if(entry.getValue().getRelationId().equals("inspector")){
                                    if(entry.getKey() instanceof PersonIso){
                                        PersonIso inspector = (PersonIso)entry.getKey();
                                        inspectorBuilder.append(inspector.getSurname() + ", " + inspector.getName());
                                        inspectorBuilder.append("\n");
                                    }
                                } else if(entry.getValue().getRelationId().equals("companion")){
                                    PersonIso companion = (PersonIso)entry.getKey();
                                    companionBuilder.append(companion.getSurname()+ ", " + companion.getName());
                                    companionBuilder.append("\n");
                                }
                            }
                            roomResult.add(companionBuilder.toString());
                            roomResult.add(inspectorBuilder.toString());
                            roomResult.add(group.getEntity().getSimpleValue("controlgroup_isroom_categorisation"));
                            if(Boolean.parseBoolean(group.getEntity().getSimpleValue("controlgroup_is_room_gotConcept"))){
                                roomResult.add("ein");
                            } else {
                                roomResult.add("kein");
                            }
                            roomResult.add(String.valueOf(group.getDbId()));
                            roomResults.add(roomResult);
                        }
                    }
                }
            } catch (CommandException e) {
                LOG.error("Error while executing command", e);
            }
        }
    }
    
    public List<List<String>> getRoomResults(){
        return roomResults;
    }
    
    public List<List<String>> getNetworkResults(){
        return networkResults;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        if(sgdbid != null){
            cacheID.append(String.valueOf(sgdbid));
        } else {
            cacheID.append("null");
        }
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        if(result instanceof Object[]){
            Object[] array = (Object[])result;
            networkResults = (ArrayList<List<String>>)array[0];
            roomResults = (ArrayList<List<String>>)array[1];
            resultInjectedFromCache = true;
            if(LOG.isDebugEnabled()){
                LOG.debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
            }
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        Object[] result = new Object[2];
        result[0] = networkResults;
        result[1] = roomResults;
        return result;
    }
}
