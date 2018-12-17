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
package sernet.verinice.rcp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.DocumentGroup;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.ExceptionGroup;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.model.iso27k.RecordGroup;
import sernet.verinice.model.iso27k.RequirementGroup;
import sernet.verinice.model.iso27k.ResponseGroup;
import sernet.verinice.model.iso27k.ThreatGroup;
import sernet.verinice.model.iso27k.VulnerabilityGroup;

/**
 * Utility class to retrieve a message for an "add group" menu entry by the
 * group's type id
 */
public final class AddGroupMessageHelper {

    public static final Map<String, String> TITLE_FOR_TYPE;

    static {
        // ISO27000

        Map<String, String> m = new HashMap<>();
        m.put(AssetGroup.TYPE_ID, Messages.AddGroup_0);
        m.put(AuditGroup.TYPE_ID, Messages.AddGroup_1);
        m.put(ControlGroup.TYPE_ID, Messages.AddGroup_2);
        m.put(DocumentGroup.TYPE_ID, Messages.AddGroup_3);
        m.put(EvidenceGroup.TYPE_ID, Messages.AddGroup_4);
        m.put(ExceptionGroup.TYPE_ID, Messages.AddGroup_5);
        m.put(FindingGroup.TYPE_ID, Messages.AddGroup_6);
        m.put(IncidentGroup.TYPE_ID, Messages.AddGroup_7);
        m.put(IncidentScenarioGroup.TYPE_ID, Messages.AddGroup_8);
        m.put(InterviewGroup.TYPE_ID, Messages.AddGroup_9);
        m.put(PersonGroup.TYPE_ID, Messages.AddGroup_10);
        m.put(ProcessGroup.TYPE_ID, Messages.AddGroup_11);
        m.put(RecordGroup.TYPE_ID, Messages.AddGroup_12);
        m.put(RequirementGroup.TYPE_ID, Messages.AddGroup_13);
        m.put(ResponseGroup.TYPE_ID, Messages.AddGroup_14);
        m.put(ThreatGroup.TYPE_ID, Messages.AddGroup_15);
        m.put(VulnerabilityGroup.TYPE_ID, Messages.AddGroup_16);
        m.put(Asset.TYPE_ID, Messages.AddGroup_17);

        // Base protection
        m.put(ApplicationGroup.TYPE_ID, Messages.AddGroupHandler_application);
        m.put(BpPersonGroup.TYPE_ID, Messages.AddGroupHandler_group);
        m.put(BpRequirementGroup.TYPE_ID, Messages.AddGroupHandler_requirement);
        m.put(BpThreatGroup.TYPE_ID, Messages.AddGroupHandler_threat);
        m.put(BusinessProcessGroup.TYPE_ID, Messages.AddGroupHandler_business_process);
        m.put(DeviceGroup.TYPE_ID, Messages.AddGroupHandler_device);
        m.put(IcsSystemGroup.TYPE_ID, Messages.AddGroupHandler_ics_system);
        m.put(ItSystemGroup.TYPE_ID, Messages.AddGroupHandler_it_system);
        m.put(NetworkGroup.TYPE_ID, Messages.AddGroupHandler_network);
        m.put(RoomGroup.TYPE_ID, Messages.AddGroupHandler_room);
        m.put(SafeguardGroup.TYPE_ID, Messages.AddGroupHandler_safeguard);
        m.put(BpDocumentGroup.TYPE_ID, Messages.AddGroupHandler_document);
        m.put(BpIncidentGroup.TYPE_ID, Messages.AddGroupHandler_incident);
        m.put(BpRecordGroup.TYPE_ID, Messages.AddGroupHandler_record);

        TITLE_FOR_TYPE = Collections.unmodifiableMap(m);

    }

    public static String getMessageForAddGroup(String groupTypeId) {
        return TITLE_FOR_TYPE.get(groupTypeId);
    }

    private AddGroupMessageHelper() {

    }
}
