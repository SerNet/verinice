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

public final class RiskAnalysisWizardBrowserUpdateListener implements
        ISelectionChangedListener {
    private TableViewer viewer;
    private SerializeBrowserLoadingListener browserLoadingListener;
    private static final Logger LOG = Logger.getLogger(RiskAnalysisWizardBrowserUpdateListener.class);
    private Object viewedElement = null;

    public RiskAnalysisWizardBrowserUpdateListener(SerializeBrowserLoadingListener browserLoadingListener, TableViewer viewer) {
        this.viewer = viewer;
        this.browserLoadingListener = browserLoadingListener;
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {

        if (event != null && event.getSource() != null && event.getSelection() != null) {
            Object eventSource = event.getSource();
            if (eventSource == viewer) {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

                if (selection != null && !selection.isEmpty()) {
                    viewedElement = selection.getFirstElement();
                }
            }
        }
        if (viewedElement != null && viewedElement instanceof OwnGefaehrdung) {
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
            // TODO rmotza delete hr?
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
