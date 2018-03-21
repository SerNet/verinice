/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.RelationNotDefinedException;

/**
 * Create and save new element of type type to the database using its class to
 * lookup the DAO from the factory.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 * @param <T>
 */
public class CreateLink<U extends CnATreeElement, V extends CnATreeElement> extends GenericCommand {

    private transient Logger log = Logger.getLogger(CreateLink.class);
    
    boolean validateLinkCreation = false;

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CreateLink.class);
        }
        return log;
    }

    private CnATreeElement dependant;
    private CnATreeElement dependency;
    private String dependantUuid;
    private String dependencyUuid;
    private CnALink link;
    private String relationId;
    private String comment;

    public CreateLink(String dependantUuid, String dependencyUuid) {
        this(dependantUuid, dependencyUuid, "", "");
    }
    
    public CreateLink(U dependant, V dependency) {
        this(dependant, dependency, "", "");
    }
    
    public CreateLink(String dependantUuid, String dependencyUuid, String relationId) {
        this(dependantUuid, dependencyUuid, relationId, "");
    }

    public CreateLink(U dependant, V dependency, String relationId) {
        this(dependant, dependency, relationId, "");
    }
    
    public CreateLink(String dependantUuid, String dependencyUuid, String relationId, String comment) {
        this.dependantUuid = dependantUuid;
        this.dependencyUuid = dependencyUuid;
        this.relationId = relationId;
        this.comment = comment;
    }

    public CreateLink(U dependant, V dependancy, String relationId, String comment) {
        this.dependant = dependant;
        this.dependency = dependancy;
        this.relationId = relationId;
        this.comment = comment;
    }

    @Override
    public void execute() {
        try {
            IBaseDao<CnALink, Serializable> linkDao = getDaoFactory().getDAO(CnALink.class);
            IBaseDao<CnATreeElement, Serializable> dependantDao = getDaoFactory().getDAO(CnATreeElement.class);
            IBaseDao<CnATreeElement, Serializable> dependencyDao = getDaoFactory().getDAO(CnATreeElement.class);

            RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
            ri.setLinksUp(true);      
            dependency = dependencyDao.findByUuid(getDependencyUuid(), ri);          
            
            ri = RetrieveInfo.getPropertyInstance();
            ri.setLinksDown(true);  
            dependant = dependantDao.findByUuid(getDependantUuid(), ri);

            if (getLog().isDebugEnabled()) {
                getLog().debug("Creating link from " + dependency.getTypeId() + " to " + dependant.getTypeId());
            }
            
            // only validate, if relationtype is set
            if(StringUtils.isEmpty(relationId) || isRelationValid(dependant, dependency, relationId)) {
                link = new CnALink(dependant, dependency, relationId, comment);
                linkDao.merge(link, true);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Relation for typeId:\t").append(relationId).append("\tfrom entityType:\t");
                sb.append(dependant.getEntityType().getId()).append("\tto\t").append(dependency.getEntityType().getId());
                sb.append(" is not defined in SNCA.xml. Link will not be created");
                throw new RelationNotDefinedException(sb.toString());
            }

        } catch (RuntimeException e) {
            getLog().error("RuntimeException while creating link.", e);
            throw e;
        } catch (Exception e) {
            String message = "Error while creating link";
            getLog().error(message, e);
            if(e instanceof RelationNotDefinedException) {
                message = "Linktype did not pass validation";
            }
            throw new RuntimeException(message, e);
        }
    }

    private String getDependantUuid() {
        if(dependantUuid!=null) {
            return dependantUuid;
        } else {
            return dependant.getUuid();
        }
    }

    private String getDependencyUuid() {
        if(dependencyUuid!=null) {
            return dependencyUuid;
        } else {
            return dependency.getUuid();
        }
    }

    public CnALink getLink() {
        return link;
    }

    /**
     * gets all possible relations outgoing from entityType of sourceElement targeting the entityType of destinationElement. If one of those equals linkType, return true
     * else return false (relationtype for source and destination is not defined in SNCA.xml )
     * @param sourceElement
     * @param destinationElement
     * @param relationType
     * @return
     */
    private boolean isRelationValid(CnATreeElement sourceElement, CnATreeElement destinationElement, String relationType) {
        if(CnALink.Id.NO_TYPE.equals(relationId)){ // special dnd itgs case which is allowed always
            return true;
        }
        // special handling for links between elements of itgs model
        if(sourceElement instanceof IBSIStrukturElement && destinationElement instanceof IBSIStrukturElement){
            return true;
        }

        for(HuiRelation relation : HUITypeFactory.getInstance().getPossibleRelations(sourceElement.getEntityType().getId(), destinationElement.getEntityType().getId())) {
            if(relationType.equals(relation.getId())) {
                return true;
            }
        };

        return false;
    }

}
