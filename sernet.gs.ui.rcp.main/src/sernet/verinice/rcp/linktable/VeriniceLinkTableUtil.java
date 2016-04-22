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

import java.io.File;
import java.util.*;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.rcp.linktable.composite.CsvExportDialog;
import sernet.verinice.rcp.linktable.composite.combo.VeriniceLinkTableOperationType;
import sernet.verinice.service.csv.ICsvExport;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceLinkTableUtil {

    private static final Logger LOG = Logger.getLogger(VeriniceLinkTableUtil.class);
    private static HashMap<String, String> vltExtensions = null;
    private static HashMap<String, String> csvExtensions = null;
    private static CsvExportDialog csvDialog;


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
        // to prevent instantiation
    }

    public static Map<String, String> getCsvExtensions() {
        return csvExtensions;
    }

    public static Map<String, String> getVltExtensions() {
        return vltExtensions;
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
        if (filePath != null) {

            File file = new File(filePath);
            String dir = file.getParent();
            Activator.getDefault().getPreferenceStore().setValue(defaultFolderPreference, dir);
        }
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

    public static String createVltFilePath(Shell shell, String text) {
        return createFilePath(shell, text, PreferenceConstants.DEFAULT_FOLDER_VLT, vltExtensions);
    }

    public static String createCsvFilePath(Shell shell, String text) {
        return createFilePath(shell, text, PreferenceConstants.DEFAULT_FOLDER_CSV_EXPORT,
                csvExtensions);
    }

    public static String createCsvFilePathAndHandleScopes(Shell shell, String text,
            VeriniceLinkTable veriniceLinkTable) {

        csvDialog = new CsvExportDialog(Display.getCurrent().getActiveShell(), text,
                veriniceLinkTable);
        if (csvDialog.open() == Dialog.OK) {
            return csvDialog.getFilePath();
        }

        return null;

    }

    public static List<String> getTableHeaders(VeriniceLinkTable veriniceLinkTable) {
        ArrayList<String> headers = new ArrayList<>();
        for (String element : veriniceLinkTable.getColumnPaths()) {
            int propertyBeginning = element
                    .lastIndexOf(VeriniceLinkTableOperationType.PROPERTY.getOutput());
            headers.add(element.substring(propertyBeginning + 1));
        }

        return headers;

    }

}
