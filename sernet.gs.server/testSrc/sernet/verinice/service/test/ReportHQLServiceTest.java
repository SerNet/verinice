/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.IReportHQLService;

/**
 *
 */
@TransactionConfiguration(transactionManager="txManager", defaultRollback=false)
@Transactional
public class ReportHQLServiceTest extends ContextConfiguration{
    
    private static final Logger LOG = Logger.getLogger(ReportHQLServiceTest.class);
    
    private static final Set<String> validHQLQueries = new HashSet<String>();
    private static final Set<String> invalidHQLQueries = new HashSet<String>();
    
    static{
        // fill set of valid HQL queries
        validHQLQueries.add("select elmt.dbId from CnATreeElement elmt " +
                "where elmt.objectType = ? AND elmt.scopeId = ?");
        validHQLQueries.add("select distinct props.propertyValue from CnATreeElement elmt " +
                                "inner join elmt.entity as entity " + 
                                "inner join entity.typedPropertyLists as propertyList " + 
                                "inner join propertyList.properties as props " +
                                "where  elmt.dbId = ? AND props.propertyType = ?");
        validHQLQueries.add("from CnATreeElement elmt " +
                                "where elmt.objectType = ? AND elmt.scopeId = ?");
        validHQLQueries.add("from CnATreeElement elmt " + 
                                "inner join elmt.entity as entity " + 
                                "inner join entity.typedPropertyLists as propertyList " + 
                                "inner join propertyList.properties as props " + 
                                "where elmt.objectType = ? " +
                                "and elmt.scopeId = ? " + 
                                "and props.propertyType = ? " + 
                                "and props.propertyValue like ? ");
        validHQLQueries.add(" from CnATreeElement elmt " + 
                                "left join fetch elmt.entity as entity " + 
                                "left join fetch entity.typedPropertyLists as propertyList " + 
                                "left join fetch propertyList.properties as props " + 
                                "where elmt.dbId IN ( " +
                                "select link.id.dependencyId from CnALink link where link.id.dependantId = ? " +  
                                ") and elmt.scopeId = ? and elmt.objectType = ?");
        validHQLQueries.add("select link.id.typeId from CnALink link" +
                " where link.id.dependencyId = ? " +
                " and link.id.dependantId = ?");
        validHQLQueries.add("select distinct elem from CnATreeElement elem"
                + " inner join fetch elem.parent as parent"
                + " inner join fetch elem.parent.parent as grandParent"
                + " inner join fetch elem.parent.parent.parent as greatGrandParent"

                + " inner join fetch elem.entity as entity"
                + " left outer join fetch entity.typedPropertyLists as propertyList"
                + " left outer join fetch propertyList.properties as props"

                + " inner join fetch elem.parent.entity as parentEntity"
                + " left outer join fetch parentEntity.typedPropertyLists as parentPropertyList"
                + " left outer join fetch parentPropertyList.properties"

                + " inner join fetch grandParent.entity as grandParentEntity"
                + " left outer join fetch grandParentEntity.typedPropertyLists as grandParentPropertyList"
                + " left outer join fetch grandParentPropertyList.properties"

                + " inner join fetch greatGrandParent.entity as greatGrandParentEntity"
                + " left outer join fetch greatGrandParentEntity.typedPropertyLists as greatGrandParentPropertyList"
                + " left outer join fetch greatGrandParentPropertyList.properties"

                + " where entity.entityType='mnums'"
                + " and elem.scopeId = ?");
        validHQLQueries.add("from CnATreeElement elmt " + 
                "inner join elmt.entity as entity " + 
                "inner join entity.typedPropertyLists as propertyList " + 
                "inner join propertyList.properties as props " + 
                "where elmt.objectType = ? " +
                "and elmt.scopeId = ? " + 
                "and props.propertyType = ? " + 
                "and props.propertyValue = ? ");
        validHQLQueries.add("from CnATreeElement elmt " + 
                    "inner join elmt.entity as entity " + 
                    "inner join entity.typedPropertyLists as propertyList " + 
                    "inner join propertyList.properties as props " + 
                    "where elmt.dbId = ? ");
        
        validHQLQueries.add("select distinct props.propertyValue from CnATreeElement elmt " +
                "inner join elmt.entity as entity " + 
                "inner join entity.typedPropertyLists as propertyList " + 
                "inner join propertyList.properties as props " +
        "where  elmt.dbId = ? AND props.propertyType = ?");
        
        validHQLQueries.add(" from CnATreeElement elmt " + 
        "inner join elmt.entity as entity " + 
        "inner join entity.typedPropertyLists as propertyList " + 
            "inner join propertyList.properties as props " + 
        "where elmt.dbId IN ( " +
        "select link.id.dependantId from CnALink link where link.id.dependencyId IN ( " +
        "select  elmt2.dbId FROM CnATreeElement elmt2 " +
        "where elmt2.scopeId = ? and  elmt2.objectType = ?) " +
        "and link.id.dependencyId = ? " +  
        ")");
        
        validHQLQueries.add(" from CnATreeElement elmt " + 
                "left join fetch elmt.entity as entity " + 
                "left join fetch entity.typedPropertyLists as propertyList " + 
                    "left join fetch propertyList.properties as props " + 
                "where elmt.dbId IN ( " +
                "select link.id.dependencyId from CnALink link where link.id.dependantId = ? " +  
                ") and elmt.scopeId = ? and elmt.objectType = ?");

        // fill set of invalid HQL queries
        invalidHQLQueries.add("from Property prop");
        invalidHQLQueries.add(" from Property prop " + 
        "where prop.propertyType IN (:types) ");
        invalidHQLQueries.add(" from CnATreeElement elmt, Property prop ");
        
    }
    
    @Resource(name="reportHQLService")
    private IReportHQLService hqlService;
    
    @Test
    public void testValidHQLQueries(){
        for(String query : validHQLQueries){
            assertTrue("failed Query, query should pass the validator:\n\"" + query + "\"", hqlService.isQueryAllowed(query));
        }
        
    }
    
    @Test
    public void testInvalidHQLQueries(){
        for(String query : invalidHQLQueries){
            assertTrue("failed Query, query should not pass the validator:\n\"" + query + "\"", !(hqlService.isQueryAllowed(query)));
        }
    }
    
    @Test
    public void areQuerysValid(){
        List<String> queries = new ArrayList<String>();
        queries.addAll(invalidHQLQueries);
        queries.addAll(validHQLQueries);
        for(String query : queries){
            assertTrue("query seems not to be well-formed hql:\n\"" + query + "\"", hqlService.isValidQuery(query));
        }
    }
    
    @Test
    public void testNonHQLStrings(){
        List<String> queries = new ArrayList<String>();
        queries.add("this is not a hql query");
        for(String query : queries){
            assertTrue("query passes the parser, but it should not:\n\"" + query + "\"", !(hqlService.isValidQuery(query)) );
        }
    }
    

}
