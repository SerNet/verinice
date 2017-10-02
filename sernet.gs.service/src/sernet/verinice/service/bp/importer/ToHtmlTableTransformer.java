/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.bp.importer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ITBP2VNA.generated.module.Document.Crossreferences;
import ITBP2VNA.generated.module.ElementalthreatRef;
import ITBP2VNA.generated.module.RequirementRef;

/**
 * @author uz[at]sernet.de
 *
 */
public class ToHtmlTableTransformer {
    private final static Logger LOG = Logger.getLogger(ToHtmlTableTransformer.class);

    private static final String HEADERSTYLE = "style=\"background: gray;\"";
    private static final String ODDSTYLE = "style=\"text-align: center;\"";;
    private static final String EVENSTYLE = "style=\"background: lightGrey;text-align: center;\"";

    private ToHtmlTableTransformer() {
        super();
    }

    /**
     * Create the html table for the requirements to thread coded in the {@link Crossreferences} table.
     * 
     * @param crossreferences
     * @return
     */
    public static String createCrossreferenceTable(Crossreferences crossreferences) {
        List<String> reqHeader = createRequirementHeader(crossreferences);
        List<String> threadHeader = createThreadHeader(crossreferences);

        if (reqHeader.isEmpty() || threadHeader.isEmpty()) {
            LOG.debug("Requierments or Thread headers are empty. ");
            LOG.debug(reqHeader);
            LOG.debug(threadHeader);
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<table style=\"border-collapse: collapse;\">").append("<th ").append(HEADERSTYLE).append(">").append("Gefährdung<br/>Anforderungen").append("</th>");
        for (String header : threadHeader) {
            builder.append("<th ").append(HEADERSTYLE).append(">").append(header).append("</th>");
        }
        builder.append("\n");
        int x = 0;
        for (RequirementRef rr : crossreferences.getRequirementRef()) {
            List<ElementalthreatRef> list = rr.getElementalthreatRef();
            builder.append("<tr>");
            String tt = reqHeader.get(x);
            builder.append("<td ").append(HEADERSTYLE).append(">").append(tt).append("</td>");
            for (ElementalthreatRef ref2 : list) {
                String isReferenced = Boolean.parseBoolean(ref2.getIsReferenced()) ? " X" : "";
                builder.append("<td ").append(x % 2 == 1 ? ODDSTYLE : EVENSTYLE).append(">").append(isReferenced).append("</td>");
            }
            builder.append("</tr>\n");
            x++;
        }

        builder.append("</table>");
        return builder.toString();
    }

    /**
     * @param crossreferences
     * @return
     */
    private static List<String> createThreadHeader(Crossreferences crossreferences) {
        List<String> threadHeader = new ArrayList<>();
        if (!crossreferences.getRequirementRef().isEmpty()) {
            RequirementRef requirementRef2 = crossreferences.getRequirementRef().get(0);
            List<ElementalthreatRef> elementalthreatRef = requirementRef2.getElementalthreatRef();
            for (ElementalthreatRef ref : elementalthreatRef) {
                threadHeader.add(ref.getIdentifier());
            }
        }
        return threadHeader;
    }

    /**
     * @param crossreferences
     * @return
     */
    private static List<String> createRequirementHeader(Crossreferences crossreferences) {
        List<String> reqHeader = new ArrayList<>();
        for (RequirementRef rr : crossreferences.getRequirementRef()) {
            reqHeader.add(rr.getIdentifier());
        }
        return reqHeader;
    }
}
