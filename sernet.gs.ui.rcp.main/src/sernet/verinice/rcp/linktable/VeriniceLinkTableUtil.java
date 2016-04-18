/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.service.csv.ICsvExport;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceLinkTableUtil {

    private static HashMap<String, String> vltExtensions = null;
    private static HashMap<String, String> csvExtensions = null;

    public static Map<String, String> getCsvExtensions() {
        return csvExtensions;
    }

    public static Map<String, String> getVltExtensions() {
        return vltExtensions;
    }

    static {
        if (vltExtensions == null) {
            vltExtensions = new HashMap<>();
            vltExtensions.put("*" + VeriniceLinkTable.VLT, "verinice link table (.vlt)");
        }
        if (csvExtensions == null) {
            csvExtensions = new HashMap<>();
            csvExtensions.put("*" + ICsvExport.CSV_FILE_SUFFIX, "CSV table (.csv)");
        }
    }

    private VeriniceLinkTableUtil() {
        // to prevent instantiating
    }

    public static String createFilePath(Shell shell, String text, String defaultFolderPreference,
            Map<String, String> filterExtensions) {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setText(text);
        dialog.setFilterPath(getDirectory(defaultFolderPreference));

        dialog.setFilterExtensions(filterExtensions.keySet().toArray(new String[] {})); // $NON-NLS-1$
        dialog.setFilterNames(filterExtensions.values().toArray(new String[] {}));
        dialog.setFilterIndex(0);
        String filePath = dialog.open();
        Activator.getDefault().getPreferenceStore().setValue(defaultFolderPreference, filePath);
        return filePath;
    }

    private static String getDirectory(String defaultFolderPreference) {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String dir = prefs.getString(defaultFolderPreference);
        if (dir == null || dir.isEmpty()) {
            dir = System.getProperty("user.home");
        }
        if (!dir.endsWith(System.getProperty("file.separator"))) {
            dir = dir + System.getProperty("file.separator");
        }
        return dir;
    }

}
