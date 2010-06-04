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
package sernet.verinice.oda.driver.impl;

import java.io.InputStream;

class VeriniceOdaDriver implements IVeriniceOdaDriver {

	private String serverURI;
	
	private VeriniceURLStreamHandlerService urlStreamHandlerFactory;
	
	VeriniceOdaDriver(String serverURI, VeriniceURLStreamHandlerService urlStreamHandlerFactory)
	{
		this.serverURI = serverURI;
		this.urlStreamHandlerFactory = urlStreamHandlerFactory;
	}
	
	@Override
	public String getServerURI() {
		return serverURI;
	}
	
	public void setImageProvider(String name, IImageProvider imageProvider)
	{
		urlStreamHandlerFactory.setImageProvider(name, imageProvider);
	}
	
	void remove(String name)
	{
		urlStreamHandlerFactory.remove(name);
	}

}
