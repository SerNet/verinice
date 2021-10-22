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

import sernet.gs.service.LinkValidator;
import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
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

    private static final Logger logger = Logger.getLogger(CreateLink.class);

    private CnATreeElement dependant;
    private CnATreeElement dependency;
    private Integer dependantId;
    private Integer dependencyId;
    private CnALink link;
    private String relationId;
    private String comment;
    private final boolean retrieveLinkedElementProperties;

    public CreateLink(U dependant, V dependency, boolean retrieveLinkedElementProperties) {
        this(dependant, dependency, "", "", retrieveLinkedElementProperties);
    }

    public CreateLink(Integer dependantId, Integer dependencyId, String relationId) {
        this(dependantId, dependencyId, relationId, "");
    }

    public CreateLink(U dependant, V dependency, String relationId) {
        this(dependant, dependency, relationId, "");
    }

    public CreateLink(U dependant, V dependency, String relationId,
            boolean retrieveLinkedElementProperties) {
        this(dependant, dependency, relationId, "", retrieveLinkedElementProperties);
    }

    public CreateLink(U dependant, V dependancy, String relationId, String comment) {
        this(dependant, dependancy, relationId, comment, true);
    }

    public CreateLink(Integer dependantId, Integer dependencyId, String relationId,
            String comment) {
        this.dependantId = dependantId;
        this.dependencyId = dependencyId;
        this.relationId = relationId;
        this.comment = comment;
        this.retrieveLinkedElementProperties = true;
    }

    public CreateLink(U dependant, V dependancy, String relationId, String comment,
            boolean retrieveLinkedElementProperties) {
        this.dependant = dependant;
        this.dependency = dependancy;
        this.relationId = relationId;
        this.comment = comment;
        this.retrieveLinkedElementProperties = retrieveLinkedElementProperties;
    }

    @Override
    public void execute() {
        try {
            IBaseDao<CnALink, Serializable> linkDao = getDaoFactory().getDAO(CnALink.class);
            IBaseDao<CnATreeElement, Serializable> dependantDao = getDaoFactory()
                    .getDAO(CnATreeElement.class);
            IBaseDao<CnATreeElement, Serializable> dependencyDao = getDaoFactory()
                    .getDAO(CnATreeElement.class);

            RetrieveInfo ri = retrieveLinkedElementProperties ? RetrieveInfo.getPropertyInstance()
                    : new RetrieveInfo();
            ri.setLinksUp(true);
            dependency = dependencyDao.retrieve(getDependencyId(), ri);

            ri = retrieveLinkedElementProperties ? RetrieveInfo.getPropertyInstance()
                    : new RetrieveInfo();
            ri.setLinksDown(true);
            dependant = dependantDao.retrieve(getDependantId(), ri);

            if (logger.isDebugEnabled()) {
                logger.debug("Creating link from " + dependency.getTypeId() + " to "
                        + dependant.getTypeId());
            }

            // only validate, if relationtype is set
            if (StringUtils.isEmpty(relationId)
                    || LinkValidator.isRelationValid(dependant, dependency, relationId)) {
                link = new CnALink(dependant, dependency, relationId, comment);
                linkDao.merge(link, true);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Relation for typeId:\t").append(relationId)
                        .append("\tfrom entityType:\t");
                sb.append(dependant.getEntityType().getId()).append("\tto\t")
                        .append(dependency.getEntityType().getId());
                sb.append(" is not defined in SNCA.xml. Link will not be created");
                throw new RelationNotDefinedException(sb.toString());
            }

        } catch (RuntimeException e) {
            logger.error("RuntimeException while creating link.", e);
            throw e;
        } catch (Exception e) {
            String message = "Error while creating link";
            logger.error(message, e);
            if (e instanceof RelationNotDefinedException) {
                message = "Linktype did not pass validation";
            }
            throw new RuntimeException(message, e);
        }
    }

    private Integer getDependantId() {
        if (dependantId != null) {
            return dependantId;
        } else {
            return dependant.getDbId();
        }
    }

    private Integer getDependencyId() {
        if (dependencyId != null) {
            return dependencyId;
        } else {
            return dependency.getDbId();
        }
    }

    public CnALink getLink() {
        return link;
    }

}
