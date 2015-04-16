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

import sernet.verinice.model.search.VeriniceSearchResultObject;

/**
 * Exports a {@link VeriniceSearchResultObject} to a CSV table.
 * Exported columns are defined in {@link IColumnStore} parameter.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ICsvExport {

    /**
     * Default file path for export
     */
    public static final String FILE_PATH_DEFAULT = "verinice-search-result.csv";
    
    /**
     * Exports a {@link VeriniceSearchResultObject} to a CSV table. Table is returned
     * as byte array.
     * 
     * @param searchResult The result of a search, see ISearchService
     * @param columnStore Defines exported columns
     * @return CSV table as byte array
     * @throws CsvExportException In case of errors, RuntimeExpeptions are wrapped in a CsvExportExceptions 
     */
    byte[] export(VeriniceSearchResultObject searchResult, IColumnStore columnStore) throws CsvExportException;
    
    /**
     * Exports a {@link VeriniceSearchResultObject} to a CSV table. Table is saved as a file.
     * Set file path with <code>setFilePath(path)</code>.
     * 
     * @param searchResult The result of a search, see ISearchService
     * @param columnStore Defines exported columns
     * @throws CsvExportException In case of errors, RuntimeExpeptions are wrapped in a CsvExportExceptions
     */
    void exportToFile(VeriniceSearchResultObject searchResult, IColumnStore columnStore)  throws CsvExportException;
    
    /**
     * @param File path for exported CSV table
     */
    void setFilePath(String path);
}
