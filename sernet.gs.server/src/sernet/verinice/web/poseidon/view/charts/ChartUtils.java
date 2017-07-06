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
package sernet.verinice.web.poseidon.view.charts;

import static sernet.gs.web.Util.getMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.chart.ChartModel;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.web.poseidon.services.CompareByTitle;

/**
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public abstract class ChartUtils {

    private static final String BOUNDLE_NAME = "sernet.verinice.web.WebMessages";

    protected static final String IMPLEMENTATION_STATUS_UNEDITED = "SingleSelectDummyValue";

    private static final Logger log = Logger.getLogger(ChartUtils.class);

    private enum DiagramColors {

        // available colors from poseidon template: '#00acac', '#2f8ee5',
        // '#efa64c', '#6c76af', '#f16383', '#63c9f1', '#2d353c'
        NO("f16383"), NOT_APPLICABLE("A8ACB1"), PARTIALLY("EFA64C"), UNEDITED("D9E0E7"), YES("00acac");

        private String color;

        private DiagramColors(String color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return color;
        }
    }

    private static final Map<String, DiagramColors> states2Colors;

    static {
        states2Colors = new HashMap<>();

        // Itgs Controls
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_JA, DiagramColors.YES);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_NEIN, DiagramColors.NO);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, DiagramColors.PARTIALLY);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET, DiagramColors.UNEDITED);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH, DiagramColors.NOT_APPLICABLE);

        // Isms Controls
        states2Colors.put(Control.IMPLEMENTED_YES, DiagramColors.YES);
        states2Colors.put(Control.IMPLEMENTED_NO, DiagramColors.NO);
        states2Colors.put(Control.IMPLEMENTED_PARTLY, DiagramColors.PARTIALLY);
        states2Colors.put(Control.IMPLEMENTED_NOTEDITED, DiagramColors.UNEDITED);
        states2Colors.put(Control.IMPLEMENTED_NA, DiagramColors.NOT_APPLICABLE);
    }

    /**
     * Maps verinice controls implementation states to a color schema in
     * Hex-Code.
     *
     * Supported controls are {@link MassnahmenUmsetzung} and {@link Control}.
     *
     * @return A comma seperated list of hex values, which can used to configure
     *         {@link ChartModel}.
     */
    public static String getColors(Iterable<String> controlStates) {

        java.util.List<String> colors = new ArrayList<>();

        SortedSet<String> sortedByLabels = new TreeSet<>(new CompareByTitle());
        for(String state : controlStates){
            sortedByLabels.add(state);
        }

        for (String state : sortedByLabels) {
            if (states2Colors.containsKey(state)) {
                colors.add(states2Colors.get(state).toString());
            } else {
                log.warn("no color found for state: " + state);
            }
        }

        return StringUtils.join(colors, ",");
    }

    /**
     * Returns the max value of a collection of numbers by the integer
     * representation of the number.
     */
    static Integer getMax(Collection<Number> values) {

        if (values == null || values.isEmpty()) {
            return 0;
        }

        Collection<Integer> buffer = new ArrayList<>();
        for (Iterator<Number> iterator = values.iterator(); iterator.hasNext();) {
            Number number = iterator.next();
            buffer.add((Integer) number);
        }

        return Collections.max(buffer);
    }

    /**
     * Returns a new map where the keys are changed to the human readable labels
     * defined in the properties files. So the semantic meaning of the mapping
     * stays the same.
     *
     * @param states
     *            Map which maps properties id to numbers.
     * @return A map with new human readable labels.
     */
    public static <T extends Object> SortedMap<String, T> translateMapKeyLabel(Map<String, T> states) {
        SortedMap<String, T> humanReadableLabels = new TreeMap<>(new NumericStringComparator());
        for (Entry<String, T> e : states.entrySet()) {
            humanReadableLabels.put(getLabel(e.getKey()), e.getValue());
        }

        return humanReadableLabels;
    }

    public static String getLabel(String propertyId) {

        if (isUnedited(propertyId)) {
            return getMessage(BOUNDLE_NAME, IMPLEMENTATION_STATUS_UNEDITED);
        }

        return getMessage(BOUNDLE_NAME, propertyId);
    }

    public static boolean isUnedited(String propertyId) {
        return MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET.equals(propertyId) || Control.IMPLEMENTED_NOTEDITED.equals(propertyId);
    }


}