/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.impl.dynamictable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class TableRow implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 201506090913L;
    
    private static final Logger LOG = Logger.getLogger(TableRow.class);
    
    private String[] columns;
    
    private int rootElement;
    
    private String identifier = "";
    
    public TableRow(int rootElement, int columnCount, String identifier){
        this.columns = new String[columnCount];
        this.rootElement = rootElement;
        this.identifier = identifier;
    }
    
    public void addProperty(String property, int index){
        columns[index] = property;
    }
    
    public void addProperty(String property){
        int insertIndex = getFirstNullIndex();
        if(insertIndex > -1){
            columns[insertIndex] = property;
        } else {
            String[] newColumns = Arrays.copyOf(columns, columns.length +1);
            newColumns[newColumns.length - 1] = property;
            columns = newColumns;
        }
    }
    
    public String getProperty(int index){
        if(columns.length < index - 1 || columns[index] == null){
            return "";
        }
        return columns[index];
    }
    
    public String getIdentifier(){
        return identifier;
    }
    
    public int getRootElement(){
        return rootElement;
    }
    
    public List<String> getPropertyList(){
        return Arrays.asList(columns);
    }
    
    private int getFirstNullIndex(){
        for(int i = 0; i < columns.length; i++){
            if(columns[i] == null){
                return i;
            }
        }
        return -1;
    }
    
    public void setProperties(String[] columns){
        this.columns = columns;
    }
    
    
    @Override
    public boolean equals(Object other){
        if ((this == other)){
            return true;
        }
        if ((other == null)){
            return false;
        }
        if (!(other instanceof TableRow)){
            return false;
        }
        return this.getIdentifier().equals(((TableRow)other).getIdentifier());
       
    }
    
    @Override
    public int hashCode() {
        int result = 17;

        result = 37 * result
                + (this.getIdentifier() == null ? 0 : this.getIdentifier().hashCode());
        return result;
    }
    
    

}
