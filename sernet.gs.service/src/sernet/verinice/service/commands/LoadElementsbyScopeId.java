/*******************************************************************************
 * Copyright (c) 2013 Julia Haas.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 * 
 */
@SuppressWarnings("serial")
public class LoadElementsbyScopeId extends GenericCommand {
    private transient Logger log = Logger.getLogger(LoadElementsbyScopeId.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadElementsbyScopeId.class);
        }
        return log;
    }
    private HashMap<Integer, String> selectedElements = new LinkedHashMap<Integer, String>();
    private Integer scopeId;
    

    public LoadElementsbyScopeId(Integer scopeId) {
        this.scopeId = scopeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        IBaseDao<CnATreeElement, Serializable> elmtsDAO = getDaoFactory().getDAO(CnATreeElement.class);

        if (selectedElements != null && !(selectedElements.containsKey(scopeId))) {
            CnATreeElement elmt = elmtsDAO.findById(scopeId);
            if(elmt instanceof ITVerbund){
            selectedElements.put(elmt.getScopeId(), elmt.getTitle());
            }
        }
    }

    public HashMap<Integer, String> getElements() {
        return selectedElements;
    }
}
