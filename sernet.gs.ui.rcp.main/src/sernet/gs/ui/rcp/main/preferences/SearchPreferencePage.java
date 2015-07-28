/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.preferences;


import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.Activator;

/**
 * Configures Parameter for verinice built in search engine.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class SearchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {


    public static final String SEMICOLON = ";"; //$NON-NLS-1$
    public static final String COMMA = ","; //$NON-NLS-1$

    private static final String[][] ENCODING_COMBO_VALUES = new String[][] { new String[] { VeriniceCharset.CHARSET_UTF_8.displayName(), VeriniceCharset.CHARSET_UTF_8.name() }, new String[] { VeriniceCharset.CHARSET_ISO_8859_15.displayName(), VeriniceCharset.CHARSET_ISO_8859_15.name() }, new String[] { VeriniceCharset.CHARSET_WINDOWS_1250.displayName(), VeriniceCharset.CHARSET_WINDOWS_1250.name() } };

    private static final String[][] SEPERATOR_COMBO_VALUES = new String[][] { new String[] { Messages.getString("SearchPreferencePage.2"), SEMICOLON }, //$NON-NLS-1$
            new String[] { Messages.getString("SearchPreferencePage.5"), COMMA } //$NON-NLS-1$
    };
    private RadioGroupFieldEditor sortColumns;
    private ComboFieldEditor seperatorFieldEditor;
    private ComboFieldEditor encodingFieldEditor;
    private BooleanFieldEditor disableField;
    private BooleanFieldEditor startField;
    private Group commonSettings;
    private boolean standalone;

    @Override
    public void init(IWorkbench arg0) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.getString("SearchPreferencePage.0")); //$NON-NLS-1$
    }


    @Override
    protected Control createContents(Composite parent) {

        Composite top = new Composite(parent, SWT.FILL | SWT.BORDER_DASH);
        top.setLayout(new GridLayout());
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        standalone = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);


        if (standalone) {
            commonSettings = new Group(top, SWT.FILL | SWT.BORDER);
            commonSettings.setLayout(new FillLayout());
            commonSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            commonSettings.setText(Messages.getString("SearchPreferencePage.13"));

            disableField = new BooleanFieldEditor(PreferenceConstants.SEARCH_DISABLE, Messages.getString("SearchPreferencePage.16"), commonSettings);
            disableField.setPreferenceStore(getPreferenceStore());
            disableField.load();
            disableField.setPropertyChangeListener(new DisableFieldListener(commonSettings));

            startField = new BooleanFieldEditor(PreferenceConstants.SEARCH_INDEX_ON_STARTUP, Messages.getString("SearchPreferencePage.14"), commonSettings);
            startField.setPreferenceStore(getPreferenceStore());
            startField.load();
            startField.setEnabled(disableIndexOnStartUp(), commonSettings);
        }

        Group sortingComposite = new Group(top, SWT.FILL | SWT.BORDER);
        sortingComposite.setLayout(new GridLayout());
        sortingComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        sortingComposite.setText(Messages.getString("SearchPreferencePage.15"));

        String[][] labelAndValues = new String[2][2];
        labelAndValues[0] = new String[] { Messages.getString("SearchPreferencePage.10"), PreferenceConstants.SEARCH_SORT_COLUMN_BY_SNCA };
        labelAndValues[1] = new String[] { Messages.getString("SearchPreferencePage.11"), PreferenceConstants.SEARCH_SORT_COLUMN_BY_ALPHABET };

        sortColumns = new RadioGroupFieldEditor(
                PreferenceConstants.SEARCH_SORT_COLUMN_EDITOR_PREFERENCES,
                Messages.getString("SearchPreferencePage.1"), 2,
                labelAndValues,
                sortingComposite);
        sortColumns.setPreferenceStore(getPreferenceStore());
        sortColumns.load();

        Group csvExportSettingsGrid = new Group(top, SWT.FILL | SWT.BORDER);
        csvExportSettingsGrid.setLayout(new GridLayout());
        csvExportSettingsGrid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        csvExportSettingsGrid.setText(Messages.getString("SearchPreferencePage.12"));

        seperatorFieldEditor = new ComboFieldEditor(
                PreferenceConstants.SEARCH_CSV_EXPORT_SEPERATOR,
                Messages.getString("SearchPreferencePage.8"),
                SEPERATOR_COMBO_VALUES, csvExportSettingsGrid);
        seperatorFieldEditor.setPreferenceStore(getPreferenceStore());
        seperatorFieldEditor.load();
        seperatorFieldEditor.fillIntoGrid(csvExportSettingsGrid, 2);

        encodingFieldEditor = new ComboFieldEditor(
                PreferenceConstants.SEARCH_CSV_EXPORT_ENCODING,
                Messages.getString("SearchPreferencePage.9"),
                ENCODING_COMBO_VALUES, 
                csvExportSettingsGrid);
        encodingFieldEditor.setPreferenceStore(getPreferenceStore());
        encodingFieldEditor.load();
        encodingFieldEditor.fillIntoGrid(csvExportSettingsGrid, 2);
        
       return top;
    }

    /**
     * If the search is completely disabled this returns also false.
     *
     */
    private boolean disableIndexOnStartUp() {
        return !disableField.getBooleanValue();
    }

    @Override
    protected void performDefaults() {

        sortColumns.loadDefault();
        seperatorFieldEditor.loadDefault();
        encodingFieldEditor.loadDefault();
        
        if (standalone) {
            startField.loadDefault();
            startField.setEnabled(true, commonSettings);
            disableField.loadDefault();
        }
        
        super.performDefaults();
    }

    @Override
    public boolean performOk() {

        sortColumns.store();
        seperatorFieldEditor.store();
        encodingFieldEditor.store();
        
        if (standalone) {
            startField.store();
            disableField.store();
        }
        
        return super.performOk();
    }

    /**
     * Sets the {@link SearchPreferencePage#startField} to false, if the
     * disableField is set to true.
     *
     * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
     */
    private final class DisableFieldListener implements IPropertyChangeListener {
        /**
         *
         */
        private final Group commonSettings;

        /**
         * @param commonSettings
         */
        private DisableFieldListener(Group commonSettings) {
            this.commonSettings = commonSettings;
        }

        @Override
        public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent arg0) {
            if (disableField.getBooleanValue() == false) {
                startField.setEnabled(true, commonSettings);
            } else {
                startField.setEnabled(false, commonSettings);
            }
        }
    }


    /*
    * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
    */
    @Override
    protected void createFieldEditors() {
        // TODO Auto-generated method stub

    }
}
