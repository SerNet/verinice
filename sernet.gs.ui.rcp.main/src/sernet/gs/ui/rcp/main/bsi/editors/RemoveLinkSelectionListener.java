/*******************************************************************************
 * Copyright (c) 2015 Moritz Reiter.
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
 *     Moritz Reiter - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.model.common.CnALink;

/**
 * @author Moritz Reiter
 */
@SuppressWarnings("restriction")
final class RemoveLinkSelectionListener implements SelectionListener {
    
    private static final Logger LOGGER = Logger.getLogger(RemoveLinkSelectionListener.class);
    
    private final LinkMaker linkMaker;
    
    public RemoveLinkSelectionListener(LinkMaker linkMaker) {
        this.linkMaker = linkMaker;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        // delete link:
        if (linkMaker.viewer.getSelection().isEmpty()) {
            return;
        }
        List<?> selection = ((IStructuredSelection) linkMaker.viewer.getSelection()).toList();
        String msg = NLS.bind(Messages.LinkMaker_6, selection.size());
        boolean confirm = MessageDialog.openConfirm(linkMaker.getShell(), Messages.LinkMaker_5, msg);
        if (!confirm) {
            return;
        }
        CnALink link = null;
        for (Object object : selection) {
            link = (CnALink) object;
            try {
                CnAElementHome.getInstance().remove(link);
                linkMaker.inputElmt.removeLinkDown(link);
            } catch (Exception e1) {
                LOGGER.error("Error while removing link", e1);
                ExceptionUtil.log(e1, Messages.LinkMaker_7);
            }
        }
        // calling linkRemoved for one link reloads all changed links
        if (link != null) {
            // notify local listeners:
            if (CnAElementFactory.isModelLoaded()) {
                CnAElementFactory.getLoadedModel().linkRemoved(link);
            }
            CnAElementFactory.getInstance().getISO27kModel().linkRemoved(link);
        }
    }
}
