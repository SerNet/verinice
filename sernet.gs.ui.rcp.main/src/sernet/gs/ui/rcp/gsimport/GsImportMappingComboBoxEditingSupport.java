/**
 *
 */
package sernet.gs.ui.rcp.gsimport;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
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

    public GsImportMappingComboBoxEditingSupport(TableViewer viewer) {
        super(viewer);
        this.viewer = viewer;

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
        if(element instanceof Object[]) {
            Object[] entry = (Object[])element;
            return getIndexOfVeriniceObjectType((String)entry[0]);

        }

        return -1;
    }

    private String[] getTranslatedVeriniceValues() {
        String[] list = new String[veriniceITGSObjectTypes.size()];
        int i = 0;
        for(String value : veriniceITGSObjectTypes) {
            String msg = HitroUtil.getInstance().getTypeFactory().getMessage(value);
            list[i] = (StringUtils.isNotEmpty(msg)) ? msg : value;
        }
        return list;
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
        //        GstoolTypeMapper.
        // somehow save that stuff to file in workspace, gstool-subtypes.properties
    }

}
