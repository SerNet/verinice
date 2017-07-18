/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.gsm;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import sernet.gs.server.security.DummyAuthentication;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.verinice.bpm.IProcessCreater;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.interfaces.bpm.IGsmService;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.GroupByTags;

/**
 * Creates processes to track vulnerabilities imported from Greenbone Security Scanner (GSM).
 * 
 * The ProcessCreator handles an organizations if the property "org_tag" of the organization
 * contains the return value of method getTag().  
 * 
 * For every organization with this tag method handleOrg(..) is called.
 * 
 * This class is configured by Spring as part of a cron job in veriniceserver-jbpm.xml
 * and veriniceserver-plain.xml.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProcessCreator implements IProcessCreater {

    private static final Logger LOG = Logger.getLogger(ProcessCreator.class);

    private static final String TAG_DEFAULT = "ap-GSM";
    
    private String tag;
    
    private IGsmService gsmService;
    private ICommandService commandService;  
    private IBaseDao<Organization,Integer> organizationDao;
    private IConfigurationService configurationService;
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IProcessCreater#create()
     */
    @Override
    public void create() {   
        SecurityContext ctx = null;
        boolean dummyAuthAdded = false;
        try { 
            ServerInitializer.inheritVeriniceContextState();
            ctx = SecurityContextHolder.getContext(); 
            dummyAuthAdded = addSecurityContext(ctx);
            List<String> orgUuids = selectOrgs();
            if (LOG.isDebugEnabled() && orgUuids.isEmpty()) {
                LOG.debug("No GSM organizations found. Tag value is: " + getTag());
            }
            for (String uuid : orgUuids) {
                RetrieveInfo ri = RetrieveInfo.getPropertyChildrenInstance();
                ri.setPermissions(true);
                Organization org = getOrganizationDao().findByUuid(uuid, ri);
                handleOrg(org); 
            }
        } catch (Exception e) {
            LOG.error("Error while creating processes.", e);
        } finally {
            if(dummyAuthAdded) {
                remoceSecurityContext(ctx);
            }
        }
    }
    
    private List<String> selectOrgs() {
        String hql = "select distinct element.uuid from CnATreeElement as element " + //$NON-NLS-1$
                "inner join element.entity as entity " + //$NON-NLS-1$
                "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
                "inner join propertyList.properties as props " + //$NON-NLS-1$
                "where props.propertyType = ? " + //$NON-NLS-1$
                "and props.propertyValue like ? "; //$NON-NLS-1$
        
        Object[] params = new Object[]{Organization.PROP_TAG,createTagParam()};        
        return getOrganizationDao().findByQuery(hql,params);     
    }

    private void handleOrg(Organization org) {
        if(org==null) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Creating GSM processes for organization: " + org.getUuid() + ", source-id is: " + org.getSourceId());
        }
        try {
            groupByTags(org);
            createProcesses(org);
            cleanUp(org);
            removeTag(org);
        } catch (Exception e) {
            LOG.error("Error while creating processes for org with id: " + org.getDbId(), e);
        }
    }

    private void groupByTags(CnATreeElement org) throws CommandException {
        Set<CnATreeElement> groups = org.getChildren();
        for (CnATreeElement group : groups) {
            if(AssetGroup.TYPE_ID.equals(group.getTypeId()) ||
               ControlGroup.TYPE_ID.equals(group.getTypeId())) {
                GroupByTags command = new GroupByTags(group.getUuid(), true);
                command = getCommandService().executeCommand(command);
            }
        }
    }
    
    private void createProcesses(CnATreeElement org) {
        IProcessStartInformation info = getGsmService().startProcessesForOrganization(org.getDbId());     
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of new tasks for organization " + org.getUuid() + ": " + info.getNumber());
        }
    }
    
    private void cleanUp(Organization org) {
        getGsmService().cleanUpOrganization(org.getDbId()); 
    }

    private void removeTag(Organization org) {
        Collection<? extends String> tags = org.getTags();
        tags.remove(getTag());
        org.setTags(tags);
        getOrganizationDao().merge(org);
    }
    
    private boolean addSecurityContext(SecurityContext ctx) {
        boolean dummyAuthAdded = false;
        String username = UUID.randomUUID().toString();
        DummyAuthentication authentication = new DummyAuthentication(username);
        String[] adminRoleArray = new String[]{ApplicationRoles.ROLE_ADMIN,ApplicationRoles.ROLE_WEB,ApplicationRoles.ROLE_USER};
        getConfigurationService().setRoles(username, adminRoleArray);
        getConfigurationService().setScopeOnly(username, false);      
        if(ctx.getAuthentication()==null) {
            ctx.setAuthentication(authentication);
            dummyAuthAdded = true;
        }
        return dummyAuthAdded;
    }

    private void remoceSecurityContext(SecurityContext ctx) {
        if(ctx!=null) {
            ctx.setAuthentication(null);
        }
    }

    private String createTagParam() {
        return new StringBuilder("%").append(getTag()).append("%").toString();
    }

    public String getTag() {
        if(tag==null) {
            tag = TAG_DEFAULT;
        }
        return tag;
    }

    public void setTag(String tagValue) {
        this.tag = tagValue;
    }

    public IGsmService getGsmService() {
        return gsmService;
    }

    public void setGsmService(IGsmService gsmService) {
        this.gsmService = gsmService;
    }

    public ICommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    public IBaseDao<Organization, Integer> getOrganizationDao() {
        return organizationDao;
    }

    public void setOrganizationDao(IBaseDao<Organization, Integer> organizationDao) {
        this.organizationDao = organizationDao;
    }

    public IConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(IConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
