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
package sernet.gs.ui.rcp.main.common.model;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;

import sernet.gs.model.Baustein;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.verinice.interfaces.CnATreeElementBuildException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IProgress;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpDocument;
import sernet.verinice.model.bp.elements.BpIncident;
import sernet.verinice.model.bp.elements.BpModel;
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
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.BausteinVorschlag;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmeKategorie;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.SubtypenZielobjekte;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.Datenverarbeitung;
import sernet.verinice.model.ds.Personengruppen;
import sernet.verinice.model.ds.StellungnahmeDSB;
import sernet.verinice.model.ds.VerantwortlicheStelle;
import sernet.verinice.model.ds.Verarbeitungsangaben;
import sernet.verinice.model.ds.Zweckbestimmung;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.DocumentGroup;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.ExceptionGroup;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.RecordGroup;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.RequirementGroup;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.ResponseGroup;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.ThreatGroup;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.iso27k.VulnerabilityGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.bp.LoadBpModel;
import sernet.verinice.service.commands.CnATypeMapper;
import sernet.verinice.service.commands.CreateAnwendung;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.CreateITNetwork;
import sernet.verinice.service.commands.CreateITVerbund;
import sernet.verinice.service.commands.UpdateElement;
import sernet.verinice.service.commands.crud.CreateBpModel;
import sernet.verinice.service.commands.crud.CreateCatalogModel;
import sernet.verinice.service.commands.crud.CreateIsoModel;
import sernet.verinice.service.commands.crud.UpdateMultipleElements;
import sernet.verinice.service.model.LoadModel;

/**
 * Factory for all model elements. Contains typed factories for sub-elements.
 *
 *
 * To add new model types see:
 *
 * https://wiki.sernet.private/wiki/Verinice/Entities
 *
 * - add new class with new type-id (String) - add type-id to Hitro-UI XML
 * Config (SNCA.xml) - add a factory for the type-id here - add the type to
 * hibernate's cnatreeelement.hbm.xml - don't forget to change the method
 * canContain() in the parent to include the new type - add Actions (add,
 * delete) to plugin.xml - create ActionDelegates which use this factory to
 * create new instances - register editor for type in EditorFactory
 *
 * @author koderman[at]sernet[dot]de
 *
 */
public final class CnAElementFactory {

    private final Logger log = Logger.getLogger(CnAElementFactory.class);

    private Object mutex = new Object();

    private static List<IModelLoadListener> listeners = new CopyOnWriteArrayList<>();

    private static volatile CnAElementFactory instance;

    private Map<String, IElementBuilder> elementbuilders = new HashMap<>();

    private CnAElementHome dbHome;

    private static BSIModel loadedModel;

    private static ISO27KModel isoModel;

    private static BpModel boModel;

    private static CatalogModel catalogModel;

    private ICommandService commandService;

    private static final String WARNING_UNCHECKED = "unchecked";
    private static final String WARNING_RAWTYPES = "rawtypes";

    private interface IElementBuilder<T extends CnATreeElement, U> {
        T build(CnATreeElement container, BuildInput<U> input) throws CommandException;
    }

    @SuppressWarnings(WARNING_RAWTYPES)
    private abstract static class ElementBuilder implements IElementBuilder {
        protected void init(CnATreeElement container, CnATreeElement child) {
            container.addChild(child);
            child.setParentAndScope(container);
        }
    }

    private final class DefaultElementBuilder extends ElementBuilder {

        private final Class<CnATreeElement> elementClass;
        private final String typeId;

        DefaultElementBuilder(String typeId) {
            this.elementClass = CnATypeMapper.getClassFromTypeId(typeId);
            this.typeId = typeId;

        }

        @Override
        public CnATreeElement build(CnATreeElement container, BuildInput input)
                throws CommandException {
            CnATreeElement child = dbHome.save(container, elementClass, typeId);
            init(container, child);
            return child;
        }
    }

    public void addLoadListener(IModelLoadListener listener) {
        if (log.isDebugEnabled()) {
            log.debug("Adding model load listener.");
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // safety: always fire one event when a loaded model is present,
        // because the model could have been loaded while the listener was in
        // the process of registering
        // himself here (race condition):
        if (loadedModel != null) {
            if (log.isDebugEnabled()) {
                log.debug("Firing safety event: bsi model loaded.");
            }
            listener.loaded(loadedModel);
        }
        if (isoModel != null) {
            if (log.isDebugEnabled()) {
                log.debug("Firing safety event: iso27k model");
            }
            listener.loaded(isoModel);
        }
        if (boModel != null) {
            if (log.isDebugEnabled()) {
                log.debug("Firing safety event: bo model");
            }
            listener.loaded(boModel);
        }

    }

    public void removeLoadListener(IModelLoadListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    @SuppressWarnings(WARNING_RAWTYPES)
    private CnAElementFactory() {
        dbHome = CnAElementHome.getInstance();

        // Datenschutz Elemente
        elementbuilders.put(StellungnahmeDSB.TYPE_ID, new ElementBuilder() {
            @Override
            public CnATreeElement build(CnATreeElement container, BuildInput input)
                    throws CommandException {
                StellungnahmeDSB child = dbHome.save(container, StellungnahmeDSB.class,
                        StellungnahmeDSB.TYPE_ID);
                init(container, child);
                return child;
            }
        });

        elementbuilders.put(Personengruppen.TYPE_ID,
                new DefaultElementBuilder(Personengruppen.TYPE_ID));
        elementbuilders.put(Datenverarbeitung.TYPE_ID,
                new DefaultElementBuilder(Datenverarbeitung.TYPE_ID));
        elementbuilders.put(Verarbeitungsangaben.TYPE_ID,
                new DefaultElementBuilder(Verarbeitungsangaben.TYPE_ID));
        elementbuilders.put(Zweckbestimmung.TYPE_ID,
                new DefaultElementBuilder(Zweckbestimmung.TYPE_ID));
        elementbuilders.put(VerantwortlicheStelle.TYPE_ID,
                new DefaultElementBuilder(VerantwortlicheStelle.TYPE_ID));

        // BSI-Grundschutz elements

        elementbuilders.put(NKKategorie.TYPE_ID, new DefaultElementBuilder(NKKategorie.TYPE_ID));
        elementbuilders.put(SonstigeITKategorie.TYPE_ID,
                new DefaultElementBuilder(SonstigeITKategorie.TYPE_ID));
        elementbuilders.put(PersonenKategorie.TYPE_ID,
                new DefaultElementBuilder(PersonenKategorie.TYPE_ID));
        elementbuilders.put(AnwendungenKategorie.TYPE_ID,
                new DefaultElementBuilder(AnwendungenKategorie.TYPE_ID));
        elementbuilders.put(GebaeudeKategorie.TYPE_ID,
                new DefaultElementBuilder(GebaeudeKategorie.TYPE_ID));
        elementbuilders.put(RaeumeKategorie.TYPE_ID,
                new DefaultElementBuilder(RaeumeKategorie.TYPE_ID));
        elementbuilders.put(TKKategorie.TYPE_ID, new DefaultElementBuilder(TKKategorie.TYPE_ID));
        elementbuilders.put(ServerKategorie.TYPE_ID,
                new DefaultElementBuilder(ServerKategorie.TYPE_ID));
        elementbuilders.put(ClientsKategorie.TYPE_ID,
                new DefaultElementBuilder(ClientsKategorie.TYPE_ID));
        elementbuilders.put(MassnahmeKategorie.TYPE_ID,
                new DefaultElementBuilder(MassnahmeKategorie.TYPE_ID));
        elementbuilders.put(Gebaeude.TYPE_ID, new DefaultElementBuilder(Gebaeude.TYPE_ID));
        elementbuilders.put(Client.TYPE_ID, new DefaultElementBuilder(Client.TYPE_ID));
        elementbuilders.put(SonstIT.TYPE_ID, new DefaultElementBuilder(SonstIT.TYPE_ID));
        elementbuilders.put(Server.TYPE_ID, new DefaultElementBuilder(Server.TYPE_ID));
        elementbuilders.put(TelefonKomponente.TYPE_ID,
                new DefaultElementBuilder(TelefonKomponente.TYPE_ID));
        elementbuilders.put(Raum.TYPE_ID, new DefaultElementBuilder(Raum.TYPE_ID));
        elementbuilders.put(NetzKomponente.TYPE_ID,
                new DefaultElementBuilder(NetzKomponente.TYPE_ID));
        elementbuilders.put(Person.TYPE_ID, new DefaultElementBuilder(Person.TYPE_ID));

        elementbuilders.put(Anwendung.TYPE_ID, new ElementBuilder() {
            @Override
            public CnATreeElement build(CnATreeElement container, BuildInput input)
                    throws CommandException {

                log.debug("Creating new Anwendung in " + container); //$NON-NLS-1$
                CreateAnwendung saveCommand = new CreateAnwendung(container, Anwendung.class);
                saveCommand = ServiceFactory.lookupCommandService().executeCommand(saveCommand);
                Anwendung child = saveCommand.getNewElement();

                init(container, child);

                return child;
            }
        });

        elementbuilders.put(BausteinUmsetzung.TYPE_ID,
                new IElementBuilder<BausteinUmsetzung, Baustein>() {
                    @Override
                    public BausteinUmsetzung build(CnATreeElement container,
                            BuildInput<Baustein> input) throws CommandException {

                        if (input == null) {
                            return dbHome.save(container, BausteinUmsetzung.class,
                                    BausteinUmsetzung.TYPE_ID);
                        } else {
                            BausteinUmsetzung bu = dbHome.save(container, input.getInput());
                            container.addChild(bu);
                            bu.setParentAndScope(container);
                            return bu;
                        }
                    }

                });

        elementbuilders.put(MassnahmenUmsetzung.TYPE_ID,
                new DefaultElementBuilder(MassnahmenUmsetzung.TYPE_ID));

        /*
         * added due to improvements on gstool-import, which could contain user
         * defined gefaehrdungen
         */
        elementbuilders.put(GefaehrdungsUmsetzung.TYPE_ID,
                new DefaultElementBuilder(GefaehrdungsUmsetzung.TYPE_ID));

        elementbuilders.put(ITVerbund.TYPE_ID, new ElementBuilder() {
            @Override
            public ITVerbund build(CnATreeElement container, BuildInput input)
                    throws CommandException {

                log.debug("Creating new ITVerbund in " + container); //$NON-NLS-1$
                boolean createChildren = true;
                if (input != null) {
                    createChildren = (Boolean) input.getInput();
                }
                CreateITVerbund saveCommand = new CreateITVerbund(container, ITVerbund.class,
                        createChildren);
                saveCommand = ServiceFactory.lookupCommandService().executeCommand(saveCommand);
                ITVerbund verbund = saveCommand.getNewElement();

                verbund.setParent(loadedModel);
                return verbund;
            }
        });

        // ISO 27000 builders
        elementbuilders.put(Organization.TYPE_ID, new DefaultElementBuilder(Organization.TYPE_ID));

        elementbuilders.put(AssetGroup.TYPE_ID, new DefaultElementBuilder(AssetGroup.TYPE_ID));
        elementbuilders.put(Asset.TYPE_ID, new DefaultElementBuilder(Asset.TYPE_ID));

        elementbuilders.put(PersonGroup.TYPE_ID, new DefaultElementBuilder(PersonGroup.TYPE_ID));
        elementbuilders.put(sernet.verinice.model.iso27k.PersonIso.TYPE_ID,
                new DefaultElementBuilder(sernet.verinice.model.iso27k.PersonIso.TYPE_ID));

        elementbuilders.put(AuditGroup.TYPE_ID, new DefaultElementBuilder(AuditGroup.TYPE_ID));
        elementbuilders.put(Audit.TYPE_ID, new DefaultElementBuilder(Audit.TYPE_ID));

        elementbuilders.put(ControlGroup.TYPE_ID, new DefaultElementBuilder(ControlGroup.TYPE_ID));
        elementbuilders.put(Control.TYPE_ID, new DefaultElementBuilder(Control.TYPE_ID));

        elementbuilders.put(ExceptionGroup.TYPE_ID,
                new DefaultElementBuilder(ExceptionGroup.TYPE_ID));
        elementbuilders.put(sernet.verinice.model.iso27k.Exception.TYPE_ID,
                new DefaultElementBuilder(sernet.verinice.model.iso27k.Exception.TYPE_ID));

        elementbuilders.put(RequirementGroup.TYPE_ID,
                new DefaultElementBuilder(RequirementGroup.TYPE_ID));
        elementbuilders.put(Requirement.TYPE_ID, new DefaultElementBuilder(Requirement.TYPE_ID));

        elementbuilders.put(Incident.TYPE_ID, new DefaultElementBuilder(Incident.TYPE_ID));
        elementbuilders.put(IncidentGroup.TYPE_ID,
                new DefaultElementBuilder(IncidentGroup.TYPE_ID));

        elementbuilders.put(IncidentScenario.TYPE_ID,
                new DefaultElementBuilder(IncidentScenario.TYPE_ID));
        elementbuilders.put(IncidentScenarioGroup.TYPE_ID,
                new DefaultElementBuilder(IncidentScenarioGroup.TYPE_ID));

        elementbuilders.put(Response.TYPE_ID, new DefaultElementBuilder(Response.TYPE_ID));
        elementbuilders.put(ResponseGroup.TYPE_ID,
                new DefaultElementBuilder(ResponseGroup.TYPE_ID));

        elementbuilders.put(Threat.TYPE_ID, new DefaultElementBuilder(Threat.TYPE_ID));
        elementbuilders.put(ThreatGroup.TYPE_ID, new DefaultElementBuilder(ThreatGroup.TYPE_ID));

        elementbuilders.put(Vulnerability.TYPE_ID,
                new DefaultElementBuilder(Vulnerability.TYPE_ID));
        elementbuilders.put(VulnerabilityGroup.TYPE_ID,
                new DefaultElementBuilder(VulnerabilityGroup.TYPE_ID));

        elementbuilders.put(DocumentGroup.TYPE_ID,
                new DefaultElementBuilder(DocumentGroup.TYPE_ID));
        elementbuilders.put(Document.TYPE_ID, new DefaultElementBuilder(Document.TYPE_ID));

        elementbuilders.put(InterviewGroup.TYPE_ID,
                new DefaultElementBuilder(InterviewGroup.TYPE_ID));
        elementbuilders.put(Interview.TYPE_ID, new DefaultElementBuilder(Interview.TYPE_ID));

        elementbuilders.put(FindingGroup.TYPE_ID, new DefaultElementBuilder(FindingGroup.TYPE_ID));
        elementbuilders.put(Finding.TYPE_ID, new DefaultElementBuilder(Finding.TYPE_ID));

        elementbuilders.put(EvidenceGroup.TYPE_ID,
                new DefaultElementBuilder(EvidenceGroup.TYPE_ID));
        elementbuilders.put(Evidence.TYPE_ID, new DefaultElementBuilder(Evidence.TYPE_ID));

        elementbuilders.put(ProcessGroup.TYPE_ID, new DefaultElementBuilder(ProcessGroup.TYPE_ID));
        elementbuilders.put(Process.TYPE_ID, new DefaultElementBuilder(Process.TYPE_ID));

        elementbuilders.put(RecordGroup.TYPE_ID, new DefaultElementBuilder(RecordGroup.TYPE_ID));
        elementbuilders.put(Record.TYPE_ID, new DefaultElementBuilder(Record.TYPE_ID));

        // Self Assessment (SAMT) builders

        elementbuilders.put(SamtTopic.TYPE_ID, new DefaultElementBuilder(SamtTopic.TYPE_ID));

        // renewed / modernized ITBP

        elementbuilders.put(ItNetwork.TYPE_ID, new ElementBuilder() {
            @Override
            public CnATreeElement build(CnATreeElement container, BuildInput input)
                    throws CommandException {
                log.debug("Creating new ItNetwork in " + container); //$NON-NLS-1$
                boolean createChildren = true;
                if (input != null) {
                    createChildren = (Boolean) input.getInput();
                }
                CreateITNetwork saveCommand = new CreateITNetwork(container, createChildren);
                saveCommand = ServiceFactory.lookupCommandService().executeCommand(saveCommand);
                ItNetwork itnetwork = saveCommand.getNewElement();

                itnetwork.setParent(boModel);
                return itnetwork;
            }
        });

        elementbuilders.put(Application.TYPE_ID, new DefaultElementBuilder(Application.TYPE_ID));
        elementbuilders.put(BpPerson.TYPE_ID, new DefaultElementBuilder(BpPerson.TYPE_ID));
        elementbuilders.put(BpRequirement.TYPE_ID,
                new DefaultElementBuilder(BpRequirement.TYPE_ID));
        elementbuilders.put(BpThreat.TYPE_ID, new DefaultElementBuilder(BpThreat.TYPE_ID));
        elementbuilders.put(BusinessProcess.TYPE_ID,
                new DefaultElementBuilder(BusinessProcess.TYPE_ID));
        elementbuilders.put(Device.TYPE_ID, new DefaultElementBuilder(Device.TYPE_ID));
        elementbuilders.put(IcsSystem.TYPE_ID, new DefaultElementBuilder(IcsSystem.TYPE_ID));
        elementbuilders.put(ItSystem.TYPE_ID, new DefaultElementBuilder(ItSystem.TYPE_ID));
        elementbuilders.put(Network.TYPE_ID, new DefaultElementBuilder(Network.TYPE_ID));
        elementbuilders.put(Room.TYPE_ID, new DefaultElementBuilder(Room.TYPE_ID));
        elementbuilders.put(Safeguard.TYPE_ID, new DefaultElementBuilder(Safeguard.TYPE_ID));
        elementbuilders.put(BpDocument.TYPE_ID, new DefaultElementBuilder(BpDocument.TYPE_ID));
        elementbuilders.put(BpIncident.TYPE_ID, new DefaultElementBuilder(BpIncident.TYPE_ID));
        elementbuilders.put(BpRecord.TYPE_ID, new DefaultElementBuilder(BpRecord.TYPE_ID));

        elementbuilders.put(ApplicationGroup.TYPE_ID,
                new DefaultElementBuilder(ApplicationGroup.TYPE_ID));
        elementbuilders.put(BpPersonGroup.TYPE_ID,
                new DefaultElementBuilder(BpPersonGroup.TYPE_ID));
        elementbuilders.put(BpRequirementGroup.TYPE_ID,
                new DefaultElementBuilder(BpRequirementGroup.TYPE_ID));
        elementbuilders.put(BpThreatGroup.TYPE_ID,
                new DefaultElementBuilder(BpThreatGroup.TYPE_ID));
        elementbuilders.put(BusinessProcessGroup.TYPE_ID,
                new DefaultElementBuilder(BusinessProcessGroup.TYPE_ID));
        elementbuilders.put(DeviceGroup.TYPE_ID, new DefaultElementBuilder(DeviceGroup.TYPE_ID));
        elementbuilders.put(IcsSystemGroup.TYPE_ID,
                new DefaultElementBuilder(IcsSystemGroup.TYPE_ID));
        elementbuilders.put(ItSystemGroup.TYPE_ID,
                new DefaultElementBuilder(ItSystemGroup.TYPE_ID));
        elementbuilders.put(NetworkGroup.TYPE_ID, new DefaultElementBuilder(NetworkGroup.TYPE_ID));
        elementbuilders.put(RoomGroup.TYPE_ID, new DefaultElementBuilder(RoomGroup.TYPE_ID));
        elementbuilders.put(SafeguardGroup.TYPE_ID,
                new DefaultElementBuilder(SafeguardGroup.TYPE_ID));
        elementbuilders.put(BpDocumentGroup.TYPE_ID, new DefaultElementBuilder(BpDocumentGroup.TYPE_ID));
        elementbuilders.put(BpIncidentGroup.TYPE_ID, new DefaultElementBuilder(BpIncidentGroup.TYPE_ID));
        elementbuilders.put(BpRecordGroup.TYPE_ID, new DefaultElementBuilder(BpRecordGroup.TYPE_ID));

    }

    public static CnAElementFactory getInstance() {
        if (instance == null) {
            instance = new CnAElementFactory();
        }
        return instance;
    }

    /**
     * Create new BSI element with new HUI Entity. The HUI Entity will be added
     * to the given container.
     *
     * @param container
     * @return the newly added element
     * @throws Exception
     */
    public CnATreeElement saveNewOrganisation(CnATreeElement container, boolean createChildren,
            boolean fireUpdates) throws CommandException {
        String title = HitroUtil.getInstance().getTypeFactory().getMessage(Organization.TYPE_ID);
        CreateElement<Organization> saveCommand = new CreateElement<>(container, Organization.class,
                title, false, createChildren);
        saveCommand = getCommandService().executeCommand(saveCommand);
        CnATreeElement child = saveCommand.getNewElement();
        container.addChild(child);
        child.setParentAndScope(container);
        // notify all listeners:
        if (fireUpdates) {
            CnAElementFactory.getModel(child).childAdded(container, child);
            CnAElementFactory.getModel(child).databaseChildAdded(child);
        }
        return child;
    }

    /**
     * Create new BSI element with new HUI Entity. The HUI Entity will be added
     * to the given container.
     *
     * @param container
     * @return the newly added element
     * @throws Exception
     */
    @SuppressWarnings({ WARNING_RAWTYPES, WARNING_UNCHECKED })
    public CnATreeElement saveNewAudit(CnATreeElement container, boolean createChildren,
            boolean fireUpdates) throws CommandException, CnATreeElementBuildException {
        IElementBuilder builder = elementbuilders.get(Audit.TYPE_ID);
        if (builder == null) {
            throw new CnATreeElementBuildException(
                    Messages.getString("CnAElementFactory.0") + Audit.TYPE_ID); //$NON-NLS-1$
        }
        CnATreeElement child = builder.build(container, null);

        // notify all listeners:
        if (fireUpdates) {
            CnAElementFactory.getModel(child).childAdded(container, child);
            CnAElementFactory.getModel(child).databaseChildAdded(child);
        }
        if (createChildren) {
            CnAElementFactory.getInstance().saveNew(child, AssetGroup.TYPE_ID, null, false);
            CnAElementFactory.getInstance().saveNew(child, ControlGroup.TYPE_ID, null, false);
            CnAElementFactory.getInstance().saveNew(child, PersonGroup.TYPE_ID, null, false);
            CnAElementFactory.getInstance().saveNew(child, FindingGroup.TYPE_ID, null, false);
            CnAElementFactory.getInstance().saveNew(child, EvidenceGroup.TYPE_ID, null, false);
            CnAElementFactory.getInstance().saveNew(child, InterviewGroup.TYPE_ID, null, false);
        }
        return child;
    }

    /**
     * Create new BSI element with new HUI Entity. The HUI Entity will be added
     * to the given container.
     *
     * @param container
     * @return the newly added element
     * @throws Exception
     */
    @SuppressWarnings({ WARNING_RAWTYPES, WARNING_UNCHECKED })
    public CnATreeElement saveNew(CnATreeElement container, String buildableTypeId,
            BuildInput input, boolean fireUpdates, boolean inheritIcon)
            throws CnATreeElementBuildException, CommandException {
        IElementBuilder builder = elementbuilders.get(buildableTypeId);
        if (builder == null) {
            log.error(Messages.getString("CnAElementFactory.0") + buildableTypeId);
            throw new CnATreeElementBuildException(
                    Messages.getString("CnAElementFactory.0") + buildableTypeId); //$NON-NLS-1$
        }
        CnATreeElement child = builder.build(container, input);

        if (inheritIcon) {
            child = inheritIcon(container.getIconPath(), container.getTypeId(), inheritIcon, child);
        }

        // notify all listeners:
        if (fireUpdates) {
            CnAElementFactory.getModel(child).childAdded(container, child);
            CnAElementFactory.getModel(child).databaseChildAdded(child);
        }
        return child;
    }

    private CnATreeElement inheritIcon(String iconPath, String containerTypeId, boolean inheritIcon,
            CnATreeElement child) throws CommandException {
        if (inheritIcon && !(ITVerbund.TYPE_ID.equals(containerTypeId)
                || Organization.TYPE_ID.equals(containerTypeId)
                || ItNetwork.TYPE_ID.equals(containerTypeId)
                || Audit.TYPE_ID.equals(containerTypeId))) {
            child.setIconPath(iconPath);
            Activator.inheritVeriniceContextState();
            UpdateElement<CnATreeElement> updateCommand = new UpdateElement<>(child, false,
                    ChangeLogEntry.STATION_ID);
            getCommandService().executeCommand(updateCommand);
            if (log.isDebugEnabled()) {
                log.debug("IconPath of containerElement:\t" + iconPath);
                log.debug("IconPath of child (after setter was called):\t" + child.getIconPath());
            }
        }
        return child;
    }

    @SuppressWarnings(WARNING_RAWTYPES)
    public CnATreeElement saveNew(CnATreeElement container, String buildableTypeId,
            BuildInput input, boolean inheritIcon)
            throws CommandException, CnATreeElementBuildException {
        return saveNew(container, buildableTypeId, input, true, inheritIcon);
    }

    public static BSIModel getLoadedModel() {
        return loadedModel;
    }

    public static boolean isModelLoaded() {
        return (loadedModel != null);
    }

    public static boolean isIsoModelLoaded() {
        return (isoModel != null);
    }

    public static boolean isBpModelLoaded() {
        return (boModel != null);
    }

    public static boolean isModernizedBpCatalogLoaded() {
        return (catalogModel != null);
    }

    public void closeModel() {
        dbHome.close();
        fireClosed();
        CnAElementFactory.dereferenceModel();
    }

    private static void dereferenceModel() {
        loadedModel = null;
    }

    private void fireClosed() {
        for (IModelLoadListener listener : listeners) {
            listener.closed(loadedModel);
        }
    }

    /**
     * Method is called to inform listener when an {@link BSIModel} is loaded or
     * created
     */
    private void fireLoad() {
        for (IModelLoadListener listener : listeners) {
            listener.loaded(loadedModel);
        }
    }

    /**
     * Method is called to inform listener when an ISO27KModel is loaded or
     * created
     *
     * If an {@link BSIModel} is created method fireLoad() is called
     *
     * @param model
     *            a new loaded or created {@link ISO27KModel}
     */
    private void fireLoad(ISO27KModel model) {
        for (IModelLoadListener listener : listeners) {
            listener.loaded(model);
        }
    }

    private void fireLoad(BpModel model) {
        for (IModelLoadListener listener : listeners) {
            listener.loaded(model);
        }
    }

    private void fireLoad(CatalogModel model) {
        for (IModelLoadListener listener : listeners) {
            listener.loaded(model);
        }
    }

    /**
     * Returns whether there is an active database connection.
     *
     * @return
     */
    public boolean isDbOpen() {
        return dbHome != null && dbHome.isOpen();
    }

    /**
     * Returns the model for a {@link CnATreeElement}
     *
     * @param element
     *            returned model belongs to this element
     * @return the model for an element
     */
    public static CnATreeElement getModel(CnATreeElement element) {
        CnATreeElement model = null;
        if (element instanceof ISO27KModel || element instanceof IISO27kElement) {
            model = CnAElementFactory.getInstance().getISO27kModel();
        } else if (element instanceof BpModel || element instanceof IBpElement) {
            model = CnAElementFactory.getInstance().getBpModel();
        } else {
            model = CnAElementFactory.getLoadedModel();
        }
        return model;
    }

    public ISO27KModel getISO27kModel() {
        if (isoModel != null) {
            return isoModel;
        }
        synchronized (mutex) {
            if (isoModel == null) {
                isoModel = loadIsoModel();
                if (isoModel == null) {
                    createIsoModel();
                }
            }
            return isoModel;
        }
    }

    public BpModel getBpModel() {
        if (boModel != null) {
            return boModel;
        }
        synchronized (mutex) {
            if (boModel == null) {
                boModel = loadBpModel();
                if (boModel == null) {
                    createBpModel();
                }
            }
        }
        return boModel;
    }

    public CatalogModel getCatalogModel() {
        if (catalogModel != null) {
            return catalogModel;
        }
        synchronized (mutex) {
            if (catalogModel == null) {
                catalogModel = loadCatalogModel();
                if (catalogModel == null) {
                    createCatalogModel();
                }
            }
        }
        return catalogModel;
    }

    private void createCatalogModel() {
        try {
            CreateCatalogModel command = new CreateCatalogModel();
            command = getCommandService().executeCommand(command);
            catalogModel = command.getElement();
            if (log.isInfoEnabled()) {
                log.info("Catalog Model created"); //$NON-NLS-1$
            }
            if (catalogModel != null) {
                fireLoad(catalogModel);
            }
        } catch (CommandException e) {
            log.error("Error creating CatalogModel", e); //$NON-NLS-1$
        }
    }

    private CatalogModel loadCatalogModel() {
        CatalogModel model = null;
        try {
            LoadModel<CatalogModel> loadModel = new LoadModel<>(CatalogModel.class);
            loadModel = getCommandService().executeCommand(loadModel);
            model = loadModel.getModel();
            if (model != null) {
                fireLoad(model);
            }
        } catch (Exception e) {
            log.error("Error loading the CatalogModel", e); //$NON-NLS-1$
            throw new RuntimeException("Error loading the CatalogModel", e);
        }
        return model;
    }

    /**
     * @return
     */
    private ISO27KModel loadIsoModel() {
        ISO27KModel model = null;
        try {
            LoadModel<ISO27KModel> loadModel = new LoadModel<>(ISO27KModel.class);
            loadModel = getCommandService().executeCommand(loadModel);
            model = loadModel.getModel();
            if (model != null) {
                fireLoad(model);
            }
        } catch (Exception e) {
            log.error(Messages.getString("CnAElementFactory.1"), e); //$NON-NLS-1$
            throw new RuntimeException(Messages.getString("CnAElementFactory.1"), e);
        }
        return model;
    }

    /**
     * @return
     */
    private void createIsoModel() {
        try {
            CreateIsoModel command = new CreateIsoModel();
            command = getCommandService().executeCommand(command);
            isoModel = command.getElement();
            if (log.isInfoEnabled()) {
                log.info("ISO27KModel created"); //$NON-NLS-1$
            }
            if (isoModel != null) {
                fireLoad(isoModel);
            }
        } catch (CommandException e) {
            log.error(Messages.getString("CnAElementFactory.2"), e); //$NON-NLS-1$
        }
    }

    private BpModel loadBpModel() {
        BpModel model = null;
        try {
            LoadBpModel modelLoadCommand = new LoadBpModel();
            modelLoadCommand = getCommandService().executeCommand(modelLoadCommand);
            model = modelLoadCommand.getModel();
            if (model != null) {
                fireLoad(model);
            }
        } catch (CommandException e) {
            log.error("Error loading model for modernized ITBP", e);
            throw new RuntimeException("Error loading model for modernized ITBP", e);
        }
        return model;
    }

    private void createBpModel() {
        try {
            CreateBpModel modelCreationCommand = new CreateBpModel();
            modelCreationCommand = getCommandService().executeCommand(modelCreationCommand);
            boModel = modelCreationCommand.getElement();
            if (log.isInfoEnabled()) {
                log.info("Model for modernized ITBP created"); //$NON-NLS-1$
            }
            if (boModel != null) {
                fireLoad(boModel);
            }

        } catch (CommandException e) {
            log.error(Messages.getString("CnAElementFactory.2"), e); //$NON-NLS-1$
        }

    }

    public BSIModel loadOrCreateModel(IProgress monitor)
            throws MalformedURLException, CommandException, CnATreeElementBuildException {
        if (!dbHome.isOpen()) {
            dbHome.open(monitor);
        }

        monitor.setTaskName(Messages.getString("CnAElementFactory.3")); //$NON-NLS-1$
        Activator.checkDbVersion();

        loadedModel = dbHome.loadModel(monitor);
        if (loadedModel != null) {

            fireLoad();
            return loadedModel;
        }

        // none found, create new model:
        log.debug("Creating new model in DB."); //$NON-NLS-1$
        monitor.setTaskName(Messages.getString("CnAElementFactory.4")); //$NON-NLS-1$
        loadedModel = new BSIModel();

        createBausteinVorschlaege();

        loadedModel = dbHome.save(loadedModel);
        ITVerbund verbund = (ITVerbund) CnAElementFactory.getInstance().saveNew(loadedModel,
                ITVerbund.TYPE_ID, null, false);
        loadedModel.addChild(verbund);

        fireLoad();
        return loadedModel;
    }

    private void createBausteinVorschlaege() {
        SubtypenZielobjekte mapping = new SubtypenZielobjekte();
        List<BausteinVorschlag> list = mapping.getMapping();
        UpdateMultipleElements<BausteinVorschlag> command = new UpdateMultipleElements<>(list,
                ChangeLogEntry.STATION_ID, ChangeLogEntry.TYPE_INSERT);
        try {
            ServiceFactory.lookupCommandService().executeCommand(command);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
    }

    /**
     * @deprecated Use method reloadAllModelsFromDatabase()
     */
    @Deprecated
    public void reloadModelFromDatabase() {
        reloadAllModelsFromDatabase();
    }

    public void reloadAllModelsFromDatabase() {
        try {
            fireClosed();
            reloadBsiModelFromDatabasePrivate();
            reloadIsoModelFromDatabasePrivate();
            reloadBpModelFromDatabasePrivate();
            reloadCatalogModelFromDatabasePrivate();
        } catch (Exception e) {
            log.error(Messages.getString("CnAElementFactory.5"), e); //$NON-NLS-1$
        }
    }

    public void reloadBsiModelFromDatabase() {
        fireClosed();
        try {
            reloadBsiModelFromDatabasePrivate();
        } catch (Exception e) {
            log.error("Could not reload BSI (old base protection) model from database", e); //$NON-NLS-1$
        }
    }

    private void reloadBsiModelFromDatabasePrivate() throws CommandException {
        if (isModelLoaded()) {
            BSIModel newModel = dbHome.loadModel(new NullMonitor());
            loadedModel.modelReload(newModel);
            loadedModel.moveListener(newModel);
            loadedModel = newModel;
            fireLoad();
        }
    }

    public void reloadIsoModelFromDatabase() {
        fireClosed();
        try {
            reloadIsoModelFromDatabasePrivate();
        } catch (Exception e) {
            log.error("Could not reload iso model from database", e); //$NON-NLS-1$
        }
    }

    private void reloadIsoModelFromDatabasePrivate() {
        if (isIsoModelLoaded()) {
            ISO27KModel newModel = loadIsoModel();
            if (log.isDebugEnabled()) {
                log.debug("reloadModelFromDatabase, ISO-model loaded"); //$NON-NLS-1$
            }
            isoModel.modelReload(newModel);
            isoModel.moveListener(newModel);
            isoModel = newModel;
            fireLoad(isoModel);
        }
    }

    public void reloadBpModelFromDatabase() {
        fireClosed();
        try {
            reloadBpModelFromDatabasePrivate();
        } catch (Exception e) {
            log.error("Could not reload (renewed) base protection model from database", e); //$NON-NLS-1$
        }
    }

    private void reloadBpModelFromDatabasePrivate() {
        if (isBpModelLoaded()) {
            BpModel newModel = loadBpModel();
            if (log.isDebugEnabled()) {
                log.debug(
                        "reloadBpModelFromDatabasePrivate, (renewed) base protection model loaded"); //$NON-NLS-1$
            }
            boModel.modelReload(newModel);
            boModel.moveListener(newModel);
            boModel = newModel;
            fireLoad(boModel);
        }
    }

    public void reloadCatalogModelFromDatabase() {
        fireClosed();
        try {
            reloadCatalogModelFromDatabasePrivate();
        } catch (Exception e) {
            log.error("Could not reload catalog model from database", e); //$NON-NLS-1$
        }
    }

    private void reloadCatalogModelFromDatabasePrivate() {
        if (isModernizedBpCatalogLoaded()) {
            CatalogModel newModel = loadCatalogModel();
            if (log.isDebugEnabled()) {
                log.debug("reloadCatalogModelFromDatabasePrivate,catalog model loaded"); //$NON-NLS-1$
            }
            catalogModel.modelReload(newModel);
            catalogModel.moveListener(newModel);
            catalogModel = newModel;
            fireLoad(catalogModel);
        }
    }

    private ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandService();
        }
        return commandService;
    }

    private ICommandService createCommandService() {
        try {
            ServiceFactory.openCommandService();
        } catch (MalformedURLException e) {
            log.error(Messages.getString("CnAElementFactory.6"), e); //$NON-NLS-1$
            throw new RuntimeException(Messages.getString("CnAElementFactory.7"), e); //$NON-NLS-1$
        }
        commandService = ServiceFactory.lookupCommandService();
        return commandService;
    }

    /**
     * @param changeLogEntry
     */
    public static void databaseChildRemoved(ChangeLogEntry changeLogEntry) {
        if (isModelLoaded()) {
            CnAElementFactory.getLoadedModel().databaseChildRemoved(changeLogEntry);
        }
        if (isIsoModelLoaded()) {
            CnAElementFactory.getInstance().getISO27kModel().databaseChildRemoved(changeLogEntry);
        }
    }

    public static boolean selectionOnlyContainsScopes(IStructuredSelection selection) {
        for (Object selectedEl : selection.toList()) {
            if (!isScope(selectedEl)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isScope(Object element) {
        return element instanceof ItNetwork || element instanceof Organization
                || element instanceof ITVerbund;
    }
}
