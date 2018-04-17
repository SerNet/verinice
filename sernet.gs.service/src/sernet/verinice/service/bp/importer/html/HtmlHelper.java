/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.bp.importer.html;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import ITBP2VNA.generated.module.BibItem;
import ITBP2VNA.generated.module.Document;
import ITBP2VNA.generated.module.Description;
import ITBP2VNA.generated.module.Document.ThreatScenario.SpecificThreats;
import ITBP2VNA.generated.module.Requirement;
import ITBP2VNA.generated.module.SpecificThreat;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.service.bp.importer.Messages;

/**
 * This class provides static helper methods, to create the object-browser
 * content for the BSI-IT-Baseline-Compendium import.
 * 
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public final class HtmlHelper {

    private static final Logger LOG = Logger.getLogger(HtmlHelper.class);

    private static final String HTML_OPEN_TABLE = "<table style=\"border-collapse: collapse;\">"; //$NON-NLS-1$
    private static final String HTML_CLOSE_TABLE = "</table>"; //$NON-NLS-1$
    private static final String HTML_OPEN_UL = "<ul>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_UL = "</ul>"; //$NON-NLS-1$
    private static final String HTML_OPEN_TR = "<tr>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_TR = "</tr>"; //$NON-NLS-1$
    private static final String HTML_OPEN_TD = "<td style=\"border: 1px solid black\">"; //$NON-NLS-1$
    private static final String HTML_CLOSE_TD = "</td>"; //$NON-NLS-1$
    private static final String HTML_SPACE = "&nbsp;"; //$NON-NLS-1$
    private static final String HTML_OPEN_PARAGRAPH = "<p>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_PARAGRAPH = "</p>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_OPEN_PARAGRAPH = "</p><p>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_H1 = "</H1>"; //$NON-NLS-1$
    private static final String HTML_OPEN_H1 = "<H1>"; //$NON-NLS-1$
    private static final String HTML_OPEN_LIST_ITEM = "<li>"; //$NON-NLS-1$
    private static final String HTML_CLOSE_LIST_ITEM = "</li>"; //$NON-NLS-1$
    private static final String HTML_BR = "<br>"; //$NON-NLS-1$

    private static final Set<String> HTML_TAG_BLACKLIST = new HashSet<>();

    static {
        HTML_TAG_BLACKLIST.add("introduction");
        HTML_TAG_BLACKLIST.add("purpose");
        HTML_TAG_BLACKLIST.add("differentiation");
        HTML_TAG_BLACKLIST.add("description");
        HTML_TAG_BLACKLIST.add("");
        HTML_TAG_BLACKLIST.add("#text");
    }

    private HtmlHelper() {
        // default constructor
    }

    /**
     * creates a formatted String like one of the following:
     * 
     * 1 Chaptertitle 1.0 Chaptertitle 1.2.3 Chaptertitle
     * 
     * to not create subchapters, set the int to -1
     * 
     * @param chapter
     * @param subChapter
     * @param subSubChapter
     * @param headline
     * @return
     */
    private static String generateChapterHeader(int chapter, int subChapter, int subSubChapter,
            String headline) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(headline)) {
            sb.append(HTML_OPEN_H1);
        }

        if (chapter >= 1) {
            sb.append(chapter);
            if (subChapter >= 0) {
                sb.append('.');
                sb.append(subChapter);
                if (subSubChapter >= 0) {
                    sb.append('.');
                    sb.append(subSubChapter);
                }
            }
        }

        if (StringUtils.isNotEmpty(headline)) {
            sb.append(HTML_SPACE);
            sb.append(headline);
            sb.append(HTML_CLOSE_H1);
            sb.append(HTML_BR);
        }

        return sb.toString();
    }

    /**
     * The description (shown in the objectbrowser) for a module needs to show
     * all (!) the content which is defined in the related XML-File. It should
     * be the equivalent to the content that is shown in the printed (pdf)
     * version of the BSI-Baseline-Protection Compendium
     * 
     * this method gathers all that information and creates a html-structure
     * around it to format it in a pretty way
     * 
     * @param module
     * @return
     */
    public static String getCompleteModuleXMLText(Document module) {
        StringBuilder descriptionBuilder = new StringBuilder();

        int chapter = 1;
        int subChapter = 0;

        chapter = getModuleIntroduction(module, descriptionBuilder, chapter);

        descriptionBuilder.append(getModuleSpecificThreats(module, chapter));

        chapter++;

        descriptionBuilder.append(getModuleReqIntro(module, chapter));

        subChapter = 1;

        descriptionBuilder.append(getModuleReqMain(module, chapter, subChapter));
        descriptionBuilder.append(
                ToHtmlTableTransformer.createCrossreferenceTable(module.getCrossreferences()));
        descriptionBuilder.append(getModuleDescriptionSuffix(module, chapter));

        return descriptionBuilder.toString();
    }

    /**
     * generates the part of the {@link BpRequirementGroup} (Module)
     * object-browser-property that describes the {@link BpRequirement} further
     * Information (the literature notes) related to the module
     * 
     * @param module
     * @param descriptionBuilder
     * @param chapter
     */
    private static String getModuleDescriptionSuffix(Document module, int chapter) {
        StringBuilder sb = new StringBuilder();
        int subChapter;
        sb.append(HTML_OPEN_PARAGRAPH);

        if (module.getBibliography() != null) {

            chapter++;
            sb.append(generateChapterHeader(chapter, -1, -1, Messages.Further_Information));

            subChapter = 1;
            sb.append(generateChapterHeader(chapter, subChapter, -1, Messages.Literature));
            for (BibItem bibItem : module.getBibliography().getBibItem()) {
                StringBuilder bibBuilder = new StringBuilder();
                bibBuilder.append(HTML_OPEN_UL);
                bibBuilder.append(HTML_OPEN_LIST_ITEM);
                bibBuilder.append(bibItem.getShortHand()).append(" ");
                String descriptionText = bibItem.getDescription();
                descriptionText = descriptionText.replaceAll("<p>", "");
                descriptionText = descriptionText.replaceAll("</p>", "");
                descriptionText = descriptionText.replaceAll("<br />", "");
                bibBuilder.append(descriptionText);
                bibBuilder.append(HTML_CLOSE_LIST_ITEM);
                bibBuilder.append(HTML_CLOSE_UL);
                sb.append(bibBuilder.toString());
            }
        }
        return sb.toString();
    }

    /**
     * generates the part of the {@link BpRequirementGroup} (Module)
     * object-browser-property that describes the {@link BpRequirement} defined
     * within that module
     * 
     * @param module
     * @param descriptionBuilder
     * @param chapter
     * @param subChapter
     */
    private static String getModuleReqMain(Document module, int chapter, int subChapter) {
        StringBuilder sb = new StringBuilder();
        sb.append(generateChapterHeader(chapter, subChapter++, -1, Messages.Basic_Requirements));
        sb.append(HTML_OPEN_PARAGRAPH);
        sb.append(Messages.Basic_Requirements_Intro);
        sb.append(HTML_CLOSE_PARAGRAPH);
        sb.append(getModuleRequirementDescription(
                module.getRequirements().getBasicRequirements().getRequirement()));

        sb.append(HTML_CLOSE_PARAGRAPH);

        sb.append(generateChapterHeader(chapter, subChapter++, -1, Messages.Standard_Requirements));
        sb.append(HTML_OPEN_PARAGRAPH);
        sb.append(Messages.Standard_Requirements_Intro);
        sb.append(HTML_CLOSE_PARAGRAPH);
        sb.append(getModuleRequirementDescription(
                module.getRequirements().getStandardRequirements().getRequirement()));

        sb.append(HTML_CLOSE_OPEN_PARAGRAPH);

        sb.append(generateChapterHeader(chapter, subChapter++, -1, Messages.High_Requirements));

        sb.append(HTML_OPEN_PARAGRAPH);
        sb.append(Messages.High_Requirements_Intro);
        sb.append(HTML_CLOSE_PARAGRAPH);

        sb.append(getModuleRequirementDescription(
                module.getRequirements().getHighLevelRequirements().getRequirement()));

        sb.append(HTML_CLOSE_PARAGRAPH);
        return sb.toString();
    }

    /**
     * get Introduction of Requirements-Description of description of
     * {@link BpRequirementGroup} (Module)
     * 
     * @param module
     * @param descriptionBuilder
     * @param chapter
     */
    private static String getModuleReqIntro(Document module, int chapter) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN_PARAGRAPH);

        sb.append(generateChapterHeader(chapter, -1, -1, Messages.Requirements));

        sb.append(module.getRequirements().getDescription());

        sb.append(HTML_OPEN_TABLE);
        sb.append(HTML_OPEN_TR);
        sb.append(HTML_OPEN_TD);
        sb.append(Messages.Main_Responsible);
        sb.append(HTML_CLOSE_TD);
        sb.append(HTML_OPEN_TD);
        sb.append(module.getRequirements().getMainResponsibleRole());
        sb.append(HTML_CLOSE_TD);
        sb.append(HTML_CLOSE_TR);
        sb.append(HTML_OPEN_TR);
        sb.append(HTML_OPEN_TD);
        sb.append(Messages.Further_Responsibles);
        sb.append(HTML_CLOSE_TD);
        sb.append(HTML_OPEN_TD);

        List<String> roles = Collections.emptyList();

        if (module.getRequirements().getFurtherResponsibleRoles() != null) {
            roles = module.getRequirements().getFurtherResponsibleRoles().getRole();
        }

        Iterator<String> iter = roles.iterator();
        while (iter.hasNext()) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(HTML_CLOSE_TD);
        sb.append(HTML_CLOSE_TR);
        sb.append(HTML_CLOSE_TABLE);
        sb.append(HTML_CLOSE_PARAGRAPH);

        return sb.toString();
    }

    /**
     * 
     * creates the specific-threats describing part of the
     * {@link BpRequirementGroup} (Module) description
     * 
     * @param module
     * @param descriptionBuilder
     * @param chapter
     */
    private static String getModuleSpecificThreats(Document module, int chapter) {
        int subChapter;
        StringBuilder sb = new StringBuilder();
        sb.append(generateChapterHeader(chapter, -1, -1, Messages.Threat_Situation));

        Description description = module.getThreatScenario().getDescription();
        String descriptionText = getAnyElementDescription("", -1, -1, -1, description.getAny());
        sb.append(descriptionText);

        SpecificThreats specificThreats = module.getThreatScenario().getSpecificThreats();

        subChapter = 1;
        for (SpecificThreat specificThreat : specificThreats.getSpecificThreat()) {
            sb.append(HTML_OPEN_PARAGRAPH);
            sb.append(
                    generateChapterHeader(chapter, subChapter++, -1, specificThreat.getHeadline()));
            String threatDescriptionText = getAnyElementDescription("",
                                       -1, -1 , -1, specificThreat.getDescription().getAny());
            sb.append(threatDescriptionText);
            sb.append(HTML_CLOSE_PARAGRAPH);
        }
        return sb.toString();
    }

    /**
     * creates the intro of a {@link BpRequirementGroup} (Module) description
     * 
     * @param module
     * @param descriptionBuilder
     * @param chapter
     * @return
     */
    private static int getModuleIntroduction(Document module, StringBuilder descriptionBuilder,
            int chapter) {
        descriptionBuilder.append(HTML_OPEN_H1);
        descriptionBuilder.append(module.getFullTitle());
        descriptionBuilder.append(HTML_CLOSE_H1);

        descriptionBuilder.append(generateChapterHeader(chapter, -1, -1, Messages.Description));
        descriptionBuilder.append(getModuleDescriptionStart(module, chapter++));
        return chapter;
    }

    /**
     * returns the description (three parts (Introduction, Purpose,
     * Differentiation)) of a module {@link BpRequirementGroup}
     * 
     * @param module
     * @param descriptionBuilder
     */
    private static String getModuleDescriptionStart(Document module, int chapter) {
        StringBuilder sb = new StringBuilder();

        List<Object> introduction = module.getDescription().getIntroduction();

        int subChapter = 1;

        for (Object o : introduction) {
            if (!(o instanceof Element)) {
                continue;
            }
            Element node = (Element) o;
            if ("introduction".equals(node.getNodeName())) {
                sb.append(generateChapterHeader(chapter, subChapter++, -1, Messages.Introduction));
                appendNodeTextEnsureParagraph(sb, node);
            } else if ("purpose".equals(node.getNodeName())) {
                sb.append(generateChapterHeader(chapter, subChapter++, -1, Messages.Purpose));
                appendNodeTextEnsureParagraph(sb, node);
            } else if ("differentiation".equals(node.getNodeName())) {
                sb.append(
                        generateChapterHeader(chapter, subChapter++, -1, Messages.Differentiation));
                appendNodeTextEnsureParagraph(sb, node);

            }
        }
        return sb.toString();
    }

    private static void appendNodeTextEnsureParagraph(StringBuilder sb, Element node) {
        String nodeText = node.getTextContent();
        boolean startsWithOpenParagraph = nodeText.trim().startsWith(HTML_OPEN_PARAGRAPH);
        if (!startsWithOpenParagraph) {
            sb.append(HTML_OPEN_PARAGRAPH);
        }
        sb.append(node.getTextContent());
        if (!startsWithOpenParagraph) {
            sb.append(HTML_CLOSE_PARAGRAPH);
        }
    }

    /**
     * returns the description of a {@link BpRequirement} HTML-Formatted
     * 
     * @param requirements
     * @return
     */
    private static String getModuleRequirementDescription(List<Requirement> requirements) {
        StringBuilder sb = new StringBuilder();

        for (Requirement requirement : requirements) {
            sb.append(HTML_OPEN_H1);
            sb.append(getRequirementDescriptionStart(requirement));
            sb.append(" ");
            sb.append(getRequirementResponsibleDescription(requirement));
            sb.append(" ");
            sb.append(getRequirementCIA(requirement));
            sb.append(HTML_CLOSE_H1);
            sb.append(HTML_OPEN_PARAGRAPH);
            sb.append(getAnyElementDescription("", -1, -1, -1,
                    requirement.getDescription().getAny()));
            sb.append(HTML_CLOSE_PARAGRAPH);
        }

        return sb.toString();
    }

    /**
     * returns the description-prefix of a {@link BpRequirement} HTML-Formatted
     * 
     * @param requirement
     */
    private static String getRequirementDescriptionStart(Requirement requirement) {
        StringBuilder sb = new StringBuilder();
        sb.append(requirement.getIdentifier());
        sb.append(" ");
        sb.append(requirement.getTitle());
        return sb.toString();
    }

    /**
     * returns a string that is the suffix to a {@link BpRequirement} title,
     * that contains all responsible roles
     * 
     * @param requirement
     */
    private static String getRequirementResponsibleDescription(Requirement requirement) {
        StringBuilder sb = new StringBuilder();

        if (requirement.getResponsibleRoles() != null
                && !requirement.getResponsibleRoles().getRole().isEmpty()) {
            sb.append("[");
            Iterator<String> iter = requirement.getResponsibleRoles().getRole().iterator();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * returns a string that is the suffix to a {@link BpRequirement} title,
     * that contains which protection categories are affected by this
     * requirement
     * 
     * C for Confidentiality I for Integrity A for Availabiltiy
     * 
     * @param requirement
     */
    private static String getRequirementCIA(Requirement requirement) {
        StringBuilder sb = new StringBuilder();

        String confidentiality = (Boolean.parseBoolean(requirement.getCia().getConfidentiality()))
                ? "C"
                : "";
        String integrity = (Boolean.parseBoolean(requirement.getCia().getIntegrity())) ? "I" : "";
        String availitbility = (Boolean.parseBoolean(requirement.getCia().getAvailability())) ? "A"
                : "";

        String cia = confidentiality + integrity + availitbility;

        if (StringUtils.isNotEmpty(cia)) {
            cia = "(" + cia + ")";
            sb.append(cia);
        }

        return sb.toString();
    }

    /**
     * transforms mixed HTML/XML-Content (given by a {@link List} of
     * {@link Element} to a html-formatted String
     * 
     * @param title
     * @param anyElements
     * @return
     */
    public static String getAnyElementDescription(String title, int chapter, int subChapter,
            int subSubChapter, List<Element> anyElements) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(title)) {
            sb.append(generateChapterHeader(chapter, subChapter, subSubChapter, title));
            sb.append(HTML_OPEN_PARAGRAPH);
        }
        for (Object element : anyElements) {
            sb.append(extractContentFromObject(element));
        }

        if (StringUtils.isNotEmpty(title)) {
            sb.append(HTML_CLOSE_PARAGRAPH);
        }

        return sb.toString();
    }

    /**
     * searches for a text-element in a tree given by an {@link Element} and
     * returns it
     * 
     * @param sb
     * @param element
     */
    private static String extractContentFromObject(Object element) {
        StringBuilder sb = new StringBuilder();
        if (element instanceof Element) {
            sb.append(unwrapText((Element) element));
        } else if (element instanceof String) {
            sb.append((String) element);
        }
        return sb.toString();
    }

    /**
     * transforms mixed HTML/XML-Content (given by a {@link List} of
     * {@link Object} to a html-formatted String
     * 
     * @param title
     * @param headlineLevel
     * @param anyObjects
     * @return
     */
    public static String getAnyObjectDescription(String title, int headlineLevel,
            List<Object> anyObjects) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(title)) {
            if (headlineLevel > 0) {
                sb.append("<H");
                sb.append(String.valueOf(headlineLevel));
                sb.append(">").append(title).append("</H");
                sb.append(String.valueOf(headlineLevel));
                sb.append(">");
            } else {
                sb.append(HTML_BR).append(title).append(HTML_BR);
            }
        }
        if (anyObjects != null) {
            sb.append(HTML_OPEN_PARAGRAPH);
            for (Object o : anyObjects) {
                sb.append(extractContentFromObject(o));
            }
            sb.append(HTML_CLOSE_PARAGRAPH);
        } else {
            LOG.debug("No Description found for :\t" + title);
        }

        return sb.toString();
    }

    /**
     * creates a html-formatted text (including nested tags) that is given by a
     * {@link Node}
     * 
     * blacklists known BSI-XML-Structure defining strings to not appear as
     * html-tags in the output
     * 
     * @param element
     * @return
     */
    private static String unwrapText(Node element) {
        StringBuilder sb = new StringBuilder();
        ArrayDeque<String> htmlElements = new ArrayDeque<>();
        Node node = element;
        String htmlFormatElement = StringUtils.isNotEmpty(node.getNodeName()) ? node.getNodeName()
                : "";
        if (!HTML_TAG_BLACKLIST.contains(htmlFormatElement)) {
            sb.append("<").append(htmlFormatElement).append(">");
            htmlElements.push(htmlFormatElement);
        }
        if (node instanceof Text) {
            sb.append(node.getNodeValue());
        } else {
            for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                sb.append(unwrapText(element.getChildNodes().item(i)));
            }
        }
        while (!htmlElements.isEmpty()) {
            sb.append("</").append(htmlElements.pop()).append(">");
        }
        return sb.toString();
    }

}
