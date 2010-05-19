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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.hibernate.HibernateException;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.Attachment;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Note;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.ds.model.Datenverarbeitung;
import sernet.gs.ui.rcp.main.ds.model.Personengruppen;
import sernet.gs.ui.rcp.main.ds.model.StellungnahmeDSB;
import sernet.gs.ui.rcp.main.ds.model.VerantwortlicheStelle;
import sernet.gs.ui.rcp.main.ds.model.Verarbeitungsangaben;
import sernet.verinice.iso27k.model.Asset;
import sernet.verinice.iso27k.model.AssetGroup;
import sernet.verinice.iso27k.model.Audit;
import sernet.verinice.iso27k.model.AuditGroup;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.Document;
import sernet.verinice.iso27k.model.DocumentGroup;
import sernet.verinice.iso27k.model.Evidence;
import sernet.verinice.iso27k.model.EvidenceGroup;
import sernet.verinice.iso27k.model.ExceptionGroup;
import sernet.verinice.iso27k.model.Finding;
import sernet.verinice.iso27k.model.FindingGroup;
import sernet.verinice.iso27k.model.Incident;
import sernet.verinice.iso27k.model.IncidentGroup;
import sernet.verinice.iso27k.model.IncidentScenario;
import sernet.verinice.iso27k.model.IncidentScenarioGroup;
import sernet.verinice.iso27k.model.Interview;
import sernet.verinice.iso27k.model.InterviewGroup;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.model.PersonGroup;
import sernet.verinice.iso27k.model.PersonIso;
import sernet.verinice.iso27k.model.ProcessGroup;
import sernet.verinice.iso27k.model.Record;
import sernet.verinice.iso27k.model.RecordGroup;
import sernet.verinice.iso27k.model.Requirement;
import sernet.verinice.iso27k.model.RequirementGroup;
import sernet.verinice.iso27k.model.Response;
import sernet.verinice.iso27k.model.ResponseGroup;
import sernet.verinice.iso27k.model.Threat;
import sernet.verinice.iso27k.model.ThreatGroup;
import sernet.verinice.iso27k.model.Vulnerability;
import sernet.verinice.iso27k.model.VulnerabilityGroup;
import sernet.verinice.samt.model.SamtTopic;

/**
 * This class maps editors for different ressources and either opens a new
 * editor or looks them up in the EditorRegistry and shows an already open
 * editor for an object
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class EditorFactory {
	
	private final Logger log = Logger.getLogger(EditorFactory.class);
	
	private static EditorFactory instance;
	private static HashMap<Class, IEditorTypeFactory> typedFactories = new HashMap<Class, IEditorTypeFactory>();

	private interface IEditorTypeFactory {
		void openEditorFor(Object o) throws Exception;
	}

	/**
	 * Singleton constructor
	 * 
	 */
	private EditorFactory() {
		// register editor-factory for bsi-element-editors:
		IEditorTypeFactory bsiEditorFactory = new IEditorTypeFactory() {

			public void openEditorFor(Object o) throws Exception {
				IEditorPart editor;

				// replace element with new instance from DB:
				CnATreeElement cnaElement = (CnATreeElement) o;
				CnATreeElement newElement = CnAElementHome.getInstance().loadById(cnaElement.getTypeId(), cnaElement.getDbId());
				
				// may be null, i.e. if we don't have permission to open the object:
				if (newElement == null)
					throw new Exception(Messages.EditorFactory_0);
				
				BSIElementEditorInput input = new BSIElementEditorInput(newElement);

				if ((editor = EditorRegistry.getInstance().getOpenEditor(input.getId())) == null) {
					// open new editor:
					editor = Activator.getActivePage().openEditor(input, BSIElementEditor.EDITOR_ID);
					EditorRegistry.getInstance().registerOpenEditor(input.getId(), editor);
				} else {
					// show existing editor:
					Activator.getActivePage().openEditor(editor.getEditorInput(), BSIElementEditor.EDITOR_ID);
				}
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
		typedFactories.put(GefaehrdungsUmsetzung.class, bsiEditorFactory);
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
		typedFactories.put(sernet.verinice.iso27k.model.Exception.class, bsiEditorFactory);
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
		typedFactories.put(sernet.verinice.iso27k.model.Process.class, bsiEditorFactory);
		typedFactories.put(ProcessGroup.class, bsiEditorFactory);
		typedFactories.put(Record.class, bsiEditorFactory);
		typedFactories.put(RecordGroup.class, bsiEditorFactory);
		
		// Self Assessment (SAMT) elements
	
        typedFactories.put(SamtTopic.class, bsiEditorFactory);
		
		IEditorTypeFactory todoItemEditorFactory = new IEditorTypeFactory() {
			public void openEditorFor(Object o) throws Exception {
				IEditorPart editor;

				// replace element with new instance from DB:
				TodoViewItem selection = (TodoViewItem) o;
				CnATreeElement newElement = CnAElementHome.getInstance().loadById(MassnahmenUmsetzung.TYPE_ID, selection.getdbId());
				BSIElementEditorInput input = new BSIElementEditorInput(newElement);

				if ((editor = EditorRegistry.getInstance().getOpenEditor(input.getId())) == null) {
					// open new editor:
					editor = Activator.getActivePage().openEditor(input, BSIElementEditor.EDITOR_ID);
					EditorRegistry.getInstance().registerOpenEditor(input.getId(), editor);
				} else {
					// show existing editor:
					Activator.getActivePage().openEditor(editor.getEditorInput(), BSIElementEditor.EDITOR_ID);
				}
			}

		};

		typedFactories.put(TodoViewItem.class, todoItemEditorFactory);

		
		IEditorTypeFactory attachmentEditorFactory = new IEditorTypeFactory() {

			public void openEditorFor(Object o) throws Exception {
				IEditorPart editor;

				Attachment attachment = (Attachment) o;
				AttachmentEditorInput input = new AttachmentEditorInput(attachment);

				if ((editor = EditorRegistry.getInstance().getOpenEditor(input.getId())) == null) {
					// open new editor:
					editor = Activator.getActivePage().openEditor(input, AttachmentEditor.EDITOR_ID);
					EditorRegistry.getInstance().registerOpenEditor(String.valueOf(input.getId()), editor);
				} else {
					// show existing editor:
					((AttachmentEditorInput)editor.getEditorInput()).setInput(attachment);
					Activator.getActivePage().openEditor(editor.getEditorInput(), AttachmentEditor.EDITOR_ID);
				}
			}
			
		};
		typedFactories.put(Attachment.class, attachmentEditorFactory);
		
		IEditorTypeFactory noteEditorFactory = new IEditorTypeFactory() {

			public void openEditorFor(Object o) throws Exception {
				IEditorPart editor;

				// replace element with new instance from DB:
				Note selection = (Note) o;
				NoteEditorInput input = new NoteEditorInput(selection);

				if ((editor = EditorRegistry.getInstance().getOpenEditor(String.valueOf(input.getId()))) == null) {
					// open new editor:
					editor = Activator.getActivePage().openEditor(input, NoteEditor.EDITOR_ID);
					EditorRegistry.getInstance().registerOpenEditor(String.valueOf(input.getId()), editor);
				} else {
					// show existing editor:
					((NoteEditorInput)editor.getEditorInput()).setInput(selection);
					Activator.getActivePage().openEditor(editor.getEditorInput(), NoteEditor.EDITOR_ID);
				}
			}
			
		};
		typedFactories.put(Note.class, noteEditorFactory);
	}

	public static EditorFactory getInstance() {
		if (instance == null)
			instance = new EditorFactory();
		return instance;
	}

	/**
	 * Checks if an editor factory is registered for the given object type and
	 * opens a new editor for it.
	 * 
	 * @param sel
	 */
	public void openEditor(Object sel) {
		IEditorTypeFactory fact;
		if ((fact = typedFactories.get(sel.getClass())) != null) {
			try {
				fact.openEditorFor(sel);
			} catch (Exception e) {
				log.error("Error while opening editor.", e); //$NON-NLS-1$
				ExceptionUtil.log(e, Messages.EditorFactory_2);
			}
		}
	}

	public void updateAndOpenObject(Object sel) {
		if (sel instanceof CnATreeElement) {
			EditorFactory.getInstance().openEditor(sel);
		}
	}

}
