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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.GSImportPreferencePage;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.preferences.ShowPreferencesAction;

/**
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class GSImportDialog extends Dialog {

    protected boolean massnahmenPersonen = true;
    protected boolean zielObjekteZielobjekte = true;
    protected boolean schutzbedarf = true;
    protected boolean bausteine = true;
    protected boolean rollen = true;
    protected boolean kosten = true;
    protected boolean umsetzung = true;
    protected boolean bausteinPersonen = true;
    private boolean notizen = true;

    public GSImportDialog(Shell shell) {
        super(shell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        container.setLayout(layout);

        String importDb = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.GS_DB_URL);

        Label intro = new Label(container, SWT.NONE);
        intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        intro.setText(NLS.bind(Messages.GSImportDialog_13, importDb));
        Link prefLink = new Link(container, SWT.NONE);
        prefLink.setText(Messages.GSImportDialog_1);
        prefLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnCode(CANCEL);
                close();
                ShowPreferencesAction action = new ShowPreferencesAction(GSImportPreferencePage.ID);
                action.run();
            }
        });

        Label intro2 = new Label(container, SWT.NONE);
        intro2.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        intro2.setText(Messages.GSImportDialog_2);

        final Button button4 = new Button(parent, SWT.CHECK);
        button4.setText(Messages.GSImportDialog_3);
        button4.setEnabled(false);
        button4.setSelection(true);
        button4.pack();
	

        final Button button5 = new Button(parent, SWT.CHECK);
        button5.setSelection(true);
        button5.setText(Messages.GSImportDialog_4);
        button5.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                bausteine = button5.getSelection();
            }
        });
        button5.pack();

        final Button button6 = new Button(parent, SWT.CHECK);
        button6.setSelection(true);
        button6.setText(Messages.GSImportDialog_5);
        button6.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                rollen = button6.getSelection();
            }
        });
        button6.pack();

        final Button button9 = new Button(parent, SWT.CHECK);
        button9.setSelection(true);
        button9.setText(Messages.GSImportDialog_6);
        button9.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                bausteinPersonen = button9.getSelection();
            }
        });
        button9.pack();

        final Button button = new Button(parent, SWT.CHECK);
        button.setSelection(true);
        button.setText(Messages.GSImportDialog_7);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                massnahmenPersonen = button.getSelection();
            }
        });
        button.pack();

        final Button button7 = new Button(parent, SWT.CHECK);
        button7.setSelection(true);
        button7.setText(Messages.GSImportDialog_8);
        button7.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                kosten = button7.getSelection();
            }
        });
        button7.pack();
		
		final Button button10 = new Button(parent, SWT.CHECK);
		button10.setSelection(true);
		button10.setText("Notizen");
		button10.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                notizen = button10.getSelection();
            }
        });
		button10.pack();

        final Button button8 = new Button(parent, SWT.CHECK);
        button8.setSelection(true);
        button8.setText(Messages.GSImportDialog_9);
        button8.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                umsetzung = button8.getSelection();
            }
        });
        button8.pack();

        final Button button2 = new Button(parent, SWT.CHECK);
        button2.setSelection(true);
        button2.setText(Messages.GSImportDialog_10);
        button2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                zielObjekteZielobjekte = button2.getSelection();
            }
        });
        button2.pack();

        final Button button3 = new Button(parent, SWT.CHECK);
        button3.setSelection(true);
        button3.setText(Messages.GSImportDialog_11);
        button3.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                schutzbedarf = button3.getSelection();
            }
        });
        button3.pack();

        return container;

    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.GSImportDialog_12);
    }

    public boolean isMassnahmenPersonen() {
        return massnahmenPersonen;
    }

    public boolean isZielObjekteZielobjekte() {
        return zielObjekteZielobjekte;
    }

    public boolean isSchutzbedarf() {
        return schutzbedarf;
    }

    public boolean isBausteine() {
        return bausteine;
    }

    public boolean isRollen() {
        return rollen;
    }

    public boolean isKosten() {
        return kosten;
    }

    public boolean isUmsetzung() {
        return umsetzung;
    }

    public boolean isBausteinPersonen() {
        return bausteinPersonen;
    }

    /**
     * @return
     */
    public boolean isNotizen() {
        return notizen;
    }

}
