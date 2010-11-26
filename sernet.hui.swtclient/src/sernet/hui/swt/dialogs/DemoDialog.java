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
package sernet.hui.swt.dialogs;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.hui.swt.widgets.IInputHelper;
import sernet.snutils.DBException;
import sernet.snutils.ExceptionHandlerFactory;

public class DemoDialog extends org.eclipse.swt.widgets.Composite {

	public static void main(String[] args) {
		try {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			DemoDialog inst = new DemoDialog(shell, SWT.NULL);
			inst.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	private Shell shell;

	public DemoDialog(Shell parent, int style) {
		super(parent, style);
		shell = parent;
	}

	public void open() {
		try {
			shell.setSize(500, 600);
			shell.setText("Hitro-UI Demo");
			shell.setLayout(new FillLayout());
			this.setLayout(new FillLayout());

			HitroUIComposite huiComposite = new HitroUIComposite(this, SWT.NULL, false);
			
			try {
				HUITypeFactory htf = HUITypeFactory.createInstance(new URL("/home/akoderman/sncaWorkspace/conf/SNCA.xml"));
				
//				Entity entity = new Entity("mnums");
				Entity entity = new Entity("mnums");
				huiComposite.createView(entity, true, true, new String[] {}, false);

				PropertyType propertyType = htf.getPropertyType("mnums", "mnums_umsetzung");
				entity.setSimpleValue(propertyType, "mnums_umsetzung_teilweise");
				
				huiComposite.setInputHelper("itverbund_mitarbeiter", new IInputHelper() {
					
					public String[] getSuggestions() {
						return new String[] {
								"Herr Meier",
								"Frau Müller",
								"Herr Kunz"
						};
					}
					
				},
				IInputHelper.TYPE_REPLACE,
				true /*show hint*/);
				
			} catch (DBException e) {
				ExceptionHandlerFactory.getDefaultHandler().handleException(e);
			}
			
			shell.layout();
			shell.open();

			Display display = shell.getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


		
}
