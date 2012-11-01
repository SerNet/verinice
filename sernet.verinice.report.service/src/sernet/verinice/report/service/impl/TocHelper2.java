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
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;



/**
 *
 */
public class TocHelper2 {
    
    
    private static int pageBreakCount = 0;
    
    private static int tocEntryCount = 0;
    
    
    private static int listOfTablesEntryCount = 0;
    
    private static int listOfFiguresEntryCount = 0;
    
    private static int engineIteration = 0;
    
    private static int pageStartCount = 0;
    
    private static int maxTocEntryLength = 0;
    
    private static Logger LOG = Logger.getLogger(TocHelper2.class);
    
    // list of tables
    private static HashMap<Integer, TocEntry<String, Integer>> loTMap = new HashMap<Integer, TocEntry<String,Integer>>();
    
    // table of contents
    private static HashMap<Integer, TocEntry<String, Integer>> tocMap = new HashMap<Integer, TocEntry<String, Integer>>();
    
    // list of figures
    private static HashMap<Integer, TocEntry<String, Integer>> loFMap = new HashMap<Integer, TocEntry<String,Integer>>();
    
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
            break;
            
        case 2: 
            
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
        }
    }
    
    public static void resetTOC(){
        tocMap.clear();
        tocEntryCount = 0;
    }
    
    public static void addTocEntry(String entryTitle, Integer pageNumber){
        entryTitle = removeTags(entryTitle);
        TocEntry<String, Integer> entry = new TocEntry<String, Integer>(entryTitle, pageNumber);
        tocMap.put(tocEntryCount, entry);
        tocEntryCount++;
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
            try{
                TocEntry<String, Integer> entry = loTMap.get(i);
                lotEntryLine[0] = entry.getTitle();
                lotEntryLine[1] = String.valueOf(entry.getPageNumber());
            } catch (NullPointerException e){
                LOG.error("Element (" + String.valueOf(i) + ") nicht gefunden");
            }
        }
        return lotEntryLine;
    }
    
    public static String[] getLoFLine(Integer i){
        String[] lofEntryLine = new String[]{"", ""};
        if(engineIteration > 1){
            try{
                TocEntry<String, Integer> entry = loFMap.get(i);
                lofEntryLine[0] = entry.getTitle();
                lofEntryLine[1] = String.valueOf(entry.getPageNumber());
            } catch (NullPointerException e){
                LOG.error("Element (" + String.valueOf(i) + ") nicht gefunden");
            }
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
            TocEntry<String, Integer> entry = new TocEntry<String, Integer>(tableName, pageNumber);
            loTMap.put(listOfTablesEntryCount, entry);
    }
    
    public static void addLoFEntry(String figureName, Integer pageNumber){
            TocEntry<String, Integer> entry = new TocEntry<String, Integer>(figureName, pageNumber);
            loFMap.put(listOfFiguresEntryCount, entry);
    }
    
    public static void addTocEntry(String entryTitle, int indent, Integer pageNumber){
        StringBuilder sb = new StringBuilder();
        String chapterNumber = null;
        entryTitle = removeTags(entryTitle);
        if(entryTitle.contains(" ")){
            String sub = entryTitle.substring(0, entryTitle.indexOf(" "));
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
                while(chapterNumber.length() < 6){ // longest entry should be something like : 1.2.3. ( which equals 6 characters)
                    chapterNumber = chapterNumber + " ";
                }
            }
        }
        if(chapterNumber != null){
            sb.append(String.valueOf(chapterNumber));
            entryTitle = entryTitle.substring(entryTitle.indexOf(" ")).trim();
        }
        sb.append(entryTitle);
        addTocEntry(sb.toString(), pageNumber);
    }
    
    public static void checkTocEntryLength(String entry){
        if(engineIteration < 3){
            if(entry.length() > maxTocEntryLength){
                maxTocEntryLength = getStringDisplaySize(entry);
            }
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
            String prefix = title.substring(0, title.indexOf(" "));
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
        Date d = new Date();
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
            String ret = destinationFormat.format(fDate);
            return ret;
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }
    
   public static String addTocPadding(String entry, boolean left){
       return entry;
   }
   
   public static int getStringDisplaySize(String input){
       GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
       BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
       Font font = new Font("Arial", Font.PLAIN, 10); 
       FontMetrics fm = bi.getGraphics().getFontMetrics(font);
       return fm.stringWidth(input);
   }
   
   public static class TocEntry<TITLE, PAGENUMBER>{
        private final TITLE title;
        private final PAGENUMBER pagenumber;
        
        public TocEntry(TITLE title, PAGENUMBER pagenumber){
            this.title = title;
            this.pagenumber = pagenumber;
        }
        
        public TITLE getTitle(){
            return title;
        }
        
        public PAGENUMBER getPageNumber(){
            return pagenumber;
        }
        
        public String toString(){
           return "<" + title + ", " + pagenumber + ">"; 
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
    }

}
