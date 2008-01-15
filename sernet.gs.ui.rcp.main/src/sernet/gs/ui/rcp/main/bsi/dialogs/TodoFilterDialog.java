package sernet.gs.ui.rcp.main.bsi.dialogs;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

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
