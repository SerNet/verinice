/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.impl;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;



/**
 *
 */
public final class TocHelper2 {
    
    private TocHelper2(){};
    
    private static int pageBreakCount = 0;
    
    private static int tocEntryCount = 0;
    
    
    private static int listOfTablesEntryCount = 0;
    
    private static int listOfFiguresEntryCount = 0;
    
    private static int engineIteration = 0;
    
    private static int pageStartCount = 0;
    
    private static int maxTocEntryLength = 0;
    
    private static final Logger LOG = Logger.getLogger(TocHelper2.class);
    
    // list of tables
    private static Map<Integer, TocEntry<String, Integer>> loTMap = new HashMap<Integer, TocEntry<String,Integer>>();
    
    // table of contents
    private static Map<Integer, TocEntry<String, Integer>> tocMap = new HashMap<Integer, TocEntry<String, Integer>>();
    
    // list of figures
    private static Map<Integer, TocEntry<String, Integer>> loFMap = new HashMap<Integer, TocEntry<String,Integer>>();
    
    static{
        TocEntry<String, Integer> dummyEntry = new TocEntry<String, Integer>("Inhaltsverzeichnis braucht 2 Iterationen", -1);
        tocMap.put(tocEntryCount, dummyEntry);
    }
    
    public static void increaseEngineIteration(){
        engineIteration++;
    }
    
    public static void increasePageBreakCount(){
        pageBreakCount++;
    }
    
    public static void increasePageStartCount(){
        if(engineIteration > 1){
            pageStartCount++;
        }
    }
    
    public static int getPageStartCount(){
        return pageStartCount;
    }
    
    public static int getPageBreakCount(){
        return pageBreakCount;
    }
    
    public static void reset(){
        switch(engineIteration){
        
        case 1:
            tocEntryCount = 0;
            listOfTablesEntryCount = 0;
            listOfFiguresEntryCount = 0;
            break;
            
        case 2: 
            tocEntryCount = 0;
            listOfTablesEntryCount = 0;
            listOfFiguresEntryCount = 0;
            break;
        
        case 3:
            tocMap.clear();
            pageBreakCount = 0;
            tocEntryCount = 0;
            listOfTablesEntryCount = 0;
            listOfFiguresEntryCount = 0;
            engineIteration = 0;
            pageStartCount = 0;
            break;

        default:
            break;
        }
    }
    
    public static void resetTOC(){
        tocMap.clear();
        tocEntryCount = 0;
    }
    
    public static void addTocEntry(String entryTitle, Integer pageNumber){
        String entryTitle0 = removeTags(entryTitle);
        TocEntry<String, Integer> entry = new TocEntry<String, Integer>(entryTitle0, pageNumber);
        int entryNumberToPut = tocEntryCount;
        for(Entry<Integer, TocEntry<String, Integer>> mapEntry : tocMap.entrySet()){
            if(mapEntry.getValue().getTitle().equals(entry.getTitle())){
                entryNumberToPut = mapEntry.getKey();
            }
        }
        if(!tocMap.containsValue(entry)){
            tocMap.put(entryNumberToPut, entry);
            tocEntryCount++;
        }
    }
    
    public static String[] getTocLine(Integer i){
        String[] tocEntryLine = new String[]{"dummyTitle", "dummyPage"};
        if(engineIteration > 2){
            TocEntry<String, Integer> entry = tocMap.get(i);
            tocEntryLine[0] = entry.getTitle();
            tocEntryLine[1] = String.valueOf(entry.getPageNumber());
        }
        return tocEntryLine;
    }
    
    public static int getTocItemCount(){
        return tocMap.size();
    }
    
    // List of Tables only needs one run, because it's rendered at the end of the report
    public static String[] getLoTLine(Integer i){
        String[] lotEntryLine = new String[]{"", ""};
        if(engineIteration > 1){
            TocEntry<String, Integer> entry = loTMap.get(i);
            String title = (entry != null) ? entry.getTitle() : null;
            Integer page = (entry != null) ? entry.getPageNumber() : null;
            lotEntryLine[0] = (title != null) ? title : "dummyTitle";
            lotEntryLine[1] = (page != null) ? page.toString() : "dummyPage";
        }
        return lotEntryLine;
    }

    public static String[] getLoFLine(Integer i){
        String[] lofEntryLine = new String[]{"", ""};
        if(engineIteration > 1){
            TocEntry<String, Integer> entry = loFMap.get(i);
            String title = (entry != null) ? entry.getTitle() : null;
            Integer page = (entry != null) ? entry.getPageNumber() : null;
            lofEntryLine[0] = (title != null) ? title : "dummyTitle";
            lofEntryLine[1] = (page != null) ? page.toString() : "dummyPage";
        }
        return lofEntryLine;        
    }
    
    public static int getListOfTablesItemCount(){
        return loTMap.size();
    }
    
    public static int getListOfFiguresItemCount(){
        return loFMap.size();        
    }
    
    public static void addLoTEntry(String tableName, Integer pageNumber){
            int entryNumberToPut = listOfTablesEntryCount;
            String entryToPut = tableName.trim();
            for(Entry<Integer, TocEntry<String, Integer>> mapEntry : loTMap.entrySet()){
                String entryTitle = tableName.substring(tableName.indexOf(':') + 1).trim();
                String mapEntryTitle = mapEntry.getValue().getTitle().substring(mapEntry.getValue().getTitle().indexOf(":") + 1).trim();
                if(entryTitle.equals(mapEntryTitle)){
                    String preFix = mapEntry.getValue().getTitle().substring(0, mapEntry.getValue().getTitle().indexOf(":") + 1);
                    entryToPut = preFix.trim() + " " + mapEntryTitle.trim();
                    entryNumberToPut = mapEntry.getKey();
                }
            }
            TocEntry<String, Integer> entry = new TocEntry<String, Integer>(entryToPut, pageNumber);
            if(!loTMap.containsValue(entry)){
                loTMap.put(entryNumberToPut, entry);
            }
    }
    
    public static void addLoFEntry(String figureName, Integer pageNumber){
            int entryNumberToPut = listOfFiguresEntryCount;
            String entryToPut = figureName.trim();
            for(Entry<Integer, TocEntry<String, Integer>> mapEntry : loFMap.entrySet()){
                String entryTitle = figureName.substring(figureName.indexOf(':') + 1).trim();
                String mapEntryTitle = mapEntry.getValue().getTitle().substring(mapEntry.getValue().getTitle().indexOf(":") + 1).trim();
                if(entryTitle.equals(mapEntryTitle)){
                    String preFix = mapEntry.getValue().getTitle().substring(0, mapEntry.getValue().getTitle().indexOf(":") + 1);
                    entryToPut = preFix.trim() + " " + mapEntryTitle.trim();
                    entryNumberToPut = mapEntry.getKey();
                }
            }
            TocEntry<String, Integer> entry = new TocEntry<String, Integer>(entryToPut, pageNumber);
            if(!loFMap.containsValue(entry)){
                loFMap.put(entryNumberToPut, entry);
            }
    }
    
    public static void addTocEntry(String entryTitle, int indent, Integer pageNumber){
        final int maxChNrLength = 6;// longest entry should be something like : 1.2.3. ( which equals 6 characters)
        StringBuilder sb = new StringBuilder();
        String chapterNumber = null;
        String entryTitle0 = removeTags(entryTitle);
        if(entryTitle0.contains(" ")){
            String sub = entryTitle0.substring(0, entryTitle0.indexOf(' '));
            Pattern pattern  = Pattern.compile("\\d+.\\d*.*\\d*.*");
            Matcher m = pattern.matcher(sub);
            if(m.matches()){
                chapterNumber = sub;
                // remove zeros from end of string
                if(chapterNumber.endsWith("0")){
                    while(chapterNumber.endsWith("0")){
                        chapterNumber = chapterNumber.substring(0, chapterNumber.length() - 1);
                    }
                }
                // add padding to ensure same intervall from number to text on every entry
                while(chapterNumber.length() < maxChNrLength){ 
                    chapterNumber = chapterNumber + " ";
                }
            }
        }
        if(chapterNumber != null){
            sb.append(String.valueOf(chapterNumber));
            entryTitle0 = entryTitle.substring(entryTitle.indexOf(' ')).trim();
        }
        for(int i = 0; i < indent; i++){
            sb.append("\t");
        }
        sb.append(entryTitle0);
        addTocEntry(sb.toString(), pageNumber);
    }
    
    public static void checkTocEntryLength(String entry){
        final int maxTocEngineIteration = 3;
        if(engineIteration < maxTocEngineIteration && entry.length() > maxTocEntryLength){
            maxTocEntryLength = getStringDisplaySize(entry);
        }
    }
    
    public static int getMaxTocEntryLength(){
        return maxTocEntryLength;
    }
    
    private static final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

    public static String removeTags(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }

        Matcher m = REMOVE_TAGS.matcher(string);
        return m.replaceAll("");
    }
    
    public static int getLoTCount(){
        return listOfTablesEntryCount;
    }
    
    public static int getLoFCount(){
        return listOfFiguresEntryCount;
    }
    
    public static void increaseLoFCount(){
        if(engineIteration > 1){
            listOfFiguresEntryCount++;
        }        
    }
    
    
    public static void increaseLoTCount(){
        if(engineIteration > 1){
            listOfTablesEntryCount++;
        }
    }
    
    public static String computeChapterNumber(String title){
        String number = "";
        if(title.contains(" ")){
            String prefix = title.substring(0, title.indexOf(' '));
            try{
                int testInt = Integer.parseInt(prefix);
                number = String.valueOf(testInt);
            } catch (NumberFormatException e){
                // do nothing
            }
        }
        return number;
    }
    
    public static void log(String msg){
        LOG.error(msg);
    }
    
    public static void iterateList(List list){
        for(Object o : list){
            LOG.error(o.getClass().getCanonicalName());
        }
    }
    
    public static String getEngineIteration(){
        return String.valueOf(engineIteration);
    }
    
    public static void inspectObject(Object object){
        log(object.getClass().getCanonicalName());
    }
    
    public static String parseDate(String date, Locale locale){
        
        SimpleDateFormat formatter = new SimpleDateFormat("EE, dd.MM.yyyy", locale);
        SimpleDateFormat destinationFormat = new SimpleDateFormat("dd.MM.yyyy", locale);
        formatter.setLenient(true);
        try {
            Date fDate = formatter.parse(date);
            return destinationFormat.format(fDate);
        } catch (ParseException e) {
            LOG.error("Error while parsing date", e);
            return date;
        }
    }
    
   public static String addTocPadding(String entry, boolean left){
       return entry;
   }
   
   public static int getStringDisplaySize(String input){
       final int fontSize = 10;
       BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
       Font font = new Font("Arial", Font.PLAIN, fontSize); 
       FontMetrics fm = bi.getGraphics().getFontMetrics(font);
       return fm.stringWidth(input);
   }
   
   public static class TocEntry<TITLE, PAGENUMBER> implements Comparable<TocEntry<TITLE, PAGENUMBER>>{
        private final String title;
        private final Integer pagenumber;
        
        public TocEntry(String title, Integer pagenumber){
            this.title = title;
            this.pagenumber = pagenumber;
        }
        
        public String getTitle(){
            return title;
        }
        
        public Integer getPageNumber(){
            return pagenumber;
        }
        
        public String toString(){
           return "<" + title + ", " + pagenumber.toString() + ">"; 
        }
        
        public boolean equals(Object entry){
            if(!(entry instanceof TocEntry)){
                return false;
            }
            return this.toString().equals(((TocEntry<TITLE, PAGENUMBER>)entry).toString()); 
        }
        
        public int hashCode(){
            return toString().hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(TocEntry<TITLE, PAGENUMBER> o) {
            if(this.pagenumber > o.pagenumber){
                return 1;
            } else if(this.pagenumber < o.pagenumber){
                return -1;
            } else {
                return 0;
            }
        }
    }

}
