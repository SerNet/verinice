/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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

import java.util.Map;
import java.util.SortedMap;

/**
 * Wraps a result of a strategy calculation.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ModuleData {

    private String itNetworkName;

    private Map<String, Map<String, Number>> data;

    public ModuleData(String itNetworkName, Map<String, Map<String, Number>> data) {
        this.setItNetworkName(itNetworkName);
        this.data = data;
    }

    public String getItNetworkName() {
        return itNetworkName;
    }

    public void setItNetworkName(String itNetworkName) {
        this.itNetworkName = itNetworkName;
    }

    public Map<String, Map<String, Number>> getData() {
        return data;
    }

    public void setData(SortedMap<String, Map<String, Number>> data) {
        this.data = data;
    }

    public boolean noData() {
        return data == null || data.isEmpty();
    }
}
