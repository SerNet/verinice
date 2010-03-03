/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package com.heatonresearch.datamover.db;


/**
 * Database class for the MySQL database.
 * 
 * @author Jeff Heaton (http://www.heatonresearch.com)
 *
 */
public class MySQL extends Database
{  
  
  /**
   * Abstrct method to process a database type. Sometimes database
   * types are not reported exactly as they need to be for proper
   * syntax. This method corrects the database type and size.
   * @param type The type reported
   * @param i The size of this column
   * @return The properly formatted type, for this database
   */
  public String processType(String type,int size)
  {
    String usigned = "UNSIGNED";
    int i = type.indexOf(usigned);
   
    if( i!=-1 )
      type = type.substring(0,i)+type.substring(i+usigned.length());
    
    if( type.equalsIgnoreCase("varchar") && (size==65535) )
      type = "TEXT";
    
    return type.trim();
  }
}
