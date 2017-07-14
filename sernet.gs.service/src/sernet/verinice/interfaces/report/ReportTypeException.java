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
package sernet.verinice.interfaces.report;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Wraps any kind of {@link Throwable}, which occurs during report creation.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ReportTypeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReportTypeException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Detects {@link SecurityException} in the cause chain.
     *
     * @return True if a {@link SecurityException} is found.
     */
    public boolean causedBySecurityException() {

        if (getCause() == null) {
            return false;
        }

        return causedBySecurityException(getCause());
    }

    private boolean causedBySecurityException(Throwable cause) {

        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        Throwable current = cause;
        while (!visited.contains(current)) {
            visited.add(current);
            if (current instanceof SecurityException) {
                return true;
            } else if (current.getCause() != null) {
                current = current.getCause();
            } else {
                break;
            }
        }

        return false;
    }
}
