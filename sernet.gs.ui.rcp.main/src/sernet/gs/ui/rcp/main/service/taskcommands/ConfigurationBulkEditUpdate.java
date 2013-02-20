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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

/**
 *  finds configuration for a given person and updates its entity with a given one
 *  used for bulkediting of account data
 */
@SuppressWarnings({"serial", "restriction"})
public class ConfigurationBulkEditUpdate extends ChangeLoggingCommand  implements IChangeLoggingCommand, IAuthAwareCommand {
    
    private transient Logger log;
    private List<Integer> dbIDs;
    private Entity dialogEntity;
    private transient IAuthService authService;
    private boolean updatePassword = false;
    private String stationId;
    private String newPassword;
    private List<String> failedUpdates;

    /**
     * @param clazz
     * @param dbIDs
     * @param dialogEntity
     */
    public ConfigurationBulkEditUpdate(List<Integer> dbIDs, Entity dialogEntity, boolean updatePW, String newPassword) {
        log = Logger.getLogger(ConfigurationBulkEditUpdate.class);
        this.dbIDs = dbIDs;
        this.dialogEntity = dialogEntity;
        this.updatePassword = updatePW;
        this.stationId = ChangeLogEntry.STATION_ID;
        this.newPassword = newPassword;
        failedUpdates = new ArrayList<String>(0);
    }
    
    
    @Override
    public void execute() {
        IBaseDao<Configuration, Serializable> dao = getDaoFactory().getDAO(Configuration.class);
        CnATreeElement pElmt = null;
        for (Integer id : dbIDs) {
            RetrieveInfo ri = new RetrieveInfo();
            ri.setParent(true).setProperties(true).setChildren(true).setChildrenProperties(true).setGrandchildren(true);
            Configuration found = dao.retrieve(id,ri); 
            pElmt = found.getPerson();
            LoadChildrenForExpansion command2 = new LoadChildrenForExpansion(pElmt);
            try {
                command2 = ServiceFactory.lookupCommandService().executeCommand(command2);
                pElmt = command2.getElementWithChildren();
            } catch (CommandException e) {
                getLog().error("Error while retrieving children", e);
            }
            // username must be set, to be able to edit password, empty username isn't allowed also
            if(found.getEntity().getProperties(Configuration.PROP_USERNAME).getProperty(0) == null ||
                    found.getEntity().getProperties(Configuration.PROP_USERNAME).getProperty(0).getPropertyValue().equals("")){
                getLog().warn("Property username not set, adding user to failed elements list");
                String personInfoString = null;
                if(pElmt instanceof Person){
                    personInfoString = getPersonInfoString((Person)pElmt);
                } else if(pElmt instanceof PersonIso){
                    personInfoString = getPersonIsoInfoString((PersonIso)pElmt);
                }
                failedUpdates.add(personInfoString);
                continue;
            }
            found.getEntity().copyEntity(dialogEntity);
            // password will not be copied with entity method, special handling needed, see below
            if(updatePassword && newPassword != null){
                // as done in ChangeOwnPassword also
                Property passProperty = found.getEntity().getProperties(Configuration.PROP_PASSWORD).getProperty(0);
                // password is hashed with a method that generates exact the same as the browser with
                // the http request would do, a1Format should be described in the rfc to md5
                String hash = getAuthService().hashPassword(found.getUser(), newPassword);
                passProperty.setPropertyValue(hash, false);
                getDaoFactory().getDAO(Configuration.class).merge(found);
            }
        }
    }
    
    private String getPersonInfoString(Person element){
        StringBuilder sb = new StringBuilder();
        IBaseDao<Person, Serializable> dao = null;
        RetrieveInfo ri = new RetrieveInfo();
        ri.setParent(true).setProperties(true).setChildren(true).setChildrenProperties(true).setGrandchildren(true);

        dao = getDaoFactory().getDAO(Person.class);
        if(!element.isChildrenLoaded() && !element.isChildrenLoaded()){
            element = dao.retrieve(element.getDbId(), ri);
        }
        if(checkStringEmpty(element.getKuerzel())){
            sb.append(element.getKuerzel());
            sb.append(" ");
        }
        if(checkStringEmpty(element.getFullName())){
            sb.append(element.getFullName());
        }
        if(sb.length() > 0){
            return sb.toString();
        } else {
            return Messages.ConfigurationBulkEdit_0;
        }
    }
    
    private String getPersonIsoInfoString(PersonIso person){
        StringBuilder sb = new StringBuilder();
        IBaseDao<PersonIso, Serializable> dao = null;
        RetrieveInfo ri = new RetrieveInfo();
        ri.setParent(true).setProperties(true).setChildren(true).setChildrenProperties(true).setGrandchildren(true);

        dao = getDaoFactory().getDAO(PersonIso.class);
        if(!person.isChildrenLoaded() && !person.isChildrenLoaded()){
            person = dao.retrieve(person.getDbId(), ri);
        }
        if(checkStringEmpty(person.getEntity().getSimpleValue(PersonIso.PROP_ABBR))){
            sb.append(person.getEntity().getSimpleValue(PersonIso.PROP_ABBR));
            sb.append(" ");
        }
        if(checkStringEmpty(person.getEntity().getSimpleValue(PersonIso.PROP_NAME))){
            sb.append(person.getEntity().getSimpleValue(PersonIso.PROP_NAME));
            sb.append(" ");
        }
        if(checkStringEmpty(person.getEntity().getSimpleValue(PersonIso.PROP_SURNAME))){
            sb.append(person.getEntity().getSimpleValue(PersonIso.PROP_SURNAME));
        }
        
        if(sb.length() > 0){
            return sb.toString();
        } else {
            return Messages.ConfigurationBulkEdit_0;
        }
    }
    
    private boolean checkStringEmpty(String s){
        if(s != null && !s.equals("")){
            return true;
        }
        return false;
    }
    
    @Override
    public List<CnATreeElement> getChangedElements() {
        // to prevent exceptions, instead of returning 'null', an empty list is returned
        return new ArrayList<CnATreeElement>(0);
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }
    
    public List<String> getFailedUpdates() {
        return failedUpdates;
    }
    
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(this.getClass());
        }
        return log;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthAwareCommand#setAuthService(sernet.verinice.interfaces.IAuthService)
     */
    @Override
    public void setAuthService(IAuthService service) {
        authService = service;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthAwareCommand#getAuthService()
     */
    @Override
    public IAuthService getAuthService() {
        return authService;
    }
    

}
