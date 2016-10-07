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
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import sernet.verinice.service.linktable.antlr.VqlLexer;
import sernet.verinice.service.linktable.antlr.VqlParser;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
@SuppressWarnings("restriction")
public class LtrParserTest {

    private static final String COMPLEX_FILE = "complex.vql";
    private static final String PROPERTY_DELIMITER_FILE = "property-delimiter.vql";
    private static final String PARENT_DELIMITER_FILE = "parent-delimiter.vql";
    private static final String LINKTYPE_DELIMITER_FILE = "linktype-delimiter.vql";
    private static final String LINK_DELIMITER_FILE = "link-delimiter.vql";
    private static final String CHILD_DELIMITER_FILE = "child-delimiter.vql";
    private static final String ALIAS_FILE = "alias.vql";
    private static final String ALIAS_LOWER_FILE = "alias-lower.vql";

    @Test
    public void testAliasPath() throws RecognitionException, TokenStreamException {
        String fileName = ALIAS_FILE;
        CommonAST parseTree = parseFile(fileName);
        // incident_scenario/asset/control/person-iso.person-iso_name
        checkPath(parseTree, new String[] { "incident_scenario", "/", "asset", "/", "control", "/", "person-iso", ".", "person-iso_name", "AS", "this-is_an-ALIAS" });
    }

    @Test
    public void testAliasLower() throws RecognitionException, TokenStreamException {
        String fileName = ALIAS_LOWER_FILE;
        CommonAST parseTree = parseFile(fileName);
        // incident_scenario/threat.threat_name
        checkPath(parseTree, new String[] { "incident_scenario", "/", "threat", ".", "threat_name", "as", "threat_name" });
    }

    @Test
    public void testChildDelimiter() throws RecognitionException, TokenStreamException {
        String fileName = CHILD_DELIMITER_FILE;
        CommonAST parseTree = parseFile(fileName);
        // assetgroup>assetgroup.assetgroup_name
        checkPath(parseTree, new String[] { "assetgroup", ">", "assetgroup", ".", "assetgroup_name" });
    }

    @Test
    public void testLinkDelimiter() throws RecognitionException, TokenStreamException {
        String fileName = LINK_DELIMITER_FILE;
        CommonAST parseTree = parseFile(fileName);
        // incident_scenario/threat.threat_name
        checkPath(parseTree, new String[] { "incident_scenario", "/", "threat", ".", "threat_name" });
    }

    @Test
    public void testLinktypeDelimiter() throws RecognitionException, TokenStreamException {
        String fileName = LINKTYPE_DELIMITER_FILE;
        CommonAST parseTree = parseFile(fileName);
        // incident_scenario/threat.threat_name
        checkPath(parseTree, new String[] { "asset", ":", "person-iso" });
    }

    @Test
    public void testParentDelimiter() throws RecognitionException, TokenStreamException {
        String fileName = PARENT_DELIMITER_FILE;
        CommonAST parseTree = parseFile(fileName);
        // samt_topic<controlgroup.controlgroup_name
        checkPath(parseTree, new String[] { "samt_topic", "<", "controlgroup", ".", "controlgroup_name" });
    }

    @Test
    public void testPropertyDelimiter() throws RecognitionException, TokenStreamException {
        String fileName = PROPERTY_DELIMITER_FILE;
        CommonAST parseTree = parseFile(fileName);
        // threat.threat_name
        checkPath(parseTree, new String[] { "threat", ".", "threat_name" });
    }

    @Test
    public void testComplexPath() throws RecognitionException, TokenStreamException {
        String fileName = COMPLEX_FILE;
        CommonAST parseTree = parseFile(fileName);
        // incident_scenario/asset/control/person-iso.person-iso_name
        checkPath(parseTree, new String[] { "incident_scenario", "/", "asset", "/", "control", "/", "person-iso", ".", "person-iso_name" });
    }

    private void checkPath(AST element, String[] names) {
        for (String name : names) {
            assertEquals(name, element.getText());
            element = element.getNextSibling();
        }
    }

    private CommonAST parseFile(String fileName) throws RecognitionException, TokenStreamException {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream(fileName);
        VqlLexer lexer = new VqlLexer(input);
        VqlParser parser = new VqlParser(lexer);
        parser.expr();
        CommonAST parseTree = (CommonAST) parser.getAST();
        return parseTree;
    }

}
