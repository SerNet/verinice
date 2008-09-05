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
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

/**
 * modal dialog to enter a new security measure ("Massnahme").
 * 
 * @author ahanekop@sernet.de
 *
 */
public class EditRisikoMassnahmenUmsetzungDialog extends Dialog {

	private Text textNumber;
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
		
		/* label number */
		final Label labelNumber = new Label(container, SWT.NONE);
		GridData gridLabelNumber = new GridData();
		gridLabelNumber.horizontalAlignment = SWT.LEFT;
	    gridLabelNumber.verticalAlignment = SWT.CENTER;
	    labelNumber.setText("Nummer:");
		labelNumber.setLayoutData(gridLabelNumber);
		
		/* text number */
		textNumber = new Text(container, SWT.BORDER);
		GridData gridTextNumber = new GridData();
		gridTextNumber.horizontalAlignment = SWT.FILL;
	    gridTextNumber.verticalAlignment = SWT.CENTER;
	    gridTextNumber.grabExcessHorizontalSpace = true;
		textNumber.setLayoutData(gridTextNumber);
		textNumber.setText(notNull(risikoMassnahmenUmsetzung.getNumber()));
		
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
		textName.setText(notNull(risikoMassnahmenUmsetzung.getTitel()));
		
		/* label description */
		final Label labelDescription = new Label(container, SWT.NONE);
		GridData gridLabelDescription = new GridData();
		gridLabelDescription.horizontalAlignment = SWT.LEFT;
	    gridLabelDescription.verticalAlignment = SWT.CENTER;
	    labelDescription.setText("Beschreibung:");
		labelDescription.setLayoutData(gridLabelDescription);
		
		/* text description */
		textDescription = new Text(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		GridData gridTextDescription = new GridData();
		gridTextDescription.horizontalAlignment = SWT.FILL;
	    gridTextDescription.verticalAlignment = SWT.FILL;
	    gridTextDescription.grabExcessHorizontalSpace = true;
	    gridTextDescription.grabExcessVerticalSpace = true;
	    gridTextDescription.widthHint = 400;
	    gridTextDescription.heightHint = 200;
		textDescription.setLayoutData(gridTextDescription);
		textDescription.setText(notNull(risikoMassnahmenUmsetzung.getDescription()));
		
		 return container;
	}
	
	private String notNull(String string) {
		return string != null ? string : "";
	}

	@Override
	protected void okPressed() {
		risikoMassnahmenUmsetzung.setName( textName.getText());
		risikoMassnahmenUmsetzung.setDescription(textDescription.getText());
		risikoMassnahmenUmsetzung.setNumber(textNumber.getText());
		
		risikoMassnahmenUmsetzung.getRisikoMassahme().setName(textName.getText());
		risikoMassnahmenUmsetzung.getRisikoMassahme().setDescription(textDescription.getText());
		risikoMassnahmenUmsetzung.getRisikoMassahme().setNumber(textNumber.getText());
		
		
		try {
			RisikoMassnahmeHome.getInstance().saveUpdate(risikoMassnahmenUmsetzung.getRisikoMassahme());
		} catch (Exception e) {
			ExceptionUtil.log(e, "Änderung konnte nicht gespeichert werden.");
		}

		super.okPressed();
	}
}
