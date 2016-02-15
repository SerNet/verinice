/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.model.Gefaehrdung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahme;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;

/**
 * Basic class for the Dialogs of create and edit OwnGefaehrdung or
 * RisikoMassnahme(nUmsetzung)
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 * @param <T>
 */
@SuppressWarnings("restriction")
public abstract class RiskAnalysisDialog<T> extends Dialog {

    protected Text textNumber;
    protected Text textName;
    protected Text textDescription;
    protected Combo textCategory;
    private RiskAnalysisDialogItems<T> items;
    protected Color defaultBackground;
    private static final Logger LOG = Logger.getLogger(RiskAnalysisDialog.class);
    private static final int MAX_DESCRIPTION_LENGTH_FOR_GEFAEHRDUNG = 30000;

    protected RiskAnalysisDialog(Shell parentShell, RiskAnalysisDialogItems<T> items) {
        super(parentShell);
        this.items = items;

    }

    protected boolean isUniqueId(String id, Object element) {
        if (items.getGenericType().equals(Gefaehrdung.class) && element instanceof Gefaehrdung) {
            return isUniqueGefaehrdung(id, element);
        } else if (items.getGenericType().equals(MassnahmenUmsetzung.class)
                && element instanceof RisikoMassnahme) {
            return isUniqueRisikoMassnahme(id, element);
        } else if (items.getGenericType().equals(MassnahmenUmsetzung.class)
                && element instanceof RisikoMassnahmenUmsetzung) {
            return isUniqueRisikoMassnahmenUmsetzung(id, element);
        } else {
            LOG.error("the type of element or list is not supported",
                    new IllegalArgumentException());
            return false;
        }
    }

    private boolean isUniqueRisikoMassnahmenUmsetzung(String id, Object element) {
        /* EditRisikoMassnahmenUmsetzungDialog */
        RisikoMassnahmenUmsetzung massnahmenUmsetzung = (RisikoMassnahmenUmsetzung) element;
        for (T item : items) {
            MassnahmenUmsetzung currentMassnahmenUmsetzung = (MassnahmenUmsetzung) item;
            boolean isUsed = currentMassnahmenUmsetzung.getKapitel().equals(id)
                    && !massnahmenUmsetzung.getUuid().equals(currentMassnahmenUmsetzung.getUuid());
            if (isUsed) {
                return false;
            }
        }
        return true;

    }

    private boolean isUniqueRisikoMassnahme(String id, Object element) {
        /* NewRisikoMassnahmeDialog */
        /* NewGefaehrdungDialog and EditGefaehrdungDialog */
        RisikoMassnahme massnahme = (RisikoMassnahme) element;
        for (T item : items) {
            MassnahmenUmsetzung currentMassnahmenUmsetzung = (MassnahmenUmsetzung) item;
            boolean isUsed = currentMassnahmenUmsetzung.getKapitel().equals(id)
                    || currentMassnahmenUmsetzung.getId().equals(id) &&
                            (!(currentMassnahmenUmsetzung instanceof RisikoMassnahmenUmsetzung) ||
                                    !massnahme
                                            .equals(((RisikoMassnahmenUmsetzung) currentMassnahmenUmsetzung)
                                                    .getRisikoMassnahme()));
            if (isUsed) {
                return false;
            }
        }
        return true;
    }


    private boolean isUniqueGefaehrdung(String id, Object element) {
        /* NewGefaehrdungDialog and EditGefaehrdungDialog */
        Gefaehrdung gefaehrdung = (Gefaehrdung) element;
        for (T item : items) {
            Gefaehrdung currentGefaehrdung = (Gefaehrdung) item;
            if (currentGefaehrdung.getId().equals(id) && !gefaehrdung.equals(currentGefaehrdung)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void okPressed() {
        if (isUniqueId(textNumber.getText(), getItem())) {

            if (descriptionLengthOK()) {

                okPressedAndApproved();
                super.okPressed();
            } else {
                MessageDialog.openError(getShell(), Messages.RiskAnalysisDialog_Error_0,
                        NLS.bind(Messages.RiskAnalysisDialog_Error_1,
                                textDescription.getText().length(), getMaxDescriptionLength()));
            }
        } else {
            MessageDialog.openError(getShell(), Messages.NewGefaehrdungDialog_Error_0, NLS.bind(
                    Messages.NewGefaehrdungDialog_Error_1, textNumber.getText()));
        }
    }

    private boolean descriptionLengthOK() {

        return textDescription.getText().length() <= getMaxDescriptionLength();
            
    }

    private int getMaxDescriptionLength() {

        return MAX_DESCRIPTION_LENGTH_FOR_GEFAEHRDUNG;
    }


    protected abstract Object getItem();

    protected abstract void okPressedAndApproved();

    /**
     * Creates the content area of the Dialog.
     * 
     * @param parent
     *            the parent Composite
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        final int gridTextDescriptionWidthHint = 400;
        final int gridTextDescriptionHeightHint = 200;
        Composite composite = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);

        /* label number */
        final Label labelNumber = new Label(composite, SWT.NONE);
        GridData gridLabelNumber = new GridData();
        gridLabelNumber.horizontalAlignment = SWT.LEFT;
        gridLabelNumber.verticalAlignment = SWT.CENTER;
        labelNumber.setText(Messages.EditGefaehrdungDialog_0);
        labelNumber.setLayoutData(gridLabelNumber);

        /* text number */
        textNumber = new Text(composite, SWT.BORDER);
        GridData gridTextNumber = new GridData();
        gridTextNumber.horizontalAlignment = SWT.FILL;
        gridTextNumber.verticalAlignment = SWT.CENTER;
        gridTextNumber.grabExcessHorizontalSpace = true;
        textNumber.setLayoutData(gridTextNumber);

        /* label name */
        final Label labelName = new Label(composite, SWT.NONE);
        GridData gridLabelName = new GridData();
        gridLabelName.horizontalAlignment = SWT.LEFT;
        gridLabelName.verticalAlignment = SWT.CENTER;
        labelName.setText(Messages.EditGefaehrdungDialog_1);
        labelName.setLayoutData(gridLabelName);

        /* text name */
        textName = new Text(composite, SWT.BORDER);
        GridData gridTextName = new GridData();
        gridTextName.horizontalAlignment = SWT.FILL;
        gridTextName.verticalAlignment = SWT.CENTER;
        gridTextName.grabExcessHorizontalSpace = true;
        textName.setLayoutData(gridTextName);

        /* label description */
        final Label labelDescription = new Label(composite, SWT.NONE);
        GridData gridLabelDescription = new GridData();
        gridLabelDescription.horizontalAlignment = SWT.LEFT;
        gridLabelDescription.verticalAlignment = SWT.TOP;
        labelDescription.setText(Messages.EditGefaehrdungDialog_2);
        labelDescription.setLayoutData(gridLabelDescription);

        /* text description */
        textDescription = new Text(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
        GridData gridTextDescription = new GridData();
        gridTextDescription.horizontalAlignment = SWT.FILL;
        gridTextDescription.verticalAlignment = SWT.FILL;
        gridTextDescription.grabExcessHorizontalSpace = true;
        gridTextDescription.grabExcessVerticalSpace = true;
        gridTextDescription.widthHint = gridTextDescriptionWidthHint;
        gridTextDescription.heightHint = gridTextDescriptionHeightHint;
        textDescription.setLayoutData(gridTextDescription);
        defaultBackground = textDescription.getBackground();

        /* label category */
        final Label labelCategory = new Label(composite, SWT.NONE);
        GridData gridLabelCategory = new GridData();
        gridLabelCategory.horizontalAlignment = SWT.LEFT;
        gridLabelCategory.verticalAlignment = SWT.TOP;
        labelCategory.setText(Messages.EditGefaehrdungDialog_3);
        labelCategory.setLayoutData(gridLabelCategory);

        addCategory(composite);

        initContents();

        textDescription.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                final int red = 250;
                final int green = red;
                final int blue = 120;

                if (descriptionLengthOK()) {
                    textDescription.setBackground(defaultBackground);
                } else {
                    textDescription
                            .setBackground(new Color(Display.getCurrent(), red, green, blue));
                }
            }
        });

        return composite;
    }

    protected void addCategory(Composite parent) {
        /* text category */
        textCategory = new Combo(parent, SWT.DROP_DOWN);
        GridData gridTextCategory = new GridData();
        gridTextCategory.horizontalAlignment = SWT.FILL;
        gridTextCategory.verticalAlignment = SWT.CENTER;
        gridTextCategory.grabExcessHorizontalSpace = true;
        textCategory.setLayoutData(gridTextCategory);
        textCategory.setItems(loadCategories());

    }

    protected abstract String[] loadCategories();

    protected abstract void initContents();

}
