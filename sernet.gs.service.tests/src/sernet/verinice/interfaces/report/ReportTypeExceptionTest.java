package sernet.verinice.interfaces.report;
/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/

import static org.junit.Assert.*;

import org.junit.Test;

import sernet.verinice.interfaces.report.ReportTypeException;

/**
 * Tests if cycle are detected when checking for {@link SecurityException} in
 * the cause chain.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class ReportTypeExceptionTest {

    @Test
    public void causeIsNull() {
        ReportTypeException reportTypeException = new ReportTypeException(null);
        assertFalse(reportTypeException.causedBySecurityException());
    }

    @Test
    public void stackTraceContainsCycle() {
        Exception exceptionOne = new CycleException();
        ReportTypeException exceptionTwo = new ReportTypeException(exceptionOne);
        assertFalse(exceptionTwo.causedBySecurityException());
    }

    @Test
    public void containsSecurityException() {
        SecurityException securityException = new SecurityException("forbidde");
        Exception somethingInBetween = new Exception(securityException);
        ReportTypeException exceptionTwo = new ReportTypeException(somethingInBetween);
        assertTrue(exceptionTwo.causedBySecurityException());
    }

    private class CycleException extends Exception {

        private static final long serialVersionUID = 1L;

        public synchronized Throwable getCause() {
            return this;
        }
    }

}
