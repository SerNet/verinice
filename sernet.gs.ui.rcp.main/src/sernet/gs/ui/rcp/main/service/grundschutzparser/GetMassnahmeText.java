/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.grundschutzparser;

import java.io.IOException;
import java.io.InputStream;

import sernet.gs.service.GSServiceException;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.verinice.interfaces.GenericCommand;

public class GetMassnahmeText extends GenericCommand {

	private String url;
	private String stand;
	private String text;
	private String encoding;

	public GetMassnahmeText(String url, String stand) {
		this.url = url;
		this.stand = stand;
	}

	public void execute() {
		try {
			encoding = GSScraperUtil.getInstance().getModel().getEncoding();
			InputStream in = GSScraperUtil.getInstance().getModel().getMassnahme(url, stand);
			text = InputUtil.streamToString(in,  encoding);
		} catch (GSServiceException e) {
			throw new RuntimeCommandException(e);
		} catch (IOException e) {
			throw new RuntimeCommandException(e);
		}
	}

	public String getEncoding() {
		return encoding;
	}

	public String getText() {
		return text;
	}

}
