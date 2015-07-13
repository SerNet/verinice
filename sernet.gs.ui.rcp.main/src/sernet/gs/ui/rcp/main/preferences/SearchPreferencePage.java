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
    private BooleanFieldEditor startField;

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

      boolean standalone = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);


      if (standalone) {
          Group indexOnStartUpGroup = new Group(top, SWT.FILL | SWT.BORDER);
          indexOnStartUpGroup.setLayout(new FillLayout());
          indexOnStartUpGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
          indexOnStartUpGroup.setText(Messages.getString("SearchPreferencePage.13"));

          startField = new BooleanFieldEditor(PreferenceConstants.SEARCH_INDEX_ON_STARTUP, Messages.getString("SearchPreferencePage.14"), indexOnStartUpGroup);
          startField.setPreferenceStore(getPreferenceStore());
          startField.load();

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

    @Override
    protected void performDefaults() {

        sortColumns.loadDefault();
        seperatorFieldEditor.loadDefault();
        encodingFieldEditor.loadDefault();
        startField.loadDefault();

        super.performDefaults();
    }

    @Override
    public boolean performOk() {

        sortColumns.store();
        seperatorFieldEditor.store();
        encodingFieldEditor.store();
        startField.store();

        return super.performOk();
    }


    /*
    * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
    */
    @Override
    protected void createFieldEditors() {
        // TODO Auto-generated method stub

    }

}
