/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.model;

/**
 * THis exception is thrown in case of validation errors
 * in the context of the object model.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ObjectModelValidationException extends Exception {

    /**
     * @param string
     */
    public ObjectModelValidationException(String message) {
        super(message);
    }

    /**
     * @param string
     * @param e
     */
    public ObjectModelValidationException(String message, Exception cause) {
        super(message, cause);
    }

}
