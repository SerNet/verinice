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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLink;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLinkRoot;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.swt.widgets.URL.URLUtil;

public class FindURLs extends GenericCommand {
	
	private static final long serialVersionUID = 9207422070204886804L;

	private static final Logger log = Logger.getLogger(FindURLs.class);

	private Set<String> allIDs;
	private DocumentLinkRoot urls;
	
	public FindURLs(Set<String> allIDs) {
		this.allIDs = allIDs;
	}

	public void execute() {
		
		urls = findEntries(allIDs);
		
		log.debug("result of findEntries(): " + urls);
	}

	public DocumentLinkRoot findEntries(Set<String> allIDs) {
		DocumentLinkRoot root = new DocumentLinkRoot();
		LoadBSIModel command = new LoadBSIModel();
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		BSIModel model = command.getModel();
		List<CnATreeElement> elements = model.getAllElementsFlatList(true);
		
		for (CnATreeElement element : elements) {
			Entity entity = element.getEntity();
			if (entity == null)
				continue;

			for (String id : allIDs) {
				PropertyList properties = entity.getProperties(id);
				if (properties != null && properties.getProperties() != null
						&& properties.getProperties().size() > 0) {

					String url = properties.getProperty(0).getPropertyValue();
					if (URLUtil.getName(url).equals("") && URLUtil.getHref(url).equals(""))
						continue;
					
					DocumentLink link = root.getDocumentLink(URLUtil.getName(url), URLUtil.getHref(url));
					if (link == null) {
						link = new DocumentLink(URLUtil
								.getName(url), URLUtil.getHref(url));
						root.addChild(link);
					}
					DocumentReference reference = new DocumentReference(element);
					hydrate(element);
					link.addChild(reference);
				}
			}
		}
		return root;
	}

	private void hydrate(CnATreeElement element) {
		element.getTitel();
	}

	public Set<String> getAllIDs() {
		return allIDs;
	}

	public DocumentLinkRoot getUrls() {
		return urls;
	}
	

}
