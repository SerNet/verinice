/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.PersonEntityOptionWrapper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.taskcommands.FindURLs;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiUrl;
import sernet.hui.common.connect.IReferenceResolver;
import sernet.hui.common.connect.IUrlResolver;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;

/**
 * The HUI framework has no knowledge aout the database, so this factory
 * generates the necessary callback methods to get objects for it.
 * 
 * Basically this is needed to get objects referenced in propertytypes,
 * i.e. a list of Authors for the field "Authors" in the entity "Book".
 * 
 * It is also used to get previously entered URLs in the URL edit dialog, so 
 * the user can simply select them from a drop-down box.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class EntityResolverFactory {

	private static IReferenceResolver personResolver;
	private static IUrlResolver urlresolver;

	public static void createResolvers(HUITypeFactory typeFactory) {
		createPersonResolver();

		// set person resolver for all properties that reference persons:
		Collection<EntityType> allEntityTypes = typeFactory.getAllEntityTypes();
		for (EntityType entityType : allEntityTypes) {
			List<PropertyType> propertyTypes = entityType.getPropertyTypes();

			addPersonResolverToTypes(typeFactory, entityType, propertyTypes);
			
			List<PropertyGroup> groups = entityType.getPropertyGroups();
			for (PropertyGroup group : groups) {
				List<PropertyType> typesInGroup = group.getPropertyTypes();
				addPersonResolverToTypes(typeFactory, entityType, typesInGroup);
			}
		}
		
		createUrlResolver(typeFactory);
		
		// set url resolver for all URL fields:
		List<PropertyType> types = typeFactory.getURLPropertyTypes();
		for (PropertyType type : types) {
			type.setUrlResolver(urlresolver);
		}
	}


	private static void addPersonResolverToTypes(HUITypeFactory typeFactory,
			EntityType entityType, List<PropertyType> propertyTypes) {
		for (PropertyType propertyType : propertyTypes) {
			if (propertyType.isReference()) {
				if (propertyType.getReferencedEntityTypeId().equals(
						Person.TYPE_ID)) {
					typeFactory.getPropertyType(entityType.getId(),
							propertyType.getId()).setReferenceResolver(
							personResolver);
				}
			}
		}
	}

	
	private static void createUrlResolver(HUITypeFactory typeFactory) {
		// create list of all fields containing urls:
		final Set<String> allIDs = new HashSet<String>();
		try {
			List<PropertyType> types;
			types = typeFactory.getURLPropertyTypes();
			for (PropertyType type : types) {
				allIDs.add(type.getId());
			}
		} catch (Exception e) {
			return;
		}
		
		// get urls out of these fields:
		urlresolver = new IUrlResolver() {
			public List<HuiUrl> resolve() {
				List<HuiUrl> result = new ArrayList<HuiUrl>();
				
				try {
					FindURLs command = new FindURLs(allIDs);
					
						command = ServiceFactory.lookupCommandService().executeCommand(command);
						
						DocumentLinkRoot root = command.getUrls();
						
						DocumentLink[] links = root.getChildren();
						for (int i = 0; i < links.length; i++) {
							HuiUrl url = new HuiUrl(links[i].getName(), links[i].getHref());
							result.add(url);
						}
				} catch (Exception e) {
					ExceptionUtil.log(e, "Fehler beim Datenzugriff."); //$NON-NLS-1$
				}
				
				return result;
			}
		};
	}


	private static void createPersonResolver() {
		if (personResolver == null) {
			personResolver = new IReferenceResolver() {

				public List<IMLPropertyOption> getAllEntitesForType(
						String entityTypeID) {
					
					
					List<IMLPropertyOption> result = new ArrayList<IMLPropertyOption>();
					

					LoadCnAElementByType<Person> command = new LoadCnAElementByType<Person>(Person.class);
					
					try {
						command = ServiceFactory.lookupCommandService()
							.executeCommand(command);
						
						List<Person> personen = command.getElements();
						
						for (Person person : personen) {
							result.add(new PersonEntityOptionWrapper(person));
						}
						
					} catch (Exception e) {
						throw new RuntimeCommandException("Fehler beim Datenzugriff.", e);
					}
					return result;
				}
			};
		}
	}

}
