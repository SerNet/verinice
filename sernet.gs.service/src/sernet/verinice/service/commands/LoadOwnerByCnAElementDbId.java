/******************************************************************************* 
 * Copyright (c) 2016 Viktor Schmidt. 
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation 
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.bpm.IControlExecutionProcess;
import sernet.verinice.model.common.ChangeLogEntry;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class LoadOwnerByCnAElementDbId extends GenericCommand {

    private static final long serialVersionUID = -1785677213993428418L;

    private Integer dbId;

    private List<ChangeLogEntry> list = new ArrayList<ChangeLogEntry>();
	
    private static final String QUERY = "from ChangeLogEntry entry where entry.elementId = ? and entry.change = ? order by entry.changetime";

    public LoadOwnerByCnAElementDbId(Integer dbId) {
        this.dbId = dbId;
	}

	@Override
    public void execute() {
        IBaseDao<? extends ChangeLogEntry, Serializable> dao = getDaoFactory().getDAO(ChangeLogEntry.class);
        list = dao.findByQuery(QUERY, new Object[] { dbId, ChangeLogEntry.TYPE_INSERT });
	}

    public String getElementOwner() {
	    if (list != null) {
            return list.get(0) == null ? IControlExecutionProcess.DEFAULT_OWNER_NAME : list.get(0).getUsername();
	    }
	    return null;
	}


}
