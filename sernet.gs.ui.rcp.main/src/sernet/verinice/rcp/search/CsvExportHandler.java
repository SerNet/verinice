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
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.service.FileUtil;
import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.preferences.SearchPreferencePage;
import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.model.search.VeriniceSearchResultTable;
import sernet.verinice.rcp.search.column.ColumnStoreFactory;
import sernet.verinice.rcp.search.column.IColumnStore;
import sernet.verinice.service.csv.CsvExport;
import sernet.verinice.service.csv.CsvExportException;
import sernet.verinice.service.csv.ICsvExport;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CsvExportHandler {
    
    private static final Logger LOG = Logger.getLogger(CsvExportHandler.class);
    private static final String CSV = ".csv";
    public static final String FILE_NAME_DEFAULT = "verinice-search-result.csv";
    
    Shell shell;
    VeriniceSearchResultTable result;
    
    public CsvExportHandler(VeriniceSearchResultTable veriniceSearchResultTable, Shell shell) {
        super();
        this.result = veriniceSearchResultTable;
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
            String message = "An error occurred during export.";
            LOG.error(message, e);
            ExceptionUtil.log(e, message);
        }
    }

    private void export(String filePath) throws CsvExportException {    
        // Selected columns
        IColumnStore columnStore = ColumnStoreFactory.getColumnStore(result.getEntityTypeId());

        List<String[]> simpleTable = SearchResultTableConverter.convertTable(result, columnStore);

        ICsvExport exporter = new CsvExport();
        exporter.setFilePath(filePath);
        exporter.setSeperator(getSeperator());
        exporter.setCharset(getCharset());
        exporter.exportToFile(simpleTable);
    }
    
    private Charset getCharset() {
        // read the charset from preference store
        // charset value is set in CharsetHandler
        String charsetName = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.SEARCH_CSV_EXPORT_ENCODING);
        Charset charset = VeriniceCharset.CHARSET_DEFAULT;
        if (charsetName != null && !charsetName.isEmpty()) {
            charset = Charset.forName(charsetName);
        }
        return charset;
    }

    /**
     * If no seperator is set, this getter will always return the default
     * seperatore {@link SearchPreferencePage#SEMICOLON}
     *
     */
    private char getSeperator() {
        String seperator = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.SEARCH_CSV_EXPORT_SEPERATOR);
        if (StringUtils.isEmpty(seperator)) {
            return SearchPreferencePage.SEMICOLON.charAt(0);
        }
        return seperator.charAt(0);
    }

    private String createFilePath() {
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setText("Export to CSV file");
        try {   
            dialog.setFilterPath(getDirectory());
            dialog.setFileName(FILE_NAME_DEFAULT);
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
        
        if (filePath == null) {
            return false;
        }
        
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
            dir = System.getProperty(IVeriniceConstants.USER_HOME);
        }       
        if (!dir.endsWith(System.getProperty(IVeriniceConstants.FILE_SEPARATOR))) {
            dir = dir + System.getProperty(IVeriniceConstants.FILE_SEPARATOR);
        }
        return dir;
    }

    public void setVeriniceSearchResultObject(VeriniceSearchResultTable veriniceSearchResultTable) {
        this.result = veriniceSearchResultTable;
    }
}
