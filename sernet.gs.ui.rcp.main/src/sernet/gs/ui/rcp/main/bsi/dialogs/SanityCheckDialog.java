package sernet.gs.ui.rcp.main.bsi.dialogs;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class SanityCheckDialog {

	public static boolean checkLayer(Shell shell, int schicht, int targetSchicht) {
		if (schicht != targetSchicht) {
			if (MessageDialog.openQuestion(
					shell,
					"Sanity Check",
					"Sie wollen einen Baustein der Schicht " + schicht
					+ " einem Zielobjekt der Schicht " + targetSchicht
					+ " zuordnen.\n\nWissen Sie, was Sie tun?")) {
				return true;
			}
			return false;
		}
		return true;
	}

}
