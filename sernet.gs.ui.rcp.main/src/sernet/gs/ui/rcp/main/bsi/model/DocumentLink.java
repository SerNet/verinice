package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class DocumentLink implements Serializable {

	public static final Object NO_LINK_MESSAGE = ""; //$NON-NLS-1$


	public DocumentLink(String name, String href) {
		super();
		this.name = name;
		this.href = href;
	}

	private Set<DocumentReference> children = new HashSet<DocumentReference>();
	private String name;
	private String href;
	

	public String getName() {
		return this.name;
	}

	public String getHref() {
		return this.href;
	}
	
	public void addChild(DocumentReference child) {
		if (children.add(child))
			child.setParent(this);
	}
	
	public Set<DocumentReference> getChildren() {
		return children;
	}


}
