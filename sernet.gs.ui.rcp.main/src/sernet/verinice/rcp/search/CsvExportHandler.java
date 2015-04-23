/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.service.FileUtil;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.model.search.VeriniceSearchResultObject;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CsvExportHandler {
    
    private static final Logger LOG = Logger.getLogger(CsvExportHandler.class);
    private static final String CSV = ".csv";
    
    Shell shell;
    VeriniceSearchResultObject result;
    
    public CsvExportHandler(VeriniceSearchResultObject veriniceSearchResultObject, Shell shell) {
        super();
        this.result = veriniceSearchResultObject;
        this.shell = shell;
    }
    
    public void run() throws CsvExportException {
        try {
            String filePath = createFilePath();
            if(check(filePath)) {
                export(filePath);
                if(filePath!=null) {
                    Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.DEFAULT_FOLDER_CSV_EXPORT, FileUtil.getFolderFromPath(filePath));
                }
            }
        } catch(Exception e) {
            String message = "An error occured during export.";
            LOG.error(message, e);
            ExceptionUtil.log(e, message);
        }
    }

    private void export(String filePath) throws CsvExportException {    
        // Selected columns
        // IColumnStore columnStore = ColumnStoreFactory.getColumnStore(result.getEntityTypeId());
        // All columns:
        IColumnStore columnStore = CsvExport.createColumnStore(result);
        ICsvExport exporter = new CsvExport();
        exporter.setFilePath(filePath);
        exporter.exportToFile(result, columnStore);
    }
    
    private String createFilePath() {
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setText("Export to CSV file");
        try {   
            dialog.setFilterPath(getDirectory());
            dialog.setFileName(ICsvExport.FILE_PATH_DEFAULT);                      
        } catch (Exception e1) {
            LOG.debug("Error with file path: " + getDirectory(), e1);
            dialog.setFileName(""); //$NON-NLS-1$
        }
        dialog.setFilterExtensions(new String[] {"*" + CSV}); //$NON-NLS-1$          
        dialog.setFilterNames(new String[] {"CSV table (.csv)"});
        dialog.setFilterIndex(0);
        String filePath = dialog.open();
        return filePath;
    }

    private boolean check(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
          MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING| SWT.YES | SWT.NO);
          mb.setText("File exists");
          mb.setMessage(filePath + " already exists. Do you want to replace it?");
          return(mb.open() == SWT.YES);
        }
        return true;
    }
   
    private String getDirectory() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String dir = prefs.getString(PreferenceConstants.DEFAULT_FOLDER_CSV_EXPORT);      
        if(dir==null || dir.isEmpty()) {
            dir = System.getProperty("user.home");
        }       
        if (!dir.endsWith(System.getProperty("file.separator"))) {
            dir = dir + System.getProperty("file.separator");
        }
        return dir;
    }

    public void setVeriniceSearchResultObject(VeriniceSearchResultObject veriniceSearchResultObject) {
        this.result = veriniceSearchResultObject;
    }
}
