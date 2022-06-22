/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade <jk{a}sernet{dot}de>.
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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

import sernet.hui.common.connect.ITargetObject;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CnATypeMapper;

public class CheckAllReferencesCommand extends GenericCommand {

    private static final long serialVersionUID = 4695607424731180807L;
    private Map<Integer, Boolean> result;

    public CheckAllReferencesCommand() {
        super();
    }

    @Override
    public void execute() {
        @NonNull
        IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
        List<CnATreeElement> targetObjects = dao.findByCriteria(DetachedCriteria.forClass(ITargetObject.class));

        List<CnATreeElement> requirements = (List<CnATreeElement>) dao.executeCallback(session -> {
            Query query = session
                    .createQuery("select r from BpRequirement r" + " join fetch r.parent p"
                            + " inner join r.linksDown l" + " where l.id.typeId not in (:types)"
                            + " and r.parent.parent.id != l.id.dependencyId");
            query.setParameterList("types", List.of(BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD,
                    BpRequirement.REL_BP_REQUIREMENT_BP_THREAT));
            query.setResultTransformer(new DistinctRootEntityResultTransformer());
            return query.list();
        });

        Map<Integer, CnATreeElement> requirementsById = requirements.stream()
                .collect(Collectors.toMap(CnATreeElement::getDbId, Function.identity()));
        result = new HashMap<>(targetObjects.size());
        for (CnATreeElement targetObject : targetObjects) {
            result.put(targetObject.getDbId(), checkLinks(targetObject, requirementsById));
        }
    }

    private boolean checkLinks(CnATreeElement targetObject,
            Map<Integer, CnATreeElement> requirementsById) {
        return targetObject.getLinksUp().stream().map(CnALink::getId)
                .map(CnALink.Id::getDependantId).filter(requirementsById::containsKey)
                .map(requirementsById::get).anyMatch(req -> checkRequirement(targetObject, req));
    }

    private boolean checkRequirement(CnATreeElement targetObject, CnATreeElement requirement) {
        CnATreeElement containingObject = requirement.getParent();
        while (containingObject != null && !(ITargetObject.class.isAssignableFrom(
                CnATypeMapper.getClassFromTypeId(containingObject.getTypeId())))) {
            if (containingObject.getParentId().equals(targetObject.getDbId())) {
                return false;
            }
            containingObject = containingObject.getParent();
        }
        return true;
    }

    public Map<Integer, Boolean> getResult() {
        return result;
    }
}
