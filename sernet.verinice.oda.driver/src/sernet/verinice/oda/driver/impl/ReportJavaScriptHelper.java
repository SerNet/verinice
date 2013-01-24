/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.oda.driver.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.oda.driver.Activator;

/**
 * like innerClass Helper of Query
 * javascript engine isnt able to access inner classes, so this is mandatory
 *
 */

public class ReportJavaScriptHelper {
	
    private final static Logger LOG = Logger.getLogger(ReportJavaScriptHelper.class);
    
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
			throw new IllegalStateException("Running the command failed.", e);
		}
	}
	
	public static void log(String msg){
	    LOG.debug(msg);
	}
}
