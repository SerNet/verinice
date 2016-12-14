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
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.verinice.oda.driver.Activator;

@SuppressWarnings("restriction")
public class ReportPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private String[][] logLvlValues = new String[][]{
            new String[]{Messages.getString("ReportLogLevel.0"), Level.INFO.toString()},
            new String[]{Messages.getString("ReportLogLevel.1"), Level.WARNING.toString()},
            new String[]{Messages.getString("ReportLogLevel.2"), Level.FINEST.toString()},
            new String[]{Messages.getString("ReportLogLevel.3"), Level.SEVERE.toString()},
            new String[]{Messages.getString("ReportLogLevel.4"), Level.ALL.toString()}
    };
    private DirectoryFieldEditor localTemplateEditor;

    private BooleanFieldEditor useSandboxEditor;
    
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
        
        DirectoryFieldEditor logFileNameEditor = new DirectoryFieldEditor(PreferenceConstants.REPORT_LOG_FILE, Messages.getString("ReportPreferencePage.3"), getFieldEditorParent());
        addField(logFileNameEditor);
        
        localTemplateEditor =  new DirectoryFieldEditor(PreferenceConstants.REPORT_LOCAL_TEMPLATE_DIRECTORY, Messages.getString("ReportPreferencePage.8"), getFieldEditorParent());
        addField(localTemplateEditor);

        BooleanFieldEditor useCacheEditor = new BooleanFieldEditor(PreferenceConstants.REPORT_USE_CACHE, Messages.getString("ReportPreferencePage.7"), getFieldEditorParent());
        addField(useCacheEditor);
        
        useSandboxEditor = new BooleanFieldEditor(PreferenceConstants.REPORT_USE_SANDBOX, Messages.getString("ReportPreferencePage.9"), getFieldEditorParent());
        addField(useSandboxEditor);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
            checkState();
        }

        if (event.getSource() == localTemplateEditor){
            Activator.getDefault().getIReportTemplateDirectoryService().setDirectory((String) event.getNewValue());
        }
        
        if(event.getSource() == useSandboxEditor) {
            // show a warning dialog if user disables security feature
            boolean deactivated = !(Boolean) event.getNewValue();
            if(deactivated){
                getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialog.openWarning(getDisplay().getActiveShell(), Messages.getString("ReportPreferencePage.12"), Messages.getString("ReportPreferencePage.13"));
                    }
                });
            }
        }
    }
    
    private static Display getDisplay() {
        
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    @Override
    protected void checkState() {
        super.checkState();
        if (!isValid()) {
            return;
        }

    }
    
}
