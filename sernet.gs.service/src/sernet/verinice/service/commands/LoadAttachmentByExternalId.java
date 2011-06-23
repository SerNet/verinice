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
import java.util.ArrayList;
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Attachment;

/**
 * Load attachments with matching data source and external id.
 * Should usually return only one element - if the external id was really unique. 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LoadAttachmentByExternalId extends GenericCommand {

	private String id;

	private List<Attachment> list = new ArrayList<Attachment>();

	private String sourceID;

	private static final String QUERY = "from sernet.verinice.model.bsi.Attachment elmt where elmt.sourceId = ? and elmt.extId = ?"; 
	
	public LoadAttachmentByExternalId( String sourceID, String id) {
		this.id = id;
		this.sourceID = sourceID;
	}

	public void execute() {
		IBaseDao<Attachment, Serializable> dao = getDaoFactory().getDAO(Attachment.class);
		list = dao.findByQuery(QUERY, new Object[] {sourceID, id});
		if(list!=null && list.size()>0) {
           if(list.size()>1) {
               throw new RuntimeException("More than one attachment found for source-id: " + sourceID + " and ext-id: " + id);
           }
		}
 
	}
	
	public Attachment getAttachment() {
	    Attachment result = null;
	    if(list!=null && list.size()>0) {
           if(list.size()>1) {
               throw new RuntimeException("More than one attachment found for source-id: " + sourceID + " and ext-id: " + id);
           }
           result = list.get(0);
        }
	    return result;
	}


}
