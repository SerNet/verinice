/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.miginfocom.swt.MigLayout;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.Permission;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadPermissions;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdatePermissions;
import sernet.gs.ui.rcp.main.service.taskcommands.FindAllRoles;

/**
 * Simple dialog that allows defining the access options for an element.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
public class AccessControlEditDialog extends Dialog {
	
	private static final Logger log = Logger.getLogger(AccessControlEditDialog.class);

	private List<CnATreeElement> elements = new ArrayList<CnATreeElement>();
	
	private List<RawPermission> rawPermissions = new ArrayList<RawPermission>();
	
	private Button inheritButton;

	private String[] allRoles;
	
	public AccessControlEditDialog(Shell parent, IStructuredSelection selection) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);

		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object next = iterator.next();
			if (next instanceof CnATreeElement) {
				CnATreeElement nextElement = (CnATreeElement) next;
				elements.add(nextElement);
			}
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		// TODO rschuster: Translate me.
		newShell.setText("Rechtevergabe");
		newShell.setSize(400, 800);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		MigLayout ml = new MigLayout("wrap 3", "[grow, fill][left][left]", "[]");
		container.setLayout(ml);
		
		Label l = new Label(container, SWT.NONE);
		l.setText("Rolle");
		
		l = new Label(container, SWT.NONE);
		l.setText("lesen");
		
		l = new Label(container, SWT.NONE);
		l.setText("schreiben");
		
		// Note: The permissions should always be loaded right from the database
		// instead of relying on the data that is currently in the CnATreeElement instance.
		// Otherwise the client that did modifications to the permissions would not see
		// the changes.
		
		// TODO akoderman: for now, only the permissions of the first element are displayed, changes will be writte to all selected elements
		CnATreeElement firstElement = elements.get(0);
		
		// TODO rschuster: Do this in a separate thread
		LoadPermissions lp = new LoadPermissions(firstElement);
		FindAllRoles findAllRoles = new FindAllRoles(true);
		try {
			lp = (LoadPermissions) ServiceFactory.lookupCommandService().executeCommand(lp);
			findAllRoles = (FindAllRoles) ServiceFactory.lookupCommandService().executeCommand(findAllRoles);
		} catch (CommandException e) {
			// TODO rschuster: Handle this more gracefully
			throw new RuntimeException(e);
		}
		
		Set<String> roles = findAllRoles.getRoles();
		this.allRoles = (String[]) roles.toArray(new String[roles.size()]);
		
		Set<Permission> perms = lp.getPermissions();
		Combo t;
		Button r, w;
		for (Permission p : perms)
		{
			t = new Combo(container,  SWT.DROP_DOWN | SWT.READ_ONLY);
			t.setItems(allRoles);
			t.setText(p.getRole());
			
			r = new Button(container, SWT.CHECK | SWT.BORDER);
			r.setSelection(p.isReadAllowed());

			w = new Button(container, SWT.CHECK | SWT.BORDER);
			w.setSelection(p.isWriteAllowed());
			
			rawPermissions.add(new RawPermission(t, r, w));
		}

		// TODO rschuster: The next lines provide some empty rows which the
		// user can use for new permissions. Ideally we want an 'add'
		// button that dynamically creates a new row for us.
		t = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		t.setText("");
		t.setItems(allRoles);
		r = new Button(container, SWT.CHECK | SWT.BORDER); r.setSelection(false);
		w = new Button(container, SWT.CHECK | SWT.BORDER); w.setSelection(false);
		rawPermissions.add(new RawPermission(t, r, w));

		t = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		t.setText("");
		t.setItems(allRoles);
		r = new Button(container, SWT.CHECK | SWT.BORDER); r.setSelection(false);
		w = new Button(container, SWT.CHECK | SWT.BORDER); w.setSelection(false);
		rawPermissions.add(new RawPermission(t, r, w));
		
		t = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		t.setText("");
		t.setItems(allRoles);
		r = new Button(container, SWT.CHECK | SWT.BORDER); r.setSelection(false);
		w = new Button(container, SWT.CHECK | SWT.BORDER); w.setSelection(false);
		rawPermissions.add(new RawPermission(t, r, w));

		inheritButton = new Button(container, SWT.CHECK);
		inheritButton.setLayoutData("spanx 3, grow");
		if (elements.size()<2)
			inheritButton.setSelection(true);
		else 
			inheritButton.setSelection(false);
		inheritButton.setText("Rechte auf alle Kindelemente anwenden");
		
		return container;
	}
		
	static class RawPermission {
		
		Combo role;
		
		Button read, write;
		
		RawPermission(Combo role, Button read, Button write)
		{
			this.role = role;
			this.read = read;
			this.write = write;
		}
	}
	
	protected void okPressed()
	{
		Set<Permission> newPerms = new HashSet<Permission>();
		for (RawPermission rp : rawPermissions)
		{
			String role = rp.role.getText();
			boolean r = rp.read.getSelection();
			boolean w = rp.write.getSelection();
			if (role != null && role.length() > 0
					&& (r || w))
			{
				Permission p = Permission.createPermission(
						null, role, r, w);
				
				newPerms.add(p);
			}
			
			for (CnATreeElement element : elements) {
				UpdatePermissions up = new UpdatePermissions(
						element,
						newPerms,
						inheritButton.getSelection());
				
				try {
					ServiceFactory.lookupCommandService().executeCommand(up);
				} catch (CommandException e) {
					// TODO rschuster: Handle this more gracefully
					throw new RuntimeException(e);
				}
			}
		}

		super.okPressed();
	}
	
}
