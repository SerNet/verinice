/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.opencsv.CSVReader;

import net._01001111.text.LoremIpsum;
import sernet.gs.service.VeriniceCharset;
import sernet.verinice.service.csv.CsvExport;
import sernet.verinice.service.csv.CsvExportException;
import sernet.verinice.service.csv.ICsvExport;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class CsvExportTest {

    private static final Object FILE_SUFFIX = "csv";
    private static final int MAX_ROWS = 500;
    private static final int MAX_COLUMNS = 50;
    private static final int MAX_WORDS = 10;
    private static final LoremIpsum LOREM = new LoremIpsum();
    private static final char SEMICOLON = ';';

    @Test
    public void testTable() throws CsvExportException, FileNotFoundException, IOException {
        ICsvExport exporter = new CsvExport();
        exporter.setFilePath(getFilePath());
        exporter.setSeperator(SEMICOLON);
        exporter.setCharset(VeriniceCharset.CHARSET_DEFAULT);
        List<String[]> table = createTable(MAX_COLUMNS,MAX_ROWS);
        exporter.exportToFile(table);
        checkExportFile(table);
    }

    @Test
    public void testRandomTable() throws CsvExportException, FileNotFoundException, IOException {
        ICsvExport exporter = new CsvExport();
        exporter.setFilePath(getFilePath());
        exporter.setSeperator(SEMICOLON);
        exporter.setCharset(VeriniceCharset.CHARSET_DEFAULT);
        List<String[]> table = createRandomTable();
        exporter.exportToFile(table);
        checkExportFile(table);
    }

    private void checkExportFile(List<String[]> table) throws FileNotFoundException, IOException {
        File exportFile = new File(getFilePath());
        assertTrue("Export file does not exists", exportFile.exists());
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(exportFile), ';', '"');
            String[] nextLine;
            Iterator<String[]> rowIterator = table.iterator();
            while ((nextLine = reader.readNext()) != null) {
                assertTrue("Not enough rows in CSV file", rowIterator.hasNext());
                String[] row = rowIterator.next();
                checkLine(nextLine, row);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void checkLine(String[] nextLine, String[] tableRow) {
        assertTrue("Wrong number of columns in CSV line: " + nextLine.length, tableRow.length==nextLine.length);
        int i=0;
        for (String csvColumn : nextLine ) {
            String tableColumn = tableRow[i];
            assertEquals("CSV column is different from  table column: " + csvColumn + " - " + tableColumn, csvColumn, tableColumn);
            i++;
        }
    }

    private List<String[]> createTable(int columns, int rows) {
        List<String[]> table = new LinkedList<String[]>();
        for (int i = 0; i < rows; i++) {
            String[] row = new String[(int) columns];
            for (int j = 0; j < columns; j++) {
                row[j] = String.valueOf(j) + "-" + String.valueOf(i);
            }
            table.add(row);
        }
        return table;
    }

    private List<String[]> createRandomTable() {
        long numberOfRows = getNumberOfRows();
        long numberOfColumns = getNumberOfColumns();
        List<String[]> table = new LinkedList<String[]>();
        for (int i = 0; i < numberOfRows; i++) {
            String[] row = new String[(int) numberOfColumns];
            for (int j = 0; j < numberOfColumns; j++) {
                row[j] = LOREM.words(getNumberOfWords());
            }
            table.add(row);
        }
        return table;
    }

    private long getNumberOfRows() {
        return Math.round(Math.random()*(getMaxRows()*1.0)) + 1;
    }

    private long getNumberOfColumns() {
        return Math.round(Math.random()*(getMaxColumns()*1.0)) + 1;
    }

    private int getNumberOfWords() {
        return (int) (Math.round(Math.random()*(getMaxWords()*1.0)) + 1);
    }


    private int getMaxRows() {
        return MAX_ROWS;
    }

    private int getMaxColumns() {
        return MAX_COLUMNS;
    }

    private int getMaxWords() {
        return MAX_WORDS;
    }

    private String getFilePath() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName().toString());
        sb.append(".").append(FILE_SUFFIX);
        return sb.toString();
    }

}
