package sernet.verinice.oda.driver.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.oda.driver.Activator;

/**
 * like innerClass Helper of Query
 * javascript engine isnt able to access inner classes, so this is mandatory
 * @author hagedorn
 *
 */

public class ReportJavaScriptHelper {
	
	public ReportJavaScriptHelper(){
		// BIRT JavaScript Constructor for use with class.newInstance
	}
	
    /**
     * Takes a {@link BufferedImage} instance and turns it into a byte array which can be used
     * by BIRT's dynamic images.
     * 
     * <p>Note: If a dataset should contain only a single image it *MUST* be wrapped
     * using {@link #wrapeSingleImageResult}.</p>
     * 
     * @param im
     * @return
     * @throws IOException
     */
    public byte[] createImageResult(BufferedImage im) throws IOException
    {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ImageIO.write(im, "png", bos);
    	return bos.toByteArray();
    }
    
	public ICommand execute(ICommand c)
	{
		try
		{
			return Activator.getDefault().getCommandService().executeCommand(c);
		} catch (CommandException e)
		{
//		    log.error("Query Helper: running a command failed.", e);
			throw new IllegalStateException("Running the command failed.", e);
		}
	}
}
