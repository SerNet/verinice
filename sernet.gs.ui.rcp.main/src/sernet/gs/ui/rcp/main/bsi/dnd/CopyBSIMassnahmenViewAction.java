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

/**
 * Copies modules  (Bausteine) from the BSI's GS-catalogues to be used elsewhere.
 * 
 * @author koderman@sernet.de
 *
 */
public class CopyBSIMassnahmenViewAction extends Action {
	private BSIMassnahmenView view;

	public CopyBSIMassnahmenViewAction(BSIMassnahmenView view, String text) {
		super(text);
		this.view = view;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setToolTipText("Kopiert die selektierten Bausteine.");
		
	}
	
	public void run() {
		List<Baustein> bausteine = view.getSelectedBausteine();
		if (bausteine.size()>0)
			CnPItems.setItems(bausteine);
	}
	
}
