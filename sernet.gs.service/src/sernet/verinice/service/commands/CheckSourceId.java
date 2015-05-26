/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Checks if a source-id exits in database
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CheckSourceId extends GenericCommand {

	private String sourceID;

	private static final String QUERY = "select count(dbId) from sernet.verinice.model.common.CnATreeElement elmt where elmt.sourceId = ?"; 
	
	private Long number = Long.valueOf(0);
	
	public CheckSourceId( String sourceID) {
		this.sourceID = sourceID;
	}

	public void execute() {
		IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
		List<Long> numberList = dao.findByQuery(QUERY, new Object[] {sourceID});
		if(numberList!=null && numberList.size()>0) {
		    number = numberList.get(0);
		}
	}
	
	public boolean exists() {
	    return getNumber()>0;
	}

    /**
     * @return the number
     */
    public Long getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(Long number) {
        this.number = number;
    }

}
