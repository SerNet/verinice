/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MassnahmenViewFilterDialog extends FilterDialog {

    private Text text1;
    private String suche;
    private boolean[] filterGefMn;

    public MassnahmenViewFilterDialog(Shell parent, String[] siegel, String suche, String[] schicht, boolean[] filterGefMn) {
        super(parent, null, siegel, schicht);
        this.suche = suche;
        this.filterGefMn = filterGefMn;
        if (this.filterGefMn == null) {
            this.filterGefMn = new boolean[] { false, false };
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        container.setLayout(layout);

        Label intro = new Label(container, SWT.NONE);
        intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
        intro.setText(Messages.MassnahmenViewFilterDialog_0);

        Label label1 = new Label(container, SWT.NONE);
        label1.setText(Messages.MassnahmenViewFilterDialog_1);

        text1 = new Text(container, SWT.BORDER);
        text1.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
        text1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                suche = text1.getText();
            }
        });

        Group boxesComposite2 = createSiegelGroup(parent);
        createSiegelCheckboxes(boxesComposite2);

        Group boxesComposite3 = createSchichtenGroup(parent);
        createSchichtCheckboxes(boxesComposite3);

        Group boxesComposite4 = createGefMnFilterGroup(parent);
        createGefMnCheckboxes(boxesComposite4);

        initContent();

        return container;
    }

    private void createGefMnCheckboxes(Group parent) {

        final Button button1 = new Button(parent, SWT.CHECK);
        button1.setText(Messages.MassnahmenViewFilterDialog_2);
        button1.setSelection(filterGefMn[0]);
        button1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (button1.getSelection()) {
                    filterGefMn[0] = true;
                } else {
                    filterGefMn[0] = false;
                }
            }
        });

        final Button button2 = new Button(parent, SWT.CHECK);
        button2.setText(Messages.MassnahmenViewFilterDialog_3);
        button2.setSelection(filterGefMn[1]);
        button2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (button2.getSelection()) {
                    filterGefMn[1] = true;
                } else {
                    filterGefMn[1] = false;
                }
            }
        });

    }

    private Group createGefMnFilterGroup(Composite parent) {
        Group boxesComposite = new Group(parent, SWT.BORDER);
        boxesComposite.setText(Messages.MassnahmenViewFilterDialog_4);
        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1);
        boxesComposite.setLayoutData(gridData);
        GridLayout layout2 = new GridLayout();
        layout2.numColumns = 2;
        boxesComposite.setLayout(layout2);
        return boxesComposite;

    }

    @Override
    protected void initContent() {
        super.initContent();
        text1.setText(suche != null ? suche : ""); //$NON-NLS-1$
        text1.setSelection(0, text1.getText().length());
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.MassnahmenViewFilterDialog_6);
    }

    public String getSuche() {
        return suche;
    }

    public boolean[] getGefFilterSelection() {
        if (filterGefMn[0] || filterGefMn[1]) {
            return this.filterGefMn;
        } else {
            return null;
        }
    }

}
