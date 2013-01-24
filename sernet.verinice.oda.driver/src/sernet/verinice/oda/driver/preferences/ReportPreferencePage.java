/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.oda.driver.preferences;


import java.util.logging.Level;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ICommandCacheClient;
import sernet.verinice.oda.driver.Activator;

/**
 *
 */
public class ReportPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private String[][] logLvlValues = new String[][]{
            new String[]{Messages.getString("ReportLogLevel.0"), Level.INFO.toString()},
            new String[]{Messages.getString("ReportLogLevel.1"), Level.WARNING.toString()},
            new String[]{Messages.getString("ReportLogLevel.2"), Level.FINEST.toString()},
            new String[]{Messages.getString("ReportLogLevel.3"), Level.SEVERE.toString()},
            new String[]{Messages.getString("ReportLogLevel.4"), Level.ALL.toString()}
    };
            
    
    public ReportPreferencePage(){
        super(GRID);
        setDescription(Messages.getString("ReportPreferencePage.0")); //$NON-NLS-1$
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench arg0) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        
        BooleanFieldEditor reportLoggingEditor = new BooleanFieldEditor(PreferenceConstants.REPORT_LOGGING_ENABLED, Messages.getString("ReportPreferencePage.1"), getFieldEditorParent());
        addField(reportLoggingEditor);
        
        ComboFieldEditor logLvlFieldEditor = new ComboFieldEditor(PreferenceConstants.REPORT_LOGGING_LVL, Messages.getString("ReportPreferencePage.2"), logLvlValues, getFieldEditorParent());
        addField(logLvlFieldEditor);
        
        StringFieldEditor logFileNameEditor = new StringFieldEditor(PreferenceConstants.REPORT_LOG_FILE, Messages.getString("ReportPreferencePage.3"), getFieldEditorParent());
        addField(logFileNameEditor);
        
        createCacheResetButton();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
            checkState();
        }
    }

    @Override
    protected void checkState() {
        super.checkState();
        if (!isValid()) {
            return;
        }

    }
    
    private void createCacheResetButton(){
        Button button = new Button((Composite) getControl(), SWT.PUSH);
        button.setText(Messages.getString("ReportPreferencePage.4")); //$NON-NLS-1$
        button.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, true));
        button.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if(MessageDialog.openConfirm(getShell(), Messages.getString("ReportPreferencePage.5"), Messages.getString("ReportPreferencePage.6"))){
                    ICommandCacheClient commandCacheClient = (ICommandCacheClient)VeriniceContext.get(VeriniceContext.COMMAND_CACHE_SERVICE);
                    commandCacheClient.resetCache();
                } else {
                    return;
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
            
        });
    }
    
}
