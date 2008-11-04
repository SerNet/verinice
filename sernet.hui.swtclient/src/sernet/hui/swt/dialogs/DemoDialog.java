package sernet.hui.swt.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
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
				HUITypeFactory.initialize("/home/akoderman/sncaWorkspace/conf/SNCA.xml");
				
//				Entity entity = new Entity("mnums");
				Entity entity = new Entity("mnums");
				huiComposite.createView(entity, true, true);

				PropertyType propertyType = HUITypeFactory.getInstance().getPropertyType("mnums", "mnums_umsetzung");
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
				IInputHelper.TYPE_REPLACE);
				
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
