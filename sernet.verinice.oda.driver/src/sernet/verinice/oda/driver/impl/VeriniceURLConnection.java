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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import sernet.verinice.interfaces.oda.IImageProvider;

public class VeriniceURLConnection extends URLConnection {
	
	private IImageProvider imageProvider;
	
	private int width, height;
	
	protected VeriniceURLConnection(URL url) {
		super(url);
		
		setDoOutput(false);
	}
	
	void setBounds(int w, int h)
	{
		width = w;
		height = h;
	}
	
	void setImageProvider(IImageProvider imageProvider)
	{
		this.imageProvider = imageProvider;
	}
	
	@Override
	public InputStream getInputStream()
	{
		return imageProvider.newInputStream(width, height);
	}

	@Override
	public final void connect() throws IOException {
	}

}
