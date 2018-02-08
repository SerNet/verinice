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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.hui.common.connect.Entity;
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
import sernet.verinice.model.bpm.TodoViewItem;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Note;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.Datenverarbeitung;
import sernet.verinice.model.ds.Personengruppen;
import sernet.verinice.model.ds.StellungnahmeDSB;
import sernet.verinice.model.ds.VerantwortlicheStelle;
import sernet.verinice.model.ds.Verarbeitungsangaben;
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
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
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
import sernet.verinice.rcp.linktable.LinkTableEditor;
import sernet.verinice.rcp.linktable.LinkTableEditorInput;

/**
 * This class is a singleton and maps editors for different ressources and
 * either opens a new editor or looks them up in the EditorRegistry and shows an
 * already open editor for an object.
 * 
 * @author koderman[at]sernet[dot]de
 * @author dm[at]sernet[dot]de
 */
public final class EditorFactory {

    private final Logger log = Logger.getLogger(EditorFactory.class);

    private static EditorFactory instance;
    private static Map<Class<?>, IEditorTypeFactory> typedFactories = new HashMap<>();

    private IEditorTypeFactory linkTableEditorFactory = new LinkTableEditorFactory();

    private interface IEditorTypeFactory {
        void openEditorFor(Object o) throws PartInitException;
    }

    /**
     * Private singleton constructor
     */
    private EditorFactory() {
        registerCnaTreeElements();
        registerGefaehrdung();
        registerTodoViewItem();
        registerAttachment();
        registerNote();
        typedFactories.put(LinkTableEditorInput.class, linkTableEditorFactory);
    }

    /**
     * @return The singleton instzance of this class
     */
    public static EditorFactory getInstance() {
        if (instance == null) {
            instance = new EditorFactory();
        }
        return instance;
    }

    /**
     * Checks if an editor factory is registered for the given object type and
     * opens a new editor for it.
     * 
     * @param o
     *            Object which is opened in the editor
     */
    public void openEditor(Object o) {
        IEditorTypeFactory factory = typedFactories.get(o.getClass());
        if (factory != null) {
            try {
                factory.openEditorFor(o);
            } catch (Exception e) {
                log.error("Error while opening editor.", e); //$NON-NLS-1$
                ExceptionUtil.log(e, Messages.EditorFactory_2);
            }
        }
    }

    /**
     * Checks if an editor factory is registered for the given object type and
     * opens a new editor for it. Also updates the object.
     * 
     * @param o
     *            Object which is opened in the editor
     */
    public void updateAndOpenObject(Object o) {
        EditorFactory.getInstance().openEditor(o);
    }

    /**
     * Checks if an editor factory is registered for the given object type and
     * opens a new editor for it. Also updates the object.
     * 
     * @param element
     *            Object which is opened in the editor
     * @param readOnly
     *            should the editor opened in readOnly mode
     */
    public void updateAndOpenObject(CnATreeElement element, boolean readOnly) {
        BSIElementEditorInput input = new BSIElementEditorInput(element, readOnly);
        try {
            openEditor(input.getId(), input, BSIElementEditor.EDITOR_ID);
        } catch (Exception e) {
            log.error("Error while opening editor.", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.EditorFactory_2);
        }
    }

    private void registerNote() {
        IEditorTypeFactory noteEditorFactory = new IEditorTypeFactory() {
            @Override
            public void openEditorFor(Object o) throws PartInitException {
                // replace element with new instance from DB:
                Note selection = (Note) o;
                NoteEditorInput input = new NoteEditorInput(selection);
                openEditor(String.valueOf(input.getId()), input, NoteEditor.EDITOR_ID);
            }
        };
        typedFactories.put(Note.class, noteEditorFactory);
    }

    private void registerAttachment() {
        IEditorTypeFactory attachmentEditorFactory = new IEditorTypeFactory() {
            @Override
            public void openEditorFor(Object o) throws PartInitException {
                Attachment attachment = (Attachment) o;
                AttachmentEditorInput input = new AttachmentEditorInput(attachment);
                openEditor(String.valueOf(input.getId()), input, AttachmentEditor.EDITOR_ID);
            }
        };
        typedFactories.put(Attachment.class, attachmentEditorFactory);
    }

    private void registerTodoViewItem() {
        IEditorTypeFactory todoItemEditorFactory = new IEditorTypeFactory() {
            @Override
            public void openEditorFor(Object o) throws PartInitException {
                // replace element with new instance from DB:
                TodoViewItem selection = (TodoViewItem) o;
                CnATreeElement element;
                try {
                    element = CnAElementHome.getInstance().loadById(MassnahmenUmsetzung.TYPE_ID, selection.getDbId());
                    openEditor(element.getId(), new BSIElementEditorInput(element), BSIElementEditor.EDITOR_ID);
                } catch (CommandException e) {
                    log.error("Error while opening editor.", e); //$NON-NLS-1$
                    ExceptionUtil.log(e, Messages.EditorFactory_2);
                }
            }
        };
        typedFactories.put(TodoViewItem.class, todoItemEditorFactory);
    }

    private void registerGefaehrdung() {
        IEditorTypeFactory gefaehrdungsUmsetzungFactory = new IEditorTypeFactory() {
            @Override
            public void openEditorFor(Object o) throws PartInitException {
                GefaehrdungsUmsetzung gefaehrdung = (GefaehrdungsUmsetzung) o;
                String id;
                if (gefaehrdung.getEntity() == null) {
                    id = Entity.TITLE + gefaehrdung.getUuid();
                } else {
                    id = gefaehrdung.getEntity().getId();
                }
                openEditor(id, new BSIElementEditorInput(gefaehrdung), BSIElementEditor.EDITOR_ID);
            }
        };
        typedFactories.put(GefaehrdungsUmsetzung.class, gefaehrdungsUmsetzungFactory);
    }

    private void registerCnaTreeElements() {
        IEditorTypeFactory bsiEditorFactory = new IEditorTypeFactory() {
            @Override
            public void openEditorFor(Object o) throws PartInitException {
                BSIElementEditorInput input;
                if (o instanceof CnATreeElement) {
                    input = new BSIElementEditorInput((CnATreeElement) o);
                } else {
                    TaskEditorContext context = (TaskEditorContext) o;
                    input = new BSIElementEditorInput(context.getElement(), context.getTask());
                }
                openEditor(input.getId(), input, BSIElementEditor.EDITOR_ID);
            }
        };
        typedFactories.put(ITVerbund.class, bsiEditorFactory);
        typedFactories.put(Client.class, bsiEditorFactory);
        typedFactories.put(SonstIT.class, bsiEditorFactory);
        typedFactories.put(NetzKomponente.class, bsiEditorFactory);
        typedFactories.put(TelefonKomponente.class, bsiEditorFactory);
        typedFactories.put(Raum.class, bsiEditorFactory);
        typedFactories.put(Server.class, bsiEditorFactory);
        typedFactories.put(Person.class, bsiEditorFactory);
        typedFactories.put(Gebaeude.class, bsiEditorFactory);
        typedFactories.put(Anwendung.class, bsiEditorFactory);
        typedFactories.put(BausteinUmsetzung.class, bsiEditorFactory);
        typedFactories.put(MassnahmenUmsetzung.class, bsiEditorFactory);
        typedFactories.put(RisikoMassnahmenUmsetzung.class, bsiEditorFactory);
        typedFactories.put(Verarbeitungsangaben.class, bsiEditorFactory);
        typedFactories.put(VerantwortlicheStelle.class, bsiEditorFactory);
        typedFactories.put(Personengruppen.class, bsiEditorFactory);
        typedFactories.put(Datenverarbeitung.class, bsiEditorFactory);
        typedFactories.put(StellungnahmeDSB.class, bsiEditorFactory);
        // ISO 27000 elements
        typedFactories.put(Organization.class, bsiEditorFactory);
        typedFactories.put(AssetGroup.class, bsiEditorFactory);
        typedFactories.put(Asset.class, bsiEditorFactory);
        typedFactories.put(PersonGroup.class, bsiEditorFactory);
        typedFactories.put(PersonIso.class, bsiEditorFactory);
        typedFactories.put(AuditGroup.class, bsiEditorFactory);
        typedFactories.put(Audit.class, bsiEditorFactory);
        typedFactories.put(ControlGroup.class, bsiEditorFactory);
        typedFactories.put(Control.class, bsiEditorFactory);
        typedFactories.put(ExceptionGroup.class, bsiEditorFactory);
        typedFactories.put(sernet.verinice.model.iso27k.Exception.class, bsiEditorFactory);
        typedFactories.put(RequirementGroup.class, bsiEditorFactory);
        typedFactories.put(Requirement.class, bsiEditorFactory);
        typedFactories.put(IncidentGroup.class, bsiEditorFactory);
        typedFactories.put(Incident.class, bsiEditorFactory);
        typedFactories.put(IncidentScenarioGroup.class, bsiEditorFactory);
        typedFactories.put(IncidentScenario.class, bsiEditorFactory);
        typedFactories.put(ResponseGroup.class, bsiEditorFactory);
        typedFactories.put(Response.class, bsiEditorFactory);
        typedFactories.put(ThreatGroup.class, bsiEditorFactory);
        typedFactories.put(Threat.class, bsiEditorFactory);
        typedFactories.put(VulnerabilityGroup.class, bsiEditorFactory);
        typedFactories.put(Vulnerability.class, bsiEditorFactory);
        typedFactories.put(DocumentGroup.class, bsiEditorFactory);
        typedFactories.put(Document.class, bsiEditorFactory);
        typedFactories.put(InterviewGroup.class, bsiEditorFactory);
        typedFactories.put(Interview.class, bsiEditorFactory);
        typedFactories.put(EvidenceGroup.class, bsiEditorFactory);
        typedFactories.put(Evidence.class, bsiEditorFactory);
        typedFactories.put(FindingGroup.class, bsiEditorFactory);
        typedFactories.put(Finding.class, bsiEditorFactory);
        typedFactories.put(sernet.verinice.model.iso27k.Process.class, bsiEditorFactory);
        typedFactories.put(ProcessGroup.class, bsiEditorFactory);
        typedFactories.put(Record.class, bsiEditorFactory);
        typedFactories.put(RecordGroup.class, bsiEditorFactory);
        // Modernized ITBP elements
        typedFactories.put(ItNetwork.class, bsiEditorFactory);
        typedFactories.put(Application.class, bsiEditorFactory);
        typedFactories.put(ApplicationGroup.class, bsiEditorFactory);
        typedFactories.put(BpPerson.class, bsiEditorFactory);
        typedFactories.put(BpPersonGroup.class, bsiEditorFactory);
        typedFactories.put(BpRequirement.class, bsiEditorFactory);
        typedFactories.put(BpRequirementGroup.class, bsiEditorFactory);
        typedFactories.put(BpThreat.class, bsiEditorFactory);
        typedFactories.put(BpThreatGroup.class, bsiEditorFactory);
        typedFactories.put(BusinessProcess.class, bsiEditorFactory);
        typedFactories.put(BusinessProcessGroup.class, bsiEditorFactory);
        typedFactories.put(Device.class, bsiEditorFactory);
        typedFactories.put(DeviceGroup.class, bsiEditorFactory);
        typedFactories.put(IcsSystem.class, bsiEditorFactory);
        typedFactories.put(IcsSystemGroup.class, bsiEditorFactory);
        typedFactories.put(ItSystem.class, bsiEditorFactory);
        typedFactories.put(ItSystemGroup.class, bsiEditorFactory);
        typedFactories.put(Network.class, bsiEditorFactory);
        typedFactories.put(NetworkGroup.class, bsiEditorFactory);
        typedFactories.put(Room.class, bsiEditorFactory);
        typedFactories.put(RoomGroup.class, bsiEditorFactory);
        typedFactories.put(Safeguard.class, bsiEditorFactory);
        typedFactories.put(SafeguardGroup.class, bsiEditorFactory);
        typedFactories.put(BpDocument.class, bsiEditorFactory);
        typedFactories.put(BpDocumentGroup.class, bsiEditorFactory);
        typedFactories.put(BpIncident.class, bsiEditorFactory);
        typedFactories.put(BpIncidentGroup.class, bsiEditorFactory);
        typedFactories.put(BpRecord.class, bsiEditorFactory);
        typedFactories.put(BpRecordGroup.class, bsiEditorFactory);

        
        // Self Assessment (SAMT) elements
        typedFactories.put(SamtTopic.class, bsiEditorFactory);
        // for task context
        typedFactories.put(TaskEditorContext.class, bsiEditorFactory);
    }

    private static IEditorPart openEditor(String id, IEditorInput input, String editorId) throws PartInitException {
        IEditorPart editor = EditorRegistry.getInstance().getOpenEditor(id);
        if (editor == null) {
            // open new editor:
            editor = getPage().openEditor(input, editorId);
            EditorRegistry.getInstance().registerOpenEditor(id, editor);
        } else {
            // show existing editor:
            editor = getPage().openEditor(editor.getEditorInput(), editorId);
        }
        return editor;
    }

    private static IWorkbenchPage getPage() {
        IWorkbenchPage page = Activator.getActivePage();
        if (page == null) {
            page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        }
        return page;
    }

    private static class LinkTableEditorFactory implements IEditorTypeFactory {

        @Override
        public void openEditorFor(Object o) throws PartInitException {
            LinkTableEditorInput input = (LinkTableEditorInput) o;
            openEditor(input.getId(), input, LinkTableEditor.EDITOR_ID);
        }

    }

}
