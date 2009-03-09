/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
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
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

public class BrowserView extends ViewPart {

	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.browserview"; //$NON-NLS-1$

	private Browser browser;

	private ISelectionListener selectionListener;

	public void createPartControl(Composite parent) {
		GridLayout gl = new GridLayout(1, false);
		parent.setLayout(gl);
		try {
			browser = new Browser(parent, SWT.NONE);
			browser.setLayoutData(new GridData(GridData.FILL_BOTH
					| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

			browser.setUrl(defaultImage());
			hookPageSelection();
		} catch (Exception e) {
			ExceptionUtil.log(e, Messages.BrowserView_3);
		}
	}

	private String defaultImage() {
		return String.format("file:///%s/html/about.html", CnAWorkspace
				.getInstance().getWorkdir()); //$NON-NLS-1$
	}

	private void hookPageSelection() {
		selectionListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {
				pageSelectionChanged(part, selection);
			}
		};
		getSite().getPage().addPostSelectionListener(selectionListener);

	}

	protected void pageSelectionChanged(IWorkbenchPart part,
			ISelection selection) {
		if (part == this)
			return;

		if (!(selection instanceof IStructuredSelection))
			return;

		Object element = ((IStructuredSelection) selection).getFirstElement();
		try {
			StatusLine.setErrorMessage("");
			if (element instanceof Massnahme) {
				Massnahme mn = (Massnahme) element;
				setUrl(BSIMassnahmenModel.getMassnahme(mn.getUrl(), mn
						.getStand()));
			}

			if (element instanceof Baustein) {
				Baustein bst = (Baustein) element;
				setUrl(BSIMassnahmenModel.getBaustein(bst.getUrl(), bst
						.getStand()));
			}

			if (element instanceof Gefaehrdung) {
				Gefaehrdung gef = (Gefaehrdung) element;
				setUrl(BSIMassnahmenModel.getGefaehrdung(gef.getUrl(), gef
						.getStand()));
			}

			if (element instanceof GefaehrdungsUmsetzung) {
				GefaehrdungsUmsetzung gefUms = (GefaehrdungsUmsetzung) element;
				if (gefUms.getUrl() == null || gefUms.getUrl().equals("null")) {
					// try OwnGefaehrdung:
					browser.stop();
					browser.setText(toHtml(gefUms));
					return;
				}
				
				setUrl(BSIMassnahmenModel.getGefaehrdung(gefUms.getUrl(),
						gefUms.getStand()));
			}

			if (element instanceof RisikoMassnahmenUmsetzung) {
				RisikoMassnahmenUmsetzung ums = (RisikoMassnahmenUmsetzung) element;
				if (ums.getRisikoMassnahme() != null) {
					browser.stop();
					browser.setText(toHtml(ums));
					return;
				}
			}

			if (element instanceof MassnahmenUmsetzung) {
				MassnahmenUmsetzung mnu = (MassnahmenUmsetzung) element;
				setUrl(BSIMassnahmenModel.getMassnahme(mnu.getUrl(), mnu
						.getStand()));
			}
			
			if (element instanceof TodoViewItem) {
				TodoViewItem item = (TodoViewItem) element;
				setUrl(BSIMassnahmenModel.getMassnahme(item.getUrl(), item.getStand())
						);
			}

			if (element instanceof BausteinUmsetzung) {
				BausteinUmsetzung bst = (BausteinUmsetzung) element;
				setUrl(BSIMassnahmenModel.getBaustein(bst.getUrl(), bst
						.getStand()));
			}

		} catch (GSServiceException e) {
			StatusLine.setErrorMessage(e.getMessage());
			Logger.getLogger(this.getClass()).error(
					Messages.BrowserView_4 + Messages.BrowserView_5);
			browser.setUrl(defaultImage());
		}

	}

	private String toHtml(GefaehrdungsUmsetzung ums) {
		StringBuffer buf = new StringBuffer();
		String cssDir = CnAWorkspace.getInstance().getWorkdir()
				+ File.separator + "html" + File.separator + "screen.css"; //$NON-NLS-1$ //$NON-NLS-2$

		buf
				.append("<html><head>"
						+ "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=iso-8859-1\"/>\n"
						+ "<link REL=\"stylesheet\" media=\"screen\" HREF=\""
						+ cssDir + "\"/>"
						+ "</head><body><div id=\"content\"><h1>");
		buf.append(ums.getId() + " " + ums.getTitel());
		buf.append("</h1><p>");
		buf.append("");
		buf.append(ums.getDescription().replaceAll("\\n", "<br/>"));
		buf.append("</p></div></body></html>");
		return buf.toString();
	}

	private String toHtml(RisikoMassnahmenUmsetzung ums) {
		StringBuffer buf = new StringBuffer();
		String cssDir = CnAWorkspace.getInstance().getWorkdir()
				+ File.separator + "html" + File.separator + "screen.css"; //$NON-NLS-1$ //$NON-NLS-2$

		buf
				.append("<html><head>"
						+ "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=iso-8859-1\"/>\n"
						+ "<link REL=\"stylesheet\" media=\"screen\" HREF=\""
						+ cssDir + "\"/>"
						+ "</head><body><div id=\"content\"><h1>");
		buf.append(ums.getNumber() + " " + ums.getName());
		buf.append("</h1><p>");
		buf.append("");
		buf.append(ums.getDescription().replaceAll("\\n", "<br/>"));
		buf.append("</p></div></body></html>");
		return buf.toString();
	}

	public void setFocus() {
		browser.setFocus();
	}

	/**
	 * Sets the contents to be displayed in the browser window.
	 * 
	 * @param is
	 *            The HTML page to be displayed as an input stream
	 */
	public void setUrl(InputStream is) {
		try {
			InputStreamReader read = new InputStreamReader(is, "iso-8859-1"); //$NON-NLS-1$
			BufferedReader buffRead = new BufferedReader(read);
			StringBuilder b = new StringBuilder();
			String line;
			boolean skip = false;
			boolean skipComplete = false;
			String cssDir = CnAWorkspace.getInstance().getWorkdir()
					+ File.separator + "html" + File.separator + "screen.css"; //$NON-NLS-1$ //$NON-NLS-2$

			while ((line = buffRead.readLine()) != null) {
				if (!skipComplete) {
					if (line.matches(".*div.*class=\"standort\".*")) //$NON-NLS-1$
						skip = true;
					else if (line.matches(".*div.*id=\"content\".*")) { //$NON-NLS-1$
						skip = false;
						skipComplete = true;
					}
				}

				// Logger.getLogger(this.getClass()).debug("PRE: " + line);

				// we strip away images et al to keep just the information we
				// need:
				line = line.replace("../../../screen.css", cssDir); //$NON-NLS-1$
				line = line.replace("../../screen.css", cssDir); //$NON-NLS-1$
				line = line.replace("../screen.css", cssDir); //$NON-NLS-1$
				line = line.replaceAll("<a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
				line = line.replaceAll("</a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
				line = line.replaceAll("<img.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
				line = line.replace((char) 160, ' '); // replace non-breaking
														// spaces

				// Logger.getLogger(this.getClass()).debug("POST: " + line);

				if (!skip) {
					// Logger.getLogger(BrowserView.class).debug(line);
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
