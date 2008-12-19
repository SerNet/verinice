package sernet.gs.ui.rcp.main.bsi.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.MassnahmenViewFilterDialog;
import sernet.gs.ui.rcp.main.bsi.filter.BSISchichtFilter;
import sernet.gs.ui.rcp.main.bsi.filter.BSISearchFilter;
import sernet.gs.ui.rcp.main.bsi.filter.GefaehrdungenFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;

/**
 * Action that opens a dialog to let the user change filter settings
 * for the given view.
 * 
 * The action will also maintain the connection between filters and viewer.
 * 
 * @author koderman@sernet.de
 *
 */
public class MassnahmenViewFilterAction extends Action {
	private Shell shell;
	private MassnahmenSiegelFilter siegelFilter;
	private BSISearchFilter suchFilter;
	private BSISchichtFilter schichtFilter;
	private GefaehrdungenFilter gefFilter;

	public MassnahmenViewFilterAction(StructuredViewer viewer,
			String title,
			MassnahmenSiegelFilter filter2,
			GefaehrdungenFilter gefFilter) {
		super(title);
		shell = viewer.getControl().getShell();
		this.siegelFilter = filter2;
		suchFilter = new BSISearchFilter(viewer);
		this.schichtFilter = new BSISchichtFilter(viewer);
		this.gefFilter = gefFilter;
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
	}
	
	@Override
	public void run() {
		MassnahmenViewFilterDialog dialog = new MassnahmenViewFilterDialog(shell,
				siegelFilter.getPattern(),
				suchFilter.getPattern(),
				schichtFilter.getPattern(),
				gefFilter.getPattern());
		
		if (dialog.open() != InputDialog.OK)
			return;
		
		siegelFilter.setPattern(dialog.getSiegelSelection());
		suchFilter.setPattern(dialog.getSuche());
		schichtFilter.setPattern(dialog.getSchichtSelection());
		gefFilter.setPattern(dialog.getGefFilterSelection());
	}
}
