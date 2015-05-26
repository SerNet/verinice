package sernet.verinice.rcp.account;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.common.configuration.Configuration;

public class AccountContentProvider implements IStructuredContentProvider {
	
	
	public AccountContentProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof PlaceHolder) {
			return new Object[] {inputElement};
		}
		List<Configuration> accountList = (List<Configuration>) inputElement;
		return accountList.toArray(new Object[accountList.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	    // empty
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// empty
	}

}
