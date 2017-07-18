/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
package sernet.hui.common.connect;

/**
 * ResolverFactoryRegistry provides instances of 
 * IEntityResolverFactory. The instance which is returned 
 * must be set during initialization process.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public final class ResolverFactoryRegistry {

    private static IEntityResolverFactory resolverFactory;
    
    private ResolverFactoryRegistry() {
        super();
    }

    public static IEntityResolverFactory getResolverFactory() {
        return resolverFactory;
    }

    
    public static void setResolverFactory(IEntityResolverFactory resolverFactory) {
        ResolverFactoryRegistry.resolverFactory = resolverFactory;
    }
}
