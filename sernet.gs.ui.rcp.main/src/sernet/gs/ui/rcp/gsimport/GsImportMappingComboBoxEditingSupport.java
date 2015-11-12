/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
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

package sernet.gs.ui.rcp.gsimport;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

import sernet.hui.common.connect.HitroUtil;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;

/**
 * @author shagedorn
 *
 */
public class GsImportMappingComboBoxEditingSupport extends EditingSupport {

    private static final Logger LOG = Logger.getLogger(GsImportMappingComboBoxEditingSupport.class);


    private static final Set<String> veriniceITGSObjectTypes = new HashSet<>(8);

    static {
        veriniceITGSObjectTypes.addAll(Arrays.asList(new String[] {ITVerbund.TYPE_ID,
                Anwendung.TYPE_ID,
                Client.TYPE_ID,
                Server.TYPE_ID,
                Raum.TYPE_ID,
                Gebaeude.TYPE_ID,
                TelefonKomponente.TYPE_ID,
                SonstIT.TYPE_ID}));
    }

    private TableViewer viewer;
    private GstoolImportMappingView view;
    private String[] translatedVeriniceValues = null;
    private Map<String, String> veriniceValuesMap = null;

    public GsImportMappingComboBoxEditingSupport(TableViewer viewer, GstoolImportMappingView view) {
        super(viewer);
        this.viewer = viewer;
        this.view = view;

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
     */
    @Override
    protected boolean canEdit(Object arg0) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
     */
    @Override
    protected CellEditor getCellEditor(Object arg0) {
        ComboBoxCellEditor choiceEditor = new ComboBoxCellEditor(this.viewer.getTable(), getTranslatedVeriniceValues(), SWT.READ_ONLY);
        choiceEditor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
        return choiceEditor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
     */
    @Override
    protected Object getValue(Object element) {
        if (element instanceof GstoolImportMappingElement) {
            GstoolImportMappingElement entry = (GstoolImportMappingElement) element;
            return getIndexOfVeriniceObjectType(entry.getKey());

        }

        return -1;
    }

    private String[] getTranslatedVeriniceValues() {
        if (translatedVeriniceValues == null) {
            translatedVeriniceValues = getMappedTranslatedVeriniceValues().keySet().toArray(new String[veriniceITGSObjectTypes.size()]);
        }
        return translatedVeriniceValues;
    }

    /**
     * Method for translating the verinice types with .properties and mapping
     * them for later back-tracking
     * 
     * @return Map<translatedType, veriniceITGSObjectType>
     */
    private Map<String, String> getMappedTranslatedVeriniceValues() {
        // lazy loading because not often changed
        if (veriniceValuesMap == null) {
            veriniceValuesMap = new TreeMap<>();
            for (String value : veriniceITGSObjectTypes) {
                String msg = HitroUtil.getInstance().getTypeFactory().getMessage(value);
                String tmp = (StringUtils.isNotEmpty(msg)) ? msg : value;
                veriniceValuesMap.put(tmp, value);
            }
        }
        return veriniceValuesMap;
    }

    private int getIndexOfVeriniceObjectType(String objectType) {
        String[] list = getTranslatedVeriniceValues();
        for(int i = 0; i < list.length; i++) {
            if(objectType.equals(list[i])) {
                return i;
            }
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
     */
    @Override
    protected void setValue(Object element, Object value) {
        int val;
        try {
            if (value instanceof Integer) {
                val = (Integer) value;
                if (element instanceof GstoolImportMappingElement) {

                    GstoolImportMappingElement oldEntry = (GstoolImportMappingElement) element;
                    GstoolImportMappingElement newEntry = new GstoolImportMappingElement(oldEntry.getKey(), getMappedTranslatedVeriniceValues().get(getTranslatedVeriniceValues()[val]));
                    GstoolTypeMapper.addGstoolSubtypeToPropertyFile(newEntry);
                    view.refresh();
                    viewer.setSelection(new StructuredSelection(newEntry), true);
                } else {
                    LOG.error("Class of Element:\t" + element.getClass().getCanonicalName());
                }

            } else {
                LOG.error("Class of value-Element:\t" + element.getClass().getCanonicalName());
            }
        } catch (Exception e) {
            LOG.error("error", e);
        }

    }
}
