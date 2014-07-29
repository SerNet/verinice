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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 *
 */
public class LoadScopeElementsById extends GenericCommand {

    private static transient Logger LOG = Logger.getLogger(LoadScopeElementsById.class);
    
    private List<CnATreeElement> list;

    private Integer scopeID;
    
    
    private static final String QUERY = "from CnATreeElement elmt " +
            "where elmt.scopeId = ? ";
    
    public LoadScopeElementsById(Integer scope){
        this.scopeID = scope;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
        List<CnATreeElement> results = dao.findByQuery(QUERY, new Object[] {scopeID});
        if(list == null){
            list = new ArrayList<CnATreeElement>(0);
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("Elements in Scope " + scopeID + " found:\t" + list.size());
        }
        for(CnATreeElement elmt : results){
            RetrieveInfo ri = new RetrieveInfo().setProperties(true);
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(elmt.getUuid(), ri); 
            try {
                command = getCommandService().executeCommand(command);
                list.add(command.getElement());
            } catch (CommandException e) {
                LOG.error("Error while executing command",e);
            }
        }
    }
    
    public List<CnATreeElement> getResults(){
        return list;
    }

}
