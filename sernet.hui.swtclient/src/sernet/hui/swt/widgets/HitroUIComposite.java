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
package sernet.hui.swt.widgets;

import java.util.Arrays;

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
		super(parent, SWT.V_SCROLL);
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
		contentCompLayout.marginWidth = 3;
		contentCompLayout.marginHeight = 3;
		contentCompLayout.numColumns = 4;
		contentCompLayout.makeColumnsEqualWidth = false;
		contentCompLayout.horizontalSpacing = 3;
		contentCompLayout.verticalSpacing = 3;
		contentComp.setLayout(contentCompLayout);
		
		// HUI composite:
		ExpandableComposite huiTwistie = new ExpandableComposite(contentComp, ExpandableComposite.NO_TITLE, ExpandableComposite.EXPANDED);
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
		fieldsCompLayout.verticalSpacing = 4;
		fieldsCompLayout.marginWidth = 2;
		fieldsCompLayout.marginHeight = 2;
		fieldsComposite.setLayout(fieldsCompLayout);

		// set comp to fill twistie:
		GridData fieldsCompLD = new GridData(GridData.FILL_BOTH);
		fieldsComposite.setLayoutData(fieldsCompLD);

		huiTwistie.setClient(fieldsComposite);
		huiTwistie.setExpanded(true);

		huiView = new HitroUIView(this,contentComp, fieldsComposite);
	
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

		huiView = new HitroUIView(this,contentComp, fieldsComposite);
	}
	
	public void createView(Entity entity, boolean editable, boolean useRules, String[] tags, boolean taggedOnly) throws DBException {
		huiView.createView(entity, editable, useRules, tags, taggedOnly);
	}
	
	public void createView(Entity entity, boolean editable, boolean useRules, String tags, boolean taggedOnly) throws DBException {
	    tags.replaceAll("\\s+", "");
        String[] individualTags = tags.split(",");
        huiView.createView(entity, editable, useRules, individualTags, taggedOnly);
    }
	
	public void closeView() {
		huiView.closeView();
	}

}
