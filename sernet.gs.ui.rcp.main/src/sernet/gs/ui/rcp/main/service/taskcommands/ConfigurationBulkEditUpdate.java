/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByEntityId;
import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;

/**
 *  finds configuration for a given person and updates its entity with a given one
 *  used for bulkediting of account data
 */
@SuppressWarnings({"serial", "restriction"})
public class ConfigurationBulkEditUpdate extends BulkEditUpdate {
    
    private transient Set<Entity> changedEntities;
    private Class<? extends CnATreeElement> clazz;
    private List<Integer> dbIDs;
    private Entity dialogEntity;

    /**
     * @param clazz
     * @param dbIDs
     * @param dialogEntity
     */
    public ConfigurationBulkEditUpdate(Class<? extends CnATreeElement> clazz, List<Integer> dbIDs, Entity dialogEntity) {
        super(clazz, dbIDs, dialogEntity);
        this.clazz=clazz;
        this.dbIDs = dbIDs;
        this.dialogEntity = dialogEntity;
        
    }
    
    
    @Override
    public void execute() {
        changedEntities = new HashSet<Entity>(dbIDs.size());
        IBaseDao<Configuration, Serializable> dao = getDaoFactory().getDAO(Configuration.class);
        for (Integer id : dbIDs) {
            Configuration found = dao.findById(id);
            Entity editEntity = found.getEntity();
            editEntity.copyEntity(dialogEntity);
            changedEntities.add(editEntity);
        }
    }
    
    @Override
    public List<CnATreeElement> getChangedElements() {
        List<CnATreeElement> changedElements = new ArrayList<CnATreeElement>(changedEntities.size());
        try {
            for (Entity entity : changedEntities) {
                LoadCnAElementByEntityId command = new LoadCnAElementByEntityId(entity.getDbId());
                command = getCommandService().executeCommand(command);
                changedElements.addAll(command.getElements());
            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass()).error("Fehler beim Laden geänderter Elemente aus Transaktionslog.", e);
        }
        return changedElements;
    }

}
