/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web.poseidon.services;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.ITVerbund;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "menuService")
@SessionScoped
public class MenuService extends GenericChartService {

    /**
     * Returns a Set of all IT-Networks which the current logged in user is
     * allowed to see.
     *
     */
    public Set<ITVerbund> getVisibleItNetworks() {
        IBaseDao<ITVerbund, Serializable> dao = getDaoFactory().getDAO(ITVerbund.class);
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        @SuppressWarnings("unchecked")
        List<ITVerbund> itNetworks = dao.findAll(ri);
        Set<ITVerbund> sortedItNetworks = new TreeSet<>(new Comparator<ITVerbund>() {

            NumericStringComparator comp = new NumericStringComparator();

            @Override
            public int compare(ITVerbund o1, ITVerbund o2) {
                return comp.compare(o1.getTitle(), o2.getTitle());
            }
        });

        sortedItNetworks.addAll(itNetworks);
        return sortedItNetworks;
    }

}
