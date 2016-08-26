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
package sernet.verinice.rcp;

import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.dialogs.UserValidationDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ImageCache;

/**
 * Implementation of OSGI service org.eclipse.equinox.p2.core.UIServices
 * which provides authentication info for a HTTP request. You can set a 
 * custom message for the auth dialog in this service.
 * 
 * Instances of this service are created by factory class {@link ServiceUIComponent}.
 * This service factory is configured in file OSGI-INF/uiservice.xml
 * and in the section "Service-Component:" in META-INF/MANIFEST.MF. 
 *
 * See this article about OSGi Declarative Services:
 * http://www.ibm.com/support/knowledgecenter/de/SSEQTP_8.5.5/com.ibm.websphere.wlp.doc/ae/twlp_declare_services_ds.html
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
@SuppressWarnings("restriction")
public class ValidationServiceUI
        extends org.eclipse.equinox.internal.p2.ui.ValidationDialogServiceUI {

    
    /*
     * (non-Javadoc)
     * @see org.eclipse.equinox.internal.provisional.p2.core.IServiceUI#getUsernamePassword(java.lang.String)
     */
    @Override
    public AuthenticationInfo getUsernamePassword(final String url) {
        final AuthenticationInfo[] result = new AuthenticationInfo[1];
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                Shell shell = ProvUI.getDefaultParentShell();
                String title = Messages.ValidationServiceUI_1;
                String message = Messages.ValidationServiceUI_0;
                Image icon = ImageCache.getInstance().getImage(ImageCache.UPDATE_SITE);
                UserValidationDialog dialog = new UserValidationDialog(shell, title, icon, message);
                if (dialog.open() == Window.OK) {
                    result[0] = dialog.getResult();
                }
            }

        });
        return result[0];
    }
}
