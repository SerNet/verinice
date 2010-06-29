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

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.osgi.service.url.AbstractURLStreamHandlerService;

import sernet.verinice.interfaces.oda.IImageProvider;

public class VeriniceURLStreamHandlerService extends AbstractURLStreamHandlerService {
	
	private static final Logger log = Logger.getLogger(VeriniceURLStreamHandlerService.class);

	private HashMap<String, IImageProvider> imageStreams = new HashMap<String, IImageProvider>();
	
	public VeriniceURLStreamHandlerService()
	{
		final File f = new File("/usr/share/pixmaps/gnome-spider.png");
		setImageProvider("test", new IImageProvider()
		{

			@Override
			public InputStream newInputStream() {
				try {
					return new FileInputStream(f);
				} catch (FileNotFoundException e) {
					log.error("Could not register 'test' image");
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

		if (provider == null)
		{
			// Generate a placeholder graphic on the fly.
			BufferedImage image = new BufferedImage(200, 60, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = (Graphics2D) image.getGraphics();
			g2d.setBackground(Color.WHITE);
			g2d.setColor(Color.BLACK);
			g2d.clearRect(0, 0, 200, 60);
			g2d.drawString(String.format("Image '%s' not available!", imageId), 5, 30);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpeg", bos);
			
			final byte[] imageData = bos.toByteArray();
			
			provider = new IImageProvider()
			{

				@Override
				public InputStream newInputStream() {
					return new ByteArrayInputStream(imageData);
				}
				
			};
		}
		
		uc.setImageProvider(provider);
		
		return uc;
	}

}
