/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.iso27k.rcp.action;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.part.IDropActionDelegate;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.PluginTransferData;

import sernet.verinice.model.iso27k.Group;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class FileDropPerformer extends PluginDropAdapter  implements DropPerformer {

    private static final Logger LOG = Logger.getLogger(FileDropPerformer.class);
    
    private boolean isActive = false;
    
    private Group group = null;
    
    public FileDropPerformer(TreeViewer viewer) {
        super(viewer);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#performDrop(java.lang.Object, java.lang.Object, org.eclipse.jface.viewers.Viewer)
     */
    @Override
    public boolean performDrop(Object data, Object target, Viewer viewer) {
        return performDrop(data);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object target, int operation, TransferData data) {
        isActive = FileTransfer.getInstance().isSupportedType(data)
                && (target instanceof Group);      
        if (LOG.isDebugEnabled()) {
            LOG.debug("Target: " + target);
            LOG.debug("validateDrop: " + isActive);
        }
        if(isActive) {
            group = (Group) target;
        } else {
            group = null;
        }
        return isActive;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
     */
    @Override
    public boolean isActive() {
        return isActive;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    @Override
    public boolean performDrop(Object data) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("performDrop... ");
        }
        try { 
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bao);
            oo.writeObject(data);
            PluginTransferData pluginData = new PluginTransferData("sernet.verinice.ismview.filedrop", bao.toByteArray());
            IDropActionDelegate delegate = getPluginAdapter(pluginData); 
            if(delegate!=null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Drop adapter found in plugin.xml: " + delegate.getClass().getName()+ ", group: " + group);
                }
                delegate.run(pluginData.getData(), group);
            }
        } catch (Exception e) {
           LOG.error("Error while performing file drop", e);
        }
        return false;
    }
    
    protected static IDropActionDelegate getPluginAdapter(PluginTransferData data) throws CoreException {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        String adapterName = data.getExtensionId();
        IExtensionPoint xpt = registry.getExtensionPoint(PlatformUI.PLUGIN_ID,IWorkbenchRegistryConstants.PL_DROP_ACTIONS);
        IExtension[] extensions = xpt.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] configs = extensions[i].getConfigurationElements();
            if (configs != null && configs.length > 0) {
                for (int j=0; j < configs.length; j++) {
                    String id = configs[j].getAttribute("id");//$NON-NLS-1$
                    if (id != null && id.equals(adapterName)) {
                        return (IDropActionDelegate) WorkbenchPlugin.createExtension(configs[j], ATT_CLASS);
                    }
                }
            }
        }
        return null;
    }

 
}
