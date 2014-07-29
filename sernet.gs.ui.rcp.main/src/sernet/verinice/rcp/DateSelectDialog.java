/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.rcp;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DateSelectDialog extends Dialog {

    private Date date = null;
    
    public DateSelectDialog(Shell shell) {
        super(shell);
        int style = SWT.MAX | SWT.CLOSE | SWT.TITLE;
        style = style | SWT.BORDER | SWT.APPLICATION_MODAL;
        setShellStyle(style | SWT.RESIZE);       
    }
    
    /**
     * @param shell
     * @param oldDate
     */
    public DateSelectDialog(Shell shell, Date oldDate) {
        this(shell);
        date = oldDate;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.DateSelectDialog_0);
     }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final DateTime dateTime = new DateTime (container, SWT.CALENDAR);
        if(date!=null) {
            dateTime.setDay(date.getDate());
            dateTime.setMonth(date.getMonth());
            dateTime.setYear(date.getYear()+1900);
        }
        dateTime.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                  int day = dateTime.getDay();
                  int month = dateTime.getMonth();
                  int year = dateTime.getYear();
                  Calendar cal = Calendar.getInstance();
                  cal.set(year, month, day);
                  date = cal.getTime();
            }
        });
        container.pack();
        parent.pack();
        return container;
    }

    public Date getDate() {
        return date;
    }
}
