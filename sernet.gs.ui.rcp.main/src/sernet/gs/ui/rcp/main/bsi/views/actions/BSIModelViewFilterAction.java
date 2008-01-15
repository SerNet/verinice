package sernet.gs.ui.rcp.main.bsi.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.BSIModelFilterDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.FilterDialog;
import sernet.gs.ui.rcp.main.bsi.filter.BSIModelElementFilter;
import sernet.gs.ui.rcp.main.bsi.filter.BSISchichtFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.filter.StringPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;

/**
 * Sets filters for the BSI model view.
 * 
 * @author koderman@sernet.de
 *
 */
public class BSIModelViewFilterAction extends Action {
	private Shell shell;
	private MassnahmenUmsetzungFilter umsetzungFilter;
	private MassnahmenSiegelFilter siegelFilter;
	private StringPropertyFilter lebenszyklusFilter;
	private StringPropertyFilter objektLebenszyklusFilter;
	private BSIModelElementFilter elementFilter;
	

	public BSIModelViewFilterAction(StructuredViewer viewer,
			String title,
			MassnahmenUmsetzungFilter filter1,
			MassnahmenSiegelFilter filter2,
			StringPropertyFilter filter3,
			StringPropertyFilter filter5,
			BSIModelElementFilter filter4) {
		super(title);
		shell = viewer.getControl().getShell();
		
		this.umsetzungFilter = filter1;
		this.siegelFilter = filter2;
		this.lebenszyklusFilter = filter3;
		this.elementFilter = filter4;
		this.objektLebenszyklusFilter = filter5;
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
	
	}
	
	
	@Override
	public void run() {
		BSIModelFilterDialog dialog = new BSIModelFilterDialog(shell,
				umsetzungFilter.getUmsetzungPattern(),
				siegelFilter.getPattern(),
				lebenszyklusFilter.getPattern(),
				objektLebenszyklusFilter.getPattern(),
				elementFilter.getPattern()
				);

		if (dialog.open() != InputDialog.OK)
			return;
		
		umsetzungFilter.setUmsetzungPattern(dialog.getUmsetzungSelection());
		siegelFilter.setPattern(dialog.getSiegelSelection());
		
		lebenszyklusFilter.setPattern(dialog.getLebenszyklus());
		objektLebenszyklusFilter.setPattern(dialog.getObjektLebenszyklus());
		
		elementFilter.setPattern(dialog.getAusblendenSelection());
	}
}
