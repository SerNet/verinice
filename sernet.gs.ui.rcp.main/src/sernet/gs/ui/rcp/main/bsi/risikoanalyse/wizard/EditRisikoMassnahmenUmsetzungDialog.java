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
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

/**
 * modal dialog to enter a new security measure ("Massnahme").
 * 
 * @author ahanekop@sernet.de
 *
 */
public class EditRisikoMassnahmenUmsetzungDialog extends Dialog {

	private Text textName;
	private Text textDescription;
	private RisikoMassnahmenUmsetzung risikoMassnahmenUmsetzung;
	
	public EditRisikoMassnahmenUmsetzungDialog(Shell parentShell, RisikoMassnahmenUmsetzung massnahme) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		risikoMassnahmenUmsetzung = massnahme;
	}
	
	@Override
	protected Control createDialogArea(Composite parentShell) {
		Composite container = (Composite) super.createDialogArea(parentShell);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		
		/* label name */
		final Label labelName = new Label(container, SWT.NONE);
		GridData gridLabelName = new GridData();
		gridLabelName.horizontalAlignment = SWT.LEFT;
	    gridLabelName.verticalAlignment = SWT.CENTER;
	    labelName.setText("Name:");
		labelName.setLayoutData(gridLabelName);
		
		/* text name */
		textName = new Text(container, SWT.BORDER);
		GridData gridTextName = new GridData();
		gridTextName.horizontalAlignment = SWT.FILL;
	    gridTextName.verticalAlignment = SWT.CENTER;
	    gridTextName.grabExcessHorizontalSpace = true;
		textName.setLayoutData(gridTextName);
		textName.setText(risikoMassnahmenUmsetzung.getTitle());
		
		/* label description */
		final Label labelDescription = new Label(container, SWT.NONE);
		GridData gridLabelDescription = new GridData();
		gridLabelDescription.horizontalAlignment = SWT.LEFT;
	    gridLabelDescription.verticalAlignment = SWT.CENTER;
	    labelDescription.setText("Beschreibung:");
		labelDescription.setLayoutData(gridLabelDescription);
		
		/* text description */
		textDescription = new Text(container, SWT.BORDER);
		GridData gridTextDescription = new GridData();
		gridTextDescription.horizontalAlignment = SWT.FILL;
	    gridTextDescription.verticalAlignment = SWT.CENTER;
	    gridTextDescription.grabExcessHorizontalSpace = true;
	    textDescription.setText(risikoMassnahmenUmsetzung.getDescription());
		textDescription.setLayoutData(gridTextDescription);
		
		 //add controls to composite as necessary
		 return container;
	}
	
	@Override
	protected void okPressed() {
		risikoMassnahmenUmsetzung.setName(textName.getText());
		risikoMassnahmenUmsetzung.setDescription(textDescription.getText());

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
