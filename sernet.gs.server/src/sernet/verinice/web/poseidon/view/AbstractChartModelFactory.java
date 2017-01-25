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

import java.util.HashMap;
import java.util.Map;

import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class AbstractChartModelFactory {

    protected static final String IMPLEMENTATION_STATUS_UNEDITED = "SingleSelectDummyValue";

    protected enum DiagramColors {

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

    static final Map<String, DiagramColors> states2Colors;

    static {
        states2Colors = new HashMap<>();
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_JA, DiagramColors.YES);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_NEIN, DiagramColors.NO);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, DiagramColors.PARTIALLY);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET, DiagramColors.UNEDITED);
        states2Colors.put(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH, DiagramColors.NOT_APPLICABLE);
    }

    public AbstractChartModelFactory() {
        super();
    }

}