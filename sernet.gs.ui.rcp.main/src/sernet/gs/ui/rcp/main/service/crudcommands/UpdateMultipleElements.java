package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class UpdateMultipleElements<T> extends GenericCommand {

	private List<T> elements;

	public UpdateMultipleElements(List<T> elements) {
		this.elements = elements;
	}
	
	public void execute() {
		ArrayList<T> mergedElements = new ArrayList<T>(elements.size());
		if (elements != null && elements.size()>0) {
			IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory()
				.getDAO(elements.get(0).getClass());
			for (T element : elements) {
				T mergedElement = dao.merge(element, true);
				mergedElements.add(mergedElement);
			}
			elements = mergedElements;
		}
	}

	public List<T> getElements() {
		return elements;
	}

}
