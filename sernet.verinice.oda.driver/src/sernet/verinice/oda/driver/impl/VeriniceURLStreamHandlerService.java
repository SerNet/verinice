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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.osgi.service.url.AbstractURLStreamHandlerService;

import sernet.verinice.interfaces.oda.IImageProvider;

public class VeriniceURLStreamHandlerService extends AbstractURLStreamHandlerService {
	
	private static final Logger LOG = Logger.getLogger(VeriniceURLStreamHandlerService.class);

	private Map<String, IImageProvider> imageStreams = new HashMap<String, IImageProvider>();
	
	public VeriniceURLStreamHandlerService()
	{
		final File f = new File("/usr/share/pixmaps/gnome-spider.png");
		setImageProvider("test", new IImageProvider()
		{

			@Override
			public InputStream newInputStream(int width, int height) {
				try {
					return new FileInputStream(f);
				} catch (FileNotFoundException e) {
					LOG.error("Could not register 'test' image");
					throw new RuntimeException();
				}
			}
			
		});
	}
	
	void setImageProvider(String name, IImageProvider imageProvider)
	{
		imageStreams.put(name, imageProvider);
	}
	
	void remove(String name)
	{
		imageStreams.remove(name);
	}

	@Override
	public URLConnection openConnection(URL u) throws IOException {
		VeriniceURLConnection uc = new VeriniceURLConnection(u);
		
		String imageId = u.getAuthority();
		IImageProvider provider = imageStreams.get(imageId);
		
		String queryPart = u.getQuery();
		HashMap<String, String> parms = new HashMap<String, String>();
		if (queryPart != null)
		{
			String[] split = queryPart.split("=|&");
			for (int i=0;i+1<split.length;i+=2)
			{
				parms.put(split[i], split[i+1]);
			}
		}
		
		int width = parms.containsKey("width") ? Integer.parseInt(parms.get("width")) : 200;
		int height = parms.containsKey("height") ? Integer.parseInt(parms.get("height")) : 60;

		if (provider == null)
		{
			// Generate a placeholder graphic on the fly.
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = (Graphics2D) image.getGraphics();
			g2d.setBackground(Color.WHITE);
			g2d.setColor(Color.BLACK);
			g2d.clearRect(0, 0, width, height);
			g2d.drawString(String.format("Image '%s' not available!", imageId), 5, 30);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpeg", bos);
			
			final byte[] imageData = bos.toByteArray();
			
			provider = new IImageProvider()
			{

				@Override
				public InputStream newInputStream(int width, int height) {
					return new ByteArrayInputStream(imageData);
				}
				
			};
		}
		
		uc.setBounds(width, height);
		uc.setImageProvider(provider);
		
		return uc;
	}

}
