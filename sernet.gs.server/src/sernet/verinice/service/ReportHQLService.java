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
package sernet.verinice.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.hql.antlr.HqlTokenTypes;
import org.hibernate.hql.ast.HqlParser;
import org.hibernate.hql.ast.util.ASTPrinter;
import org.hibernate.hql.ast.util.ASTUtil;

import sernet.verinice.interfaces.IReportHQLService;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

/**
 *
 */
public class ReportHQLService implements IReportHQLService {
    
    private static final Logger LOG = Logger.getLogger(ReportHQLService.class);
    
    /**
     * analyzes the given hql on its content, 
     * decides if the query fulfills the verinice policies
     */
    @Override
    public boolean isQueryAllowed(String qry) {
        HqlParser parser = HqlParser.getInstance(qry);
        try {
            parser.statement();

            AST ast = parser.getAST();

            if(isQueryRoot(ast)){
                AST queryStart = ast.getFirstChild();
                if(isAllowedStatementBeginning(queryStart)){
                    List<AST> selectStatements = getAllSelectStatements(ast);
                    if(!validateSelectStatements(qry, ast, selectStatements)){
                        return false; // query violates security policies
                    }
                    
                } else {
                    return false; // query does not start with a valid statement
                }
            } else {
                return false; // query is not formed well
            }
        } catch (RecognitionException e) {
            LOG.error("Error parsing the hql-qry", e);
            return false;
        } catch (TokenStreamException e) {
            LOG.error("Error parsing the hql-qry", e);
            return false;
        }
        return true;
    }

    /**
     * validates a given list of select-statements 
     * which can be one of the following:
     * SELECT, SELECT FROM, FROM
     * @param qry
     * @param ast
     * @param selectStatements
     * @return
     */
    private boolean validateSelectStatements(String qry, AST ast, List<AST> selectStatements) {
        for(AST selectStatement : selectStatements){
            if(!checkRangeStatements(selectStatement)){
                if(LOG.isDebugEnabled()){
                    ASTPrinter printer = new ASTPrinter(HqlTokenTypes.class);
                    String tree = printer.showAsString(ast, "verinice HQL Query String Representation");
                    LOG.debug("Checking if following hql is allowed:\n" + tree + "\n\n" + qry);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * checks if the target of a range statement (argument of an select statement) is element
     * of the whitelist
     * @param selectStatement
     * @return
     */
    private boolean checkRangeStatements(AST selectStatement) {
        for (AST rangeStatement : getRangeofSelectStatement(selectStatement)){
            AST child = rangeStatement.getFirstChild();
            while(child != null){
                if(!isSelectionTargetValid(child)){
                    return false;
                }
                child = child.getNextSibling();
            }
        }
        return true;
    }

    /**
     * is AST-element instance of {@link CnATreeElement} or {@link CnALink}
     * @param child
     * @return
     */
    private boolean isSelectionTargetValid(AST child) {
        // whitelisting, only select on cnatreeelement and cnalink allowed
        Set<String> allowedSelections = new HashSet<String>();
        allowedSelections.add(CnATreeElement.class.getSimpleName());
        allowedSelections.add(CnALink.class.getSimpleName());
        if(HqlParser.IDENT == child.getType() && !(allowedSelections.contains(child.getText()))){
            if(LOG.isDebugEnabled()){
                LOG.debug("qry is not allowed to be executed from a verinice report because usage of:\t\"" + child.getText() + "\" in a select statement");
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isValidQuery(String qry) {
        try {
            return isValidHQL(qry);
        } catch (Exception e) {
            LOG.error("Query could not be parsed", e);
            return false;
        }
    }
    
    /**
     * filters given {@link AST}-Tree on select statementes (/elements)
     * @param astRoot
     * @return
     */
    private List<AST> getAllSelectStatements(AST astRoot){
        ASTUtil.FilterPredicate selectPredicate = new ASTUtil.IncludePredicate() {
            
            @Override
            public boolean include(AST node) {
                return HqlParser.SELECT_FROM == node.getType() ||
                        HqlParser.SELECT == node.getType();
            }
        };
        return ASTUtil.collectChildren(astRoot, selectPredicate);
    }
    
    /**
     * returns the target of a select-statement
     * @param selectRoot
     * @return
     */
    private List<AST> getRangeofSelectStatement(AST selectRoot){
        ASTUtil.FilterPredicate rangePredicate = new ASTUtil.IncludePredicate() {
            
            @Override
            public boolean include(AST node) {
                return HqlParser.RANGE == node.getType();
            }
        };
        return ASTUtil.collectChildren(selectRoot, rangePredicate);
    }
    
    /**
     * validates query on well-formed hql
     * @param hql
     * @return
     * @throws RecognitionException
     * @throws TokenStreamException
     */
    private boolean isValidHQL(String hql) throws RecognitionException, TokenStreamException{
        HqlParser parser = HqlParser.getInstance(hql);
        parser.statement();
        return parser.getParseErrorHandler().getErrorCount() == 0;
    }
    
    /**
     * param an ast node
     * @return if ast is the query root
     */
    private boolean isQueryRoot(AST ast){
        return ast != null && HqlParser.QUERY ==  ast.getType();
    }
    
    /**
     * defines a whitelist for query start-elements
     * @param an ast node
     * @return if node is an allowed beginning of a hql query
     */
    private boolean isAllowedStatementBeginning(AST ast){
        return HqlParser.SELECT == ast.getType() ||
                HqlParser.FROM == ast.getType() ||
                HqlParser.SELECT_FROM == ast.getType();
    }
    
}
