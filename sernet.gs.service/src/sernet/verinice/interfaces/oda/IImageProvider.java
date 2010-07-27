package sernet.verinice.interfaces.oda;

import java.io.InputStream;

public interface IImageProvider {

	InputStream newInputStream(int width, int height);
	
}
