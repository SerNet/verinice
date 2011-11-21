/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

import java.net.URL;

public interface IInternalServer {
	
	void setGSCatalogURL(URL url);
	
	void setDSCatalogURL(URL url);
	
	void configure(String url, String user, String pass,
			String driver, String dialect);

	void start() throws IllegalStateException;
	
	void stop();
	
	boolean isRunning();
	
	void addInternalServerStatusListener(IInternalServerStartListener listener);
	
	void removeInternalServerStatusListener(IInternalServerStartListener listener);
}
