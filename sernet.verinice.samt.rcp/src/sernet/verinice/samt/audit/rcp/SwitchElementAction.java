/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.samt.audit.rcp;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.hui.common.connect.HitroUtil;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Set the {@link CnATreeElement} type which is displayed in a {@link GenericElementView}
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class SwitchElementAction extends Action {
    
    private GenericElementView groupView;

    private String objectTypeId;

    /**
     * Creates an action to set the typeId of an groupView
     * 
     * @param groupView the view the type is displyed
     * @param typeId {@link CnATreeElement} type
     */
    public SwitchElementAction(GenericElementView groupView, String typeId) {
        this.groupView = groupView;
        this.objectTypeId = typeId;
        String title = HitroUtil.getInstance().getTypeFactory().getMessage(typeId);
        setText(title);
        setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getISO27kTypeImage(objectTypeId)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        groupView.switchElement(objectTypeId);
    }
}
