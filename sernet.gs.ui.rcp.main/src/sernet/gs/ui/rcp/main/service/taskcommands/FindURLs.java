package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLink;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLinkRoot;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.swt.widgets.URL.URLUtil;

public class FindURLs extends GenericCommand {

	private BSIModel model;
	private Set<String> allIDs;
	private DocumentLinkRoot urls;
	
	public FindURLs(Set<String> allIDs) {
		this.allIDs = allIDs;
	}

	public void execute() {
		LoadBSIModel command = new LoadBSIModel();
		getCommandService().executeCommand(command);
		model = command.getModel();
		urls = findEntries(allIDs);
	}

	public DocumentLinkRoot findEntries(Set<String> allIDs) {
		DocumentLinkRoot root = new DocumentLinkRoot();
		if (model == null)
			return root;
		List<CnATreeElement> elements = model
				.getAllElements(false /* do not filter Massnahmen, they contain links too */);
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
					link.addChild(reference);
				}
			}
		}
		return root;
	}

	public Set<String> getAllIDs() {
		return allIDs;
	}

	public DocumentLinkRoot getUrls() {
		return urls;
	}
	

}
