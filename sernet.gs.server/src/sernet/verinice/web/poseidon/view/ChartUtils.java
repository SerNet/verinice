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
package sernet.verinice.web.poseidon.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.chart.ChartModel;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.service.model.IObjectModelService;
import sernet.verinice.web.Messages;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
abstract class ChartUtils {

    protected static final String IMPLEMENTATION_STATUS_UNEDITED = "SingleSelectDummyValue";

    private enum  DiagramColors {

        NO("FF4747"), NOT_APPLICABLE("BFBFBF"), PARTIALLY("FFE47A"), UNEDITED("4a93de"), YES("5fcd79");

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
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_JA, DiagramColors.YES);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_NEIN, DiagramColors.NO);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, DiagramColors.PARTIALLY);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET, DiagramColors.UNEDITED);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH, DiagramColors.NOT_APPLICABLE);
    }

    /**
     * Maps {@link MassnahmenUmsetzung#getUmsetzung()} to a color schema in
     * Hex-Code.
     *
     * @return A comma seperated list of hex values, which can used to configure
     *         {@link ChartModel}.
     */
    static String getColors(Iterable<String> massnahmenUmsetzungStates) {

        java.util.List<String> colors = new ArrayList<>();
        for (String state : massnahmenUmsetzungStates) {
            colors.add(states2Colors.get(state).toString());
        }

        return StringUtils.join(colors, ",");
    }

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

    static <T extends Object> SortedMap<String, T> transalteMapKeyLabel(Map<String, T> states) {
        SortedMap<String, T> humanReadableLabels = new TreeMap<>(new NumericStringComparator());
        for (Entry<String, T> e : states.entrySet()) {
            humanReadableLabels.put(getLabel(e.getKey()), e.getValue());
        }

        return humanReadableLabels;
    }

    private static String getLabel(String propertyId) {

        if (MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET.equals(propertyId)) {
            return Messages.getString(IMPLEMENTATION_STATUS_UNEDITED);
        }

        return getObjectModelService().getLabel(propertyId);
    }


    private static IObjectModelService getObjectModelService() {
        return (IObjectModelService) VeriniceContext.get(VeriniceContext.OBJECT_MODEL_SERVICE);
    }


}