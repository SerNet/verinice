/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard page of wizard {@link IndividualProcessWizard}. User sets the due date
 * and the reminder period on this page.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DatePage extends WizardPage {

    private static final Logger logger = Logger.getLogger(DatePage.class);

    public static final String NAME = "DATE_PAGE"; //$NON-NLS-1$

    public static final String ASSIGNEE_SELECTION_DIRECT = "ASSIGNEE_SELECTION_DIRECT"; //$NON-NLS-1$

    public static final String ASSIGNEE_SELECTION_RELATION = "ASSIGNEE_SELECTION_RELATION"; //$NON-NLS-1$

    private DateTime datePicker;

    private LocalDate dueDate;

    private Combo periodCombo;

    private Button[] radios = new Button[2];

    private Integer periodDays;

    private static final int MAX_PERIOD = 30;

    private static final int DEFAULT_PERIOD = 7;

    private String assigneeSelectionMode = ASSIGNEE_SELECTION_DIRECT;

    private boolean isRelation = true;

    protected DatePage() {
        super(NAME);
        setTitle(Messages.DatePage_3);
        setMessage(Messages.DatePage_4);
        setControl(datePicker);
    }

    public DatePage(boolean relation) {
        this();
        this.isRelation = relation;
    }

    private void addFormElements(Composite composite) {

        final Label dueDateLabel = new Label(composite, SWT.NONE);
        dueDateLabel.setText(Messages.DatePage_5);
        datePicker = new DateTime(composite, SWT.CALENDAR);
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        datePicker.setLayoutData(gd);
        datePicker.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                LocalDate newDueDate = LocalDate.of(datePicker.getYear(), datePicker.getMonth() + 1,
                        datePicker.getDay());
                if (newDueDate.isAfter(LocalDate.now())) {
                    dueDate = newDueDate;
                    int diff = (int) ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
                    int newMaxPeriod = Math.min(diff, MAX_PERIOD);
                    if (newMaxPeriod != periodCombo.getItemCount()) {
                        periodCombo.setItems(createPeriodArray(newMaxPeriod));
                        periodCombo.pack();
                    }
                    int newPeriodDays = Math.min(newMaxPeriod, periodDays);
                    setPeriod(newPeriodDays);
                } else {
                    datePicker.setDate(dueDate.getYear(), dueDate.getMonthValue() - 1,
                            dueDate.getDayOfMonth());
                }

                setPageComplete(isValid());
            }
        });

        final Label periodLabel = new Label(composite, SWT.NONE);
        periodLabel.setText(Messages.DatePage_6);

        periodCombo = new Combo(composite,
                SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        String[] selectablePeriods = createPeriodArray(DEFAULT_PERIOD);
        periodCombo.setItems(selectablePeriods);
        int defaultPeriodIndex = DEFAULT_PERIOD - 1;
        periodDays = DEFAULT_PERIOD;
        periodCombo.select(defaultPeriodIndex);
        periodCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                periodDays = periodCombo.getSelectionIndex() + 1;
                setPageComplete(isValid());
            }
        });

        dueDate = LocalDate.now().plusDays(periodDays);
        datePicker.setDate(dueDate.getYear(), dueDate.getMonthValue() - 1, dueDate.getDayOfMonth());

        if (isRelation) {

            radios[0] = new Button(composite, SWT.RADIO);
            radios[0].setText(Messages.DatePage_7);
            radios[0].setSelection(true);
            radios[0].addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    radios[1].setSelection(!radios[0].getSelection());
                    if (radios[0].getSelection()) {
                        assigneeSelectionMode = ASSIGNEE_SELECTION_DIRECT;
                    }
                }

            });

            radios[1] = new Button(composite, SWT.RADIO);
            radios[1].setText(Messages.DatePage_8);
            radios[1].setSelection(false);
            radios[1].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    radios[0].setSelection(!radios[1].getSelection());
                    if (radios[1].getSelection()) {
                        assigneeSelectionMode = ASSIGNEE_SELECTION_RELATION;
                    }
                }

            });
            setRelationModeRadios();
        }
    }

    /*
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    @Override
    public IWizardPage getNextPage() {
        RelationPage relationPage = (RelationPage) getWizard().getPage(RelationPage.NAME);
        PersonPage personPage = (PersonPage) getWizard().getPage(PersonPage.NAME);
        if (ASSIGNEE_SELECTION_RELATION.equals(assigneeSelectionMode)) {
            personPage.setActive(false);
            relationPage.setActive(true);
            return relationPage;
        } else {
            relationPage.setActive(false);
            personPage.setActive(true);
            return personPage;
        }
    }

    /*
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.
     * widgets .Composite)
     */
    @Override
    public void createControl(Composite parent) {
        final int defaultMarginWidthHeight = 10;
        final Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = defaultMarginWidthHeight;
        composite.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gd);

        addFormElements(composite);

        composite.pack();

        // Required to avoid an error in the system
        setControl(composite);
        setPageComplete(false);
    }

    public boolean isValid() {
        setErrorMessage(null);
        if (dueDate == null) {
            setErrorMessage(Messages.DatePage_9);
            return false;
        }
        if (dueDate.isBefore(LocalDate.now())) {
            setErrorMessage(Messages.DatePage_10);
            return false;
        }

        LocalDate reminderDate = dueDate.minusDays(periodDays);
        if (reminderDate.isBefore(LocalDate.now())) {
            setErrorMessage(Messages.DatePage_11);
            return false;
        }
        return true;
    }

    /*
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        boolean complete = super.isPageComplete();
        if (logger.isDebugEnabled()) {
            logger.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete;
    }

    private static String[] createPeriodArray(int maxPeriod) {
        String[] periodArray = new String[maxPeriod];
        for (int i = 0; i < periodArray.length; i++) {
            periodArray[i] = String.valueOf(i + 1);
        }
        return periodArray;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Integer getPeriod() {
        return periodDays;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        datePicker.setDate(dueDate.getYear(), dueDate.getMonthValue() - 1, dueDate.getDayOfMonth());
        setPageComplete(isValid());
    }

    public void setPeriod(Integer period) {
        this.periodDays = period;
        int selectionIndex = period - 1;
        periodCombo.select(selectionIndex);
        setPageComplete(isValid());
    }

    public String getAssigneeSelectionMode() {
        return assigneeSelectionMode;
    }

    public void setAssigneeSelectionMode(String assigneeSelectionMode) {
        this.assigneeSelectionMode = assigneeSelectionMode;
        setRelationModeRadios();
    }

    public void setRelationModeRadios() {
        if (radios[0] != null && radios[1] != null) {
            if (ASSIGNEE_SELECTION_DIRECT.equals(this.assigneeSelectionMode)) {
                radios[0].setSelection(true);
                radios[1].setSelection(false);
            } else if (ASSIGNEE_SELECTION_RELATION.equals(this.assigneeSelectionMode)) {
                radios[1].setSelection(true);
                radios[0].setSelection(false);
            }
        }
    }

}
