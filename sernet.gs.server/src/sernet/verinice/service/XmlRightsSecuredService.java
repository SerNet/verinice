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
package sernet.verinice.service;

/**
 * Provides a rights management in verinice based on a xml configuration.
 *
 * This class is exposed via spring remote to the client. That's why we cannot
 * trust that the client behaves nicely and does not arbitrary override the
 * configuration without legitimation. For that the
 * {@link XmlRightsService#updateConfiguration(sernet.verinice.model.auth.Auth)}
 * method is secured via spring security.
 *
 * The server side {@link RightsServerHandler} uses the unsecured
 * {@link XmlRightsService}.
 *
 *
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public class XmlRightsSecuredService extends XmlRightsService {

}
