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
package sernet.verinice.service.commands;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 */
@SuppressWarnings("serial")
public class LoadElementWithRetrieveInfoByUUID extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadElementWithRetrieveInfoByUUID.class);
    
    private RetrieveInfo retrieveInfo = null;
    private String uuId = null;
    private CnATreeElement element = null;

    public LoadElementWithRetrieveInfoByUUID(RetrieveInfo rInfo, String uuId){
        this.retrieveInfo = rInfo;
        this.uuId = uuId;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
        element = dao.findByUuid(uuId, retrieveInfo);
    }
    
    public CnATreeElement getElement() {
        return element;
    }

}
