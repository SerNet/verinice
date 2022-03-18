/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade <jk{a}sernet{dot}de>.
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
 ******************************************************************************/
package sernet.verinice.service.bp.exceptions;

/**
 * This exception is thrown when an error occurs in base protection referencing.
 */
public class BpReferencingException extends RuntimeException {

    private static final long serialVersionUID = -500792059797286483L;

    public BpReferencingException() {
        super("An error occurred in base protection referencing.");
    }

    public BpReferencingException(String message) {
        super(message);
    }

}
