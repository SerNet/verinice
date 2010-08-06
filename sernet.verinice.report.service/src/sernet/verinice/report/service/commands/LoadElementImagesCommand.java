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
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads the images that have been stored along with a finding.
 * 
 * TODO: The images are supposedly attached to a measure, the {@link CnATreeElement}
 * to which the measure belongs or a completely different entity. The {@link #id}
 * property is supposed to be used for this.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
@SuppressWarnings("serial")
public class LoadElementImagesCommand extends GenericCommand {
	
	private int id;
	
	public LoadElementImagesCommand(int id)
	{
		this.id = id;
	}

	private List<byte[]> result;

	public List<byte[]> getResult() {
		return result;
	}

	@Override
	public void execute() {
		ArrayList<byte[]> r = new ArrayList<byte[]>();

		final int max = (int) (Math.random() * 4);
		for (int i = 0; i < max; i++) {
			BufferedImage image = new BufferedImage(320, 240,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = (Graphics2D) image.getGraphics();
			g2d.setBackground(Color.GREEN);
			g2d.setColor(Color.WHITE);
			g2d.clearRect(0, 0, 320, 240);
			g2d.drawString(String.format("Demoimage '%s' of '%s'", i+1, max), 5, 30);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				ImageIO.write(image, "jpeg", bos);
			} catch (IOException e) {
				// TODO: Not cared for
			}

			final byte[] imageData = bos.toByteArray();
			r.add(imageData);
		}
		
		result = r;
	}

}
