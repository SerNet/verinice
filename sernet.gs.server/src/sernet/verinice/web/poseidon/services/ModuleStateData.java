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

import java.util.Collections;
import java.util.Map;

import sernet.verinice.model.bsi.ITVerbund;

/**
 * Wraps a result of a strategy calculation for an {@link ITVerbund} grouped by
 * module chapter names.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ModuleStateData {

    private final String scopeName;

    private final Map<String, Map<String, Number>> data;

    private final boolean dataAvailable;

    public ModuleStateData(String scopeName) {
        this(scopeName, Collections.<String, Map<String, Number>> emptyMap(), false);
    }

    public ModuleStateData(String scopeName, Map<String, Map<String, Number>> data) {
       this(scopeName, data, true);
    }

    private ModuleStateData(String scopeName, Map<String, Map<String, Number>> data, boolean dataAvailable) {
        this.scopeName = scopeName;
        this.data = data;
        this.dataAvailable = dataAvailable;
    }


    public String getScopeName() {
        return scopeName;
    }


    public Map<String, Map<String, Number>> getData() {
        return data;
    }

    public boolean dataAvailable() {
        return dataAvailable;
    }
}
