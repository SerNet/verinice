/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp;

import java.text.Collator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sernet.gs.service.NumericStringCollator;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpDocument;
import sernet.verinice.model.bp.elements.BpIncident;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.BpRecord;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpDocumentGroup;
import sernet.verinice.model.bp.groups.BpIncidentGroup;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bp.groups.BpRecordGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.BusinessProcessGroup;
import sernet.verinice.model.bp.groups.DeviceGroup;
import sernet.verinice.model.bp.groups.IcsSystemGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.NetworkGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This tree sorter sorts groups in IT networks regardless of
 * of there names. Instead of sorting by name a static order
 * based on the type is used. See Map typeSortCategoryMap
 * for the order. Elements with a smaller category id are
 * preceded by elements with a higher category id.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class BaseProtectionTreeSorter extends ViewerSorter {
    private static Map<String, Integer> typeSortCategoryMap = new HashMap<>();

    //TODO: when the qualifier will be a number (an option) we do not need this anymore
    private static final String HIGH = "high";
    private static final String STANDARD = "standard";
    private static final String BASIC = "basic";

    static {
        // Sort order of groups in IT network
        typeSortCategoryMap.put(BusinessProcessGroup.TYPE_ID,10);
        typeSortCategoryMap.put(BusinessProcess.TYPE_ID,10);
        typeSortCategoryMap.put(ApplicationGroup.TYPE_ID,20);
        typeSortCategoryMap.put(Application.TYPE_ID,20);
        typeSortCategoryMap.put(ItSystemGroup.TYPE_ID,30);
        typeSortCategoryMap.put(ItSystem.TYPE_ID,30);
        typeSortCategoryMap.put(IcsSystemGroup.TYPE_ID,40);
        typeSortCategoryMap.put(IcsSystem.TYPE_ID,40);
        typeSortCategoryMap.put(DeviceGroup.TYPE_ID,50);
        typeSortCategoryMap.put(Device.TYPE_ID,50);
        typeSortCategoryMap.put(NetworkGroup.TYPE_ID,60);
        typeSortCategoryMap.put(Network.TYPE_ID,60);
        typeSortCategoryMap.put(RoomGroup.TYPE_ID,70);
        typeSortCategoryMap.put(Room.TYPE_ID,70);
        typeSortCategoryMap.put(BpPersonGroup.TYPE_ID,80);
        typeSortCategoryMap.put(BpPerson.TYPE_ID,80);
        typeSortCategoryMap.put(BpRequirementGroup.TYPE_ID,90);
        typeSortCategoryMap.put(BpRequirement.TYPE_ID,90);
        typeSortCategoryMap.put(BpThreatGroup.TYPE_ID,100);
        typeSortCategoryMap.put(BpThreat.TYPE_ID,100);
        typeSortCategoryMap.put(SafeguardGroup.TYPE_ID,110);
        typeSortCategoryMap.put(Safeguard.TYPE_ID,110);
        typeSortCategoryMap.put(BpDocumentGroup.TYPE_ID,120);
        typeSortCategoryMap.put(BpDocument.TYPE_ID,120);
        typeSortCategoryMap.put(BpIncidentGroup.TYPE_ID,130);
        typeSortCategoryMap.put(BpIncident.TYPE_ID,130);
        typeSortCategoryMap.put(BpRecordGroup.TYPE_ID,140);
        typeSortCategoryMap.put(BpRecord.TYPE_ID,140);
    }

    static final Collator numericStringCollator = new NumericStringCollator();

    public BaseProtectionTreeSorter() {
        super(numericStringCollator);
    }

    @Override
    public int category(Object element) {
        int category = 0;
        if (element instanceof CnATreeElement) {
            Integer mapValue = typeSortCategoryMap.get(((CnATreeElement) element).getTypeId());
            if (mapValue != null) {
                category = mapValue;
            }
        }
        return category;
    }

    @Override
    public int compare(Viewer viewer, Object o1, Object o2) {
        int result = 0;
        if (o1 instanceof Safeguard && o2 instanceof Safeguard) {
            Safeguard sg1 = (Safeguard) o1;
            Safeguard sg2 = (Safeguard) o2;
            result = quallifierToValue(Safeguard.PROP_QUALIFIER, sg1)
                    - quallifierToValue(Safeguard.PROP_QUALIFIER, sg2);
        } else if (o1 instanceof BpRequirement && o2 instanceof BpRequirement) {
            BpRequirement br1 = (BpRequirement) o1;
            BpRequirement br2 = (BpRequirement) o2;
            result = quallifierToValue(BpRequirement.PROP_QUALIFIER, br1)
                    - quallifierToValue(BpRequirement.PROP_QUALIFIER, br2);
        }
        if (result == 0) {
            result = super.compare(viewer, o1, o2);
        }
        return result;
    }

    private int quallifierToValue(String qualifier, CnATreeElement e) {
        String propertyValue = e.getEntity().getRawPropertyValue(qualifier);
        if (propertyValue == null) {
            return 0;
        }
        if (propertyValue.contains(BASIC)) {
            return 1;
        } else if (propertyValue.contains(STANDARD)) {
            return 2;
        } else if (propertyValue.contains(HIGH)) {
            return 3;
        }
        return 0;
    }
}
