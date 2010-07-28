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

@SuppressWarnings("serial")
public class LoadElementImagesCommand extends GenericCommand {

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
