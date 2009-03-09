/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
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

public class GetMassnahmeText extends GenericCommand {

	private String url;
	private String stand;
	private String text;

	public GetMassnahmeText(String url, String stand) {
		this.url = url;
		this.stand = stand;
	}

	public void execute() {
		try {
			InputStream in = BSIMassnahmenModel.getMassnahme(url, stand);
			text = InputUtil.streamToString(in,  "iso-8859-1");
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
