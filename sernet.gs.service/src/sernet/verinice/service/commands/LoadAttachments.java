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
package sernet.verinice.service.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.TimeFormatter;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAttachmentDao;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Addition;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads files/attachmets meta data for a {@link CnATreeElement}
 * or for all elements if no id is set.
 * File data will not be loaded by this command. 
 * Use command LoadAttachmentFile to load file data from database.
 * 
 * @see LoadAttachmentFile
 * @see Attachment
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class LoadAttachments extends GenericCommand implements IAuthAwareCommand{

	private transient Logger log = Logger.getLogger(LoadAttachments.class);
	
	private transient IAuthService authService;
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(LoadAttachments.class);
		}
		return log;
	}

	private Integer cnAElementId;
	
	private List<Attachment> attachmentList;
	
	private String[] roles;
	
	public List<Attachment> getAttachmentList() {
		return attachmentList;
	}

	public void setAttachmentList(List<Attachment> noteList) {
		this.attachmentList = noteList;
	}

	/**
	 * Creates a new command, to load attachments for {@link CnATreeElement}
	 * with id cnAElementId.
	 * If cnAElementId all attachments will be loaded.
	 * 
	 * @param cnAElementId Id of a {@link CnATreeElement} or null
	 */
	public LoadAttachments(Integer cnAElementId) {
		super();
		this.cnAElementId = cnAElementId;
	}
	
	public LoadAttachments(Integer cnAElementId, String[] roles){
	    this(cnAElementId);
	    this.roles = roles;
	}

	/**
	 * Loads attachments for for {@link CnATreeElement}
	 * with id cnAElementId.
	 * File data will not be loaded by this command. 
 	 * Use {@link LoadAttachmentFile} to load file data from database.
	 * 
	 * if PermissionHandling is needed, attachments are loaded by additionDao via HQL
	 * @see sernet.verinice.interfaces.ICommand#execute()
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	public void execute() {
	    if (getLog().isDebugEnabled()) {
	        getLog().debug("executing, id is: " + getCnAElementId() + "...");
	    }
	    IAttachmentDao dao = getDaoFactory().getAttachmentDao();
	    long startTime = System.currentTimeMillis();
	    if(getCnAElementId() != null || getAuthService().getAdminUsername().equals(getAuthService().getUsername())){ // no permission handling needed
	        attachmentList = dao.loadAttachmentList(getCnAElementId());
	    } else { 
	        IBaseDao<Addition, Integer> additionDao = getDaoFactory().getDAO(Addition.TYPE_ID);
	        String hql = "from Addition addition " +
	                "where addition.cnATreeElementId IN (" +
	                    "select elmt.dbId from CnATreeElement elmt " +
	                    "inner join elmt.permissions as perm " +
	                    "where elmt.dbId IN ( " +
	                        "select ad.cnATreeElementId from Addition ad " +
	                     ") " +
	                     "and perm.role IN (:roles) " +
	                     "and perm.readAllowed = :readAllowed )";

	        String [] paramNames = new String[]{"roles", "readAllowed"};
	        Object[] params = new Object[]{roles, true};
	        attachmentList = new ArrayList<Attachment>();
	        for(Object o : additionDao.findByQuery(hql, paramNames, params)){
	            if(o instanceof Attachment){
	                attachmentList.add((Attachment)o);
	            }
	        }


	        if (getLog().isDebugEnabled()) {
	            getLog().debug("number of attachments found: " + attachmentList.size());
	        }
	        List<Attachment> filteredList = new ArrayList<Attachment>(0);
	        for (Attachment attachment : attachmentList) {
	            Entity entity = attachment.getEntity();
	            if(entity!=null) {
	                for (PropertyList pl : entity.getTypedPropertyLists().values()) {
	                    for (Property p : pl.getProperties()) {
	                        p.setParent(entity);
	                    }
	                }
	            }
	        }
	        setAttachmentList(attachmentList);
	    }
	    if(getLog().isDebugEnabled()){
	        getLog().debug("Tooks :\t" + TimeFormatter.getHumanRedableTime(System.currentTimeMillis()-startTime) + " to load " + attachmentList.size() + " attachments");
	    }
	    
	}

	public void setCnAElementId(Integer cnAElementId) {
		this.cnAElementId = cnAElementId;
	}


	public Integer getCnAElementId() {
		return cnAElementId;
	}

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

}
