/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.hibernate.HibernateException;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
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

/**
 * This class maps editors for different ressources and either opens a new
 * editor or looks them up in the EditorRegistry and shows an already open
 * editor for an object
 * 
 * @author koderman@sernet.de
 * 
 */
public class EditorFactory {
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
				CnATreeElement newElement = CnAElementHome.getInstance().loadById(cnaElement.getClass(), cnaElement.getDbId());
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

		IEditorTypeFactory todoItemEditorFactory = new IEditorTypeFactory() {

			public void openEditorFor(Object o) throws Exception {
				IEditorPart editor;

				// replace element with new instance from DB:
				TodoViewItem selection = (TodoViewItem) o;
				CnATreeElement newElement = CnAElementHome.getInstance().loadById(MassnahmenUmsetzung.class, selection.getdbId());
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
					Activator.getActivePage().openEditor(editor.getEditorInput(), BSIElementEditor.EDITOR_ID);
				}
			}
			
		};
		typedFactories.put(Note.class, noteEditorFactory);

		// TODO register more editor-factories here
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
			} catch (HibernateException e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Der Editor kann nicht geöffnet werden.");
			} catch (Exception e) {
				ExceptionUtil.log(e, "Konnte Editor nicht öffnen.");
			}
		}
	}

	public void updateAndOpenObject(Object sel) {
		if (sel instanceof CnATreeElement) {
			EditorFactory.getInstance().openEditor(sel);
		}
	}

}
