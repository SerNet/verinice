package sernet.gs.ui.rcp.main.bsi.views;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.bsi.Attachment;

public class AttachmentContentProvider implements IStructuredContentProvider {

	FileView fileView;
	
	TableViewer viewer;
	
	public AttachmentContentProvider(FileView fileView) {
		this.fileView = fileView;
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof PlaceHolder) {
			return new Object[] {inputElement};
		}
		List<Attachment> attachmentList = (List<Attachment>) inputElement;
		return attachmentList.toArray(new Object[attachmentList.size()]);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) viewer;
	}

}
