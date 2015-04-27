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
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.search.VeriniceSearchResultObject;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.column.ColumnStore;
import sernet.verinice.rcp.search.column.IColumn;
import sernet.verinice.rcp.search.column.IColumnFactory;
import sernet.verinice.rcp.search.column.IColumnStore;

import com.opencsv.CSVWriter;

/**
 * Exports a {@link VeriniceSearchResultObject} to a CSV table.
 * Exported columns are defined in {@link IColumnStore} parameter.
 * 
 * This implementation uses opencsv to create CSV, see: http://opencsv.sourceforge.net/
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CsvExport implements ICsvExport {
 
    private static final String UTF_8 = "UTF-8";
    private static final String ERROR_MESSAGE = "Error while exporting search result to CSV";
    
    private String filePath = FILE_PATH_DEFAULT;
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.ICsvExport#exportToFile(sernet.verinice.model.search.VeriniceSearchResultObject, sernet.verinice.rcp.search.ColumnStore)
     */
    @Override
    public void exportToFile(VeriniceSearchResultObject result, IColumnStore columnStore) throws CsvExportException {
        try {
            FileUtils.writeByteArrayToFile(new File(filePath), export(result, columnStore));
        } catch (IOException e) {
            throw new CsvExportException(ERROR_MESSAGE, e);
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.ICsvExport#export(sernet.verinice.model.search.VeriniceSearchResultObject, sernet.verinice.rcp.search.ColumnStore)
     */
    @Override
    public byte[] export(VeriniceSearchResultObject result, IColumnStore columnStore) throws CsvExportException {       
        CSVWriter writer = null;
        try {
            if(columnStore==null) {
                columnStore = createColumnStore(result);
            }
            StringWriter stringWriter = new StringWriter();
            writer = doExport(result, columnStore, stringWriter);
            return stringWriter.toString().getBytes(UTF_8);
        } catch (RuntimeException e) {
            throw new CsvExportException(ERROR_MESSAGE, e);
        } catch (Exception e) {
            throw new CsvExportException(ERROR_MESSAGE, e);
        } finally {
            try {
                if(writer!=null) {
                    writer.close();
                }
            } catch (IOException e) {
                throw new CsvExportException(ERROR_MESSAGE, e);
            }
        }
    }

    private CSVWriter doExport(VeriniceSearchResultObject result, IColumnStore columnStore, Writer writer ) {      
        CSVWriter csvWriter = new CSVWriter(writer);
        List<VeriniceSearchResultRow> rows = result.getAllResults();
        exportHeader(columnStore, csvWriter);
        if(rows==null || rows.isEmpty()) {
            return csvWriter;
        }     
        for (VeriniceSearchResultRow row : rows) {
            exportRow(row, columnStore, csvWriter);                         
        }
        return csvWriter;
    }

    private void exportHeader(IColumnStore columnStore, CSVWriter csvWriter) {
        Iterator<IColumn> columns = columnStore.getColumns().iterator();
        String[] csvColumns = new String[columnStore.getColumns().size()];
        for (int i = 0; i < csvColumns.length; i++) {
            IColumn column = columns.next();
            csvColumns[i] = column.getTitle();
        }
        csvWriter.writeNext(csvColumns);
    }

    private void exportRow(VeriniceSearchResultRow row, IColumnStore columnStore, CSVWriter csvWriter) {
        Set<IColumn> columns = columnStore.getColumns();
        String[] rowArray = new String[columns.size()];
        int i=0;
        for (IColumn column : columns ) {
            rowArray[i] = row.getValueFromResultString(row.getValueFromResultString(column.getId()));
            i++;                         
        }
        csvWriter.writeNext(rowArray);
    }
    
    public static IColumnStore createColumnStore(VeriniceSearchResultObject result) {
        IColumnStore columnStore = new ColumnStore();
        List<VeriniceSearchResultRow> rows = result.getAllResults();
        for (VeriniceSearchResultRow row : rows) {
            Set<String> types = row.getPropertyTypes();
            for (String id : types ) {
                PropertyType type = new PropertyType();
                type.setId(id);   
                columnStore.setVisible(IColumnFactory.getPropertyTypeColumn(type, columnStore), true);
            }                                 
        }
        return columnStore;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.ICsvExport#setFilePath(java.lang.String)
     */
    @Override
    public void setFilePath(String path) {
        this.filePath = path;
    }

}
