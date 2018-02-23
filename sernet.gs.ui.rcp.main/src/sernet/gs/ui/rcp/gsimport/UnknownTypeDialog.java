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
package sernet.gs.ui.rcp.gsimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import sernet.hui.common.connect.HUITypeFactory;

/**
 * Dialog is shown if unknown GSTOOL types are found during
 * GSTOOL-Import.
 *
 * Special confirm dialog with a button "Add types". Click on this button
 * add properties to {@link GstoolTypeMapper}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de> - add scrolledComposite for labels of unknown types
 */
public class UnknownTypeDialog extends Dialog {
    
    private boolean result = false;

    private Set<String> unknownTypes;
    
    private Shell dialogShell;
    
    public UnknownTypeDialog(Shell parentShell, Set<String> unknownTypes) {
        super(parentShell);
        this.unknownTypes = unknownTypes;
    }
    public boolean open () {
        Shell parent = getParent();
        dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        dialogShell.setLayout(new GridLayout(1, false));
        dialogShell.setText(Messages.GstoolTypeValidator_0);
        dialogShell = (Shell)createDialogArea(dialogShell);
        dialogShell.setSize(500,500);
        Display display = parent.getDisplay();
        dialogShell.open();
        while (!dialogShell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
        }
        return result;
    }
    
   
    public Control createDialogArea(Composite parent) {
        parent.setLayout(new GridLayout(1, true));
        
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, true));
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        final Composite introductionComposite = new Composite(container, SWT.NONE);
        introductionComposite.setLayout(new GridLayout(1, false));
        introductionComposite.setLayoutData(new GridData(SWT.HORIZONTAL, SWT.TOP, true, false));
        
        final Label introTextLabel = new Label(introductionComposite, SWT.WRAP);
        introTextLabel.setLayoutData(new GridData(SWT.HORIZONTAL, SWT.TOP, true, false));
        introTextLabel.setText(getIntroductionString());
        
        final Composite scrolledContainer = new Composite(container, SWT.NONE);
        scrolledContainer.setLayout(new GridLayout(1, true));
        scrolledContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        final ScrolledComposite scrolledComposite = 
                new ScrolledComposite(scrolledContainer, SWT.V_SCROLL);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setLayout(new GridLayout(1, false));
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        
        final Composite unknownTypesComposite = new Composite(scrolledComposite, SWT.NONE);
        unknownTypesComposite.setLayout(new GridLayout(1, false));
        unknownTypesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        List<String> list = new ArrayList<>(unknownTypes.size());
        list.addAll(unknownTypes);
        
        Collections.sort(list);
        
        for (int i = 0; i < list.size(); i++) {
            final Label unknownTypesLabel = new Label(unknownTypesComposite, SWT.NONE);
            unknownTypesLabel.setText(list.get(i));
        }

        scrolledComposite.setContent(unknownTypesComposite);
        scrolledComposite.setMinSize(200,200);
        scrolledComposite.setSize(250, 250);
        
        scrolledComposite.setVisible(true);
        scrolledComposite.layout();
        container.layout();
        unknownTypesComposite.layout();
        scrolledComposite.addControlListener(new ControlListener() {
            
            @Override
            public void controlResized(ControlEvent e) {
                scrolledComposite.setMinSize(
                        scrolledContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }
            
            @Override
            public void controlMoved(ControlEvent e) {
                // do nothing
            }
        });
        createButtonsForButtonBar(container);
        return parent;
    }

    private void addUnknownTypes() {
        for (String type : unknownTypes) {
            GstoolImportMappingElement mappingEntry = 
                    new GstoolImportMappingElement(type, 
                            GstoolImportMappingElement.UNKNOWN);
            GstoolTypeMapper.addGstoolSubtypeToPropertyFile(mappingEntry);
        }
        result = true;
    }


    protected void createButtonsForButtonBar(Composite parent) {
        Composite buttonComposite = new Composite(parent, SWT.BOTTOM | SWT.RIGHT);
        buttonComposite.setLayout(new GridLayout(3, false));
        buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));
        Button addTypeButton = new Button(buttonComposite, SWT.PUSH | SWT.RIGHT);
        addTypeButton.setText(Messages.UnknownTypeDialog_1);
        addTypeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                addUnknownTypes();
                cancelPressed();
            }
        });
        Button cancelButton = new Button(buttonComposite, SWT.PUSH | SWT.RIGHT);
        cancelButton.setText(Messages.UnknownTypeDialog_3);
        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                cancelPressed();
            }
        });
        Button okButton = new Button(buttonComposite, SWT.PUSH | SWT.RIGHT);
        okButton.setText(Messages.UnknownTypeDialog_0);
        okButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                result = true;
                dialogShell.close();
            }
        });
    }
    
    private void cancelPressed() {
        result = false;
        dialogShell.close();
    }
    
    private static String getIntroductionString() {
        final String defaultTypeTitle = HUITypeFactory.getInstance()
                .getMessage(GstoolTypeMapper.DEFAULT_TYPE_ID);
        return NLS.bind(Messages.GstoolTypeValidator_1, defaultTypeTitle);
    }

}