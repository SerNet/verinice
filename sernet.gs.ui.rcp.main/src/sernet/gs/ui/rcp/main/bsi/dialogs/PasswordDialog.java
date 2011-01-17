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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.common.CnATreeElement;

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

    private Text text;
    private Text text2;
    /**
     * 
     */
    private static final int SIZE_X = 400;
    /**
     * 
     */
    private static final int SIZE_Y = 150;
    private Color oldBackground;
    private String password;

    /**
     * @param parentShell
     */
    public PasswordDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.MAX | SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.PasswordDialog_0);
        newShell.setSize(SIZE_X, SIZE_Y);
        
        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x-SIZE_X/2, cursorLocation.y-SIZE_Y/2));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {

        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FormLayout());
        
        
        Label label1 = new Label(container, SWT.NULL);
        label1.setText(Messages.PasswordDialog_1);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(0, 5);
        label1.setLayoutData(formData);
        label1.pack();
        
        text = new Text(container, SWT.BORDER|SWT.PASSWORD);
        FormData formData2 = new FormData();
        formData2.top = new FormAttachment(0, 5);
        formData2.left = new FormAttachment(33, 0);
        formData2.right = new FormAttachment(100, -5);
        text.setLayoutData(formData2);

        Label label2 = new Label(container, SWT.NULL);
        label2.setText(Messages.PasswordDialog_2);
        FormData formDataLabel2 = new FormData();
        formDataLabel2.top = new FormAttachment(text, 5);
        formDataLabel2.left = new FormAttachment(0, 5);
        label2.setLayoutData(formDataLabel2);
        label2.pack();

        text2 = new Text(container, SWT.BORDER|SWT.PASSWORD);
        FormData formdata3 = new FormData();
        formdata3.top = new FormAttachment(text, 5);
        formdata3.left = new FormAttachment(33,0);
        formdata3.right = new FormAttachment(100, -5);
        text2.setLayoutData(formdata3);
        oldBackground = text2.getBackground();
        text2.addKeyListener(new KeyListener() {
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (!text.getText().equals(text2.getText())) {
                    //yellow:
                    text2.setBackground(new Color(Display.getCurrent(), 250,250,120));
                } else {
                    text2.setBackground(oldBackground);
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
               
            }
        });
        
        
        return container;
    
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (text.getText().equals(text2.getText())) {
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


