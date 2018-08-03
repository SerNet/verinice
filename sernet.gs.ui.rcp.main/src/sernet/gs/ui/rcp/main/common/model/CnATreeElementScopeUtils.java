/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
package sernet.gs.ui.rcp.main.common.model;

import java.util.stream.Stream;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Domain;
import sernet.verinice.service.commands.CnATypeMapper;

/**
 * Utility class to retrieve an element's scope via its model
 */
public final class CnATreeElementScopeUtils {

    public static CnATreeElement getScope(CnATreeElement element) {
        Domain elementDomain = CnATypeMapper.getDomainFromTypeId(element.getTypeId());
        Integer scopeElementId = element.getScopeId();

        CnAElementFactory cnAElementFactory = CnAElementFactory.getInstance();
        Stream<CnATreeElement> potentialScopes;
        switch (elementDomain) {
        case BASE_PROTECTION:
            potentialScopes = cnAElementFactory.getBpModel().getChildren().stream();
            break;
        case BASE_PROTECTION_OLD:
            potentialScopes = CnAElementFactory.getLoadedModel().getChildren().stream();
            break;
        case ISM:
            potentialScopes = cnAElementFactory.getISO27kModel().getChildren().stream();
            break;

        default:
            throw new IllegalArgumentException("Unsupported domain " + elementDomain);
        }
        potentialScopes = Stream.concat(potentialScopes,
                cnAElementFactory.getCatalogModel().getChildren().stream());
        return potentialScopes.filter(scope -> scope.getDbId().equals(scopeElementId)).findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Unable to find scope for " + element));
    }

    private CnATreeElementScopeUtils() {

    }
}
