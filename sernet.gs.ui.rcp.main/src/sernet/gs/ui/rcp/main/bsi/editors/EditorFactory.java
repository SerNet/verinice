package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;

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
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.ds.model.Datenverarbeitung;
import sernet.gs.ui.rcp.main.ds.model.Personengruppen;
import sernet.gs.ui.rcp.main.ds.model.StellungnahmeDSB;
import sernet.gs.ui.rcp.main.ds.model.VerantwortlicheStelle;
import sernet.gs.ui.rcp.main.ds.model.Verarbeitungsangaben;

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
				BSIElementEditorInput input = new BSIElementEditorInput((CnATreeElement) o);
				
				
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
		
		typedFactories.put(Verarbeitungsangaben.class, bsiEditorFactory);
		typedFactories.put(VerantwortlicheStelle.class, bsiEditorFactory);
		typedFactories.put(Personengruppen.class, bsiEditorFactory);
		typedFactories.put(Datenverarbeitung.class, bsiEditorFactory);
		typedFactories.put(StellungnahmeDSB.class, bsiEditorFactory);
		
		
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
			} catch (Exception e) {
				ExceptionUtil.log(e, "Konnte Editor nicht öffnen.");
			}
		}
	}
	
	
}
