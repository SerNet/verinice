package sernet.verinice.iso27k.rcp.action;

import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.LdapImportDialog;

public class ImportPersonFromLdap extends RightsEnabledAction {

	public static final String ID = "sernet.verinice.iso27k.rcp.action.ImportPersonFromLdap"; //$NON-NLS-1$
	private final IWorkbenchWindow window;

	public ImportPersonFromLdap(IWorkbenchWindow window, String label) {
		this.window = window;
		setText(label);
		setId(ID);
		setActionDefinitionId(ID);
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.PERSON));
		setToolTipText(Messages.getString("ImportPersonFromLdap.1")); //$NON-NLS-1$
		setRightID(ActionRightIDs.IMPORTLDAP);
		setEnabled(checkRights());
	}

	public void run() {
		final LdapImportDialog dialog = new LdapImportDialog(window.getShell());
		if (dialog.open() != Window.OK) {
			return;
		}
	}
}
