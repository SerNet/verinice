package sernet.gs.ui.rcp.main.bsi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.star.ucb.CommandFailedException;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.PersonEntityOptionWrapper;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementByType;
import sernet.gs.ui.rcp.main.service.taskcommands.FindURLs;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiUrl;
import sernet.hui.common.connect.IReferenceResolver;
import sernet.hui.common.connect.IUrlResolver;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IContextMenuListener;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.swt.widgets.IInputHelper;

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
		
		createUrlResolver();
		
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

	
	private static void createUrlResolver() {
		// create list of all fields containing urls:
		final Set<String> allIDs = new HashSet<String>();
		try {
			List<PropertyType> types;
			types = HUITypeFactory.getInstance().getURLPropertyTypes();
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
					ServiceFactory.lookupCommandService().executeCommand(command);
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
					LoadElementByType<Person> command = new LoadElementByType<Person>(Person.class);
					
					try {
						ServiceFactory.lookupCommandService().executeCommand(command);
						List<Person> personen = command.getElements();
						
						for (Person person : personen) {
							result.add(new PersonEntityOptionWrapper(person));
						}
						
					} catch (Exception e) {
						ExceptionUtil.log(e, "Fehler beim Datenzugriff."); //$NON-NLS-1$
					}
					return result;
				}
			};
		}
	}

}
