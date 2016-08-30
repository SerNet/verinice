/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
package sernet.verinice.service.linktable;

import java.util.List;

/**
 * <p>
 * Service to create Link Tables. Link Tables are used as a data source in BIRT
 * reports or to export CSV data.
 *
 * This service simplifies the user request to implement a table that displays a
 * table over all elements of type $a, and all to that element linked elements
 * of type $b. It returns a {@link List<List<String>}, so that a standard BIRT
 * report table could be filled with that data. The configuration for table
 * generation can be created programmatically or loaded from a VLT file.
 * A VLT (verinice link table) file is a JSON file with suffix '.vlt'. See JSON
 * schema VltSchema.json in package sernet.verinice.report.service.impl.dynamictable.vlt.
 * </p>
 *
 * Usage:
 *
 * <pre>
 * ============================================================================================
 * LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
 * builder.addScopeId(org.getScopeId())
 * .addColumnPath("asset/asset_name")
 * .addColumnPath("asset:person_iso")
 * .addColumnPath("asset/person_iso.person_iso_name");
 * .addLinkTypeId("rel_asset_person_respo");
 * List<List<String>> resultTable = service.createTable(builder.build());
 * ============================================================================================
 * </pre>
 *
 * Syntax for column path is described with the verinice query language (VQL):
 *
 * <pre>
 * VQL : typeName (linkedType | parentType | childType)* (linkType | property) (alias)?;
 *
 * linkedType : LINK typeName ;
 * parentType : PARENT typeName ;
 * childType  : CHILD typeName ;
 * linkType   : LT linkTypeName ;
 * property   : PROP propertyName ;
 * alias      : AS aliasName;
 *
 * LINK   : '/'  ; (Separates two entity types that are linked to each other.)
 * CHILD  : '>'  ; (Separates two entity types that are in a parent>child relation.)
 * PARENT : '<'  ; (Separates two entity types that are in a child<parent relation.)
 * PROP   : '.'  ; (Separates an entity type from a property type of the entity.)
 * LT     : ':'  ; (Separates two entity types that are linked to each other. Outputs the type of the link.)
 * AS     : "AS" ; (Keyword to set an alias or name for a path)
 *
 * typeName :     Alphanumeric ;
 * linkTypeName : Alphanumeric ;
 * propertyName : Alphanumeric ;
 * Alphanumeric : ('_' | '-' | '0'..'9' | 'A'..'Z' | 'a'..'z')+  ;
 * </pre>
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ILinkTableService {

    /**
     * Creates a Link Table with the configuration defined in
     * a configuration.
     *
     * @param configuration A Link Table configuration
     * @return A Link Table
     */
    List<List<String>> createTable(ILinkTableConfiguration configuration);

    /**
     * Creates a Link Table with the configuration defined in VLT file
     * with path vltFilePath.
     *
     * A VLT (verinice link table) file is a JSON file with suffix '.vlt'.
     * See JSON schema VltSchema.json in package
     * sernet.verinice.report.service.impl.dynamictable.vlt.
     *
     * @param vltFilePath The full path to a VLT file
     * @return A Link Table
     */
    List<List<String>> createTable(String vltFilePath);
    
    /**
     * Sets strategy for calculating the linked table.
     * 
     * Must be called before {@link #createTable()};
     */
    void setLinkTableCreator(LinkedTableCreator linkedTableCreator);

}