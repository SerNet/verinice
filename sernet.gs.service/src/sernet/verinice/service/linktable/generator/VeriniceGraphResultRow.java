/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable.generator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Collects every found match @link {@link VeriniceGraphResultEntry}.
 *
 * A {@link VeriniceGraphResultRow} represents a row in the link later table.
 * The main difference to the final is, that it does not store final strings of
 * {@link CnATreeElement} properties in there but
 * {@link VeriniceGraphResultEntry}. This entries hold the information which
 * properties have to be printed out to the link table and can be handled easier
 * than plain strings in the table.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
final class VeriniceGraphResultRow {

    Map<String, VeriniceGraphResultEntry> entries = new HashMap<>();

    private final Logger log = Logger.getLogger(VeriniceGraphResultRow.class);

    boolean contains(VeriniceGraphResultEntry e) {
        return entries.containsKey(e.getColumnKey());
    }

    void addEntry(VeriniceGraphResultEntry e) {
        entries.put(e.getColumnKey(), e);
        e.add(this);
    }

    VeriniceGraphResultEntry getEntry(String columnKey) {
        return entries.get(columnKey);
    }

    VeriniceGraphResultRow duplicate() {
        VeriniceGraphResultRow newRow = new VeriniceGraphResultRow();
        newRow.entries = new HashMap<>(this.entries);
        return newRow;
    }

    Map<String, String> getExpandedRow() {
        Map<String, String> row = new HashMap<>();
        for (VeriniceGraphResultEntry e : entries.values())
            if (e != null)
                e.getEntries(row);

        return row;
    }

    void removeEntry(VeriniceGraphResultEntry e) {
        log.debug("remove it self: " + e.getColumnKey());
        entries.remove(e.getColumnKey());
    }
}
