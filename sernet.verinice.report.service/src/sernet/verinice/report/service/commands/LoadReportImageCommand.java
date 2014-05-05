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
package sernet.verinice.report.service.commands;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import sernet.verinice.interfaces.GenericCommand;

/**
 * Loads and returns various image needed for a report.
 * 
 * TODO: A special place needs to be designated to store images which is then 
 * accessed through this command.
 * TODO: The sizes of the pictures should be fixed, otherwise BIRT will stretch or shrink them,
 * making them look ugly. With a bit of Javascript hackery it might be possible to make the
 * image element in the report adopt the size of the actual image. According to BIRT documentation
 * changes to report design elements are only allowed during onPrepare(). 
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class LoadReportImageCommand extends GenericCommand {

	private byte[] result;
	
	private String imageKey;
	
	public LoadReportImageCommand(String imageKey)
	{
		this.imageKey = imageKey;
	}
	
	public byte[] getResult() {
		return (result != null) ? result.clone() : null;
	}

	@Override
	public void execute() {
	    final int imageWidth = 320;
	    final int imageHeight = 240;
	    final int stringPosX = 5;
	    final int stringPosY = 30;
		BufferedImage image = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setBackground(Color.RED);
		g2d.setColor(Color.WHITE);
		g2d.clearRect(0, 0, imageWidth, imageHeight);
		g2d.drawString(String.format("ReportImage: '%s'", imageKey), stringPosX, stringPosY);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "jpeg", bos);
		} catch (IOException e) {
			// TODO: Not cared for
		}

		result = bos.toByteArray();
	}
	
}
