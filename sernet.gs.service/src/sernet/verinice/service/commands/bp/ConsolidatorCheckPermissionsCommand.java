/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade
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
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.CollectionUtil;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnATreeElement;

public class ConsolidatorCheckPermissionsCommand extends GenericCommand
        implements IAuthAwareCommand {

    private static final long serialVersionUID = -5680861433620803190L;

    private static final Logger logger = Logger
            .getLogger(ConsolidatorCheckPermissionsCommand.class);

    private static final Criterion IS_SAFEGUARD = Restrictions.eq("objectType", Safeguard.TYPE_ID);
    private static final Criterion IS_THREAT = Restrictions.eq("objectType", BpThreat.TYPE_ID);
    private static final Criterion IS_THREAT_OR_SAFEGUARD = Restrictions.in("objectType",
            Set.of(Safeguard.TYPE_ID, BpThreat.TYPE_ID));

    private final Set<Integer> moduleIDs;

    private final boolean checkModule;

    private final boolean checkRequirements;

    private final boolean checkLinkedSafeguards;

    private final boolean checkLinkedThreats;

    private HashMap<Integer, PermissionDeniedReason> permissionIssues;

    private transient IAuthService authService;

    private transient IBaseDao<CnATreeElement, Serializable> dao;

    private transient String userName;

    private transient Criterion typeRestrictionForLinkedDependencies;

    public ConsolidatorCheckPermissionsCommand(Set<Integer> moduleIDs, boolean checkModule,
            boolean checkRequirements, boolean checkLinkedSafeguards, boolean checkLinkedThreats) {
        this.moduleIDs = moduleIDs;
        this.checkModule = checkModule;
        this.checkRequirements = checkRequirements;
        this.checkLinkedSafeguards = checkLinkedSafeguards;
        this.checkLinkedThreats = checkLinkedThreats;
    }

    @Override
    public void execute() {
        permissionIssues = new HashMap<>(moduleIDs.size());
        dao = getDaoFactory().getDAO(CnATreeElement.class);
        userName = authService.getUsername();
        if (checkLinkedSafeguards) {
            if (checkLinkedThreats) {
                typeRestrictionForLinkedDependencies = IS_THREAT_OR_SAFEGUARD;
            } else {
                typeRestrictionForLinkedDependencies = IS_SAFEGUARD;
            }
        } else {
            if (checkLinkedThreats) {
                typeRestrictionForLinkedDependencies = IS_THREAT;
            } else {
                typeRestrictionForLinkedDependencies = null;
            }
        }
        CollectionUtil.partition(List.copyOf(moduleIDs), IDao.QUERY_MAX_ITEMS_IN_LIST)
                .forEach(this::checkModules);
    }

    private void checkModules(List<Integer> moduleIDs) {

        if (logger.isInfoEnabled()) {
            logger.info("Loading requirements for " + moduleIDs.size() + " modules");
        }
        @SuppressWarnings("unchecked")
        List<CnATreeElement> requirements = dao.findByCriteria(DetachedCriteria
                .forClass(BpRequirement.class).add(Restrictions.in("parentId", moduleIDs)));
        Map<Integer, List<CnATreeElement>> requirementsByModule = requirements.stream()
                .collect(Collectors.groupingBy(CnATreeElement::getParentId));
        requirementsByModule.forEach((moduleId, reqs) -> {

            if (checkModule) {
                if (logger.isInfoEnabled()) {
                    logger.info("Checking module " + moduleId);
                }
                if (dao.filterWritableElements(Set.of(reqs.get(0).getParent()), userName)
                        .isEmpty()) {
                    permissionIssues.put(moduleId, PermissionDeniedReason.MODULE);
                    return;
                }
            }
            checkModuleRequirements(moduleId, reqs);

        });
    }

    private void checkModuleRequirements(Integer moduleId,
            List<CnATreeElement> moduleRequirements) {
        if (checkRequirements) {
            if (dao.filterWritableElements(moduleRequirements, userName)
                    .size() != moduleRequirements.size()) {
                permissionIssues.put(moduleId, PermissionDeniedReason.REQUIREMENTS);
                return;
            }
        }
        checkLinkedObjects(moduleId, moduleRequirements);
    }

    private void checkLinkedObjects(Integer moduleId, List<CnATreeElement> moduleRequirements) {
        Set<Integer> moduleRequirementIDs = moduleRequirements.stream().map(CnATreeElement::getDbId)
                .collect(Collectors.toSet());
        if (logger.isInfoEnabled()) {
            logger.info(
                    "Loading dependencies for " + moduleRequirementIDs.size() + " requirements");
        }

        if (typeRestrictionForLinkedDependencies != null) {
            DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class)
                    .createAlias("linksUp", "lu")
                    .add(Restrictions.and(typeRestrictionForLinkedDependencies,
                            Restrictions.in("lu.id.dependantId", moduleRequirementIDs)));
            @SuppressWarnings("unchecked")
            List<CnATreeElement> dependencies = dao.findByCriteria(crit);
            if (logger.isInfoEnabled()) {
                logger.info("Checking permissions for " + dependencies.size() + " dependencies");
            }
            if (dao.filterWritableElements(dependencies, userName).size() != dependencies.size()) {
                permissionIssues.put(moduleId, PermissionDeniedReason.LINKED_OBJECTS);
            }
        }
    }

    @Override
    public void setAuthService(IAuthService authService) {
        this.authService = authService;

    }

    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    public Map<Integer, PermissionDeniedReason> getPermissionIssues() {
        return permissionIssues;
    }

    public enum PermissionDeniedReason {
        MODULE, REQUIREMENTS, LINKED_OBJECTS;
    }
}