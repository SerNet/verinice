/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.interfaces.bpm.ITask;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class NewQmIssueDialog extends TitleAreaDialog {

    private static final String[] PRIORITY_ARRAY = {ITask.PRIO_LOW,ITask.PRIO_NORMAL,ITask.PRIO_HIGH};
    private static final String[] PRIORITY_LABEL_ARRAY = {Messages.NewQmIssueDialog_0,Messages.NewQmIssueDialog_1,Messages.NewQmIssueDialog_2};
    private static final int DEFAULT_PRIORITY_INDEX = 1;
    
    private static final int DIALOG_WIDTH = 400;
    
    private String elementTitle;
    
    private String description;
    
    private String priority;

    /**
     * @param parentShell
     */
    public NewQmIssueDialog(Shell parentShell) {
        super(parentShell);
        int style = SWT.MAX | SWT.CLOSE | SWT.TITLE;
        style = style | SWT.BORDER | SWT.APPLICATION_MODAL;
        setShellStyle(style | SWT.RESIZE);    
    }
    

    /**
     * @param activeShell
     * @param string
     */
    public NewQmIssueDialog(Shell activeShell, String title) {
        this(activeShell);
        final int maxTitleLength = 40;
        String newTitle = title;
        if(title.length()>maxTitleLength) {
            newTitle = title.substring(0, maxTitleLength) + "..."; //$NON-NLS-1$
        }
        this.elementTitle = newTitle;
    }


    private void addFormElements(Composite composite) {
        final int dialogWidthSubtrahend = 30;
        final int gdHeightHint = 150;
        if(elementTitle!=null) {
            final Label objectLabel = new Label(composite, SWT.NONE);
            objectLabel.setText(Messages.NewQmIssueDialog_4);
            final Label object = new Label(composite, SWT.NONE);
           
            FontData[] fD = object.getFont().getFontData();
            for (int i = 0; i < fD.length; i++) {
                fD[i].setStyle(SWT.BOLD);
            }
            Font newFont = new Font(getShell().getDisplay(),fD);
            object.setFont(newFont);
            GC gc = new GC(object);
            Point size = gc.textExtent(elementTitle);
            if(size.x > DIALOG_WIDTH - dialogWidthSubtrahend) {
                elementTitle = trimTitleByWidthSize(gc,elementTitle,DIALOG_WIDTH-dialogWidthSubtrahend) + "..."; //$NON-NLS-1$
            }
            object.setText(elementTitle);
        }
        
        final Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText(Messages.NewQmIssueDialog_6);
        final Text textArea = new Text(composite, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.WRAP);
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        gd.heightHint = gdHeightHint;
        
        textArea.setLayoutData(gd);
        textArea.addFocusListener(new FocusListener() {            
            @Override
            public void focusLost(FocusEvent e) {
                description = textArea.getText();
            }
            
            @Override
            public void focusGained(FocusEvent e) {           
            }
        });
        
        final Label priorityLabel = new Label(composite, SWT.NONE);
        priorityLabel.setText(Messages.NewQmIssueDialog_7);
        final Combo priorityCombo = new Combo(composite, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        priorityCombo.setItems(PRIORITY_LABEL_ARRAY);
        priorityCombo.select(DEFAULT_PRIORITY_INDEX);
        priority = PRIORITY_ARRAY[DEFAULT_PRIORITY_INDEX];
        priorityCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                priority = PRIORITY_ARRAY[priorityCombo.getSelectionIndex()];
            }
        });
    }
    
    /**
     * @param gc
     * @param elementTitle2
     * @return
     */
    private String trimTitleByWidthSize(GC gc, String elementTitle, int width) {
        String newTitle = elementTitle.substring(0, elementTitle.length()-1);
        Point size = gc.textExtent(newTitle + "..."); //$NON-NLS-1$
        if(size.x>width) {
            newTitle = trimTitleByWidthSize(gc, newTitle, width);
        }
        return newTitle;
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        final int shellHeight = 450;
        newShell.setSize(DIALOG_WIDTH, shellHeight);
        newShell.setText(Messages.NewQmIssueDialog_9);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        final int defaultMarginWidth = 10;
        setTitle(Messages.NewQmIssueDialog_10);
        setMessage(Messages.NewQmIssueDialog_11);
        
        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = defaultMarginWidth;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);

        ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setExpandHorizontal(true);
        
        Composite innerComposite = new Composite (scrolledComposite, SWT.NONE); 
        scrolledComposite.setContent(innerComposite); 
        innerComposite.setLayoutData(new GridData (SWT.FILL, SWT.FILL,true, false)); 
        innerComposite.setLayout(new GridLayout (1, false));
        
        addFormElements(innerComposite);
        
        scrolledComposite.setVisible(true);
        Point size = innerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);   
        innerComposite.setSize(size); 
                  
        // Build the separator line
        Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        composite.pack(); 
        
        setDialogLocation();
        
        return composite;
    }
    
    private void setDialogLocation() {
        Rectangle monitorArea = getShell().getDisplay().getPrimaryMonitor().getBounds();
        Rectangle shellArea = getShell().getBounds();
        int x = monitorArea.x + (monitorArea.width - shellArea.width)/2;
        int y = monitorArea.y + (monitorArea.height - shellArea.height)/2;
        getShell().setLocation(x,y);
    }


    public String getDescription() {
        return description;
    }


    public String getPriority() {
        return priority;
    }

}
