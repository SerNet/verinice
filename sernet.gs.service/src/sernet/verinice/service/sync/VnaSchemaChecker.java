/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels <bw@sernet.de>.
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
 *     Benjamin Weißenfels <bw@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.sync;

/**
 * Validates the verinice schema against the verinice instance which tries to
 * import the xml/vna verinice data.
 * 
 * Every verinice instance has knowledge about the xml/schema it supports. So
 * every validator has to check if the xml data includes a compatible version
 * number.
 * 
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public interface VnaSchemaChecker {

    /**
     * Checks if two vna schema versions are compatible to each other.
     * 
     */
    public boolean isCompatible(VnaSchemaVersion vnaSchemaVersion);
}
