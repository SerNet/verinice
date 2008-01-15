package sernet.hui.swt.widgets.multiselectionlist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;


public class MultiSelectionDialog extends org.eclipse.swt.widgets.Dialog {

	private Shell dialogShell;
	private Entity entity;
	private PropertyType propertyType;

	public MultiSelectionDialog(Shell parent, int style, Entity ent, PropertyType type) {
		super(parent, style);
		this.entity = ent;
		this.propertyType = type;
	}

	public void open() {
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

			dialogShell.setLayout(new GridLayout(1, false));
			dialogShell.setSize(400, 300);
			dialogShell.setText("Optionen für " + propertyType.getName());
			
			//Composite content = new Composite(dialogShell, SWT.NULL);
			//content.setLayout(new FillLayout(SWT.VERTICAL));
			
			MultiSelectionList mList = new MultiSelectionList(entity, propertyType, dialogShell);
			mList.create();
			GridData scrolledComposite1LData = new GridData();
			scrolledComposite1LData.grabExcessVerticalSpace = true;
			scrolledComposite1LData.horizontalAlignment = GridData.FILL;
			scrolledComposite1LData.verticalAlignment = GridData.FILL;
			scrolledComposite1LData.grabExcessHorizontalSpace = true;
			mList.setLayoutData(scrolledComposite1LData);
			
			
			// set selected options:
			List options = new ArrayList();
			List properties = entity.getProperties(propertyType.getId()).getProperties();
			if (properties != null) {
				for (Iterator iter = properties.iterator(); iter.hasNext();) {
					Property prop = (Property) iter.next();
					String optionId = prop.getPropertyValue();
					options.add(propertyType.getOption(optionId));
				}
				mList.setSelection(options, true);
			}
			
			Composite buttons = new Composite(dialogShell, SWT.NULL);
			GridLayout contLayout = new GridLayout(2, false);
			contLayout.horizontalSpacing = 5;
			buttons.setLayout(contLayout);
			
			GridData containerLData = new GridData();
			containerLData.horizontalAlignment = GridData.END;
			buttons.setLayoutData(containerLData);
			
			Button okayBtn = new Button(buttons, SWT.PUSH);
			okayBtn.setText("Fertig");
			okayBtn.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent arg0) {
					close();
				}
				
				public void widgetDefaultSelected(SelectionEvent arg0) {
					close();
				}
			});
			
			dialogShell.layout();
			//dialogShell.pack();
			dialogShell.open();
			Display display = dialogShell.getDisplay();
			while (!dialogShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void close() {
		dialogShell.dispose();
	}

}
