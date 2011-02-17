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
package sernet.hui.swt.widgets.URL;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.hui.common.connect.HuiUrl;
import sernet.hui.common.connect.PropertyType;


/**
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class URLControlDialog extends Dialog {


	private Text nameText;

	private Text hrefText;

	private String href;

	private String name;

	private Combo combo;

	private PropertyType type;

	private List<HuiUrl> previousUrls;

	public URLControlDialog(Shell shell,
			String name, String href, PropertyType type) {
		super(shell);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
		this.name = name;
		this.href = href;
		this.type = type;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		Label label1 = new Label(container, SWT.NONE);
		label1.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
				false, false, 1, 1));
		label1.setText("Name");
		
		nameText = new Text(container, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,		
				true, false, 1, 1));
		nameText.setText(name);
		
		Label label2 = new Label(container, SWT.NONE);
		label2.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
				false, false, 1, 1));
		label2.setText("Link");
		
		hrefText = new Text(container, SWT.BORDER);
		hrefText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 1, 1));
		hrefText.setText(href);
		
		Label label3 = new Label(container, SWT.NONE);
		label3.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
				false, false, 1, 1));
		label3.setText("Auswahl");
		
		combo = new Combo(container, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false, 1, 1));
		
		previousUrls = type.getResolvedUrls();
		Collections.sort(previousUrls, new Comparator<HuiUrl>() {
			public int compare(HuiUrl o1, HuiUrl o2) {
				return o1.name.compareTo(o2.name);
			}
			
		});
		
		combo.setItems(getNames(previousUrls));
		combo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				HuiUrl url = previousUrls.get(combo.getSelectionIndex());
				nameText.setText(url.name);
				hrefText.setText(url.url);
			}
		});
		
		
		return container;
	}
	
	private String[] getNames(List<HuiUrl> previousUrls2) {
		String[] result = new String[previousUrls2.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = previousUrls2.get(i).name;
		}
		return result;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Link Eigenschaften");
	}

	public String getHref() {
		return href;
	}
	
	@Override
	public boolean close() {
		this.href = hrefText.getText();
		this.name = nameText.getText();
		return super.close();
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
