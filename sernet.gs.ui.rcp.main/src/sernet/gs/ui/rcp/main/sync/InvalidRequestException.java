/*******************************************************************************
 * InvalidRequestException.java
 *
 * Copyright (c) 2009 Andreas Becker <andreas.r.becker@rub.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * 12.08.2009
 * @author Andreas Becker
 ******************************************************************************/
package sernet.gs.ui.rcp.main.sync;

@SuppressWarnings("serial")
public class InvalidRequestException extends Exception
{
	public InvalidRequestException()
	{
		super();
	}
	
	public InvalidRequestException( String arg0 )
	{
		super( arg0 );
	}
}
