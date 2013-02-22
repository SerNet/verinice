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

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.StatusLine;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.ILinkedWithEditorView;
import sernet.verinice.iso27k.rcp.LinkWithEditorPartListener;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("restriction")
public class BrowserView extends ViewPart implements ILinkedWithEditorView {
    
	private static final Logger LOG = Logger.getLogger(BrowserView.class);
	
	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.browserview"; //$NON-NLS-1$

	private Browser browser;

	private ISelectionListener selectionListener;
	
	private IPartListener2 linkWithEditorPartListener  = new LinkWithEditorPartListener(this);
    
    private boolean linkingActive = true;
    
    private SerialiseBrowserLoadingListener serialiseListener;

	public void createPartControl(Composite parent) {
		GridLayout gl = new GridLayout(1, false);
		parent.setLayout(gl);
        toggleLinking(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.LINK_TO_EDITOR));
		try {
			browser = new Browser(parent, SWT.NONE);
			browser.setLayoutData(new GridData(GridData.FILL_BOTH
					| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
			
			serialiseListener = new SerialiseBrowserLoadingListener(browser);
			browser.addProgressListener(serialiseListener);
			
			browser.setUrl(defaultImage());

		    Action linkWithEditorAction = new Action(Messages.BrowserView_0, IAction.AS_CHECK_BOX) {
	            @Override
	            public void run() {
	                toggleLinking(isChecked());
	            }
	        };
	        linkWithEditorAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.LINKED));
	        getViewSite().getActionBars().getToolBarManager().add(linkWithEditorAction);
	        linkWithEditorAction.setChecked(isLinkingActive());
			
			hookPageSelection();
		} catch (Exception e) {
			ExceptionUtil.log(e, Messages.BrowserView_3);
		}
	}
	
	protected void toggleLinking(boolean checked) {
        this.linkingActive = checked;
        if (checked) {
            editorActivated(getSite().getPage().getActiveEditor());
        }
    }
	
	protected boolean isLinkingActive() {
        return linkingActive;
    }
	
	public String getRightID(){
	    return ActionRightIDs.BSIBROWSER;
	}
	
	private String defaultImage() {
		return String.format("file:///%s/html/about.html", CnAWorkspace //$NON-NLS-1$
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
		getSite().getPage().addPartListener(linkWithEditorPartListener);
	}

	protected void pageSelectionChanged(IWorkbenchPart part,
			ISelection selection) {
		if (part == this){
			return;
		}
		if (!(selection instanceof IStructuredSelection)){
			return;
		}
		Object element = ((IStructuredSelection) selection).getFirstElement();
		elementSelected(element);
	}
	
	protected void elementSelected(Object element) {
	    try {
            StatusLine.setErrorMessage(""); //$NON-NLS-1$
            setText(HtmlWriter.getHtml(element));

        } catch (GSServiceException e) {
            StatusLine.setErrorMessage(Messages.BrowserView_4 + Messages.BrowserView_5);
            LOG.error(Messages.BrowserView_4 + Messages.BrowserView_5, e);
            browser.setUrl(defaultImage());
        }
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
	public void setText(String text) {
	    if(text!=null) {
	        serialiseListener.setText(text);
	    }
	}

	@Override
	public void dispose() {
		getSite().getPage().removePostSelectionListener(selectionListener);
		getSite().getPage().removePartListener(linkWithEditorPartListener);
		super.dispose();
	}

	/* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.ILinkedWithEditorView#editorActivated(org.eclipse.ui.IEditorPart)
     */
    @Override
    public void editorActivated(IEditorPart activeEditor) {
        if (!isLinkingActive() || !getViewSite().getPage().isPartVisible(this)) {
            return;
        }
        CnATreeElement element = BSIElementEditorInput.extractElement(activeEditor);
        if(element==null) {
            return;
        } 
        elementSelected(element);       
    }
}
