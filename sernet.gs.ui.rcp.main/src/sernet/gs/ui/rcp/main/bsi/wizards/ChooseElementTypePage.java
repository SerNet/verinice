package sernet.gs.ui.rcp.main.bsi.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.reports.ISMReport;
import sernet.gs.ui.rcp.main.reports.ISMTypedItemReport;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnATreeElementTitles;
import sernet.hui.common.connect.EntityType;
import sernet.verinice.iso27k.model.Organization;

/**
 * Choose which entitytype to include in the report.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class ChooseElementTypePage extends WizardPage {

    private Combo entityTypeCombo;
    private Button relationsBtn;
    private List<EntityType> allEntityTypes;
    private EntityType selectedEntityType;

    protected ChooseElementTypePage() {
        super("chooseObjectTypeForExport");
        setTitle("Objekttyp auswählen");
        setDescription("Wählen Sie den Objekttyp, für den der Report erstellt werden soll (nur für ISO 27k-Reports).");
    }

    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);
        setControl(container);

        final Label label2 = new Label(container, SWT.NULL);
        GridData gridData7 = new GridData(GridData.HORIZONTAL_ALIGN_END);
        label2.setLayoutData(gridData7);
        label2.setText("Objekttyp:");

        entityTypeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        entityTypeCombo.setEnabled(false);
        entityTypeCombo.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                int s = entityTypeCombo.getSelectionIndex();
                selectedEntityType = allEntityTypes.get(s);
                ((ISMTypedItemReport)getExportWizard().getReport()).setEntityTypeId(selectedEntityType.getId());
                updatePageComplete();
            }

        });

        relationsBtn = new Button(container, SWT.CHECK);
        GridData gd9 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        relationsBtn.setLayoutData(gd9);
        relationsBtn.setText("Inklusive Relationen");
        relationsBtn.setVisible(false);
        relationsBtn.pack();

        loadEntityTypes();
    }

    /**
	 * 
	 */
    private void loadEntityTypes() {
        Collection<EntityType> entityTypes = new ArrayList<EntityType>(); 
        entityTypes.addAll(HitroUtil.getInstance().getTypeFactory().getAllEntityTypes());
        
        // remove everything but group types:
        for (Iterator iterator = entityTypes.iterator(); iterator.hasNext();) {
            EntityType entityType = (EntityType) iterator.next();
            if (entityType.getId().indexOf("group") < 0) {
                iterator.remove();
            }
        }

        this.allEntityTypes = new ArrayList<EntityType>(entityTypes.size());
        this.allEntityTypes.addAll(entityTypes);

        entityTypeCombo.removeAll();
        for (EntityType type : allEntityTypes) {
            entityTypeCombo.add(type.getName());
        }
        entityTypeCombo.setEnabled(true);
        entityTypeCombo.pack();
    }

    ExportWizard getExportWizard() {
        return ((ExportWizard) getWizard());
    }

    private void updatePageComplete() {
        if (!(getExportWizard().getReport() instanceof ISMReport)) {
            entityTypeCombo.setEnabled(false);
            relationsBtn.setEnabled(false);
            setMessage(null);
            setPageComplete(true);
            return;
        }

        boolean complete = selectedEntityType != null;
        if (!complete) {
            setMessage(null);
            setPageComplete(false);
            return;
        }

        setPageComplete(true);
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        updatePageComplete();
    }

}