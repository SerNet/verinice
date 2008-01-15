package sernet.gs.ui.rcp.main.bsi.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;

/**
 * Wraps BSI element as editor input.
 * 
 * @author koderman@sernet.de
 *
 */
public class BSIElementEditorInput implements IEditorInput {

	private CnATreeElement element;
	

	public BSIElementEditorInput(CnATreeElement element) {
		this.element = element;
	}
	
	public boolean exists() {
		return true;
	}
	
	public CnATreeElement getCnAElement() {
		return element;
	}
	
	public Entity getEntity() {
		return element.getEntity();
	}

	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages()
			.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
	}
	
	public String getId() {
		return element.getId();
	}

	public String getName() {
		return element.getTitle();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "BSI Element";
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	

	

}
