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
package sernet.verinice.service.csv;

import java.nio.charset.Charset;
import java.util.List;


import sernet.gs.service.VeriniceCharset;


/**
 * Exports a {@link VeriniceSearchResultTable} to a CSV table.
 * Exported columns are defined in {@link IColumnStore} parameter.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ICsvExport {

    /**
     * Default file path for export
     */
    public static final String FILE_PATH_DEFAULT = "verinice-data.csv";
    public static final String CSV_FILE_SUFFIX = ".csv";
    public static final Charset CHARSET_DEFAULT = VeriniceCharset.CHARSET_DEFAULT;
    public static final char SEPERATOR_SEMICOLON = ';';
    public static final char SEPERATOR_COMMA = ',';
    public static final char SEPERATOR_DEFAULT = SEPERATOR_SEMICOLON;


    /**
     * Exports a table of Strings to a CSV table. CSV table is returned
     * as byte array.
     *
     * @param table A table of Strings
     * @return CSV table as byte array
     * @throws CsvExportException
     */
    byte[] export(List<String[]> table) throws CsvExportException;

    /**
     * Exports a table of Strings to a CSV table. CSV table is saved as a file.
     * Set file path with <code>setFilePath(path)</code>.
     *
     * @param table A table of Strings
     * @throws CsvExportException
     */
    void exportToFile(List<String[]> table)  throws CsvExportException;

    List<String[]> convert(List<List<String>> table);

    /**
     * @param File path for exported CSV table
     */
    void setFilePath(String path);

    /**
     * Sets the seperator for the values (typically on of those ",", ;, \t)
     *
     */
    void setSeperator(char seperator);

    /**
     * Sets the charset for this export.
     *
     */
    void setCharset(Charset charset);
}
