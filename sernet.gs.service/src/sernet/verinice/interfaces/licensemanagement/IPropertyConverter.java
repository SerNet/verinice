/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.licensemanagement;

import java.util.Date;

import sernet.verinice.model.licensemanagement.ConversionNotPossibleException;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public interface IPropertyConverter {
    
    String convertToString(Object property) throws ConversionNotPossibleException;
    
    Integer convertToInteger(Object property) throws ConversionNotPossibleException;
    
    Date convertToDate(Object property) throws ConversionNotPossibleException;
    
    Long convertToLong(Object property) throws ConversionNotPossibleException;

}
