/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dialogs;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * Dialog to let user enter a password, twice for confirmation.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class PasswordDialog extends Dialog {


    private static final int TEXT_WIDTH = 125;

    protected static final int WIZARD_NUM_COLS_ROOT = 2;
    protected static final Point DEFAULT_MARGINS = new Point(10, 10);

    private Text text;
    private Text text2;
    private Color oldBackground;
    private String password;

    public PasswordDialog(Shell parentShell) {
        super(parentShell);
    }
    
    @Override
    protected void configureShell(Shell newShell) {

        super.configureShell(newShell);
        newShell.setText(Messages.PasswordDialog_0);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {

        Composite container = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(DEFAULT_MARGINS)
                .generateLayout(container);
        
        Label label1 = new Label(container, SWT.NULL);
        label1.setText(Messages.PasswordDialog_1);
        text = new Text(container, SWT.BORDER|SWT.PASSWORD);
        GridDataFactory.fillDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(text);

        Label label2 = new Label(container, SWT.NULL);
        label2.setText(Messages.PasswordDialog_2);
        text2 = new Text(container, SWT.BORDER | SWT.PASSWORD);
        GridDataFactory.fillDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(text2);
        oldBackground = text2.getBackground();

        addListeners();
        container.pack();
        return container;

    }

    private void addListeners() {
        text.addFocusListener(new FocusListener() {
            
            @Override
            public void focusLost(FocusEvent e) {
                String pwd = text.getText();
                if(pwd.matches(".*[ÄäÖöÜüß€]+.*")) { //$NON-NLS-1$
                    MessageDialog.openWarning(PasswordDialog.this.getShell(), Messages.AccountDialog_5, Messages.AccountDialog_6);
                    text.setText(""); //$NON-NLS-1$
                    text2.setText(""); //$NON-NLS-1$
                    text.setFocus();
                }

            }
            
            @Override
            public void focusGained(FocusEvent e) {
                // nothing to do
            }
        });

        text2.addKeyListener(new KeyListener() {
            
            @Override
            public void keyReleased(KeyEvent e) {
                final int red = 250;
                final int green = red;
                final int blue = 120;
                if (!text.getText().equals(text2.getText())) {
                    text2.setBackground(new Color(Display.getCurrent(), red,green,blue));
                } else {
                    text2.setBackground(oldBackground);
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                // nothing to do
            }
        });
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (!text.getText().isEmpty() && text.getText().equals(text2.getText())) {
            password = text.getText();
            super.okPressed();
        } 
        else {
            MessageDialog.openWarning(this.getShell(), Messages.PasswordDialog_3, Messages.PasswordDialog_4);
        }
    }

    /**
     * @return
     */
    public String getPassword() {
        return password;
    }

}


