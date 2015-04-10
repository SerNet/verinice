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
	
	private boolean isAdmin;
	
	private boolean isScopeOnly;
	
	private Integer scopeId;
	
	/** Query-Setup-Objects **/
    private String hql;
    private Object[] params;
    private String[] paramNames;
	
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
	
	public LoadAttachments(Integer cnAElementId, String[] roles, boolean isAdmin, boolean isScopeOnly, Integer scopeId){
	    this(cnAElementId);
	    this.roles = (roles!=null) ? roles.clone() : null;
	    this.isAdmin = isAdmin;
	    this.isScopeOnly = isScopeOnly;
	    this.scopeId = scopeId;
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
	@Override
	public void execute() {
	    if (getLog().isDebugEnabled()) {
	        getLog().debug("executing, id is: " + getCnAElementId() + "...");
	    }
	    long startTime = System.currentTimeMillis();
	    
	    fillAttachmentList();

	    if (getLog().isDebugEnabled()) {
	        getLog().debug("number of attachments found: " + attachmentList.size());
	    }

	    initializeAttachmentList();
	    setAttachmentList(attachmentList);
	    if(getLog().isDebugEnabled()){
	        getLog().debug("It takes :\t" + TimeFormatter.getHumanRedableTime(System.currentTimeMillis()-startTime) + " to load " + attachmentList.size() + " attachments");
	    }
	    
	}

	/** decides if permissionHandling is needed or not **/
    private void fillAttachmentList() {
        String username = getAuthService().getUsername();
	    IAttachmentDao dao = getDaoFactory().getAttachmentDao();
	    if(getCnAElementId() != null || !isPermissionHandlingNeeded(username)){ // no permission handling needed
	        attachmentList = dao.loadAttachmentList(getCnAElementId());
	    } else{
	        fillPermissonHandled(); // load only allowed attachmnets
	    }
    }

    private void initializeAttachmentList() {
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
    }
    
    /** passes query and params to addition-dao **/
    private void fillPermissonHandled() {
        IBaseDao<Addition, Integer> additionDao = getDaoFactory().getDAO(Addition.TYPE_ID);
        hql = getQuery();
        attachmentList = new ArrayList<Attachment>();
        for(Object o : additionDao.findByQuery(hql, paramNames, params)){
            if(o instanceof Attachment){
                attachmentList.add((Attachment)o);
            }
        }
    }

    /** computes hql-Query dependent on users admin and scopeOnly properties **/
    private String getQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("from Addition addition ");
        sb.append("where addition.cnATreeElementId IN ( ");
        sb.append("select elmt.dbId from CnATreeElement elmt ");
        if(isScopeOnly && isAdmin){
            // scopeOnly-Admin is allowed to see everything within its scope
            sb.append(" where elmt.scopeId = :scopeId)");
            hql = sb.toString();
            params = new Object[]{scopeId};
            paramNames = new String[]{"scopeId"};
        } else { 
            // full permission handling needed
            sb.append(getPermissionHandlingQueryPart());
        }
        return sb.toString();
    }

    /**  create hql-query and fill parameter arrays**/
    private String getPermissionHandlingQueryPart() {
        StringBuilder sb = new StringBuilder();
        sb.append("inner join elmt.permissions as perm ");
        sb.append("where elmt.dbId IN ( ");
        sb.append("select ad.cnATreeElementId from Addition ad ) ");
        if(isScopeOnly){
            sb.append(" and elmt.scopeId = :scopeId");
        }
        sb.append(" and perm.role IN (:roles) ");
        sb.append(" and perm.readAllowed = :readAllowed )");
        hql = sb.toString();
        if(isScopeOnly){
            params = new Object[]{scopeId, roles, true};
            paramNames = new String[]{"scopeId", "roles", "readAllowed"};
        } else {
            paramNames = new String[]{"roles", "readAllowed"};
            params = new Object[]{roles, true};
        }
        return sb.toString();
    }
	
    /** detect if permission handling is needed at all by the following parameters (one is sufficient)
     * - ask authservice for permissionhandling
     * - if username equals admin username no permissionhandling is needed
     * - if user is admin and NOT scopeOnlyUser no permissionhandling is needed 
     * @param username
     * @return permissionHandling needed or not by boolean value
     */
	private boolean isPermissionHandlingNeeded(String username) {
        return !(!getAuthService().isPermissionHandlingNeeded() 
                || (getAuthService().getAdminUsername().equals(username))
                || (isAdmin && !isScopeOnly)); 
    }
	
	public void setCnAElementId(Integer cnAElementId) {
		this.cnAElementId = cnAElementId;
	}

	public Integer getCnAElementId() {
		return cnAElementId;
	}

    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    @Override
    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

}
