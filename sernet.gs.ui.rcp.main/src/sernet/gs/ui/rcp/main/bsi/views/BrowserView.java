/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

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
import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.StatusLine;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

public class BrowserView extends ViewPart {
    
	private static final Logger LOG = Logger.getLogger(BrowserView.class);
	
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
				setText(GSScraperUtil.getInstance().getModel().getMassnahmeHtml(mn.getUrl(), mn.getStand()));
			}

			if (element instanceof Baustein) {
				Baustein bst = (Baustein) element;
				setUrl(GSScraperUtil.getInstance().getModel().getBaustein(bst.getUrl(), bst.getStand()), bst.getEncoding());
			}

			if (element instanceof Gefaehrdung) {
				Gefaehrdung gef = (Gefaehrdung) element;
				setUrl(GSScraperUtil.getInstance().getModel().getGefaehrdung(gef.getUrl(), gef.getStand()), gef.getEncoding());
			}

			if (element instanceof GefaehrdungsUmsetzung) {
				GefaehrdungsUmsetzung gefUms = (GefaehrdungsUmsetzung) element;
				if (gefUms.getUrl() == null || gefUms.getUrl().equals("null")) {
					// try OwnGefaehrdung:
					browser.stop();
					browser.setText(toHtml(gefUms));
					return;
				}
				
				setUrl(GSScraperUtil.getInstance().getModel().getGefaehrdung(gefUms.getUrl(),gefUms.getStand()), "iso-8859-1");
			}

			if (element instanceof RisikoMassnahmenUmsetzung) {
				RisikoMassnahmenUmsetzung ums = (RisikoMassnahmenUmsetzung) element;
				RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(ums);
				if (ums.getRisikoMassnahme() != null) {
					browser.stop();
					browser.setText(toHtml(ums));
					return;
				}
			}

			if (element instanceof MassnahmenUmsetzung) {
				MassnahmenUmsetzung mnu = (MassnahmenUmsetzung) element;
				setText(GSScraperUtil.getInstance().getModel().getMassnahmeHtml(mnu.getUrl(), mnu.getStand()));
			}
			
			if (element instanceof TodoViewItem) {
				TodoViewItem item = (TodoViewItem) element;
				setText(GSScraperUtil.getInstance().getModel().getMassnahmeHtml(item.getUrl(), item.getStand()));
			}

			if (element instanceof BausteinUmsetzung) {
				BausteinUmsetzung bst = (BausteinUmsetzung) element;
				setUrl(GSScraperUtil.getInstance().getModel().getBaustein(bst.getUrl(), bst.getStand()), bst.getEncoding());
			}
			
			if (element instanceof IItem) {
				IItem item = (IItem) element;
				StringBuilder sb = new StringBuilder();
				writeHtml(sb, item.getName(), item.getDescription(), VeriniceCharset.CHARSET_UTF_8.name());
				setText(sb.toString());	
			}
			
			if (element instanceof IControl) {
			    IControl control = (IControl) element;
				StringBuilder sb = new StringBuilder();
				writeHtml(sb, control.getTitle(), control.getDescription(), VeriniceCharset.CHARSET_UTF_8.name());
				setText(sb.toString());			
			}
			
			if (element instanceof Threat) {
			    Threat item = (Threat) element;
                StringBuilder sb = new StringBuilder();
                writeHtml(sb, item.getTitle(), item.getDescription(), VeriniceCharset.CHARSET_UTF_8.name());
                setText(sb.toString());         
            }

			if (element instanceof Vulnerability) {
			    Vulnerability item = (Vulnerability) element;
			    StringBuilder sb = new StringBuilder();
			    writeHtml(sb, item.getTitle(), item.getDescription(), VeriniceCharset.CHARSET_UTF_8.name());
			    setText(sb.toString());         
			}
			

		} catch (GSServiceException e) {
			StatusLine.setErrorMessage(e.getMessage());
			Logger.getLogger(this.getClass()).error(
					Messages.BrowserView_4 + Messages.BrowserView_5);
			browser.setUrl(defaultImage());
		}

	}

	private String toHtml(GefaehrdungsUmsetzung ums) {
		StringBuilder buf = new StringBuilder();
		writeHtml(buf, ums.getId() + " " + ums.getTitle(), ums.getDescription(), "iso-8859-1");
		return buf.toString();
	}
	
	private void writeHtml(StringBuilder buf, String headline, String bodytext, String encoding) {
		String cssDir = CnAWorkspace.getInstance().getWorkdir()+ File.separator + "html" + File.separator + "screen.css"; //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("<html><head>");
		buf.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=").append(encoding).append("\"/>\n");
		buf.append("<link REL=\"stylesheet\" media=\"screen\" HREF=\"").append(cssDir).append("\"/>");
		buf.append("</head><body><div id=\"content\"><h1>");
		buf.append(headline);
		buf.append("</h1><p>");
		buf.append("");
		buf.append(bodytext.replaceAll("\\n", "<br/>"));
		buf.append("</p></div></body></html>");
	}

	private String toHtml(RisikoMassnahmenUmsetzung ums) {
		StringBuilder buf = new StringBuilder();
		RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(ums);
		writeHtml(buf, ums.getNumber() + " " + ums.getName(), ums.getDescription(), "iso-8859-1");
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
	public void setUrl(InputStream is, String encoding) {
		try {
			if ( !(encoding.equalsIgnoreCase("iso-8859-1") || encoding.equalsIgnoreCase("utf-8")) ) {
				encoding = "utf-8";
			}
			
			InputStreamReader read = new InputStreamReader(is, encoding); //$NON-NLS-1$
			BufferedReader buffRead = new BufferedReader(read);
			StringBuilder b = new StringBuilder();
			String line;
			boolean skip = false;
			boolean skipComplete = false;
			String cssDir = CnAWorkspace.getInstance().getWorkdir()
					+ File.separator + "html" + File.separator + "screen.css"; //$NON-NLS-1$ //$NON-NLS-2$

			while ((line = buffRead.readLine()) != null) {
				if (!skipComplete) {
					if (line.matches(".*div.*id=\"menuoben\".*")
							|| line.matches(".*div.*class=\"standort\".*")) //$NON-NLS-1$
						skip = true;
					else if (line.matches(".*div.*id=\"content\".*")) { //$NON-NLS-1$
						skip = false;
						skipComplete = true;
					}
				}

				// Logger.getLogger(this.getClass()).debug("PRE: " + line);

				// we strip away images et al to keep just the information we
				// need:
				line = line.replace("../../media/style/css/screen.css", cssDir); //$NON-NLS-1$
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
	
	/**
	 * Sets the contents to be displayed in the browser window.
	 * 
	 * @param is
	 *            The HTML page to be displayed as an input stream
	 */
	public void setText(String text) {
		browser.stop();
		browser.setText(text);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePostSelectionListener(selectionListener);
		super.dispose();
	}
}
