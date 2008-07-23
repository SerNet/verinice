package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control; 
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;

/**
 * modal dialog to enter a new security measure ("Massnahme").
 * 
 * @author ahanekop@sernet.de
 *
 */
public class EditMassnahmeUmsetzungDialog extends Dialog {

	private Text textName;
	private MassnahmenUmsetzung massnahmeUmsetzung;
	
	public EditMassnahmeUmsetzungDialog(Shell parentShell, MassnahmenUmsetzung massnahme) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		massnahmeUmsetzung = massnahme;
	}
	
	@Override
	protected Control createDialogArea(Composite parentShell) {
		Composite container = (Composite) super.createDialogArea(parentShell);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		
		/* label: name of Massnahme */
		final Label label2 = new Label(container, SWT.NONE);
		GridData data3 = new GridData();
		data3.horizontalAlignment = SWT.LEFT;
	    data3.verticalAlignment = SWT.CENTER;
	    label2.setText("Name:");
		label2.setLayoutData(data3);
		
		/* text: name of Massnahme */
		textName = new Text(container, SWT.BORDER);
		GridData data4 = new GridData();
		data4.horizontalAlignment = SWT.FILL;
	    data4.verticalAlignment = SWT.CENTER;
	    data4.grabExcessHorizontalSpace = true;
		textName.setLayoutData(data4);
		textName.setText(massnahmeUmsetzung.getTitle());
		
		 //add controls to composite as necessary
		 return container;
	}
	
	@Override
	protected void okPressed() {
		massnahmeUmsetzung.setName(textName.getText());

		/* TODO persistent speichern 
		try {
			OwnGefaehrdungHome.getInstance().saveUpdate(ownGefaehrdung);
		} catch (Exception e) {
			ExceptionUtil.log(e, "Änderung konnte nicht gespeichert werden.");
		}
		*/
		
		super.okPressed();
	}
}
