/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin
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
package sernet.verinice.service.commands.bp.converter;

import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This command converts one or more IT networks from old IT base protection to
 * new IT base protection.
 */
public class ConverterCommand extends GenericCommand {

    private static final long serialVersionUID = 7289994802720127190L;

    private Collection<String> itNetworkUuids;

    public ConverterCommand(Collection<String> itNetworkUuidSet) {
        super();
        this.itNetworkUuids = itNetworkUuidSet;
    }

    @Override
    public void execute() {
        @SuppressWarnings("unchecked")
        List<ITVerbund> itNetworks = getDaoFactory().getDAO(ITVerbund.class)
                .findByCriteria(DetachedCriteria.forClass(ITVerbund.class)
                        .add(Restrictions.in(CnATreeElement.UUID, itNetworkUuids)));
        MasterConverter converter = new MasterConverter(getDaoFactory());
        converter.convert(itNetworks);
    }

}
