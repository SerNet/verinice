package sernet.hui.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import sernet.hui.common.connect.PropertyGroup;
import sun.print.PSPrinterJob.EPSPrinter;

/**
 * Groups items in the GUI. Groups can be expanded and collapsed.
 * 
 * @author koderman@sernet.de
 *
 */
public class PropertyTwistie implements IHuiControl {

	private PropertyGroup propGroup;
	private HitroUIView parent;
	private Composite parentComp;
	private Composite fieldsComposite;
	private ExpandableComposite huiTwistie;
	
	public Control getControl() {
		return huiTwistie;
	}

	public PropertyTwistie(HitroUIView parent,  Composite parentComp, 
			PropertyGroup propGroup) {
		this.propGroup = propGroup;
		this.parent = parent;
		this.parentComp = parentComp;
	}

	public void create() {
		// composite:
		huiTwistie = new ExpandableComposite(
				parentComp, SWT.BORDER, ExpandableComposite.EXPANDED
						| ExpandableComposite.FOCUS_TITLE | ExpandableComposite.TWISTIE);

		// set twistie to fill row:
		GridData huiTwistieLD = new GridData();
		huiTwistieLD.grabExcessHorizontalSpace = true;
		huiTwistieLD.horizontalAlignment = GridData.FILL;
		huiTwistieLD.horizontalSpan = 2;
		huiTwistie.setLayoutData(huiTwistieLD);
		huiTwistie.setText(propGroup.getName());
		
		huiTwistie.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanged(ExpansionEvent arg0) {
				parent.resizeContainer();
			}

			public void expansionStateChanging(ExpansionEvent arg0) {
				// nothing
			}
		});

		fieldsComposite = new Composite(huiTwistie, SWT.NULL);
		
		// set comp layout:
		GridLayout fieldsCompLayout = new GridLayout(2, false);
		fieldsCompLayout.verticalSpacing = 2;
		fieldsCompLayout.marginWidth = 2;
		fieldsCompLayout.marginHeight = 2;
		fieldsComposite.setLayout(fieldsCompLayout);

		// set comp to fill twistie:
		GridData fieldsCompLD = new GridData(GridData.FILL_BOTH);
		fieldsComposite.setLayoutData(fieldsCompLD);

		huiTwistie.setClient(fieldsComposite);
		huiTwistie.setExpanded(true);
		huiTwistie.pack(true);
		huiTwistie.layout(true);
		
	}

	public void setValue(String value) {
		// do nothing
	}

	public Composite getFieldsComposite() {
		return fieldsComposite;
	}

	public void setFocus() {
		// do nothing
	}
	
	public boolean validate() {
		return true;
	}
	
	public void update() {
		// n.a.
	}

}
