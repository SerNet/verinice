/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads files/attachmets data for a {@link CnATreeElement}
 * File meta-data data will not be loaded by this command. 
 * Use {@link LoadAttachments} to load file meta-data from database.
 * 
 * @see LoadAttachment
 * @see FileView
 * @see AttachmentFile
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class LoadAttachmentFile extends GenericCommand {

	private transient Logger log = Logger.getLogger(LoadAttachmentFile.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(LoadAttachmentFile.class);
		}
		return log;
	}

	private Integer dbId;
	
	private AttachmentFile attachmentFile;
	

	public LoadAttachmentFile(Integer dbId) {
		super();
		this.dbId = dbId;
	}

	public void execute() {
		if (getLog().isDebugEnabled()) {
			getLog().debug("executing, id is: " + getDbId() + "...");
		}
		if(getDbId()!=null) {
			IBaseDao<AttachmentFile, Serializable> dao = getDaoFactory().getDAO(AttachmentFile.class);		
			setAttachmentFile(dao.retrieve(getDbId(),null));
		}
	}

	public void setDbId(Integer cnAElementId) {
		this.dbId = cnAElementId;
	}

	public Integer getDbId() {
		return dbId;
	}
	
	public AttachmentFile getAttachmentFile() {
		return attachmentFile;
	}

	public void setAttachmentFile(AttachmentFile attachmentFile) {
		this.attachmentFile = attachmentFile;
	}

}
