/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.iso27k;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import sernet.gs.service.CsvFile;

/**
 * @author Daniel <dm[at]sernet[dot]de>
 */
public class ImportConfiguration implements Serializable {

	private transient Logger log = Logger.getLogger(ImportConfiguration.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(ImportConfiguration.class);
		}
		return log;
	}
	
	
	public static final char COMMA = ',';
	
	public static final char SEMICOLON = ';';
	
	public static final char TAB = '\t';
	
	public static final char SPACE = ' ';
	
	public static final char SINGLE_QUOTE = '\'';
	
	public static final char DOUBLE_QUOTE = '"';
	
	
	public static final char SEPERATOR_DEFAULT = COMMA;
	
	public static final char QUOTE_DEFAULT = DOUBLE_QUOTE;
	
	
	public static final int NUMBER_OF_COLUMNS = 3;
	
	
	private char seperator = SEPERATOR_DEFAULT;
	
	private char quote = QUOTE_DEFAULT;
	
	private boolean isEmpty = false;
	

	/**
	 * Creates a configuration for import:
	 * 
	 * <ul>
	 * <li>Removes empty lines from the file content</li>
	 * <li>Tries to guess the separator char</li>
	 * </ul>
	 * 
	 * @param csvFile a Csv file
	 */
	public ImportConfiguration(CsvFile csvFile) {
		try {
			StringBuilder sb = new StringBuilder();
			// guess seperator from content
			List<String> lineList = IOUtils.readLines(new ByteArrayInputStream(csvFile.getFileContent()));
			boolean sepComma = true, sepSemicolon = true, sepTab = true, sepSpace = true;
			int nComma=0, nSemicolon=0, nTab=0, nSpace=0;
			if(lineList==null || lineList.size()==0) {
				setEmpty(true);
			} else {
				// find first not empty line
				String line = null;
				int notEmptyLineNumber = 0;
				while((line==null || line.length()==0) && notEmptyLineNumber<lineList.size()) {
					line = lineList.get(notEmptyLineNumber);
					notEmptyLineNumber++;
				}
				if(line!=null && line.length()>0) {
					sb.append(line).append('\n');
					nComma = line.replaceAll("[^,]","").length();
					nSemicolon = line.replaceAll("[^;]","").length();
					nTab = line.replaceAll("[^\t]","").length();
					nSpace = line.replaceAll("[^ ]","").length();
				}
				if(lineList.size()>notEmptyLineNumber+1) {
					// every line must have same number of seperators
					for (int i = notEmptyLineNumber; i < lineList.size(); i++) {
						line = lineList.get(i);
						// ignore empty lines
						if(line!=null && line.length()>0) {
							sb.append(line).append('\n');
							if(sepComma) {
								sepComma = (nComma==line.replaceAll("[^,]","").length() && nComma>0);
							}
							if(sepSemicolon) {
								sepSemicolon = (nSemicolon==line.replaceAll("[^;]","").length() && nSemicolon>0);
							}
							if(sepTab) {
								sepTab = (nTab==line.replaceAll("[^\t]","").length() && nTab>0);
							}
							if(sepSpace) {
								sepSpace = (nSpace==line.replaceAll("[^ ]","").length() && nSpace>0);
							}
						}
					}
					csvFile.setFileContent(sb.toString().getBytes());
				} else {
					// only one line
					sepComma = (nComma==ImportConfiguration.NUMBER_OF_COLUMNS-1);
					sepSemicolon = (nSemicolon==ImportConfiguration.NUMBER_OF_COLUMNS-1);
					sepTab = (nTab==ImportConfiguration.NUMBER_OF_COLUMNS-1);
					sepSpace = (nSpace==ImportConfiguration.NUMBER_OF_COLUMNS-1);
				}
				// only true if all others are false
				if(sepComma && (!sepSemicolon && !sepTab && !sepSpace)) {
					setSeperator(ImportConfiguration.COMMA);			
				}
				else if(sepSemicolon && (!sepComma && !sepTab && !sepSpace)) {
					setSeperator(ImportConfiguration.SEMICOLON);			
				}
				else if(sepTab && (!sepSemicolon && !sepComma && !sepSpace)) {
					setSeperator(ImportConfiguration.TAB);			
				}
				else if(sepSpace && (!sepSemicolon && !sepTab && !sepComma)) {
					setSeperator(ImportConfiguration.SPACE);			
				}
				if (getLog().isInfoEnabled()) {
					if(getSeperator()==ImportConfiguration.SPACE) {
						getLog().info("Seperartor char is: space");
					} else if(getSeperator()==ImportConfiguration.TAB) {
						getLog().info("Seperartor char is: tab");
					} else {
						getLog().info("Seperartor char is: " + getSeperator());
					}
					
				}
			}
		} catch(Exception e) {
			getLog().error("Error while creating configurtation", e);
		}
	}
	
	public char getSeperator() {
		return seperator;
	}

	public void setSeperator(char seperator) {
		this.seperator = seperator;
	}

	public char getQuote() {
		return quote;
	}

	public void setQuote(char quote) {
		this.quote = quote;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}


}
