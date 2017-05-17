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

import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import sernet.verinice.web.poseidon.view.charts.ChartUtils;

/**
 * Wraps implementation states of safeguards or controls for an Organization,
 * IT-Network or ControlGroup.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class StateData implements Serializable {

    private static final long serialVersionUID = -865803370582004650L;

    private String scopeName;

    private SortedMap<String, Number> states;

    private SortedMap<String, Number> translatedMessageKeysMap;

    private String colors;

    public StateData(String scopeName, Map<String, Number> states) {
        this.scopeName = scopeName;
        this.states = new TreeMap<>(new CompareByTitle());
        this.states.putAll(states);
        this.colors = ChartUtils.getColors(this.states.keySet());
    }

    public String getScopeName() {
        return scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public SortedMap<String, Number> getStates() {
        if (translatedMessageKeysMap == null)
            translatedMessageKeysMap = ChartUtils.translateMapKeyLabel(states);
        return translatedMessageKeysMap;
    }

    public void setStates(SortedMap<String, Number> states) {
        this.states = states;
    }

    /**
     * Checks if the diagram contains data, which means that there has to be at
     * least one state key with a value > 0.
     *
     */
    public boolean dataAvailable() {
        if (states != null) {
            for (Number number : states.values()) {
                if (number.intValue() > 0) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * Returns an json array with rgb colors for decorating the states in
     * charts.
     */
    public String getColors() {
        return colors;
    }

}
