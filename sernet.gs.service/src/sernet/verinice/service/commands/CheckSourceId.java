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

    private static final String QUERY = "select dbId from sernet.verinice.model.common.CnATreeElement elmt where elmt.sourceId = ?";

    private boolean exists = false;

    public CheckSourceId(String sourceID) {
        this.sourceID = sourceID;
    }

    public void execute() {
        IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
        exists = (Boolean) dao.executeCallback(session -> !session.createQuery(QUERY)
                .setString(0, sourceID).setMaxResults(1).list().isEmpty());
    }

    public boolean exists() {
        return exists;
    }

}
