/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan
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
package sernet.verinice.model.bp;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;

public final class SecurityLevelUtil {

    private static final List<SecurityLevel> levels = Arrays.asList(SecurityLevel.values());

    /**
     * Retrieve all child elements of type
     * {@Link IImplementableSecurityLevelProvider} from parent element.
     */
    public static List<IImplementableSecurityLevelProvider> findProviders(CnATreeElement parent) {
        return Retriever
                .retrieveElement(parent,
                        new RetrieveInfo().setChildren(true).setChildrenProperties(true))
                .getChildren().stream()
                .filter(e -> e instanceof IImplementableSecurityLevelProvider)
                .map(r -> (IImplementableSecurityLevelProvider) r).collect(Collectors.toList());
    }

    /** Determines if a security level is yet to be implemented. */
    public static boolean getImplementationPending(ImplementationStatus status) {
        return status != ImplementationStatus.YES && status != ImplementationStatus.NOT_APPLICABLE;
    }

    /**
     * Determines the security level that is implemented within a list of
     * providers.
     * 
     * @param providers
     *            A list of security level providers.
     * @return The highest level that is fully implemented by given providers.
     */
    public static SecurityLevel getImplementedSecurityLevel(
            List<IImplementableSecurityLevelProvider> providers) {
        SecurityLevel implementedLevel = null;

        for (SecurityLevel level : levels) {
            List<IImplementableSecurityLevelProvider> levelproviders = providers.stream()
                    .filter(r -> r.getSecurityLevel() == level).collect(Collectors.toList());

            if (!levelproviders.isEmpty()) {
                if (levelproviders.stream().anyMatch(r -> r.getImplementationPending())) {
                    // Some providers are pending -> level not implemented.
                    break;
                }
                // Consider this level implemented.
                implementedLevel = level;
            }
        }

        return implementedLevel;
    }
}
