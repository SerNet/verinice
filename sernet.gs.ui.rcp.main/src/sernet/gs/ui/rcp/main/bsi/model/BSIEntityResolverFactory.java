/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dialogs.CnATreeElementSelectionDialog;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.PersonEntityOptionWrapper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.FastLoadCnAElementsByIds;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByEntityUuid;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.taskcommands.FindAllRoles;
import sernet.gs.ui.rcp.main.service.taskcommands.FindHuiUrls;
import sernet.gs.ui.rcp.main.service.taskcommands.FindRelationsFor;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiUrl;
import sernet.hui.common.connect.IEntityResolverFactory;
import sernet.hui.common.connect.IReferenceResolver;
import sernet.hui.common.connect.IUrlResolver;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.commands.SaveElement;

/**
 * The HUI framework has no knowledge aout the database, so this factory
 * generates the necessary callback methods to get objects for it.
 * 
 * Basically this is needed to get objects referenced in propertytypes, i.e. a
 * list of Authors for the field "Authors" in the entity "Book".
 * 
 * It is also used to get previously entered URLs in the URL edit dialog, so the
 * user can simply select them from a drop-down box.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class BSIEntityResolverFactory implements IEntityResolverFactory {

    private IReferenceResolver cnalinkresolver;

    private static final Logger LOG = Logger.getLogger(BSIEntityResolverFactory.class);
    private static final String STD_ERR_MSG = "Error while loading data";
    
    private static IReferenceResolver roleResolver;
    private static IReferenceResolver personResolver;
    private static IUrlResolver urlresolver;
    
    public void createResolvers(HUITypeFactory typeFactory) {
        createPersonResolver();
        createRoleResolver();

        // set person resolver for all properties that reference persons:
        Collection<EntityType> allEntityTypes = typeFactory.getAllEntityTypes();
        for (EntityType entityType : allEntityTypes) {
            List<PropertyType> propertyTypes = entityType.getPropertyTypes();

            addPersonResolverToTypes(typeFactory, entityType, propertyTypes);
            addRoleResolverToTypes(typeFactory, entityType, propertyTypes);

            List<PropertyGroup> groups = entityType.getPropertyGroups();
            for (PropertyGroup group : groups) {
                List<PropertyType> typesInGroup = group.getPropertyTypes();
                addPersonResolverToTypes(typeFactory, entityType, typesInGroup);
                addRoleResolverToTypes(typeFactory, entityType, typesInGroup);
            }
        }

        createUrlResolver(typeFactory);

        // set url resolver for all URL fields:
        List<PropertyType> types = typeFactory.getURLPropertyTypes();
        for (PropertyType type : types) {
            type.setUrlResolver(urlresolver);
        }
    }

    private void addPersonResolverToTypes(HUITypeFactory typeFactory, EntityType entityType, List<PropertyType> propertyTypes) {
        for (PropertyType propertyType : propertyTypes) {
            if (propertyType.isReference() &&
                    propertyType.getReferencedEntityTypeId().equals(Person.TYPE_ID)) {
                typeFactory.getPropertyType(entityType.getId(), propertyType.getId()).setReferenceResolver(personResolver);
            }
        }
    }

    private void addRoleResolverToTypes(HUITypeFactory typeFactory, EntityType entityType, List<PropertyType> propertyTypes) {
        for (PropertyType propertyType : propertyTypes) {
            if (propertyType.isReference() && 
                    propertyType.getReferencedEntityTypeId().equals(Configuration.ROLE_TYPE_ID)) {
                typeFactory.getPropertyType(entityType.getId(), propertyType.getId()).setReferenceResolver(roleResolver);
            }
        }
    }

    private void createUrlResolver(HUITypeFactory typeFactory) {
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
                List<HuiUrl> result = Collections.emptyList();

                try {
                    FindHuiUrls command = new FindHuiUrls(allIDs);

                    command = ServiceFactory.lookupCommandService().executeCommand(command);

                    result = command.getList();

                } catch (Exception e) {
                    LOG.error(STD_ERR_MSG, e); //$NON-NLS-1$
                }

                return result;
            }
        };
    }
    
    /**
     * Creates a resolver for all properties of type cnalink-reference.
     * Allows to create cnalinks to one or more objects according to a given relation id.
     * 
     * Note: Since the IReferenceResolver interface was originally designed for the "old" references, 
     * some of the methods do nothing in this resolver.
     * 
     */
    private void createCnaLinkReferenceResolver() {
        if (cnalinkresolver==null) {
            cnalinkresolver = new IReferenceResolver() {
                
                @Override
                public String getTitlesOfLinkedObjects(String referencedRelationId, String entityUuid) {
                    StringBuilder sb = new StringBuilder();
                    
                    CnATreeElement rootElmt;
                    try {
                        rootElmt = loadElementByEntityUuid(entityUuid);
                        FindRelationsFor cmd = new FindRelationsFor(rootElmt);
                        cmd = ServiceFactory.lookupCommandService().executeCommand(cmd);
                        rootElmt = cmd.getElmt();
                        
                        Set<CnALink> linksUp = rootElmt.getLinksUp();
                        for (Iterator iterator = linksUp.iterator(); iterator.hasNext();) {
                            CnALink cnALink = (CnALink) iterator.next();
                            if (cnALink.getRelationId().equals(referencedRelationId)) {
                                if (sb.length()>0) {
                                    sb.append(", ");
                                }
                                sb.append(CnALink.getRelationObjectTitle(rootElmt, cnALink));
                            }
                        }
                        
                        Set<CnALink> linksDown = rootElmt.getLinksDown();
                        for (Iterator iterator = linksDown.iterator(); iterator.hasNext();) {
                            CnALink cnALink = (CnALink) iterator.next();
                            if (cnALink.getRelationId().equals(referencedRelationId)) {
                                if (sb.length()>0) {
                                    sb.append(", ");
                                }
                                sb.append(CnALink.getRelationObjectTitle(rootElmt, cnALink));
                            }
                        }
                    } catch (CommandException e) {
                        ExceptionUtil.log(e, "");
                    }
                    
                    return sb.toString();
                }
                
                @Override
                public List<IMLPropertyOption> getReferencedEntitesForType(String referencedEntityTypeId, List<Property> references) {
                 // not implemented for this resolver
                    return new ArrayList<IMLPropertyOption>(0);
                }
                
                @Override
                public List<IMLPropertyOption> getAllEntitesForType(String referencedEntityTypeId) {
                    // not implemented for this resolver
                    return new ArrayList<IMLPropertyOption>(0);
                }
                
                @Override
                public void createLinks(String referencedEntityType, String linkType, String entityUuid) {
                    
                    
                    CnATreeElement inputElmt;
                    // find the inputElmt by UUID:
                    try {
                        inputElmt = loadElementByEntityUuid(entityUuid);
                        CnATreeElementSelectionDialog dialog = new CnATreeElementSelectionDialog(referencedEntityType, inputElmt);
                        
                        if (dialog.open() != Window.OK){
                            return;
                        }
                        List<CnATreeElement> linkTargets = dialog.getSelectedElements();
                        
                        // this method also fires events for added links:
                        CnAElementHome.getInstance().createLinksAccordingToBusinessLogic(inputElmt, linkTargets, linkType);
                    } catch (CommandException e) {
                        ExceptionUtil.log(e, "Error while creating links.");
                    }
                    
                                        
                }
                
                @Override
                public void addNewEntity(Entity parentEntity, String newName) {
                    // do nothing, not supported
                }
            };
        }
    }

    protected CnATreeElement loadElementByEntityUuid(String entityUuid) throws CommandException {
        LoadCnAElementByEntityUuid command = new LoadCnAElementByEntityUuid(entityUuid);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        return command.getElement();
    }
    

    private void createPersonResolver() {
        if (personResolver == null) {
            personResolver = new IReferenceResolver() {

                public List<IMLPropertyOption> getAllEntitesForType(String entityTypeID) {

                    List<IMLPropertyOption> result = new ArrayList<IMLPropertyOption>();

                    LoadCnAElementByType<Person> command = new LoadCnAElementByType<Person>(Person.class);

                    try {
                        command = ServiceFactory.lookupCommandService().executeCommand(command);

                        List<Person> personen = command.getElements();

                        for (Person person : personen) {
                            result.add(new PersonEntityOptionWrapper(person.getEntity()));
                        }

                    } catch (Exception e) {
                    	LOG.error("Error while loading element", e); //$NON-NLS-1$
                        throw new RuntimeCommandException(STD_ERR_MSG, e); //$NON-NLS-1$
                    }
                    return result;
                }

                public void addNewEntity(Entity parentEntity, String name) {
                    // not supported, do nothing
                }

                public List<IMLPropertyOption> getReferencedEntitesForType(String referencedEntityTypeId, List<Property> references) {

                    List<IMLPropertyOption> result = new ArrayList<IMLPropertyOption>();

                    List<Integer> dbIds = new ArrayList<Integer>();
                    for (Property prop : references) {
                        dbIds.add(Integer.parseInt(prop.getPropertyValue()));
                    }
                    FastLoadCnAElementsByIds command = new FastLoadCnAElementsByIds(dbIds);

                    try {
                        command = ServiceFactory.lookupCommandService().executeCommand(command);

                        List<Entity> personen = command.getFoundItems();

                        for (Entity person : personen) {
                            result.add(new PersonEntityOptionWrapper(person));
                        }

                    } catch (Exception e) {
                    	LOG.error("Error while loading elements", e); //$NON-NLS-1$
                        throw new RuntimeCommandException(STD_ERR_MSG, e); //$NON-NLS-1$
                    }
                    return result;

                }

                @Override
                public void createLinks(String referencedEntityType, String linkType, String entityUuid) {
                 // do nothing, not implemented for persons
                }

                @Override
                public String getTitlesOfLinkedObjects(String referencedCnaLinkType, String entityUuid) {
                    // not implemented
                    return null;
                }
            };
        }
    }

    private void createRoleResolver() {
        if (roleResolver == null) {
            roleResolver = new IReferenceResolver() {

                public List<IMLPropertyOption> getAllEntitesForType(String entityTypeID) {
                    List<IMLPropertyOption> result = new ArrayList<IMLPropertyOption>();

                    try {
                        FindAllRoles far = new FindAllRoles(false /*
                                                                   * filter out
                                                                   * user roles,
                                                                   * should not
                                                                   * be
                                                                   * selectable
                                                                   * by end user
                                                                   */);
                        far = ServiceFactory.lookupCommandService().executeCommand(far);

                        for (String role : far.getRoles()) {
                            // Empty roles may happen for non-initialized
                            // Configuration instances.
                            if (role.length() > 0) {
                                result.add(new PropertyOption(role, role));
                            }
                        }

                    } catch (Exception e) {
                    	LOG.error("Error while loading roles", e); //$NON-NLS-1$
                        throw new RuntimeCommandException(STD_ERR_MSG, e); //$NON-NLS-1$
                    }
                    return result;
                }

                public void addNewEntity(Entity parentEntity, String newName) {
                    try {
                        PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(Configuration.TYPE_ID, Configuration.PROP_ROLES);
                        parentEntity.createNewProperty(type, newName);

                        SaveElement<Entity> command = new SaveElement<Entity>(parentEntity);
                        ServiceFactory.lookupCommandService().executeCommand(command);
                    } catch (CommandException e) {
                    	LOG.error("Error while saving elements", e); //$NON-NLS-1$
                        throw new RuntimeCommandException(STD_ERR_MSG, e); //$NON-NLS-1$
                    }
                }

                public List<IMLPropertyOption> getReferencedEntitesForType(String referencedEntityTypeId, List<Property> references) {
                    return null;
                }


                @Override
                public String getTitlesOfLinkedObjects(String referencedCnaLinkType, String uuid) {
                    // not implemented, do nothing
                    return null;
                }

                @Override
                public void createLinks(String referencedEntityType, String linkType, String entityUuid) {
                    // not implemented, do nothing
                }
            };
        }

    }
}
