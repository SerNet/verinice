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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net._01001111.text.LoremIpsum;
import sernet.verinice.model.search.Occurence;
import sernet.verinice.model.search.VeriniceSearchResultTable;
import sernet.verinice.model.search.VeriniceSearchResultRow;

/**
 * Creates a random search result with lorem ipsum content for unit testing.
 * JLorem is used to generate text: https://github.com/oliverdodd/jlorem
 * 
 * Number of rows and columns are controlled by two upper bounds: rows and columns.
 * The exact value is random value. Some cells in result are empty. 
 * You can control how many cells are filled with parameter fillfactor;
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class SearchResultGenerator {

    public static final String SEPERATOR = "_";  
    private static final LoremIpsum LOREM = new LoremIpsum();
    
    public static long rows = 200;
    public static long columns = 10;
    public static double fillFactor = 0.5;
    
    public static VeriniceSearchResultTable createResult(String phrase) {
        String type = LOREM.randomWord();

        List<String> propertyList = createPropertyList(type); 
        VeriniceSearchResultTable result = new VeriniceSearchResultTable(type, type, propertyList.toArray(new String[propertyList.size()]));
        long n = Math.round(Math.random()*(getRows()*1.0)) + 1;
        for (int i = 0; i < n; i++) {
            result.addVeriniceSearchResultRow(createRow(propertyList, phrase));
        }

        return result;
    }

    private static VeriniceSearchResultRow createRow(List<String> propertyList, String phrase) {
        String occurenceProperty = getRandomProperty(propertyList);
        String occurenceText = createOccurence(phrase) ;
        VeriniceSearchResultRow row = new VeriniceSearchResultRow(UUID.randomUUID().toString(), createOccurence(occurenceProperty, occurenceText));
        for (String property : propertyList) {        
            if(occurenceProperty.equals(property)) {
                String text = occurenceText.replace("</em>", "").replace("<em>", "");
                row.addProperty(occurenceProperty, text);
            } else if(Math.random() < getFillFactor()) {
                row.addProperty(property, LOREM.sentenceFragment());
            } 
        }
        return row;
    }
    
    /**
     * @param property
     * @param phrase
     * @return [property]: lorem ipsum <em>phrase</em> lorem ipsum
     */
    private static Occurence createOccurence(String property, String text) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(property).append("]: ").append(text); 
        Occurence occurence = new Occurence();
        occurence.addFragment(property, LOREM.randomWord(), text);
        return occurence;
    }
    
    /**
     * @param property
     * @param phrase
     * @return [property]: lorem ipsum <em>phrase</em> lorem ipsum
     */
    private static String createOccurence(String phrase) {
        StringBuilder sb = new StringBuilder();
        sb.append(LOREM.sentenceFragment());
        sb.append(" <em>").append(phrase).append("</em> ");
        sb.append(LOREM.sentenceFragment());
        return sb.toString();
    }

    private static String getRandomProperty(List<String> propertyList) {
        int n = propertyList.size()-1;
        int i = (int) Math.round(Math.random()*(n*1.0));
        return propertyList.get(i);
    }

    private static List<String> createPropertyList(String type) {
        long n = Math.round(Math.random()*(getColumns()*1.0)) + 10;
        List<String> list = new LinkedList<String>();
        for (int i = 0; i < n; i++) {
            list.add(createPropertyType(type));
        }
        return list;
    }
    
    private static String createPropertyType(String type) {
       StringBuilder sb = new StringBuilder();
       sb.append(type).append(SEPERATOR);
       sb.append(LOREM.randomWord()).append(SEPERATOR).append(LOREM.randomWord());
       return sb.toString();
    }
    
    public static long getRows() {
        return rows;
    }
    
    public static void setRows(long rows) {
        SearchResultGenerator.rows = rows;
    }

    public static long getColumns() {
        return columns;
    }

    public static void setColumns(long columns) {
        SearchResultGenerator.columns = columns;
    }

    public static double getFillFactor() {
        return fillFactor;
    }

    public static void setFillFactor(double fillFactor) {
        SearchResultGenerator.fillFactor = fillFactor;
    }
}
