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
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;

/**
 * Configures Parameter for verinice built in search engine.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class SearchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor sortColumns;

    @Override
    public void init(IWorkbench arg0) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.getString("SearchPreferencePage.0"));
    }

    @Override
    protected void createFieldEditors() {
        sortColumns = new BooleanFieldEditor(PreferenceConstants.SEARCH_SORT_COLUMN_BY_SNCA, Messages.getString("SearchPreferencePage.1"), getFieldEditorParent());
        addField(sortColumns);
    }

}
