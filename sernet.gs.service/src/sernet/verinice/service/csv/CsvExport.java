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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;


/**
 * Exports a table (List<String[]>) to a CSV file.
 *
 * This implementation uses opencsv to create CSV, see: http://opencsv.sourceforge.net/
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CsvExport implements ICsvExport {

    private static final Logger LOG = Logger.getLogger(CsvExport.class);

    private static final String ERROR_MESSAGE = "Error while exporting to CSV"; //$NON-NLS-1$

    private String filePath = FILE_PATH_DEFAULT;

    private char seperator = SEPERATOR_DEFAULT;
    private Charset charset = CHARSET_DEFAULT;

    public CsvExport() {
        super();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.ICsvExport#exportToFile(sernet.verinice.model.search.VeriniceSearchResultTable, sernet.verinice.rcp.search.ColumnStore)
     */
    @Override
    public void exportToFile(List<String[]> table) throws CsvExportException {
        try {
            FileUtils.writeByteArrayToFile(new File(filePath), export(table));
        } catch (IOException e) {
            throw new CsvExportException(ERROR_MESSAGE, e);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.ICsvExport#export(sernet.verinice.model.search.VeriniceSearchResultTable, sernet.verinice.rcp.search.ColumnStore)
     */
    @Override
    public byte[] export(List<String[]> table) throws CsvExportException {
        CSVWriter writer = null;
        try {
            StringWriter stringWriter = new StringWriter();
            writer = new CSVWriter(stringWriter, getSeperator());
            writer.writeAll(table);
            return stringWriter.toString().getBytes(getCharsetName());
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
                LOG.error(ERROR_MESSAGE, e);
            }
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.csv.ICsvExport#convert(java.util.List)
     */
    @Override
    public List<String[]> convert(List<List<String>> table) {
        List<String[]> result = new LinkedList<>();
        for (List<String> row : table) {
            result.add(row.toArray(new String[row.size()]));
        }
        return result;
    }

    private Charset getCharset() {
        return this.charset;
    }

    public String getCharsetName() {
        return getCharset().name();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.ICsvExport#setFilePath(java.lang.String)
     */
    @Override
    public void setFilePath(String path) {
        this.filePath = path;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.ICsvExport#setSeperator(char)
     */
    @Override
    public void setSeperator(char seperator)    {
        this.seperator = seperator;
    }

    public char getSeperator() {
       return seperator;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.ICsvExport#setCharset(org.apache.commons.lang.CharSet)
     */
    @Override
    public void setCharset(Charset charset) {
       this.charset = charset;
    }
}
