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
package sernet.verinice.rcp.linktable.composite.combo;

import sernet.verinice.rcp.linktable.composite.Messages;

public enum VeriniceLinkTableOperationType implements IVeriniceLinkTableOperationType {

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

    VeriniceLinkTableOperationType(String output, String label, String defaultMessage) {
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

    public static VeriniceLinkTableOperationType getOperationType(String value) {
        for (VeriniceLinkTableOperationType type : VeriniceLinkTableOperationType.values()) {

            if (type.output.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("unsupported value " + value);
    }

}