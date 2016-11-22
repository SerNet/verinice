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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DescriptionDialog extends TitleAreaDialog {

    private static final int DIALOG_WIDTH = 400;
    
    private String title;
    
    private String description;

    /**
     * @param parentShell
     */
    public DescriptionDialog(Shell parentShell) {
        super(parentShell);
        int style = SWT.MAX | SWT.CLOSE | SWT.TITLE;
        style = style | SWT.BORDER | SWT.APPLICATION_MODAL;
        setShellStyle(style | SWT.RESIZE);    
    }
    

    /**
     * @param activeShell
     * @param string
     */
    public DescriptionDialog(Shell activeShell, String title) {
        this(activeShell);
        final int maxTitleLength = 40;
        String newTitle = title;
        if(title.length()>maxTitleLength) {
            newTitle = title.substring(0, maxTitleLength) + "..."; //$NON-NLS-1$
        }
        this.title = newTitle;
    }


    private void addFormElements(Composite composite) {
        final int dialogWidthSubtrahend = 30;
        final int gdHeightHint = 130;
        if(title!=null) {
            final Label objectLabel = new Label(composite, SWT.NONE);
            objectLabel.setText(Messages.NewQmIssueDialog_4);
            final Label titleLabel = new Label(composite, SWT.NONE);
           
            FontData[] fD = titleLabel.getFont().getFontData();
            for (int i = 0; i < fD.length; i++) {
                fD[i].setStyle(SWT.BOLD);
            }
            Font newFont = new Font(getShell().getDisplay(),fD);
            titleLabel.setFont(newFont);
            GC gc = new GC(titleLabel);
            Point size = gc.textExtent(title);
            if(size.x > DIALOG_WIDTH - dialogWidthSubtrahend) {
                title = trimTitleByWidthSize(gc,title,DIALOG_WIDTH-dialogWidthSubtrahend) + "..."; //$NON-NLS-1$
            }
            titleLabel.setText(title);
        }
        
        final Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText(Messages.DescriptionDialog_0);
        final Text descriptionText = new Text(composite, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.WRAP);
        if(description != null)
        {
            descriptionText.setText(description);
        }
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        gd.heightHint = gdHeightHint;
        
        descriptionText.setLayoutData(gd);
        descriptionText.addFocusListener(new FocusListener() {            
            @Override
            public void focusLost(FocusEvent e) {
                description = descriptionText.getText();
            }
            
            @Override
            public void focusGained(FocusEvent e) {           
            }
        });

    }
    
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
        final int shellHeight = 370;
        newShell.setSize(DIALOG_WIDTH, shellHeight);
        newShell.setText(Messages.NewQmIssueDialog_9);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        final int defaultMarginWidth = 10;
        setTitle(Messages.DescriptionDialog_1);
        setMessage(Messages.DescriptionDialog_2);
        
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

    public void setDescription(String description) {
        this.description = description;
    }
}
