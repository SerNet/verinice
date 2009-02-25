package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.ChangeLogWatcher;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ObjectDeletedException;
import sernet.gs.ui.rcp.main.ds.model.Datenverarbeitung;
import sernet.gs.ui.rcp.main.ds.model.Personengruppen;
import sernet.gs.ui.rcp.main.ds.model.StellungnahmeDSB;
import sernet.gs.ui.rcp.main.ds.model.VerantwortlicheStelle;
import sernet.gs.ui.rcp.main.ds.model.Verarbeitungsangaben;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;

/**
 * This class maps editors for different ressources and either
 * opens a new editor or looks them up in the EditorRegistry
 * and shows an already open editor for an object
 * 
 * @author koderman@sernet.de
 *
 */
public class EditorFactory {
	private static EditorFactory instance;
	private static HashMap<Class, IEditorTypeFactory> typedFactories 
		= new HashMap<Class, IEditorTypeFactory>();
	
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
				CnATreeElement newElement 
					= CnAElementHome.getInstance().loadById(cnaElement.getClass(), cnaElement.getDbId());
				BSIElementEditorInput input = new BSIElementEditorInput(newElement);
				
				
				if ((editor = EditorRegistry.getInstance().getOpenEditor(input.getId())) == null) {
					// open new editor:
					editor = Activator.getActivePage().openEditor(input,
							BSIElementEditor.EDITOR_ID);
					EditorRegistry.getInstance().registerOpenEditor(input.getId(), editor);
				}
				else {
					// show existing editor:
					Activator.getActivePage().openEditor(editor.getEditorInput(),
							BSIElementEditor.EDITOR_ID);
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
				CnATreeElement newElement 
					= CnAElementHome.getInstance().loadById(MassnahmenUmsetzung.class, selection.getdbId());
				BSIElementEditorInput input = new BSIElementEditorInput(newElement);
				
				
				if ((editor = EditorRegistry.getInstance().getOpenEditor(input.getId())) == null) {
					// open new editor:
					editor = Activator.getActivePage().openEditor(input,
							BSIElementEditor.EDITOR_ID);
					EditorRegistry.getInstance().registerOpenEditor(input.getId(), editor);
				}
				else {
					// show existing editor:
					Activator.getActivePage().openEditor(editor.getEditorInput(),
							BSIElementEditor.EDITOR_ID);
				}
			}
			
		};
		
		typedFactories.put(TodoViewItem.class, todoItemEditorFactory);
		
		
		// TODO register more editor-factories here
	}
	
	public static EditorFactory getInstance() {
		if (instance == null)
			instance = new EditorFactory();
		return instance;
	}

	/**
	 * Checks if an editor factory is registered for the given object type
	 * and opens a new editor for it.
	 * @param sel
	 */
	public void openEditor(Object sel) {
		IEditorTypeFactory fact;
		if (( fact = typedFactories.get(sel.getClass()) ) != null) {
			try {
				fact.openEditorFor(sel);
			} catch (HibernateException e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						"Error", "Der Editor kann nicht geöffnet werden.");
			} catch (Exception e) {
				ExceptionUtil.log(e, "Konnte Editor nicht öffnen.");
			}
		}
	}
	
	public void updateAndOpenObject(Object sel) {
		try {
			ChangeLogWatcher.getInstance().updateChanges(sel);
			if (sel instanceof CnATreeElement) {
				EditorFactory.getInstance().openEditor(sel);
			}
		} catch (ObjectDeletedException e) {
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
					"Achtung", "Der Editor kann nicht geöffnet werden: " +
					"das Objekt wurde von einem anderen Benutzer gelöscht.");
		}
	}

	
	
	
}
