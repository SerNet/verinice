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
 * The DatabaseException is thrown by the data mover whenever
 * an error occurs.
 * 
 * @author Jeff Heaton (http://www.heatonresearch.com)
 * Used by permission.
 *
 */
public class DatabaseException extends Exception
{
  /**
   * Serial id
   */
  private static final long serialVersionUID = 838904293060250128L;

  
  /**
   * Construct an exception based on another exception.
   * 
   * @param e The other exception.
   */
  public DatabaseException(Exception e)
  {
    super(e);
  }
}
