/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;

/**
 * Interface for views which are linked to editor content.
 * Use this interface together with {@sernet.verinice.iso27k.rcp.LinkWithEditorPartListener} 
 * as follows:
 * 
 * public class MyView extends ViewPart implements ILinkedWithEditorView {
 *   ...
 *   IPartListener2 linkWithEditorPartListener  = new LinkWithEditorPartListener(this);
 *   ...  
 *   public void createPartControl(Composite parent) {
 *     ...
 *     getSite().getPage().addPartListener(linkWithEditorPartListener);
 *     ...
 *   }
 *   ...
 *   public void editorActivated(IEditorPart editor) {
 *     // update view with content from editor
 *     ...
 *   }
 *   ...
 * }
 *   
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ILinkedWithEditorView {

    /**
     * @return
     */
    IViewSite getViewSite();

    /**
     * @param activeEditor
     */
    void editorActivated(IEditorPart activeEditor);

}
