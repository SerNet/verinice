package sernet.springclient;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.net.auth.Authentication;
import org.eclipse.ui.internal.net.auth.UserValidationDialog;

class AuthDialog extends UserValidationDialog {
    
    private static boolean canceled = false;
    
    public static Authentication getAuthentication(final String host, final String message) {
        class UIOperation implements Runnable {
            private Authentication authentication;
            public void run() {
                authentication = AuthDialog.askForAuthentication(host, message);
            }
        }
        UIOperation uio = new UIOperation();
        if (Display.getCurrent() != null) {
            uio.run();
        } else {
            Display.getDefault().syncExec(uio);
        }
        return uio.authentication;
    }
    /**
     * Gets user and password from a user Must be called from UI thread
     * 
     * @return UserAuthentication that contains the userid and the password or
     *         <code>null</code> if the dialog has been cancelled
     */
    protected static Authentication askForAuthentication(String host, String message) {
        Authentication authentication = null; 
        UserValidationDialog ui = new AuthDialog(null, host, message); 
        if(!canceled) {
            ui.open();         
        } 
        authentication = ui.getAuthentication();
        return authentication;
    }
     
    /**
     * @param parentShell
     * @param host
     * @param message
     */
    protected AuthDialog(Shell parentShell, String host, String message) {
        super(parentShell, host, message);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("verinice.PRO - Login"); 
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected void cancelPressed() {
        canceled=true;
        super.cancelPressed();
    }
}