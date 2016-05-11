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
package sernet.verinice.rcp.linktable.ui.combo;

import sernet.verinice.rcp.linktable.ui.Messages;

public enum LinkTableOperationType implements ILinkTableOperationType {

    PROPERTY(".", Messages.VeriniceLinkTableOperationType_00,
            Messages.VeriniceLinkTableOperationType_01),

    RELATION_OBJECT("/", Messages.VeriniceLinkTableOperationType_10,
            Messages.VeriniceLinkTableOperationType_11),

    RELATION(":", Messages.VeriniceLinkTableOperationType_20,
            Messages.VeriniceLinkTableOperationType_21),

    GROUP("<", Messages.VeriniceLinkTableOperationType_30,
            Messages.VeriniceLinkTableOperationType_31),

    CHILD(">", Messages.VeriniceLinkTableOperationType_40,
            Messages.VeriniceLinkTableOperationType_41);


    private final String output;
    private final String label;
    private final String defaultMessage;

    LinkTableOperationType(String output, String label, String defaultMessage) {
        this.output = output;
        this.label = label;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getLabel() {
        return output + " " + label;
    }

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public String toString() {
        return getOutput();
    }

    public static LinkTableOperationType getOperationType(String value) {
        for (LinkTableOperationType type : LinkTableOperationType.values()) {

            if (type.output.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("unsupported value " + value);
    }

    public static boolean isRelation(ILinkTableOperationType type) {
        return type == RELATION || type == RELATION_OBJECT;
    }

    public static boolean isRelation(String type) {
        return type.equals(RELATION_OBJECT.getOutput()) || type.equals(RELATION.getOutput());
    }

    public static String toolTip(){
        StringBuilder builder = new StringBuilder();
        for (LinkTableOperationType type : values()) {
            builder.append(type.getLabel() + "\n");
        }
        return builder.toString();
    }

}