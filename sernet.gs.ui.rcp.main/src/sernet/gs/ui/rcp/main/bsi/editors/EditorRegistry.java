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

import org.eclipse.ui.IEditorPart;

/**
 * All open editors must register with this class with the object
 * that is being edited.
 * 
 * This class is used by the EditorFactory to get references to editors
 * for objects that are already open.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public final class EditorRegistry {
	private static EditorRegistry instance;
	private Map<String, IEditorPart> openEditors = new HashMap<String, IEditorPart>();
	
	private EditorRegistry() {}
	
	public static EditorRegistry getInstance() {
		if (instance == null){
			instance = new EditorRegistry();
		}
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
