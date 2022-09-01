/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade.
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
package sernet.verinice.model.iso27k;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.common.CnALink;

public final class ProtectionRequirementUtils {

    private static final Logger logger = Logger.getLogger(ProtectionRequirementUtils.class);

    private static final Set<String> PROTECTION_REQUIREMENT_PROVIDER_TYPES = Set.of(Network.TYPE_ID,
            Device.TYPE_ID, Room.TYPE_ID, IcsSystem.TYPE_ID, ItSystem.TYPE_ID, Application.TYPE_ID,
            BusinessProcess.TYPE_ID, SonstIT.TYPE_ID, Gebaeude.TYPE_ID, TelefonKomponente.TYPE_ID,
            Raum.TYPE_ID, Server.TYPE_ID, Anwendung.TYPE_ID, Client.TYPE_ID, Asset.TYPE_ID,
            Process.TYPE_ID);

    private static final Set<String> ALL_KNOWN_LINK_TYPES;
    private static final Set<String> LINK_TYPES_WITH_PRP_DEPENDANT;
    private static final Set<String> LINK_TYPES_WITH_PRP_DEPENDENCY;

    public static final boolean isProtectionRequirementsProvider(String typeId) {
        return PROTECTION_REQUIREMENT_PROVIDER_TYPES.contains(typeId);
    }

    static {
        HashSet<String> allKnownLinkTypes = new HashSet<>();
        HashSet<String> linkTypesWithPRPDependant = new HashSet<>();
        HashSet<String> linkTypesWithPRPDependency = new HashSet<>();
        HUITypeFactory.getInstance().getAllEntityTypes()
                .forEach(entityType -> entityType.getPossibleRelations().forEach(relation -> {
                    allKnownLinkTypes.add(relation.getId());
                    if (PROTECTION_REQUIREMENT_PROVIDER_TYPES.contains(relation.getFrom())) {
                        linkTypesWithPRPDependant.add(relation.getId());
                    }
                    if (PROTECTION_REQUIREMENT_PROVIDER_TYPES.contains(relation.getTo())) {
                        linkTypesWithPRPDependency.add(relation.getId());
                    }
                }));
        ALL_KNOWN_LINK_TYPES = Collections.unmodifiableSet(allKnownLinkTypes);
        LINK_TYPES_WITH_PRP_DEPENDANT = Collections.unmodifiableSet(linkTypesWithPRPDependant);
        LINK_TYPES_WITH_PRP_DEPENDENCY = Collections.unmodifiableSet(linkTypesWithPRPDependency);
    }

    public static boolean dependantIsProtectionRequirementsProvider(CnALink link) {
        String relationId = link.getRelationId();
        if (!(ALL_KNOWN_LINK_TYPES.contains(relationId))) {
            logger.warn("Unknown link type found in " + link);
            return isProtectionRequirementsProvider(link.getDependant().getTypeId());
        }
        return LINK_TYPES_WITH_PRP_DEPENDANT.contains(relationId);
    }

    public static boolean dependencyIsProtectionRequirementsProvider(CnALink link) {
        String relationId = link.getRelationId();
        if (!(ALL_KNOWN_LINK_TYPES.contains(relationId))) {
            logger.warn("Unknown link type found in " + link);
            return isProtectionRequirementsProvider(link.getDependency().getTypeId());
        }
        return LINK_TYPES_WITH_PRP_DEPENDENCY.contains(relationId);
    }

    private ProtectionRequirementUtils() {

    }

}
