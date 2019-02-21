/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.service.commands.bp.converter;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnALink.Id;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Converts links between elements and reference-type properties
 */
public final class LinkConverter {

    private static final Logger logger = Logger.getLogger(LinkConverter.class);

    private static final String LINK_COMMENT_CHECK_TYPE = "[PRÜFEN:Verknüpfungstyp]";

    private static final Map<String, LinkConversionInfo> LINK_MAPPINGS = Stream
            .of(entry2("client_itverbund", "rel_bp_itnetwork_bp_itsystem", true),
                    entry2("client_netzkomponente", "rel_bp_itsystem_bp_network"),
                    entry2("client_raum_located", "rel_bp_itsystem_bp_room"),
                    entry2("gebaeude_itverbund", "rel_bp_itnetwork_bp_room", true),
                    entry2("netzkomponente_raum_located", "rel_bp_network_bp_room"),
                    entry2("netzkomponente_itverbund", "rel_bp_itnetwork_bp_network"),
                    entry2("raum_gebaeude", "rel_bp_room_bp_room"),
                    entry2("raum_itverbund", "rel_bp_itnetwork_bp_room", true),
                    entry2("rel_person_raum", "rel_bp_person_bp_room"),
                    entry2("rel_person_server", "rel_bp_person_bp_itsystem"),
                    entry2("rel_person_tkkomponente", "rel_bp_person_bp_itsystem"),
                    entry2("rel_person_netzkomponente", "rel_bp_person_bp_network"),
                    entry2("rel_person_itverbund", "rel_bp_itnetwork_bp_person", true),
                    entry2("rel_person_client", "rel_bp_person_bp_itsystem"),
                    entry2("rel_person_sonstit", "rel_bp_person_bp_itsystem"),
                    entry2("rel_person_gebaeude", "rel_bp_person_bp_room"),
                    entry2("server_itverbund", "rel_bp_itnetwork_bp_itsystem", true),
                    entry2("server_netzkomponente", "rel_bp_itsystem_bp_network"),
                    entry2("server_raum_located", "rel_bp_itsystem_bp_room"),
                    entry2("server_server_dep", "rel_bp_itsystem_bp_itsystem"),
                    entry2("server_server_vm", "rel_bp_itsystem_bp_itsystem_virtualized"),
                    entry2("sonstit_itverbund", "rel_bp_itnetwork_bp_itsystem", true),
                    entry2("sonstit_netzkomponente", "rel_bp_itsystem_bp_network"),
                    entry2("sonstit_raum_located", "rel_bp_itsystem_bp_room"),
                    entry2("tkkomponente_itverbund", "rel_bp_itnetwork_bp_itsystem", true),
                    entry2("tkkomponente_raum_located", "rel_bp_itsystem_bp_room"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, LinkConversionInfo> LINK_MAPPINGS_ANWENDUNG_APPLICATION = Stream
            .of(entry2("anwendung_client", "rel_bp_application_bp_itsystem"),
                    entry2("anwendung_itverbund", "rel_bp_itnetwork_bp_application", true),
                    entry2("anwendung_netzkomponente", "rel_bp_application_bp_network"),
                    entry2("anwendung_person_accountable",
                            "rel_bp_application_bp_person_accountable"),
                    entry2("anwendung_person_consulted", "rel_bp_application_bp_person_consulted"),
                    entry2("anwendung_person_informed", "rel_bp_application_bp_person_informed"),
                    entry2("anwendung_person_responsible", "rel_bp_person_bp_application", true),
                    entry2("anwendung_server", "rel_bp_application_bp_itsystem"),
                    entry2("anwendung_sonstit", "rel_bp_application_bp_itsystem"),
                    entry2("anwendung_tkkomponente", "rel_bp_application_bp_itsystem"),
                    entry2("rel_person_anwendung", "rel_bp_person_bp_application"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, LinkConversionInfo> LINK_MAPPINGS_ANWENDUNG_BUSINESS_PROCESS = Stream
            .of(entry2("anwendung_client", "rel_bp_businessprocess_bp_itsystem"),
                    entry2("anwendung_itverbund", "rel_bp_itnetwork_bp_businessproces", true),
                    entry2("anwendung_netzkomponente", "rel_bp_businessprocess_bp_network"),
                    entry2("anwendung_person_accountable",
                            "rel_bp_businessprocess_bp_person_accountable"),
                    entry2("anwendung_person_consulted",
                            "rel_bp_businessprocess_bp_person_consulted"),
                    entry2("anwendung_person_informed",
                            "rel_bp_businessprocess_bp_person_informed"),
                    entry2("anwendung_person_responsible", "rel_bp_person_bp_businessprocess",
                            true),
                    entry2("anwendung_server", "rel_bp_businessprocess_bp_itsystem"),
                    entry2("anwendung_sonstit", "rel_bp_businessprocess_bp_itsystem"),
                    entry2("anwendung_tkkomponente", "rel_bp_businessprocess_bp_itsystem"),
                    entry2("rel_person_anwendung", "rel_bp_person_bp_businessprocess"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> REFERENCE_PROPERTY_LINK_MAPPINGS = Stream
            .of(entry("client_anwender_link", "rel_bp_person_bp_itsystem_user"),
                    entry("client_admin_link", "rel_bp_person_bp_itsystem_administrator"),
                    entry("server_anwender_link", "rel_bp_person_bp_itsystem_user"),
                    entry("server_admin_link", "rel_bp_person_bp_itsystem_administrator"),
                    entry("tkkomponente_anwender_link", "rel_bp_person_bp_itsystem_user"),
                    entry("tkkomponente_admin_link", "rel_bp_person_bp_itsystem_administrator"),
                    entry("sonstit_anwender_link", "rel_bp_person_bp_itsystem_user"),
                    entry("sonstit_admin_link", "rel_bp_person_bp_itsystem_administrator"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> REFERENCE_PROPERTY_LINK_MAPPINGS_ANWENDUNG_APPLICATION = Stream
            .of(entry("anwendung_benutzer_link", "rel_bp_person_bp_application_user"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    private static final Map<String, String> REFERENCE_PROPERTY_LINK_MAPPINGS_ANWENDUNG_BUSINESS_PROCESS = Stream
            .of(entry("anwendung_benutzer_link", "rel_bp_person_bp_businessprocess_user"),
                    entry("anwendung_eigentümer_link", "rel_bp_person_bp_businessprocess_owner"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    public static void convertLinks(Map<CnATreeElement, CnATreeElement> convertedElements,
            @NonNull IBaseDao<@NonNull CnALink, Serializable> cnaLinkDao) {
        for (Entry<CnATreeElement, CnATreeElement> e : convertedElements.entrySet()) {
            CnATreeElement originalSource = e.getKey();
            CnATreeElement convertedElement = e.getValue();
            convertLinks(originalSource, convertedElement, convertedElements, cnaLinkDao);
            convertReferencePropertiesToLinks(originalSource, convertedElement, convertedElements,
                    cnaLinkDao);
        }
    }

    private static void convertLinks(CnATreeElement originalSource, CnATreeElement convertedElement,
            Map<CnATreeElement, CnATreeElement> convertedElements,
            IBaseDao<@NonNull CnALink, Serializable> cnaLinkDao) {
        if (logger.isInfoEnabled()) {
            logger.info("Converting links for " + originalSource);
        }
        Set<CnALink> linksDown = originalSource.getLinksDown();
        Set<CnALink.Id> createdLinksForConvertedElement = new HashSet<>(linksDown.size());
        for (CnALink link : linksDown) {
            String originalLinkType = link.getRelationId();
            CnATreeElement originalDependency = link.getDependency();
            CnATreeElement newDependant = convertedElement;
            CnATreeElement newDependency = convertedElements.get(originalDependency);
            if (newDependency == null) {
                logger.warn("Not converting " + link + ", dependency element was not converted");
                continue;
            }
            String linkComment = link.getComment();
            LinkConversionInfo linkConversionInfo = null;
            if (originalLinkType.isEmpty()) {
                linkConversionInfo = findMatchingLinkTypeInSNCA(convertedElement.getTypeId(),
                        newDependency.getTypeId());
                if (linkConversionInfo != null) {
                    Level logLevel = Level.INFO;

                    if (linkConversionInfo.multiplePossibleRelations) {
                        logLevel = Level.WARN;
                        if (StringUtils.isEmpty(linkComment)) {
                            linkComment = LINK_COMMENT_CHECK_TYPE;
                        } else {
                            linkComment = String.join(" ", LINK_COMMENT_CHECK_TYPE, linkComment);
                        }
                    }
                    if (logger.isEnabledFor(logLevel)) {
                        logger.log(logLevel,
                                "Using " + linkConversionInfo.relationId
                                        + " for generic original link " + link + " ("
                                        + originalSource.getTypeId() + "->"
                                        + originalDependency.getTypeId() + ")");
                    }
                }
            } else {
                linkConversionInfo = findNewLinkType(convertedElement, newDependency,
                        originalLinkType);
            }
            if (linkConversionInfo != null) {
                String newLinkType = linkConversionInfo.relationId;
                if (linkConversionInfo.definedInOtherDirection) {
                    newDependant = newDependency;
                    newDependency = convertedElement;
                }

                if (isRelationValid(newDependant, newDependency, newLinkType)) {
                    Id linkId = new CnALink.Id(newDependant.getDbId(), newDependency.getDbId(),
                            newLinkType);
                    if (!createdLinksForConvertedElement.contains(linkId)) {
                        CnALink newLink = new CnALink(newDependant, newDependency, newLinkType,
                                linkComment);
                        cnaLinkDao.saveOrUpdate(newLink);
                        createdLinksForConvertedElement.add(linkId);
                        if (logger.isInfoEnabled()) {
                            logger.info("Created link " + newLink);
                        }
                    } else {
                        logger.warn("Skipping duplicate link " + link);
                    }
                } else {
                    logger.warn("Not converting link " + link + ", invalid link type " + newLinkType
                            + " for relation from " + newDependant + " to " + newDependency);
                }

            } else {
                logger.warn("Not converting " + link + ", no link type ID found");
            }
        }
    }

    private static LinkConversionInfo findNewLinkType(CnATreeElement newSource,
            CnATreeElement newDestination, String originalLinkType) {
        // special handling for Anwendung
        if (newSource instanceof Application || newDestination instanceof Application) {
            return LINK_MAPPINGS_ANWENDUNG_APPLICATION.get(originalLinkType);
        } else if (newSource instanceof BusinessProcess
                || newDestination instanceof BusinessProcess) {
            return LINK_MAPPINGS_ANWENDUNG_BUSINESS_PROCESS.get(originalLinkType);
        }
        return LINK_MAPPINGS.get(originalLinkType);
    }

    private static LinkConversionInfo findMatchingLinkTypeInSNCA(String sourceId, String targetId) {
        Set<HuiRelation> possibleRelations = HitroUtil.getInstance().getTypeFactory()
                .getPossibleRelations(sourceId, targetId);
        boolean inverse = false;
        if (possibleRelations.isEmpty()) {
            inverse = true;
            possibleRelations = HitroUtil.getInstance().getTypeFactory()
                    .getPossibleRelations(targetId, sourceId);
        }
        if (!possibleRelations.isEmpty()) {
            Iterator<HuiRelation> it = possibleRelations.iterator();
            HuiRelation relation = it.next();
            if ("rel_bp_itsystem_bp_itsystem_virtualized".equals(relation.getId())
                    && it.hasNext()) {
                relation = it.next();
            }

            if (inverse) {
                logger.warn("Found inverse direction" + relation + " trying to find a match for "
                        + sourceId + "->" + targetId);
            }
            return new LinkConversionInfo(relation.getId(), inverse, possibleRelations.size() > 1);
        }
        return null;
    }

    private static void convertReferencePropertiesToLinks(CnATreeElement originalElement,
            CnATreeElement convertedElement, Map<CnATreeElement, CnATreeElement> convertedElements,
            @NonNull IBaseDao<@NonNull CnALink, Serializable> cnaLinkDao) {
        // special handling for Anwendung
        if (convertedElement instanceof Application) {
            convertReferencePropertiesToLinks(originalElement, convertedElement, convertedElements,
                    REFERENCE_PROPERTY_LINK_MAPPINGS_ANWENDUNG_APPLICATION, cnaLinkDao);
        } else if (convertedElement instanceof BusinessProcess) {
            convertReferencePropertiesToLinks(originalElement, convertedElement, convertedElements,
                    REFERENCE_PROPERTY_LINK_MAPPINGS_ANWENDUNG_BUSINESS_PROCESS, cnaLinkDao);
        } else {
            convertReferencePropertiesToLinks(originalElement, convertedElement, convertedElements,
                    REFERENCE_PROPERTY_LINK_MAPPINGS, cnaLinkDao);
        }
    }

    private static void convertReferencePropertiesToLinks(CnATreeElement originalElement,
            CnATreeElement convertedElement, Map<CnATreeElement, CnATreeElement> convertedElements,
            Map<String, String> referencePropertyLinkMappings,
            @NonNull IBaseDao<@NonNull CnALink, Serializable> cnaLinkDao) {
        for (Entry<String, String> entry : referencePropertyLinkMappings.entrySet()) {
            String referencePropertyId = entry.getKey();

            PropertyList values = originalElement.getEntity().getTypedPropertyLists()
                    .get(referencePropertyId);
            if (values == null || values.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No value found for " + referencePropertyId);
                }
                continue;
            }
            for (Property property : values.getProperties()) {
                String sourceValue = property.getPropertyValue();
                if (logger.isDebugEnabled()) {
                    logger.debug("Found reference: " + referencePropertyId + " to " + sourceValue);
                }
                Integer sourceDbId = Integer.parseInt(sourceValue);
                Optional<CnATreeElement> sourceElement = convertedElements.keySet().stream()
                        .filter(item -> sourceDbId.equals(item.getDbId())).findFirst();
                sourceElement.ifPresent(value -> {
                    // the links are defined from the person object to the
                    // target object, so the converted element becomes the link
                    // *dependency*
                    CnATreeElement dependant = convertedElements.get(value);
                    CnATreeElement dependency = convertedElement;
                    String linkType = entry.getValue();
                    if (isRelationValid(dependant, dependency, linkType)) {
                        CnALink link = new CnALink(dependant, dependency, linkType, "");
                        cnaLinkDao.saveOrUpdate(link);
                        if (logger.isInfoEnabled()) {
                            logger.info("Created link " + link);
                        }
                    } else {
                        logger.warn("Not converting reference " + referencePropertyId + " from "
                                + originalElement + ", invalid link type " + linkType
                                + " for relation from " + dependant + " to " + dependency);
                    }
                });
            }
        }
    }

    private static boolean isRelationValid(CnATreeElement sourceElement,
            CnATreeElement destinationElement, String relationType) {
        for (HuiRelation relation : HUITypeFactory.getInstance().getPossibleRelations(
                sourceElement.getEntityType().getId(),
                destinationElement.getEntityType().getId())) {
            if (relationType.equals(relation.getId())) {
                return true;
            }
        }
        return false;
    }

    private static Entry<String, String> entry(String key, String value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    private static Entry<String, LinkConversionInfo> entry2(String key, String value) {
        return entry2(key, value, false);
    }

    private static Entry<String, LinkConversionInfo> entry2(String key, String value,
            boolean invert) {
        return new AbstractMap.SimpleImmutableEntry<>(key,
                new LinkConversionInfo(value, invert, false));
    }

    private LinkConverter() {

    }

    private static class LinkConversionInfo {

        private LinkConversionInfo(String relationId, boolean definedInOtherDirection,
                boolean multiplePossibleRelations) {
            this.relationId = relationId;
            this.definedInOtherDirection = definedInOtherDirection;
            this.multiplePossibleRelations = multiplePossibleRelations;
        }

        private final String relationId;
        private final boolean definedInOtherDirection;
        private final boolean multiplePossibleRelations;
    }
}
