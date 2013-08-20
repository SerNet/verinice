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
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 * 
 */
@SuppressWarnings("serial")
public class LoadAllScopesTitles extends GenericCommand {
   
    private transient Logger log = Logger.getLogger(LoadAllScopesTitles.class);
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadAllScopesTitles.class);
        }
        return log;
    }
    
    private static final String QUERY = "select distinct elmt from CnATreeElement elmt " +
            "join fetch elmt.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where elmt.objectType = ? " + //$NON-NLS-1$
             "or elmt.objectType = ?"; //$NON-NLS-1$
    
    
    private HashMap<Integer, String> selectedElements = new HashMap<Integer, String>();

    public LoadAllScopesTitles() {
    }       
     
    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
       List<Object> list = dao.findByQuery(QUERY, new Object[] {"it-verbund", Organization.TYPE_ID});
        if(list != null && list.size() > 0){
            for(Object elmt : list){
                if(elmt instanceof ITVerbund){
                    ITVerbund itverbund = (ITVerbund)elmt;
                    selectedElements.put(itverbund.getDbId(), itverbund.getTitle());
                }else if(elmt instanceof Organization){
                    Organization org = (Organization) elmt;
                    selectedElements.put(org.getDbId(), org.getTitle());
                }
            }
        } 
    }
   
    
    public HashMap<Integer, String> getElements() {
        return selectedElements;
    }
}
