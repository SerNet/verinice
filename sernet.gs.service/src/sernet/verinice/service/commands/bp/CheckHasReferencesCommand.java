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

import org.eclipse.jdt.annotation.NonNull;

import sernet.hui.common.connect.ITargetObject;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CnATypeMapper;

public class CheckHasReferencesCommand extends GenericCommand {

    private static final long serialVersionUID = -6451473033394207319L;

    private Integer targetID;

    private boolean result;

    public CheckHasReferencesCommand(Integer targetID) {
        super();
        this.targetID = targetID;
    }

    @Override
    public void execute() {
        @NonNull
        IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
        CnATreeElement targetObject = dao.findById(targetID);
        result = checkLinks(targetObject);
    }

    private boolean checkLinks(CnATreeElement targetObject) {
        return targetObject.getLinksUp().stream()
                .filter(l -> l.getDependant().getTypeId().equals(BpRequirement.TYPE_ID))
                .map(CnALink::getDependant).anyMatch(req -> checkRequirement(targetObject, req));
    }

    private boolean checkRequirement(CnATreeElement targetObject, CnATreeElement requirement) {
        CnATreeElement containingObject = requirement.getParent();
        while (containingObject != null && !(ITargetObject.class.isAssignableFrom(
                CnATypeMapper.getClassFromTypeId(containingObject.getTypeId())))) {
            containingObject = containingObject.getParent();
        }
        return containingObject != null && !targetObject.equals(containingObject);
    }

    public boolean getResult() {
        return result;
    }
}
