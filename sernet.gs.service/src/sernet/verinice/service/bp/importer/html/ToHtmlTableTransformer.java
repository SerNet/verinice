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
package sernet.verinice.service.bp.importer.html;

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

    private static final String HEADERSTYLE = "style=\"background: lightGray;\"";
    private static final String ODDSTYLE = "";
    private static final String EVENSTYLE = "style=\"background: lightGrey\"";
    private static final String DATASTYLE = "style=\"text-align: center;\"";
    private static final String TABLESTYLE = "style=\"border-collapse: collapse;\"";
    
    private ToHtmlTableTransformer() {
        super();
    }

    /**
     * Create the html table for the requirements to thread coded in the
     * {@link Crossreferences} table.
     * 
     * @param crossreferences
     * @return
     */
    public static String createCrossreferenceTable(Crossreferences crossreferences) {
        StringBuilder builder = new StringBuilder();
        createCrossReferenceTable(crossreferences, builder);
        return builder.toString();
    }

    /**
     * Create the html table for the requirements to thread coded in the
     * {@link Crossreferences} table.
     * 
     * @param crossreferences
     * @param builder
     *            a string builder containing the table in the end
     */
    public static void createCrossReferenceTable(Crossreferences crossreferences, StringBuilder builder) {
        List<String> requirementHeader = createRequirementHeader(crossreferences);
        List<String> threadHeader = createThreadHeader(crossreferences);

        if (requirementHeader.isEmpty() || threadHeader.isEmpty()) {
            LOG.debug("Requierments or Thread headers are empty. ");
            LOG.debug(requirementHeader);
            LOG.debug(threadHeader);
            return;
        }

        builder.append("<table ").append(TABLESTYLE).append(">").append("<th ").append(HEADERSTYLE).append(">").append("Gefährdung<br/>Anforderungen").append("</th>");
        for (String header : threadHeader) {
            builder.append("<th ").append(HEADERSTYLE).append(">").append(header).append("</th>");
        }
        builder.append("\n");
        int row = 0;
        for (RequirementRef reference : crossreferences.getRequirementRef()) {
            List<ElementalthreatRef> threadRefs = reference.getElementalthreatRef();
            builder.append("<tr ").append(row % 2 == 1 ? ODDSTYLE : EVENSTYLE).append(">");
            if (threadRefs.size() != threadHeader.size()) {
                LOG.warn("Elementar threads number don't match header size. Header: " + threadHeader.size() + " current list of threads: " + threadRefs.size());
            }
            if (row > requirementHeader.size()) {
                LOG.warn("More references than requirement headers, finishing row and table. ");
                builder.append("</tr>");
                break;
            }
            String headerTitle = requirementHeader.get(row);
            builder.append("<td ").append(HEADERSTYLE).append(">").append(headerTitle).append("</td>");
            for (ElementalthreatRef threadRef : threadRefs) {
                String isReferenced = Boolean.parseBoolean(threadRef.getIsReferenced()) ? "X" : "";
                builder.append("<td ").append(DATASTYLE).append(">").append(isReferenced).append("</td>");
            }
            builder.append("</tr>");
            row++;
        }
        builder.append("</table>");
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

    public static String stripHtml(String string) {
        String striped = string.replaceAll("<differentiation>[ ]*</differentiation>", "").replaceAll("<H1>[ ]*</H1>", "");
        return striped;
    }
}
