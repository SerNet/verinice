/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable;

import java.util.UUID;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;

/**
 * The user can edit link table configurations with a link table editor ({@link LinkTableEditor}).
 * This class wraps the input for a link table editor. Link table configurations are saved in .vlt
 * files. Class {@link VeriniceLinkTable} is used for (de-)serialization of the .vlt files.
 * 
 * LinkTableEditorInput contains an instance of class {@link VeriniceLinkTable}, the file path of 
 * the .vlt file and a UUID for unsaved configurations. The file path is utilized as a primary 
 * key for a link table editor input. To distinguish between unsaved instances a UUID is created
 * and used as a key until the instance is saved to a file.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class LinkTableEditorInput implements IEditorInput {

    /**
     * Default tool tip which is shown if tool tip is <code>null</code>. 
     */
    private static final String TOOL_TIP_DEFAULT = "";
    
    /**
     * The link table configuration, the content of a .vlt file
     */
    private VeriniceLinkTable veriniceLinkTable;
    
    /**
     * The path to the .vlt file
     */
    private String filePath;
    
    /**
     * Used until the instance is saved to a file to distinguish between unsaved instances
     */
    private String uuid;
    
    public LinkTableEditorInput(VeriniceLinkTable veriniceLinkTable) {
        super();
        this.veriniceLinkTable = veriniceLinkTable;
        this.uuid = UUID.randomUUID().toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    @Override
    public String getName() {
        return LinkTableEditor.getEditorTitle(getFilePath());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    @Override
    public String getToolTipText() {
        String toolTip = getFilePath();
        if(toolTip==null) {
            toolTip = TOOL_TIP_DEFAULT;
        }
        return toolTip;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes") // See: IAdaptable
    @Override
    public Object getAdapter(Class arg0) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    @Override
    public boolean exists() {
        return getFilePath()!=null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    @Override
    public IPersistableElement getPersistable() {
        return null;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LinkTableEditorInput other = (LinkTableEditorInput) obj;
        if (filePath == null) {
            if (other.filePath != null)
                return false;
        } else if (!filePath.equals(other.filePath))
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }

    public String getId() {
        String id = getFilePath();
        if(id==null) {
            id = uuid;
        }
        return id;
    }

    public VeriniceLinkTable getInput() {
        return veriniceLinkTable;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
