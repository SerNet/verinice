/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.rcp.group;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog to select tags.
 * For each tag a checkbox is shown.
 * 
 * @see GroupByTagHandler
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GroupByTagDialog extends TitleAreaDialog {

    private Set<String> tags;

    private Set<String> tagsSelected;

    /**
     * @param activeShell
     * @param selectedOrganization
     */
    public GroupByTagDialog(Shell activeShell, Set<String> tags) {
        super(activeShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.tags = tags;
    }

    @Override
    protected Control createDialogArea(Composite parent) { 
        final int layoutMarginWidth = 10;
        final int layoutMarginHeight = layoutMarginWidth;
        final int gdMinWidth = 400;
        final int gdHeightHint = 200;
        setTitle(Messages.GroupByTagDialog_0);
        setMessage(Messages.GroupByTagDialog_1, IMessageProvider.INFORMATION);

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = layoutMarginWidth;
        layout.marginHeight = layoutMarginHeight;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);    
        
        final Group groupOrganization = new Group(composite, SWT.NONE);    
        groupOrganization.setText(Messages.GroupByTagDialog_2);
        GridLayout groupOrganizationLayout = new GridLayout(1, true);
        groupOrganization.setLayout(groupOrganizationLayout);
        gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        gd.minimumWidth = gdMinWidth;
        gd.heightHint = gdHeightHint; 
        groupOrganization.setLayoutData(gd);

        ScrolledComposite scrolledComposite = new ScrolledComposite(groupOrganization, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setExpandHorizontal(true);
        
        Composite innerComposite = new Composite (scrolledComposite, SWT.NONE); 
        scrolledComposite.setContent(innerComposite); 
        innerComposite.setLayoutData(new GridData (SWT.FILL, SWT.FILL,true, false)); 
        innerComposite.setLayout(new GridLayout (1, false));
        
        
        SelectionListener tagListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkbox = (Button) e.getSource();
                String tag = (String) checkbox.getData();
                if(checkbox.getSelection()) {
                    tagsSelected.add(tag);
                } else {
                    tagsSelected.remove(tag);
                }
                super.widgetSelected(e);
            }
        };
     
        tagsSelected = new HashSet<String>();
        for (String tag : tags) {
            final Button radioOrganization = new Button(innerComposite, SWT.CHECK);
            radioOrganization.setText(tag);
            radioOrganization.setData(tag);
            radioOrganization.setSelection(true);
            tagsSelected.add(tag);
            radioOrganization.addSelectionListener(tagListener);
        }
        
        scrolledComposite.setVisible(true);
        Point size = innerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT); 
        size.y += tags.size() * 2;
        innerComposite.setSize(size); 
        groupOrganization.layout(); 
                  
        composite.pack();     
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        super.okPressed();
    }

    /* Getters and Setters: */

    public Set<String> getTagsSelected() {
        return tagsSelected;
    }
}
