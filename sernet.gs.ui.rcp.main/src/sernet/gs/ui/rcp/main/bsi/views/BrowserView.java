package sernet.gs.ui.rcp.main.bsi.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.StatusLine;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;

public class BrowserView extends ViewPart {

	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.browserview"; //$NON-NLS-1$

	private Browser browser;

	private ISelectionListener selectionListener;
	
	public void createPartControl(Composite parent) {
		GridLayout gl = new GridLayout(1, false);
		parent.setLayout(gl);
		try {
			browser = new Browser(parent, SWT.NONE);
			browser.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
					| GridData.GRAB_VERTICAL));
		
			browser.setUrl(defaultImage());
			hookPageSelection();
		} catch (Exception e) {
			ExceptionUtil.log(e, Messages.BrowserView_3);
		}
	}

	private String defaultImage() {
		return String.format("file:///%s/html/about.html",
				CnAWorkspace.getInstance().getWorkdir()); //$NON-NLS-1$
	}

	private void hookPageSelection() {
		selectionListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				pageSelectionChanged(part, selection);
			}
		};
		getSite().getPage().addPostSelectionListener(selectionListener);
		
	}

	protected void pageSelectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part == this)
			return;
		
		if (! (selection instanceof IStructuredSelection))
			return;
		
		Object element = ((IStructuredSelection)selection).getFirstElement();
		try {
			StatusLine.setErrorMessage("");
			if (element instanceof Massnahme) {
				Massnahme mn = (Massnahme) element;
				setUrl(BSIMassnahmenModel.getMassnahme(mn.getUrl(), mn.getStand()));
			}
			
			if (element instanceof Baustein) {
				Baustein bst = (Baustein) element;
				setUrl(BSIMassnahmenModel.getBaustein(bst.getUrl(), bst.getStand()));
			}
			
			if (element instanceof Gefaehrdung) {
				Gefaehrdung gef = (Gefaehrdung) element;
				setUrl(BSIMassnahmenModel.getGefaehrdung(gef.getUrl(), gef.getStand()));
			}
			
			if (element instanceof MassnahmenUmsetzung) {
				MassnahmenUmsetzung mnu = (MassnahmenUmsetzung) element;
				setUrl(BSIMassnahmenModel.getMassnahme(mnu.getUrl(), mnu.getStand()));
			}
			
			if (element instanceof BausteinUmsetzung) {
				BausteinUmsetzung bst = (BausteinUmsetzung) element;
				setUrl(BSIMassnahmenModel.getBaustein(bst.getUrl(), bst.getStand()));
			}
			
		} catch (GSServiceException e) {
			StatusLine.setErrorMessage(e.getMessage());
			Logger.getLogger(this.getClass()).error(Messages.BrowserView_4 +
					Messages.BrowserView_5);
			browser.setUrl(defaultImage());
		}
		
	}

	public void setFocus() {
		browser.setFocus();
	}
	
	/**
	 * Sets the contents to be displayed in the browser window.
	 * 
	 * @param is The HTML page to be displayed as an input stream
	 */
	public void setUrl(InputStream is) {
		try {
			InputStreamReader read = new InputStreamReader(is, "iso-8859-1"); //$NON-NLS-1$
			BufferedReader buffRead = new BufferedReader(read);
			StringBuilder b = new StringBuilder();
			String line;
			boolean skip = false;
			boolean skipComplete = false;
			String cssDir = CnAWorkspace.getInstance().getWorkdir() + File.separator
							+ "html"+ File.separator + "screen.css"; //$NON-NLS-1$ //$NON-NLS-2$
			
			
			while ((line = buffRead.readLine()) != null) {
				if (!skipComplete) {
					if (line.matches(".*div.*class=\"standort\".*")) //$NON-NLS-1$
						skip = true;
					else if (line.matches(".*div.*id=\"content\".*")) { //$NON-NLS-1$
						skip = false;
						skipComplete = true;
					}
				}
				
//				Logger.getLogger(this.getClass()).debug("PRE:  " + line);
	
				// we strip away images et al to keep just the information we need:
				line = line.replace("../../../screen.css", cssDir); //$NON-NLS-1$
				line = line.replace("../../screen.css", cssDir); //$NON-NLS-1$
				line = line.replace("../screen.css", cssDir); //$NON-NLS-1$
				line = line.replaceAll("<a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
				line = line.replaceAll("</a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
				line = line.replaceAll("<img.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
				line = line.replace((char)160,' '); // replace non-breaking spaces 
				
//				Logger.getLogger(this.getClass()).debug("POST: " + line);
				
				if (!skip) {
					//Logger.getLogger(BrowserView.class).debug(line);
					b.append(line);
				}
			}
			browser.stop();
			browser.setText(b.toString());
		} catch (Exception e) {
			Logger.getLogger(BrowserView.class).error(e);
		}
	}
	
	@Override
	public void dispose() {
		getSite().getPage().removePostSelectionListener(selectionListener);
		super.dispose();
	}
}
