/*******************************************************************************
 * Copyright (c) 2015 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;

import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.bsi.views.HtmlWriter;
import sernet.gs.ui.rcp.main.bsi.views.SerializeBrowserLoadingListener;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;

/**
 * Listener for updating the Browser in the RiskAnalysisWizard as soon as the
 * selection in the viewer changes
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public final class RiskAnalysisWizardBrowserUpdateListener implements
        ISelectionChangedListener {
    private TableViewer viewer;
    private SerializeBrowserLoadingListener browserLoadingListener;
    private static final Logger LOG = Logger.getLogger(RiskAnalysisWizardBrowserUpdateListener.class);
    private Object viewedElement = null;
    public static final SelectionChangedEvent UPDATE_CURRENT = null;

    public RiskAnalysisWizardBrowserUpdateListener(SerializeBrowserLoadingListener browserLoadingListener, TableViewer viewer) {
        this.viewer = viewer;
        this.browserLoadingListener = browserLoadingListener;
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {

        if (!event.equals(UPDATE_CURRENT) && event.getSource() == viewer) {
            IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            if (selection != null && !selection.isEmpty()) {
                viewedElement = selection.getFirstElement();
            }
        }
        if (viewedElement == null) {
            LOG.warn("viewedElement cannot be null at this point");
            return;
        }
        if (viewedElement instanceof OwnGefaehrdung) {
            setOwnGefaehrdungDescription(viewedElement);
        } else if (viewedElement instanceof RisikoMassnahmenUmsetzung) {
            setRisikoMassnahmenUmsetzungDescription(viewedElement);
        } else {
            renderAndSetHtmlDescription(viewedElement);
        }
    }

    private void renderAndSetHtmlDescription(Object firstElement) {
        try {
            String htmlText = HtmlWriter.getHtml(firstElement);
            htmlText = htmlText.replaceAll("<hr[^>]*>", "<br><hr width=\"90%\">");
            browserLoadingListener.setText(htmlText);
        } catch (GSServiceException e) {
            LOG.error(e);
        }
    }

    private void setRisikoMassnahmenUmsetzungDescription(Object firstElement) {

        RisikoMassnahmenUmsetzung risikoMassnahmenUmsetzung = (RisikoMassnahmenUmsetzung) firstElement;
        if (risikoMassnahmenUmsetzung.getText() != null) {
            browserLoadingListener.setText(risikoMassnahmenUmsetzung.getText());
        } else {
            browserLoadingListener.setText(Messages.RiskAnalysisBrowserUpdateListener_0);
        }
    }

    private void setOwnGefaehrdungDescription(Object firstElement) {

        OwnGefaehrdung ownGefaehrdung = (OwnGefaehrdung) firstElement;
        if (ownGefaehrdung.getBeschreibung() != null) {
            browserLoadingListener.setText(ownGefaehrdung.getBeschreibung());
        } else {
            browserLoadingListener.setText(Messages.RiskAnalysisBrowserUpdateListener_0);
        }
    }
}
