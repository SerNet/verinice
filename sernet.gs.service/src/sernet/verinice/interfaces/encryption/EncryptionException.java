/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.encryption;

/**
 * Runtime exception that can be thrown when something went wrong during the en-
 * or decryption process.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * 
 */
public class EncryptionException extends RuntimeException {

	private static final long serialVersionUID = 7347723539184218433L;

	public EncryptionException() {
		super();
	}

	public EncryptionException(String message) {
		super(message);
	}

	public EncryptionException(Throwable cause) {
		super(cause);
	}
	
	public EncryptionException(String message, Throwable cause) {
		super(message, cause);
	}
}
