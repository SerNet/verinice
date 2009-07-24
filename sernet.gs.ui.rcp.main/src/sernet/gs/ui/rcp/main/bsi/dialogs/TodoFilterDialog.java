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
package sernet.gs.ui.rcp.main.bsi.dialogs;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TodoFilterDialog extends FilterDialog {

	private Text text1;
	private Text text2;

	private String umsetzungDurch;
	protected String zielobjekt;

	public TodoFilterDialog(Shell parent,
			String[] umsetzung, 
			String[] siegel,
			String umsetzungDurch,
			String zielobjekt) {
		super(parent, umsetzung, siegel, null);
		this.umsetzungDurch = umsetzungDurch;
		this.zielobjekt = zielobjekt;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		Label intro = new Label(container, SWT.NONE);
		intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
				false, false, 2, 1));
		intro.setText("Wählen Sie aus, nach welchen Kriterien die Liste der Maßnahmen" +
				" gefiltert wird.");
		
		Label label1 = new Label(container, SWT.NONE);
		label1.setText("Verantwortlich ");
		
		text1 = new Text(container, SWT.BORDER);
		text1.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 1, 1));
		text1.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				umsetzungDurch = text1.getText();
			}
		});
		
		Label label2 = new Label(container, SWT.NONE);
		label2.setText("Zielobjekt");
		
		text2 = new Text(container, SWT.BORDER);
		text2.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 1, 1));
		text2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				zielobjekt = text2.getText();
			}
		});
		
		Group boxesComposite = createUmsetzungGroup(parent);
		Group boxesComposite2 = createSiegelGroup(parent);
		createUmsetzungCheckboxes(boxesComposite);
		createSiegelCheckboxes(boxesComposite2);
		initContent();
		
		return container;
	}

	protected void initContent() {
		super.initContent();
		text1.setText(umsetzungDurch != null ? umsetzungDurch : "");
		text2.setText(zielobjekt != null ? zielobjekt : "");
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Maßnahmen Filter Einstellungen");
	}
	
	public String getUmsetzungDurch() {
		return umsetzungDurch;
	}
	
	public String getZielobjekt() {
		return zielobjekt;
	}
}
