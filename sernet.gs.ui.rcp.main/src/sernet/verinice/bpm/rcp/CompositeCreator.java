/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * CompositeCreator contains static methods to create composite.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class CompositeCreator {

    public static final int WIDTH_4 = 4;
    
    private CompositeCreator() {
        // Use static methods
        // Don't create instances of this class
    }
    
    public static Composite create1ColumnComposite(Composite composite) {
        return create1ColumnComposite(composite, true, true);
    }
    
    public static Composite create1ColumnComposite(Composite composite, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace ) {
        Composite comboComposite = new Composite(composite, SWT.NONE);
        int horizontalAlignment = (grabExcessHorizontalSpace) ? SWT.FILL : SWT.LEFT;
        int verticalAlignment = (grabExcessVerticalSpace) ? SWT.FILL : SWT.LEFT;
        GridData gridData = new GridData(horizontalAlignment, verticalAlignment, grabExcessHorizontalSpace, grabExcessVerticalSpace);
        comboComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        comboComposite.setLayout(gridLayout);
        return comboComposite;
    }
    
    public static Composite create2ColumnComposite(Composite composite) {
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        return create2ColumnComposite(composite, gridData);
    }
    
    public static Composite create2ColumnComposite(Composite composite, GridData gridData) {
        Composite comboComposite = new Composite(composite, SWT.NONE);
        comboComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        comboComposite.setLayout(gridLayout);
        return comboComposite;
    }
    
    public static Composite create6ColumnComposite(Composite parentComposite) {
        Composite composite = new Composite(parentComposite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        composite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(6, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        composite.setLayout(gridLayout);
        return composite;
    }
    
    public static SashForm createSplitComposite(Composite parent, int orientation) {
        SashForm container = new SashForm(parent, orientation);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        container.setLayoutData(gridData);
        container.setSashWidth(WIDTH_4);
        container.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GRAY));
        return container;
    }
    
    public static ScrolledComposite createScrolledComposite(Composite parent) {
        ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL );
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        return scrolledComposite;
    }
}
