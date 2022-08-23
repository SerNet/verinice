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
package sernet.verinice.bp.rcp.bcm;

import java.util.Set;

import org.eclipse.jface.fieldassist.ControlDecoration;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.verinice.model.bp.BCMUtils;
import sernet.verinice.model.bp.BCMUtils.BCMProperties;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.common.CnATreeElement;

class ZeitkritischUIUpdater implements IEntityChangedListener {
    private final CnATreeElement element;
    private final Set<String> relevantProperties;
    private final ControlDecoration decoration;

    ZeitkritischUIUpdater(CnATreeElement element, BCMProperties bcmProperties,
            ControlDecoration decoration) {
        this.element = element;
        this.decoration = decoration;
        relevantProperties = Set.of(bcmProperties.propertyImpact24h, bcmProperties.propertyImpact3d,
                bcmProperties.propertyImpact7d, bcmProperties.propertyImpact14d,
                bcmProperties.propertyImpact30d, BusinessProcess.PROP_DEDUCE_PROCESS_ZEITKRITISCH);
    }

    @Override
    public void propertyChanged(PropertyChangedEvent event) {
        if (relevantProperties.contains(event.getProperty().getPropertyType())) {
            performUIUpdate();
        }
    }

    void performUIUpdate() {
        boolean deduceZeitkritisch = element.getEntity()
                .isFlagged(BusinessProcess.PROP_DEDUCE_PROCESS_ZEITKRITISCH);
        if (deduceZeitkritisch) {
            CnATreeElement scope = Retriever.retrieveElement(element.getScopeId(),
                    RetrieveInfo.getPropertyInstance());
            String damagePotentialValueRaw = scope.getEntity()
                    .getRawPropertyValue(ItNetwork.PROP_UNTRAGBARKEITSNIVEAU);
            if (damagePotentialValueRaw == null || damagePotentialValueRaw.isEmpty()
                    || damagePotentialValueRaw
                            .equals(BCMUtils.DAMAGE_POTENTIAL_VALUE_UNEDITED_RAW)) {
                decoration.setDescriptionText(Messages.untragbarkeitsniveauMissing);
                decoration.show();
            } else {
                boolean valueFound = BCMUtils.updateProcessZeitkritisch(element,
                        damagePotentialValueRaw);
                if (!valueFound) {
                    decoration.setDescriptionText(Messages.damageAssessmentIncomplete);
                    decoration.show();
                } else {
                    decoration.hide();
                }
            }
        } else {
            decoration.hide();
        }
    }

    @Override
    public void selectionChanged(IMLPropertyType type, IMLPropertyOption opt) {
        // not relevant
    }
}