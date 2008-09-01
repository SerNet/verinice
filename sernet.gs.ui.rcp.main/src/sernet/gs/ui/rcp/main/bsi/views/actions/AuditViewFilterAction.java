package sernet.gs.ui.rcp.main.bsi.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.TodoFilterDialog;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.filter.StringPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.ZielobjektPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;

public class AuditViewFilterAction extends Action {
	private Shell shell;
	private MassnahmenUmsetzungFilter umsetzungFilter;
	private MassnahmenSiegelFilter siegelFilter;
	private StringPropertyFilter umsetzungDurchFilter;
	private ZielobjektPropertyFilter zielobjektFilter;

	public AuditViewFilterAction(StructuredViewer viewer,
			String title,
			MassnahmenUmsetzungFilter filter1,
			MassnahmenSiegelFilter filter2) {
		super(title);
		shell = viewer.getControl().getShell();
		
		this.umsetzungFilter = filter1;
		this.siegelFilter = filter2;
		this.umsetzungDurchFilter = new StringPropertyFilter(viewer,
				MassnahmenUmsetzung.P_NAECHSTEREVISIONDURCH);
		this.zielobjektFilter = new ZielobjektPropertyFilter(viewer);
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
		
	}
	
	@Override
	public void run() {
		TodoFilterDialog dialog = new TodoFilterDialog(shell, 
				umsetzungFilter.getUmsetzungPattern(),
				siegelFilter.getPattern(),
				umsetzungDurchFilter.getPattern(),
				zielobjektFilter.getPattern());
		if (dialog.open() != InputDialog.OK)
			return;
		
		umsetzungFilter.setUmsetzungPattern(dialog.getUmsetzungSelection());
		siegelFilter.setPattern(dialog.getSiegelSelection());
		umsetzungDurchFilter.setPattern(dialog.getUmsetzungDurch());
		zielobjektFilter.setPattern(dialog.getZielobjekt());
	}
}
