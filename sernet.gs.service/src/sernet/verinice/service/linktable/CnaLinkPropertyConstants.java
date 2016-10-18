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
package sernet.verinice.service.linktable;

import sernet.verinice.model.common.CnALink;
import sernet.verinice.service.linktable.antlr.VqlParserTokenTypes;

/**
 *
 * Defines constants for property values of the {@link VqlParserTokenTypes} ":"
 * which represents a {@link CnALink}.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface CnaLinkPropertyConstants {

    String TYPE_TITLE = "title";
    String TYPE_DESCRIPTION = "description";
    String TYPE_RISK_VALUE_C = "risk-value-c";
    String TYPE_RISK_VALUE_I = "risk-value-i";
    String TYPE_RISK_VALUE_A = "risk-value-a";
    String TYPE_RISK_VALUE_C_WITH_CONTROLS = "risk-value-c-with-controls";
    String TYPE_RISK_VALUE_I_WITH_CONTROLS = "risk-value-i-with-controls";
    String TYPE_RISK_VALUE_A_WITH_CONTROLS = "risk-value-a-with-controls";
    String TYPE_RISK_TREATMENT = "risk-treatment";

    public String[] ALL_PROPERTIES = new String[]{
            TYPE_TITLE,TYPE_DESCRIPTION,
            TYPE_RISK_VALUE_C,
            TYPE_RISK_VALUE_I,
            TYPE_RISK_VALUE_A,
            TYPE_RISK_VALUE_C_WITH_CONTROLS,
            TYPE_RISK_VALUE_I_WITH_CONTROLS,
            TYPE_RISK_VALUE_A_WITH_CONTROLS,
            TYPE_RISK_TREATMENT};
}
