package sernet.gs.ui.rcp.main.bsi.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

public class DocumentLinkRoot {

	private Set<DocumentLink> children = new HashSet<DocumentLink>();
	
	

	public void addChild(DocumentLink link) {
		this.children.add(link);
	}
	
	public DocumentLink[] getChildren() {
		return (DocumentLink[]) children.toArray(new DocumentLink[children.size()]);
	}

	public DocumentLink getDocumentLink(String name, String href) {
		for (DocumentLink link : children) {
			if (link.getName().equals(name)
					&& link.getHref().equals(href))
				return link;
		}
		return null;
	}
}
