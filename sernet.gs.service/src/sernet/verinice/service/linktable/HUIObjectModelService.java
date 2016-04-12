/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable;

import java.util.*;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class HUIObjectModelService implements IObjectModelService {

    private static HUIObjectModelService instance = null;
    private static HUITypeFactory huiTypeFactory = null;

    private HUIObjectModelService() {
        ServerInitializer.inheritVeriniceContextState();
        huiTypeFactory = HUITypeFactory.getInstance();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.linktable.ILinkTableContentService#
     * getRelationsFrom(java.lang.String)
     */
    @Override
    public Set<String> getRelations(String fromEntityTypeID, String toEntityTypeID) {
        if (huiTypeFactory.getEntityType(fromEntityTypeID) == null
                || huiTypeFactory.getEntityType(toEntityTypeID) == null) {
            return new HashSet<>();
        }
        Set<HuiRelation> relations = huiTypeFactory.getPossibleRelations(fromEntityTypeID,
                toEntityTypeID);
        relations.addAll(huiTypeFactory.getPossibleRelations(toEntityTypeID,
                fromEntityTypeID));
        HashSet<String> relationIds = new HashSet<>();
        for (HuiRelation huiRelation : relations) {

            relationIds.add(huiRelation.getId());
        }

        return relationIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.linktable.ILinkTableContentService#
     * getPossibleRelationPartners(java.lang.String)
     */
    @Override
    public Set<String> getPossibleRelationPartners(String typeID) {
        if (huiTypeFactory.getEntityType(typeID) == null) {
            return new HashSet<>();
        }
        HashSet<String> possiblePartners = new HashSet<>();

        Set<HuiRelation> relations = huiTypeFactory.getPossibleRelationsFrom(typeID);
        for (HuiRelation relation : relations) {
            possiblePartners.add(relation.getTo());
        }
        relations = huiTypeFactory.getPossibleRelationsTo(typeID);
        for (HuiRelation relation : relations) {
            possiblePartners.add(relation.getFrom());
        }
        return possiblePartners;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.ILinkTableContentService#getTypeIDs()
     */
    @Override
    public Set<String> getAllTypeIDs() {
        return huiTypeFactory.getAllTypeIds();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.linktable.ILinkTableContentService#
     * getPossibleProperties(java.lang.String)
     */
    @Override
    public Set<String> getPossibleProperties(String typeID) {
        if (huiTypeFactory.getEntityType(typeID) == null) {
            return new HashSet<>();
        }
        return new HashSet<>(
                Arrays.asList(huiTypeFactory.getEntityType(typeID).getAllPropertyTypeIds()));
    }

    public static HUIObjectModelService getInstance() {
        if (instance == null) {
            instance = new HUIObjectModelService();
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.ILinkTableContentService#getLabel(java.
     * lang.String)
     */
    @Override
    public String getLabel(String id) {
        return huiTypeFactory.getMessage(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.ILinkTableContentService#getLabel(java.
     * lang.String)
     */
    @Override
    public String getRelationLabel(String id) {
        return huiTypeFactory.getMessage(id + "_name");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.IObjectModelService#getPossibleChildren
     * (java.lang.String)
     */
    @Override
    public Set<String> getPossibleChildren(String typeID) {
        // TODO rmotza adapt
        HashMap<String, HashSet<String>> dummytypes = new HashMap<>();
        
        String[] children = new String[] { "raeumekategorie", "serverkategorie",
                "tkkategorie",
                "netzkategorie",
                "clientskategorie", "sonstitkategorie", "personkategorie", "gebaeudekategorie",
                "anwendungenkategorie"
        };
        dummytypes.put("itverbund", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "raum" };
        dummytypes.put("raeumekategorie", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "server" };
        dummytypes.put("serverkategorie", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "tkkomponente" };
        dummytypes.put(
                "tkkategorie", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "netzkomponente" };
        dummytypes.put(
                "netzkategorie", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "client" };
        dummytypes.put(
                "clientskategorie", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "sonstit" };
        dummytypes.put("sonstitkategorie", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "person" };
        dummytypes.put("personkategorie", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "gebaeude" };
        dummytypes.put("gebaeudekategorie", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "anwendung" };
        dummytypes.put(
                "anwendungenkategorie", new HashSet<>(Arrays.asList(children)));
        if (dummytypes.get(typeID) != null) 
            return dummytypes.get(typeID);
        
        return new HashSet<>();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.IObjectModelService#getPossibleParents(
     * java.lang.String)
     */
    @Override
    public Set<String> getPossibleParents(String typeID) {
        // TODO rmotza adapt
        HashMap<String, HashSet<String>> dummytypes = new HashMap<>();

        dummytypes.put("itverbund", new HashSet<String>());
        String[] children = new String[] { "raeumekategorie" };
        dummytypes.put("raum", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "serverkategorie" };
        dummytypes.put("server", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "tkkategorie" };
        dummytypes.put("tkkomponente", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "netzkategorie" };
        dummytypes.put("netzkomponente", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "clientskategorie" };
        dummytypes.put("client", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "sonstitkategorie" };
        dummytypes.put("sonstit", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "personkategorie" };
        dummytypes.put("person", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "gebaeudekategorie" };
        dummytypes.put("gebaeude", new HashSet<>(Arrays.asList(children)));
        children = new String[] { "anwendungenkategorie" };
        dummytypes.put("anwendung", new HashSet<>(Arrays.asList(children)));
        if (dummytypes.get(typeID) != null)
            return dummytypes.get(typeID);

        return new HashSet<>();

    }
    

}
