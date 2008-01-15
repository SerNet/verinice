package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.views.BSIMassnahmenView;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;

/**
 * Copies modules  (Bausteine) from the BSI's GS-catalogues to be used elsewhere.
 * 
 * @author koderman@sernet.de
 *
 */
public class CopyBSIModelViewAction extends Action {
	private BsiModelView view;

	public CopyBSIModelViewAction(BsiModelView view, String text) {
		super(text);
		this.view = view;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setToolTipText("Kopiert die selektierten Elemente.");
		
	}
	
	public void run() {
		CnPItems.setItems(view.getSelection().toList());
	}
	
}
