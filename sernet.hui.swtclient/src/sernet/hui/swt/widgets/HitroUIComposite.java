package sernet.hui.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import sernet.hui.common.connect.Entity;
import sernet.snutils.DBException;

public class HitroUIComposite extends ScrolledComposite {
	
	
	private HitroUIView huiView;
	
	public HitroUIComposite(Composite parent, int style, boolean twistie) {
		// scrollable composite:
		super(parent, SWT.V_SCROLL
				| SWT.BORDER);
		this.setExpandHorizontal(true);
		this.setExpandVertical(true);
		GridLayout scrollLayout = new GridLayout(4, true);
		this.setLayout(scrollLayout);
		
		if (twistie)
			createTwistieGroup();
		else
			createGroup();
		
	}
	
	public void setInputHelper(String typeid, IInputHelper helper, int type, boolean showHint) {
		huiView.setInputHelper(typeid, helper, type, showHint);
	}
	
	public void resetInitialFocus() {
		huiView.setInitialFocus();
	}

	private void createGroup() {
		// form composite:
		Composite contentComp = new Composite(this, SWT.NULL);
		this.setContent(contentComp);
		
		GridData contentCompLD = new GridData();
		contentCompLD.grabExcessHorizontalSpace = true;
		contentCompLD.horizontalAlignment = GridData.FILL;
		contentCompLD.horizontalSpan = 4;
		contentComp.setLayoutData(contentCompLD);

		GridLayout contentCompLayout = new GridLayout(4, true);
		contentCompLayout.marginWidth = 5;
		contentCompLayout.marginHeight = 5;
		contentCompLayout.numColumns = 4;
		contentCompLayout.makeColumnsEqualWidth = false;
		contentCompLayout.horizontalSpacing = 5;
		contentCompLayout.verticalSpacing = 5;
		contentComp.setLayout(contentCompLayout);
		
		// HUI composite:
		ExpandableComposite huiTwistie = new ExpandableComposite(
				contentComp, SWT.BORDER, ExpandableComposite.EXPANDED
						);
		huiTwistie.setText("");

		// set twistie to fill row:
		GridData huiTwistieLD = new GridData();
		huiTwistieLD.grabExcessHorizontalSpace = true;
		huiTwistieLD.horizontalAlignment = GridData.FILL;
		huiTwistieLD.horizontalSpan = 4;
		huiTwistie.setLayoutData(huiTwistieLD);

		huiTwistie.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanged(ExpansionEvent arg0) {
				huiView.resizeContainer();
			}

			public void expansionStateChanging(ExpansionEvent arg0) {
				// nothing
			}
		});

		Composite fieldsComposite = new Composite(huiTwistie, SWT.NULL);
		
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

		huiView = new HitroUIView(this,
				contentComp, fieldsComposite);
	
	}

	private void createTwistieGroup() {
		// form composite:
		Composite contentComp = new Composite(this, SWT.NULL);
		this.setContent(contentComp);
		
		GridData contentCompLD = new GridData();
		contentCompLD.grabExcessHorizontalSpace = true;
		contentCompLD.horizontalAlignment = GridData.FILL;
		contentCompLD.horizontalSpan = 4;
		contentComp.setLayoutData(contentCompLD);

		GridLayout contentCompLayout = new GridLayout(4, true);
		contentCompLayout.marginWidth = 5;
		contentCompLayout.marginHeight = 5;
		contentCompLayout.numColumns = 4;
		contentCompLayout.makeColumnsEqualWidth = false;
		contentCompLayout.horizontalSpacing = 5;
		contentCompLayout.verticalSpacing = 5;
		contentComp.setLayout(contentCompLayout);
		
		// HUI composite:
		ExpandableComposite huiTwistie = new ExpandableComposite(
				contentComp, SWT.BORDER, ExpandableComposite.EXPANDED
						| ExpandableComposite.TWISTIE);
		huiTwistie.setText("");

		// set twistie to fill row:
		GridData huiTwistieLD = new GridData();
		huiTwistieLD.grabExcessHorizontalSpace = true;
		huiTwistieLD.horizontalAlignment = GridData.FILL;
		huiTwistieLD.horizontalSpan = 4;
		huiTwistie.setLayoutData(huiTwistieLD);

		huiTwistie.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanged(ExpansionEvent arg0) {
				huiView.resizeContainer();
			}

			public void expansionStateChanging(ExpansionEvent arg0) {
				// nothing
			}
		});

		Composite fieldsComposite = new Composite(huiTwistie, SWT.NULL);
		
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

		huiView = new HitroUIView(this,
				contentComp, fieldsComposite);
	}
	
	public void createView(Entity entity, boolean editable, boolean useRules) throws DBException {
		huiView.createView(entity, editable, useRules);
	}
	
	public void closeView() {
		huiView.closeView();
	}

}
