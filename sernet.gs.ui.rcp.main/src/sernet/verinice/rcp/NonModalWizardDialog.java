package sernet.verinice.rcp;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class NonModalWizardDialog extends WizardDialog {
    public NonModalWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
        int style = SWT.CLOSE | SWT.MAX | SWT.TITLE;
        style = style | SWT.BORDER | SWT.RESIZE;
        setShellStyle(style | getDefaultOrientation());         
    }
}