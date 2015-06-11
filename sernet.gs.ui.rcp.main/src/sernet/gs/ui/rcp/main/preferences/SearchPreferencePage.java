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
    
    private static final String[][] ENCODING_COMBO_VALUES = new String[][]{
        new String[]{VeriniceCharset.CHARSET_UTF_8.displayName(),VeriniceCharset.CHARSET_UTF_8.name()},
        new String[]{VeriniceCharset.CHARSET_ISO_8859_15.displayName(),VeriniceCharset.CHARSET_ISO_8859_15.name()},
        new String[]{VeriniceCharset.CHARSET_WINDOWS_1250.displayName(),VeriniceCharset.CHARSET_WINDOWS_1250.name()}       
    };
    
    private static final String[][] SEPERATOR_COMBO_VALUES = new String[][]{
        new String[]{Messages.getString("SearchPreferencePage.2"),SEMICOLON}, //$NON-NLS-1$
        new String[]{Messages.getString("SearchPreferencePage.5"),COMMA}        //$NON-NLS-1$
    };

    @Override
    public void init(IWorkbench arg0) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.getString("SearchPreferencePage.0")); //$NON-NLS-1$
    }

    @Override
    protected void createFieldEditors() {
        boolean standalone = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);
        if(standalone) {
            BooleanFieldEditor startField = new BooleanFieldEditor(PreferenceConstants.SEARCH_INDEX_ON_STARTUP, "Update index on startup", getFieldEditorParent()); //$NON-NLS-1$
            addField(startField);
        }
        BooleanFieldEditor sortColumns = new BooleanFieldEditor(PreferenceConstants.SEARCH_SORT_COLUMN_BY_SNCA, Messages.getString("SearchPreferencePage.1"), getFieldEditorParent()); //$NON-NLS-1$
        addField(sortColumns);
        ComboFieldEditor seperatorFieldEditor = new ComboFieldEditor(PreferenceConstants.SEARCH_CSV_EXPORT_SEPERATOR, 
                Messages.getString("SearchPreferencePage.8"),  //$NON-NLS-1$
                SEPERATOR_COMBO_VALUES, 
                getFieldEditorParent());
        addField(seperatorFieldEditor);
        ComboFieldEditor encodingFieldEditor = new ComboFieldEditor(PreferenceConstants.SEARCH_CSV_EXPORT_ENCODING, 
                Messages.getString("SearchPreferencePage.9"),  //$NON-NLS-1$
                ENCODING_COMBO_VALUES, 
                getFieldEditorParent());
        addField(encodingFieldEditor);
    }

}
