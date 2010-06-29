package sernet.verinice.oda.driver.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import sernet.verinice.interfaces.oda.IImageProvider;

public class VeriniceURLConnection extends URLConnection {
	
	private IImageProvider imageProvider;
	
	protected VeriniceURLConnection(URL url) {
		super(url);
		
		setDoOutput(false);
	}
	
	void setImageProvider(IImageProvider imageProvider)
	{
		this.imageProvider = imageProvider;
	}
	
	@Override
	public InputStream getInputStream()
	{
		return imageProvider.newInputStream();
	}

	@Override
	public final void connect() throws IOException {
	}

}
