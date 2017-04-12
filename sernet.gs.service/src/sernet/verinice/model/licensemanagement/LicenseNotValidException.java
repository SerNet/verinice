/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
package sernet.verinice.model.licensemanagement;

/**
 * 
 * This exception will be thrown, if a user clicks an element that contain
 * license restricted content that is configured to be shown in the 
 * BrowserView and the user does not have a valid exception for that
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseNotValidException extends LicenseManagementException {

    private static final long serialVersionUID = 201702241109L;

    public LicenseNotValidException(String msg) {
        super(msg);
    }

    public LicenseNotValidException(String msg, Throwable t) {
        super(msg, t);
    }


}
