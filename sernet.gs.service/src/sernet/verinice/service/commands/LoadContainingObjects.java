/*******************************************************************************
 * Copyright (c) 2020 Jochen Kemnade.
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
package sernet.verinice.service.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.CollectionUtil;
import sernet.hui.common.connect.ITargetObject;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnATreeElement;

public class LoadContainingObjects extends GenericCommand {

    private static final long serialVersionUID = -7569416216775689625L;
    private final Set<Integer> elementIDs;
    private Map<Integer, CnATreeElement> result;

    public LoadContainingObjects(List<CnATreeElement> elements) {
        this.elementIDs = elements.stream().map(CnATreeElement::getDbId)
                .collect(Collectors.toSet());
    }

    @Override
    public void execute() {
        result = new HashMap<>(elementIDs.size());
        CollectionUtil.partition(new ArrayList<>(elementIDs), 500).forEach(partition -> {
            @SuppressWarnings("unchecked")
            List<CnATreeElement> elements = getDaoFactory().getDAO(CnATreeElement.class)
                    .findByCriteria(DetachedCriteria.forClass(CnATreeElement.class)
                            .add(Restrictions.in("dbId", partition)));
            elements.forEach(element -> {
                CnATreeElement containingObject = element.getParent();
                String elementType = element.getTypeId();
                if (BpRequirement.TYPE_ID.equals(elementType)
                        || Safeguard.TYPE_ID.equals(elementType)
                        || BpThreat.TYPE_ID.equals(elementType)) {
                    while (containingObject != null && !(ITargetObject.class.isAssignableFrom(
                            CnATypeMapper.getClassFromTypeId(containingObject.getTypeId())))) {
                        containingObject = containingObject.getParent();
                    }
                }
                if (containingObject != null) {
                    containingObject.getEntity().getTypedPropertyLists().values()
                            .forEach(propertyList -> propertyList.getProperties()
                                    .forEach(Hibernate::initialize));
                    result.put(element.getDbId(), containingObject);
                }
            });
        });
    }

    public Map<Integer, CnATreeElement> getResult() {
        return result;
    }

}
