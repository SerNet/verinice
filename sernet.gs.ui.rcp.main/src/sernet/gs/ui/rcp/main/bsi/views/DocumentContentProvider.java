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

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.URLUtil;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.DocumentLink;
import sernet.verinice.model.bsi.DocumentLinkRoot;
import sernet.verinice.model.bsi.DocumentReference;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.validation.CnAValidation;

/**
 * Content Provider for document view.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class DocumentContentProvider implements ITreeContentProvider, IBSIModelListener {



	private TreeViewer viewer;
	
	private static final int ADD     = 0;
	private static final int UPDATE  = 1;
	private static final int REMOVE  = 2;
	private static final int REFRESH = 3;
	
	private static final String DOCUMENT_PROPERTY_SUFFIX_GERMAN = "_dokument";
	private static final String DOCUMENT_PROPERTY_SUFFIX_ENGLISH = "_document";
	
	public DocumentContentProvider(TreeViewer viewer) {
		this.viewer = viewer;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DocumentLink) {
			DocumentLink doclink = (DocumentLink) parentElement;
			Set<DocumentReference> children = doclink.getChildren();
			return (DocumentReference[]) children
					.toArray(new DocumentReference[children.size()]);
		}
		else if (parentElement instanceof DocumentLinkRoot) {
			DocumentLinkRoot root = (DocumentLinkRoot) parentElement;
			return root.getChildren();
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
	    if (element instanceof DocumentReference) {
			DocumentReference ref = (DocumentReference) element;
			return ref.getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
	    if (element instanceof DocumentLink) {
			DocumentLink doclink = (DocumentLink) element;
			return doclink.getChildren().size() > 0;
		}
		return false;
	}

	public void dispose() {
		if (CnAElementFactory.getLoadedModel() != null){
			CnAElementFactory.getLoadedModel().removeBSIModelListener(this);
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput,
			Object newInput) {
		if (CnAElementFactory.getLoadedModel() != null) {
		    CnAElementFactory.getLoadedModel().removeBSIModelListener(this);
			CnAElementFactory.getLoadedModel().addBSIModelListener(this);
		}
		
		modelRefresh(null);
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void childAdded(CnATreeElement category, CnATreeElement child) {
		if(hasDocumentProperty(child) && !getDocumentPropertyId(child).isEmpty()){
		    updateViewer(ADD, child, category );
		    modelRefresh(null);
		}
	}
	
	private boolean hasDocumentProperty(CnATreeElement element){
	    Activator.inheritVeriniceContextState();
	    for(String id : HUITypeFactory.getInstance().getEntityType(element.getEntityType().getId()).getAllPropertyTypeIds()){
	        if(isDocumentProperty(id)){
	            return true;
	        }
	    }
	    return false;
	}
	
	private String getDocumentPropertyId(CnATreeElement element){
        for(String id : HUITypeFactory.getInstance().getEntityType(element.getEntityType().getId()).getAllPropertyTypeIds()){
            if(isDocumentProperty(id)){
                return id;
            }
        }
        return null;
	}
	
	private boolean isDocumentProperty(String id){
	    if(id != null && (id.contains(DOCUMENT_PROPERTY_SUFFIX_ENGLISH) || id.contains(DOCUMENT_PROPERTY_SUFFIX_GERMAN))){
	        return true;
	    }
	    return false;
	}

	public void childChanged(CnATreeElement child) {
	    try{
	        if(hasDocumentProperty(child)){
	            DocumentLinkRoot dlr = (DocumentLinkRoot)viewer.getInput();
	            if(!getDocumentPropertyId(child).isEmpty()){
	                setThreadSetViewerInput(addInputElement(dlr, child));
	            } else { // documentProperty is existant but empty, remove link if necessary
                    setThreadSetViewerInput(removeInputElement(dlr, child));
	            }
	            modelRefresh(null);
	        }
	    } catch (Exception t){
	        Logger.getLogger(DocumentContentProvider.class).error("Error in changing documentlinkroot children:", t);
	    }
	      
	}
	
	public void linkChanged(CnALink old, CnALink link, Object source) {
		// do nothing
	}
	
	public void linkRemoved(CnALink link) {
	    // do nothing
		
	}
	
	public void linkAdded(CnALink link) {
		// do nothing
	}

	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		modelRefresh(null);
	}

	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	public void modelRefresh() {
		modelRefresh(null);
	}

	public void modelRefresh(Object source) {
		if (Display.getCurrent() != null) {
			viewer.refresh();
		}
		else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.refresh();
				}
			});
		}
	}

	public void databaseChildAdded(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void databaseChildChanged(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void databaseChildRemoved(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	
	public void modelReload(BSIModel newModel) {
		
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#databaseChildRemoved(java.lang.Integer)
	 */
	public void databaseChildRemoved(ChangeLogEntry id) {
		// TODO Auto-generated method stub
	    
		
	}
	
    @Override
    public void validationAdded(Integer scopeId){};
    
    @Override
    public void validationRemoved(Integer scopeId){};
    
    @Override
    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation){};
    
    void updateViewer(final int type, final Object child, final Object parent) {
        
        if (Display.getCurrent() != null) {
            doUpdateViewer(type, child, parent);
            return;
        }
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                doUpdateViewer(type, child, parent);
            }
        });
    }
    
    private void doUpdateViewer(int type, Object child, Object parent){
        
        switch (type) {
        case ADD:
            viewer.add(child, parent);
            return;
        case UPDATE:
            viewer.update(child, new String[] { getDocumentPropertyId((CnATreeElement)child) });
            viewer.refresh(true);
            viewer.refresh();
            return;
        case REMOVE:
            viewer.remove(child);
            return;
        case REFRESH:
            viewer.refresh();
            return;
        }
    }
    
    private DocumentLink getDocumentLink(CnATreeElement elmt){
        String rawURL = elmt.getEntity().getSimpleValue(getDocumentPropertyId(elmt));

        String name = URLUtil.getName(rawURL);
        String url = URLUtil.getHref(rawURL);
        if(!name.isEmpty() && !url.isEmpty()){
            return new DocumentLink(name, url);
        }
        return null;
    }
    
    private DocumentLinkRoot addInputElement(DocumentLinkRoot root, CnATreeElement elmt){
        DocumentLink link = getDocumentLink(elmt);
        if(!isLinkContainedInRoot(root, link)){
            root.addChild(link);
        }
        return root;
    }
    
    private DocumentLinkRoot removeInputElement(DocumentLinkRoot root, CnATreeElement elmt){
        DocumentLink link = getDocumentLink(elmt);
        if(isLinkContainedInRoot(root, link)){
            root.removeChild(link);
        }
        return root;
    }
    
    private boolean isLinkContainedInRoot(DocumentLinkRoot root, DocumentLink link){
        for(DocumentLink child : root.getChildren()){
            if(child!=null && child.equals(link)){
                return true;
            }
            
        }
        return false;
    }
    
    private void setThreadSetViewerInput(final DocumentLinkRoot dlr){
        Display display = (Display.getCurrent() != null) ? Display.getCurrent() : Display.getDefault();
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                viewer.setInput(dlr);
            }
        });
    }
    
}
