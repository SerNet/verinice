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
package sernet.verinice.rcp.search.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net._01001111.text.LoremIpsum;

import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.search.VeriniceSearchResultObject;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.CsvExport;
import sernet.verinice.rcp.search.CsvExportException;
import sernet.verinice.rcp.search.IColumnStore;
import sernet.verinice.rcp.search.ICsvExport;

import com.opencsv.CSVReader;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CsvExportTest {

    private static final Logger LOG = Logger.getLogger(CsvExportTest.class);
    
    private static final LoremIpsum LOREM = new LoremIpsum();
    private static final Object FILE_SUFFIX = "csv";
    public static double visabilityFactor = 0.2;

    @Test
    public void testSearchResultGenerator() {
        for (int i = 0; i < 500; i++) {
            testSearchResult();
        }

    }
    
    @Test
    public void testExport() throws CsvExportException, FileNotFoundException, IOException {
        for (int i = 0; i < 100; i++) {
            String phrase = LOREM.randomWord();
            VeriniceSearchResultObject result = SearchResultGenerator.createResult(phrase);
            IColumnStore columnStore = CsvExport.createColumnStore(result);
            setInvisibleColumns(columnStore);
            //assertTrue("No visible column", columnStore.getColumns().size()>0);
            ICsvExport exporter = new CsvExport();
            exporter.setFilePath(getFilePath());
            exporter.exportToFile(result, columnStore);    
            checkExportFile(result, columnStore);
        }
    }

    @Test(expected=CsvExportException.class)
    public void testExeptions() throws CsvExportException {
        ICsvExport exporter = new CsvExport();
        exporter.setFilePath("/diesen/ordner/gibt/es/nicht/export.csv");
        String phrase = LOREM.randomWord();
        VeriniceSearchResultObject result = SearchResultGenerator.createResult(phrase);
        IColumnStore columnStore = CsvExport.createColumnStore(result);
        exporter.exportToFile(result, columnStore);      
    }
    
    @Test()
    public void testEmptyResult() throws CsvExportException, FileNotFoundException, IOException {
        ICsvExport exporter = new CsvExport();
        exporter.setFilePath(getFilePath());
        String phrase = LOREM.randomWord();
        VeriniceSearchResultObject result = new VeriniceSearchResultObject(phrase);
        IColumnStore columnStore = CsvExport.createColumnStore(result);
        exporter.exportToFile(result, columnStore);  
        checkExportFile(result, columnStore);
    }
    
    private void testSearchResult() {
        String phrase = LOREM.randomWord();
        VeriniceSearchResultObject result = SearchResultGenerator.createResult(phrase);
        assertNotNull("Result is null", result);
        List<VeriniceSearchResultRow> rows = result.getAllResults();
        assertFalse("Result is empty", rows.isEmpty());
        for (VeriniceSearchResultRow row : rows) {
            String occurence = row.getFieldOfOccurence();
            assertNotNull("occurence is null", occurence);
            assertTrue("Occurence field does not contain phrase: " + phrase, occurence.contains(phrase));
            boolean found = false;
            Set<String> types = row.getPropertyTypes();
            assertFalse("Row is empty", types.isEmpty());
            for (String type : types) {
                if (row.getValueFromResultString(type).contains(phrase)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Phrase not found in row: " + phrase, found);
        }
    }

    private void checkExportFile(VeriniceSearchResultObject result, IColumnStore columnStore) throws FileNotFoundException, IOException {
        File exportFile = new File(getFilePath());
        assertTrue("Export file does not exists", exportFile.exists());
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(exportFile), ',', '"', 1);
            String[] nextLine;
            Iterator<VeriniceSearchResultRow> rows = result.getAllResults().iterator();
            while ((nextLine = reader.readNext()) != null) {          
                assertTrue("To many lines in CSV file", rows.hasNext());
                VeriniceSearchResultRow row = rows.next(); 
                checkLine(nextLine, row, columnStore);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void checkLine(String[] nextLine, VeriniceSearchResultRow row, IColumnStore columnStore) {
        Set<PropertyType> columns = columnStore.getColumns();
        assertTrue("Wrong number of columns in CSV line: " + nextLine.length, nextLine.length==1 || columns.size()==nextLine.length);
        int i=0;
        for (PropertyType type : columns ) {
            String searchResultValue = row.getValueFromResultString(type.getId());
            String csvValue = nextLine[i];
            while(!searchResultValue.isEmpty() && csvValue.isEmpty()) {
                i++;
                csvValue = nextLine[i];
            }
            assertTrue("Search result values is different from CSV value: " + searchResultValue + " - " + csvValue, searchResultValue.equals(csvValue));
            i++;                              
        }
    }
    
    private IColumnStore setInvisibleColumns(IColumnStore columnStore) {
        List<PropertyType> columns = new LinkedList<PropertyType>(columnStore.getColumns());
        for (PropertyType propertyType : columns) {
            if(Math.random() > visabilityFactor) {
                columnStore.setVisible(propertyType, false);
            }
        }
        return columnStore;
    }

    private String getFilePath() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName().toString());
        sb.append(".").append(FILE_SUFFIX);
        return sb.toString();
    }
    
    

}
