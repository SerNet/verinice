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

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard page of wizard {@link IndividualProcessWizard}.
 * User sets the dua date and the reminder period on this page.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DatePage extends WizardPage {

    private static final Logger LOG = Logger.getLogger(DatePage.class);
    
    public static final String NAME = "DATE_PAGE"; //$NON-NLS-1$

    public static final String ASSIGNEE_SELECTION_DIRECT = "ASSIGNEE_SELECTION_DIRECT"; //$NON-NLS-1$

    public static final String ASSIGNEE_SELECTION_RELATION = "ASSIGNEE_SELECTION_RELATION"; //$NON-NLS-1$

    private DateTime datePicker;

    private Calendar dueDate;
    
    private Combo priorityCombo;
    
    private Button[] radios = new Button[2];

    private String period;

    private String[] periodArray;

    private static final int MAX_PERIOD = 30;

    private static final int DEFAULT_PERIOD = 6;

    private Calendar now = Calendar.getInstance();

    private String assigneeSelectionMode = ASSIGNEE_SELECTION_DIRECT;

    private boolean isRelation = true;
    
    /**
     * @param elementTitle
     * @param pageName
     */
    protected DatePage() {
        super(NAME);
        setTitle(Messages.DatePage_3);
        setMessage(Messages.DatePage_4);
        setControl(datePicker);
        initPeriodArray();
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
            public void widgetSelected(SelectionEvent e) {
                dueDate = Calendar.getInstance();
                dueDate.set(Calendar.YEAR, datePicker.getYear());
                dueDate.set(Calendar.MONTH, datePicker.getMonth());
                dueDate.set(Calendar.DAY_OF_MONTH, datePicker.getDay());
                setPageComplete(isValid());
            }
        });

        final Label periodLabel = new Label(composite, SWT.NONE);
        periodLabel.setText(Messages.DatePage_6);
        priorityCombo = new Combo(composite, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        priorityCombo.setItems(periodArray);
        priorityCombo.select(DEFAULT_PERIOD);
        period = periodArray[DEFAULT_PERIOD];
        priorityCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                period = periodArray[priorityCombo.getSelectionIndex()];
                setPageComplete(isValid());
            }
        });

        if(isRelation) {
    
            radios[0] = new Button(composite, SWT.RADIO);
            radios[0].setSelection(true);
            radios[0].setText(Messages.DatePage_7);
            radios[0].addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    radios[1].setSelection(!radios[0].getSelection());
                    if (radios[0].getSelection()) {
                        assigneeSelectionMode = ASSIGNEE_SELECTION_DIRECT;
                    }
                }
    
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
    
            radios[1] = new Button(composite, SWT.RADIO);
            radios[1].setSelection(false);
            radios[1].setText(Messages.DatePage_8);
            radios[1].addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    radios[0].setSelection(!radios[1].getSelection());
                    if (radios[1].getSelection()) {
                        assigneeSelectionMode = ASSIGNEE_SELECTION_RELATION;
                    }
                }
    
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
            setRelationModeRadios();
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    @Override
    public IWizardPage getNextPage() {
        RelationPage relationPage = (RelationPage) getWizard().getPage(RelationPage.NAME);
        PersonPage personPage = (PersonPage) getWizard().getPage(PersonPage.NAME);
        if(ASSIGNEE_SELECTION_RELATION.equals(assigneeSelectionMode)) {
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
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
     * .Composite)
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
        boolean valid = true;
        setErrorMessage(null);
        if (valid && dueDate == null) {
            valid = false;
            setErrorMessage(Messages.DatePage_9);
        }
        if (valid && dueDate.before(now)) {
            valid = false;
            setErrorMessage(Messages.DatePage_10);
        }
        if (valid) {
            Calendar reminderDate = (dueDate != null) ? (Calendar) dueDate.clone() : null;
            if(reminderDate != null){
                reminderDate.add(Calendar.DATE, Integer.valueOf(period) * (-1));
                if (reminderDate.before(now)) {
                    valid = false;
                    setErrorMessage(Messages.DatePage_11);
                }
            }
        }
        return valid;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        boolean complete = super.isPageComplete();
        if (LOG.isDebugEnabled()) {
            LOG.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete;
    }

    private void initPeriodArray() {
        periodArray = new String[MAX_PERIOD];
        for (int i = 0; i < periodArray.length; i++) {
            periodArray[i] = String.valueOf(i + 1);
        }
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public String getPeriod() {
        return period;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
        datePicker.setYear(dueDate.get(Calendar.YEAR));
        datePicker.setMonth(dueDate.get(Calendar.MONTH));
        datePicker.setDay(dueDate.get(Calendar.DAY_OF_MONTH));
        setPageComplete(isValid());
    }

    public void setPeriod(String period) {
        this.period = period;
        for (int i = 0; i < periodArray.length; i++) {
            if(periodArray[i].equals(period)){
                priorityCombo.select(i);
                break;
            }
        }
        setPageComplete(isValid());   
    }

    public String getAssigneeSelectionMode() {
        return assigneeSelectionMode;
    }

    public void setAssigneeSelectionMode(String assigneeSelectionMode) {
        this.assigneeSelectionMode = assigneeSelectionMode;
        setRelationModeRadios();
    }

    /**
     * 
     */
    public void setRelationModeRadios() {
        if(radios[0]!=null && radios[1]!=null) {
            if(ASSIGNEE_SELECTION_DIRECT.equals(this.assigneeSelectionMode)) {
                radios[0].setSelection(true);
                radios[1].setSelection(false);
            }
            if(ASSIGNEE_SELECTION_RELATION.equals(this.assigneeSelectionMode)) {
                radios[1].setSelection(true);
                radios[0].setSelection(false);
            }
        }
    }

}
