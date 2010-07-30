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
 * making them look ugly.
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
		return result;
	}

	@Override
	public void execute() {
		BufferedImage image = new BufferedImage(320, 240,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setBackground(Color.RED);
		g2d.setColor(Color.WHITE);
		g2d.clearRect(0, 0, 320, 240);
		g2d.drawString(String.format("ReportImage: '%s'", imageKey), 5, 30);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "jpeg", bos);
		} catch (IOException e) {
			// TODO: Not cared for
		}

		result = bos.toByteArray();
	}
	
}
