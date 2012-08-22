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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.bsi.dialogs.Messages;
import sernet.verinice.interfaces.bpm.ITask;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class NewQmIssueDialog extends TitleAreaDialog {

    private String description;
    
    private String priority;
    
    private static final String[] priorityArray = {ITask.PRIO_LOW,ITask.PRIO_NORMAL,ITask.PRIO_HIGH};
    private static final String[] priorityLabelArray = {"low","normal","high"};

    private static final int DEFAULT_PRIORITY_INDEX = 1;
    
    /**
     * @param parentShell
     */
    public NewQmIssueDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.MAX | SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
    }
    

    private void addFormElements(Composite composite) {
        final Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText("Description");
        final Text textArea = new Text(composite, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.WRAP);
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        gd.heightHint = 150;
        
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
        priorityLabel.setText("Priority");
        final Combo priorityCombo = new Combo(composite, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        priorityCombo.setItems(priorityLabelArray);
        priorityCombo.select(DEFAULT_PRIORITY_INDEX);
        priority = priorityArray[DEFAULT_PRIORITY_INDEX];
        priorityCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                priority = priorityArray[priorityCombo.getSelectionIndex()];
            }
        });
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(400, 500);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle("New QM Issue");
        setMessage("To start a new QM issue fill in the form an press OK.", IMessageProvider.INFORMATION);
        
        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);    
        
        final Group groupOrganization = new Group(composite, SWT.NONE);    
        //groupOrganization.setText(Messages.GroupByTagDialog_2);
        GridLayout groupOrganizationLayout = new GridLayout(1, true);
        groupOrganization.setLayout(groupOrganizationLayout);
        gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        groupOrganization.setLayoutData(gd);

        ScrolledComposite scrolledComposite = new ScrolledComposite(groupOrganization, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setExpandHorizontal(true);
        
        Composite innerComposite = new Composite (scrolledComposite, SWT.NONE); 
        scrolledComposite.setContent(innerComposite); 
        innerComposite.setLayoutData(new GridData (SWT.FILL, SWT.FILL,true, false)); 
        innerComposite.setLayout(new GridLayout (1, false));
        
        addFormElements(innerComposite);
        
        scrolledComposite.setVisible(true);
        Point size = innerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT); 
        size.y += 2 * 2;
        innerComposite.setSize(size);
        groupOrganization.layout(); 
                  
        composite.pack(); 
        return composite;
    }


    public String getDescription() {
        return description;
    }


    public String getPriority() {
        return priority;
    }

}
