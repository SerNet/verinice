package sernet.gs.ui.rcp.main.bsi.views;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.bsi.model.DocumentLink;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HibernateDocumentLinkDAO;
import sernet.gs.ui.rcp.main.common.model.IDocumentLinkDAO;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

public class DocumentView extends ViewPart {
	
	public static final String ID = "sernet.gs.ui.rcp.main.documentview";

	private TreeViewer viewer;

	public DocumentView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent,SWT.FULL_SELECTION);
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);

		TreeViewerColumn column = new TreeViewerColumn(viewer,SWT.NONE);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof DocumentLink)
					return ((DocumentLink)element).getName();
				return "";
			}
		});
		column.getColumn().setText("Name");

		TreeViewerColumn column2 = new TreeViewerColumn(viewer,SWT.NONE);
		column2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof DocumentLink)
					return ((DocumentLink)element).getHref();
				return "";
			}
		});
		column2.getColumn().setText("Link");

		TreeViewerColumn column3 = new TreeViewerColumn(viewer,SWT.NONE);
		column3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof DocumentReference) {
					DocumentReference docref = (DocumentReference) element;
					return docref.getCnaTreeElement().getTitel();
				}
				return "";
			}
		});
		column3.getColumn().setText("Referenziert in");
		
		viewer.setContentProvider(new DocumentContentProvider());
		
		Set<String> allIDs = new HashSet<String>();
		List<PropertyType> types = HUITypeFactory.getInstance().getURLPropertyTypes();
		for (PropertyType type : types) {
			allIDs.add(type.getId());
		}
		IDocumentLinkDAO dao = new HibernateDocumentLinkDAO();
		DocumentLinkRoot root = dao.findEntries(allIDs);
		viewer.setInput(root);
		
		column.getColumn().setWidth(200);
		column2.getColumn().setWidth(100);
		column3.getColumn().setWidth(100);
	}
	

	@Override
	public void setFocus() {
		
	}

}
