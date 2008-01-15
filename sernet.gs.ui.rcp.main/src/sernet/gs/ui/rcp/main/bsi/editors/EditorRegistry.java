package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.HashMap;

import org.eclipse.ui.IEditorPart;

/**
 * All open editors must register with this class with the object
 * that is being edited.
 * 
 * This class is used by the EditorFactory to get references to editors
 * for objects that are already open.
 * 
 * @author koderman@sernet.de
 *
 */
public class EditorRegistry {
	private static EditorRegistry instance;
	private HashMap<String, IEditorPart> openEditors = new HashMap<String, IEditorPart>();
	
	private EditorRegistry() {}
	
	public static EditorRegistry getInstance() {
		if (instance == null)
			instance = new EditorRegistry();
		return instance;
	}
	
	public IEditorPart getOpenEditor(String key) {
		return (IEditorPart)openEditors.get(key);
	}
	
	public void registerOpenEditor(String key, IEditorPart editor) {
		openEditors.put(key,editor);
	}
	
	public void closeEditor(String key) {
		openEditors.remove(key);
	}
}
