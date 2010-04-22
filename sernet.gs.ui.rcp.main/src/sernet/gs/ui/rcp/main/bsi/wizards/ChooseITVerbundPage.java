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
package sernet.gs.ui.rcp.main.bsi.wizards;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnATreeElementTitles;

/**
 * Wizard page to allow the user to choose the ITVerbund for the report about to
 * be created.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class ChooseITVerbundPage extends WizardPage {

    private Combo itverbundCombo;
    private List<ITVerbund> itverbuende;
    private ITVerbund selectedITVerbund = null;

    public ITVerbund getSelectedITVerbund() {
        return selectedITVerbund;
    }

    protected ChooseITVerbundPage() {
        super("chooseITVerbund"); //$NON-NLS-1$
        setTitle(Messages.ChooseITVerbundPage_1);
        setDescription(Messages.ChooseITVerbundPage_2);
    }

    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);
        setControl(container);

        final Label label2 = new Label(container, SWT.NULL);
        GridData gridData7 = new GridData(GridData.HORIZONTAL_ALIGN_END);
        label2.setLayoutData(gridData7);
        label2.setText(Messages.ChooseITVerbundPage_3);

        itverbundCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        itverbundCombo.setEnabled(false);

        itverbundCombo.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                int s = itverbundCombo.getSelectionIndex();
                selectedITVerbund = itverbuende.get(s);
                updatePageComplete();
            }

        });

        loadITVerbuende();
    }

    /**
	 * 
	 */
    private void loadITVerbuende() {
        LoadCnATreeElementTitles<ITVerbund> compoundLoader = new LoadCnATreeElementTitles<ITVerbund>(ITVerbund.class);
        try {
            compoundLoader = ServiceFactory.lookupCommandService().executeCommand(compoundLoader);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.ChooseITVerbundPage_4);
        }

        this.itverbuende = compoundLoader.getElements();

        itverbundCombo.removeAll();

        for (ITVerbund c : itverbuende) {
            itverbundCombo.add(c.getTitle());
        }
        itverbundCombo.setEnabled(true);
        itverbundCombo.pack();
    }

    ExportWizard getExportWizard() {
        return ((ExportWizard) getWizard());
    }

    private void updatePageComplete() {
        boolean complete = selectedITVerbund != null;
        if (!complete) {
            setMessage(null);
            setPageComplete(false);
            return;
        }

        setPageComplete(true);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        updatePageComplete();
    }

}
