package sernet.gs.ui.rcp.main.bsi.views;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLink;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HibernateDocumentLinkDAO;
import sernet.gs.ui.rcp.main.common.model.IDocumentLinkDAO;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

public class DocumentView extends ViewPart {
	
	public static final String ID = "sernet.gs.ui.rcp.main.documentview";

	private TreeViewer viewer;

	private Action doubleClickAction;
	
	private IModelLoadListener loadListener = new IModelLoadListener() {
		public void closed(BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (viewer.getContentProvider() != null)
						setInput();
				}
			});
		}
		
		public void loaded(final BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (viewer.getContentProvider() != null) {
						setInput();
					}
				}

				
			});
		}
	};

	public DocumentView() {
		// TODO Auto-generated constructor stub
	}
	
	protected void setInput() {
		Set<String> allIDs = new HashSet<String>();
		try {
			List<PropertyType> types;
			types = HUITypeFactory.getInstance().getURLPropertyTypes();
			for (PropertyType type : types) {
				allIDs.add(type.getId());
			}
		} catch (Exception e) {
			return;
		}
		IDocumentLinkDAO dao = new HibernateDocumentLinkDAO();
		DocumentLinkRoot root = dao.findEntries(allIDs);
		viewer.setInput(root);
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent,SWT.FULL_SELECTION | SWT.MULTI);
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
		
		viewer.setContentProvider(new DocumentContentProvider(viewer));
		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof DocumentLink && e2 instanceof DocumentLink) {
					DocumentLink link1 = (DocumentLink) e1;
					DocumentLink link2 = (DocumentLink) e2;
					return link1.getName().compareTo(link2.getName());
				}
				if (e1 instanceof DocumentReference && e2 instanceof DocumentReference) {
					DocumentReference ref1 = (DocumentReference) e1;
					DocumentReference ref2 = (DocumentReference) e2;
					return ref1.getCnaTreeElement().getTitel().compareTo(ref2.getCnaTreeElement().getTitel());
				}
				return 0;
			}
		});
		
		setInput();
		
		column.getColumn().setWidth(200);
		column2.getColumn().setWidth(100);
		column3.getColumn().setWidth(100);
		
		makeActions();
		hookActions();
		getSite().setSelectionProvider(viewer);
		CnAElementFactory.getInstance().addLoadListener(loadListener);
		
	}
	

	@Override
	public void setFocus() {
		
	}
	
	private void makeActions() {
		doubleClickAction = new Action() {
			public void run() {
				Object sel = ((IStructuredSelection)viewer.getSelection())
					.getFirstElement();
				if (sel instanceof DocumentReference) {
					DocumentReference ref = (DocumentReference) sel;
					EditorFactory.getInstance().openEditor(ref.getCnaTreeElement());
				}
				else if (sel instanceof DocumentLink) {
					DocumentLink link = (DocumentLink) sel;
					Program.launch(link.getHref());
				}
			}
		};
	}
	
	private void hookActions() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
}

}
