package sernet.gs.ui.rcp.main.bsi.views;

import java.util.Set;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.model.DocumentLink;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;

public class DocumentContentProvider implements ITreeContentProvider {



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
		// TODO Auto-generated method stub
		
	}

	public void inputChanged(Viewer viewer, Object oldInput,
			Object newInput) {
		// do nothing
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
	


}
