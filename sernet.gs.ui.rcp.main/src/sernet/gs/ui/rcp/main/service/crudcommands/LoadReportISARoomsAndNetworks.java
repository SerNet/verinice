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
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    
    private int rootElmt;
    private int sgdbid;
    
    private static final Logger LOG = Logger.getLogger(LoadReportISARoomsAndNetworks.class);
    private static final String OVERVIEW_PROPERTY = "controlgroup_is_NoIso_group";
    private static final String IS_ITROOM_PROPERTY = "controlgroup_is_room";
    
    public static final String[] ROOMCOLUMNS = new String[] { 
                                            "RAUMNAME",
                                            "BEGEHUNGSDATUM",
                                            "BEGLEITPERSONEN",
                                            "INSPEKTEUR",
                                            "EINSTUFUNG",
                                            "HATKONZEPT",
                                            "ROOMDBID",
                                            "NR"
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
            try {
                LoadReportElements cgFinder = new LoadReportElements(ControlGroup.TYPE_ID, rootElmt);
                cgFinder = getCommandService().executeCommand(cgFinder);
                List<CnATreeElement> cList = new ArrayList<CnATreeElement>();
                cList.addAll(cgFinder.getElements());
                for(CnATreeElement c : cList){
                    if(c instanceof ControlGroup){
                        ControlGroup group = (ControlGroup)c;
                        if( group.getEntity() != null &&
                                group.getEntity().getSimpleValue(OVERVIEW_PROPERTY) != null &&
                                group.getEntity().getSimpleValue(OVERVIEW_PROPERTY).equals("1") &&
                                group.getEntity().getSimpleValue(IS_ITROOM_PROPERTY) != null && 
                                group.getEntity().getSimpleValue(IS_ITROOM_PROPERTY).equals("1")){
                            List<String> roomResult = new ArrayList<String>();
                            // adding title
                            roomResult.add(group.getTitle());
                            // adding date of inspection
                            Locale locale = Locale.getDefault();
                            DateFormat formatter = new SimpleDateFormat("EE, dd.MM.yyyy", locale);
                            DateFormat destinationFormat = new SimpleDateFormat("yyyy-MM-dd", locale);
                            formatter.setLenient(true);
                            Date fDate = formatter.parse(group.getEntity().getSimpleValue("controlgroup_isroom_dateofinspection"));
                            roomResult.add(destinationFormat.format(fDate));
                            // adding inspecteur and companions
                            Map<CnATreeElement,CnALink> linkedPersonsMap = CnALink.getLinkedElements(group, PersonIso.TYPE_ID);
                            StringBuilder inspectorBuilder = new StringBuilder();
                            StringBuilder companionBuilder = new StringBuilder();
                            for(Entry<CnATreeElement, CnALink> entry : linkedPersonsMap.entrySet()){
                                if(LOG.isDebugEnabled()){
                                    LOG.debug("RelationId of Link from " + group.getTitle() + " to " + entry.getKey().getTitle() + ":\t" + entry.getValue().getRelationId());
                                }
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
            } catch (java.text.ParseException e){
                LOG.error("Error parsing Date", e);
            }
            for(List<String> list : roomResults){
                list.add(String.valueOf(roomResults.indexOf(list) + 1));
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
        cacheID.append(String.valueOf(sgdbid));
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
