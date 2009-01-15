package sernet.gs.ui.rcp.main.service.grundschutzparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

public class GetGefaehrdungText extends GenericCommand {

	private String url;
	private String stand;
	private String text;

	public GetGefaehrdungText(String url, String stand) {
		this.url = url;
		this.stand = stand;
	}

	public void execute() {
		try {
			InputStream in = BSIMassnahmenModel.getGefaehrdung(url, stand);
			text = InputUtil.streamToString(in);
		} catch (GSServiceException e) {
			throw new RuntimeCommandException(e);
		} catch (IOException e) {
			throw new RuntimeCommandException(e);
		}
	}

	public String getText() {
		return text;
	}

}
