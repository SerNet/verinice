/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.List;
import java.util.stream.Stream;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.hui.swt.widgets.IInputHelper;
import sernet.verinice.interfaces.CommandException;
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
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpIncidentGroup;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.BusinessProcessGroup;
import sernet.verinice.model.bp.groups.DeviceGroup;
import sernet.verinice.model.bp.groups.IcsSystemGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.NetworkGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Schutzbedarf;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.Exception;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

public final class InputHelperFactory {

    private static IInputHelper schutzbedarfHelper;
    private static IInputHelper tagHelper;
    private static IInputHelper personHelper;

    public static void setInputHelpers(EntityType entityType, HitroUIComposite huiComposite2) {
        if (personHelper == null) {
            personHelper = () -> {
                try {
                    List<Person> personen = CnAElementHome.getInstance().getPersonen();
                    if (personen.isEmpty()) {
                        return new String[] { Messages.InputHelperFactory_0 };
                    }
                    return personen.stream().map(Person::getTitle).toArray(String[]::new);

                } catch (CommandException e) {
                    ExceptionUtil.log(e, Messages.InputHelperFactory_1);
                    return new String[] { Messages.InputHelperFactory_0 };
                }
            };
        }

        if (tagHelper == null) {
            tagHelper = () -> {
                try {
                    return CnAElementHome.getInstance().getTags().stream().toArray(String[]::new);
                } catch (CommandException e) {
                    ExceptionUtil.log(e, Messages.InputHelperFactory_5);
                    return new String[] {};
                }
            };
        }

        if (schutzbedarfHelper == null) {
            schutzbedarfHelper = () -> new String[] { Messages.InputHelperFactory_4,
                    Messages.InputHelperFactory_2, Messages.InputHelperFactory_3 };
        }

        boolean showHint = Activator.getDefault().getPluginPreferences()
                .getBoolean(PreferenceConstants.INPUTHINTS);

        // Tag Helpers:

        Stream.of(
                // BSI elements
                Anwendung.PROP_TAG, Client.PROP_TAG, Gebaeude.PROP_TAG, NetzKomponente.PROP_TAG,
                Person.PROP_TAG, Raum.PROP_TAG, Server.PROP_TAG, SonstIT.PROP_TAG,
                TelefonKomponente.PROP_TAG, ITVerbund.PROP_TAG,
                // ISO27k elements
                Asset.PROP_TAG, Audit.PROP_TAG, Control.PROP_TAG, Document.PROP_TAG,
                Evidence.PROP_TAG, Exception.PROP_TAG, Finding.PROP_TAG, Incident.PROP_TAG,
                IncidentScenario.PROP_TAG, Interview.PROP_TAG, PersonIso.PROP_TAG, Process.PROP_TAG,
                Record.PROP_TAG, Requirement.PROP_TAG, Response.PROP_TAG, Threat.PROP_TAG,
                Vulnerability.PROP_TAG, Organization.PROP_TAG,
                // modernized ITBP elements and groups
                Application.PROP_TAG, ApplicationGroup.PROP_TAG, BpDocument.PROP_TAG,
                BpIncident.PROP_TAG, BpIncidentGroup.PROP_TAG, BpPerson.PROP_TAG,
                BpPersonGroup.PROP_TAG, BpRecord.PROP_TAG, BpRequirement.PROP_TAG,
                BpRequirementGroup.PROP_TAG, BpThreat.PROP_TAG, BpThreatGroup.PROP_TAG,
                BusinessProcess.PROP_TAG, BusinessProcessGroup.PROP_TAG, Device.PROP_TAG,
                DeviceGroup.PROP_TAG, IcsSystem.PROP_TAG, IcsSystemGroup.PROP_TAG,
                ItNetwork.PROP_TAG, ItSystem.PROP_TAG, ItSystemGroup.PROP_TAG, Network.PROP_TAG,
                NetworkGroup.PROP_TAG, Room.PROP_TAG, RoomGroup.PROP_TAG, Safeguard.PROP_TAG,
                SafeguardGroup.PROP_TAG)
                .forEach(propertyName -> huiComposite2.setInputHelper(propertyName, tagHelper,
                        IInputHelper.TYPE_ADD, showHint));

        setSchutzbedarfHelpers(entityType, huiComposite2, showHint);

    }

    private static void setSchutzbedarfHelpers(EntityType entityType,
            HitroUIComposite huiComposite2, boolean showHint) {
        for (PropertyGroup group : entityType.getPropertyGroups()) {
            for (PropertyType type : group.getPropertyTypes()) {
                if (Schutzbedarf.isIntegritaetBegruendung(type.getId())
                        || Schutzbedarf.isVerfuegbarkeitBegruendung(type.getId())
                        || Schutzbedarf.isVertraulichkeitBegruendung(type.getId())) {
                    huiComposite2.setInputHelper(type.getId(), schutzbedarfHelper,
                            IInputHelper.TYPE_REPLACE, showHint);
                }
            }
        }

    }

    private InputHelperFactory() {

    }
}
