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

	@Override
	public String getContentType() {
		return "image/svg+xml";
	}
	
	

}
