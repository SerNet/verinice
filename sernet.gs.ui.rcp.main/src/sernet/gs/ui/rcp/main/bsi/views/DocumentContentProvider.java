package sernet.gs.ui.rcp.main.bsi.views;

import java.util.Set;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLink;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class DocumentContentProvider implements ITreeContentProvider, IBSIModelListener {



	private TreeViewer viewer;

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
		if (CnAElementFactory.getCurrentModel() != null)
			CnAElementFactory.getCurrentModel().removeBSIModelListener(this);
	}

	public void inputChanged(Viewer viewer, Object oldInput,
			Object newInput) {
		if (CnAElementFactory.getCurrentModel() != null) {
			CnAElementFactory.getCurrentModel().removeBSIModelListener(this);
			CnAElementFactory.getCurrentModel().addBSIModelListener(this);
		}
		modelRefresh();
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void childAdded(CnATreeElement category, CnATreeElement child) {
		modelRefresh();
	}

	public void childChanged(CnATreeElement category, CnATreeElement child) {
		modelRefresh();
	}

	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		modelRefresh();
	}

	public void modelRefresh() {
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
	


}
