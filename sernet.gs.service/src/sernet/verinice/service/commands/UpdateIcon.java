/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Update the icon of a set of elements
 */
public class UpdateIcon extends GenericCommand implements IChangeLoggingCommand {

    private Set<String> elementUUIDs;
    private String iconPath;
    private String stationId;
    private List<CnATreeElement> changedElements;

    public UpdateIcon(Set<String> elementUUIDs, String iconPath, String stationId) {
        this.elementUUIDs = elementUUIDs;
        this.iconPath = iconPath;
        this.stationId = stationId;
    }

    /*
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        changedElements = new ArrayList<>(elementUUIDs.size());
        IBaseDao<@NonNull CnATreeElement, Serializable> dao = getDaoFactory()
                .getDAO(CnATreeElement.class);
        List<CnATreeElement> elements = dao
                .findByCriteria(DetachedCriteria.forClass(CnATreeElement.class)
                        .add(Restrictions.in(CnATreeElement.UUID, elementUUIDs)));
        for (CnATreeElement element : elements) {
            element.setIconPath(iconPath);
            dao.saveOrUpdate(element);
            changedElements.add(element);
        }
    }

    /*
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    /*
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }

    @Override
    public List<CnATreeElement> getChangedElements() {
        return changedElements;
    }

}
