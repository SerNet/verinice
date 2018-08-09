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

import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Domain;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.service.commands.CnATypeMapper;

/**
 * Utility class to retrieve an element's scope via its model
 */
public final class CnATreeElementScopeUtils {

    private static final CnAElementFactory cnAElementFactory = CnAElementFactory.getInstance();

    public static CnATreeElement getScope(CnATreeElement element) {
        Integer scopeElementId = element.getScopeId();
        if (element.getDbId() == scopeElementId) {
            return element;
        }
        Domain elementDomain = CnATypeMapper.getDomainFromTypeId(element.getTypeId());
        Class<?> importGroupClass = getImportGroupClassForDomain(elementDomain);
        CnATreeElement modelForDomain = getModelForDomain(elementDomain);

        Stream<CnATreeElement> potentialScopes = modelForDomain.getChildren().stream()
                .flatMap(child -> {
                    if (importGroupClass.isInstance(child)) {
                        return child.getChildren().stream();
                    } else {
                        return Stream.of(child);
                    }
                });

        potentialScopes = Stream.concat(potentialScopes,
                cnAElementFactory.getCatalogModel().getChildren().stream());
        return potentialScopes.filter(scope -> scope.getDbId().equals(scopeElementId)).findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Unable to find scope for " + element));
    }

    private static Class<?> getImportGroupClassForDomain(Domain domain) {
        switch (domain) {
        case BASE_PROTECTION:
            return ImportBpGroup.class;
        case BASE_PROTECTION_OLD:
            return ImportBsiGroup.class;
        case ISM:
            return ImportIsoGroup.class;
        default:
            throw new IllegalArgumentException("Unsupported domain " + domain);
        }
    }

    private static CnATreeElement getModelForDomain(Domain domain) {
        switch (domain) {
        case BASE_PROTECTION:
            return cnAElementFactory.getBpModel();
        case BASE_PROTECTION_OLD:
            return CnAElementFactory.getLoadedModel();
        case ISM:
            return cnAElementFactory.getISO27kModel();

        default:
            throw new IllegalArgumentException("Unsupported domain " + domain);
        }
    }

    private CnATreeElementScopeUtils() {

    }
}
