/*******************************************************************************
 * Copyright (c) 2018 Alexander Ben Nasrallah.
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.risk.ui;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateContext;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateResult;
import sernet.verinice.service.bp.risk.RiskService;

/**
 * A utility class for the risk configuration GUI.
 */
public class RiskConfigurationUtil {

    public interface RiskConfigurationChangeListener {
        void riskConfigurationChanged();
    }

    private ScrolledComposite scrolledMatrixTab;
    private ScrolledComposite scrolledImpactTab;
    private ScrolledComposite scrolledFrequenciesTab;
    private ScrolledComposite scrolledRiskValueTab;

    private ImpactConfigurator impactConfigurator;
    private RiskValuesConfigurator riskValueConfigurator;
    private FrequencyConfigurator frequenciesConfigurator;
    private RiskMatrixConfigurator riskMatrixConfigurator;

    private ItNetwork itNetwork;
    private RiskConfiguration currentRiskConfiguration;
    private RiskService riskService;

    private final Consumer<RiskConfiguration> configurationUpdateListener = this::updateConfiguration;
    private final RiskConfigurationChangeListener riskChangeListener;

    public RiskConfigurationUtil(ItNetwork itNetwork,
            RiskConfigurationChangeListener riskChangeListner) {
        super();
        this.itNetwork = itNetwork;
        currentRiskConfiguration = itNetwork.getRiskConfiguration();
        if (currentRiskConfiguration == null) {
            currentRiskConfiguration = DefaultRiskConfiguration.getInstance();
        }
        this.riskChangeListener = riskChangeListner;
    }

    public void dispose() {
        scrolledFrequenciesTab.dispose();

    }

    public void doSave() {
        itNetwork.setRiskConfiguration(currentRiskConfiguration);
        RiskConfigurationUpdateContext updateContext = new RiskConfigurationUpdateContext(
                itNetwork.getUuid(), currentRiskConfiguration);
        updateContext.setDeletedFrequencies(frequenciesConfigurator.getDeleted());
        updateContext.setDeletedImpacts(impactConfigurator.getDeleted());
        updateContext.setDeletedRisks(riskValueConfigurator.getDeleted());
        RiskConfigurationUpdateResult updateResult = getRiskService()
                .updateRiskConfiguration(updateContext);
        RiskConfigurationUpdateResultDialog.openUpdateResultDialog(updateResult);
        impactConfigurator.reset();
        frequenciesConfigurator.reset();
        riskValueConfigurator.reset();
        riskChangeListener.riskConfigurationChanged();
    }

    public boolean isDirty() {
        return !currentRiskConfiguration.deepEquals(itNetwork.getRiskConfiguration());
    }

    public ScrolledComposite createRiskMatrixPage(Composite parent) {
        scrolledMatrixTab = createScrollableComposite(parent);
        riskMatrixConfigurator = new RiskMatrixConfigurator(scrolledMatrixTab,
                currentRiskConfiguration, configurationUpdateListener);
        scrolledMatrixTab.setContent(riskMatrixConfigurator);
        return scrolledMatrixTab;
    }

    public ScrolledComposite createRiskValuePage(Composite parent) {
        scrolledRiskValueTab = createScrollableComposite(parent);
        riskValueConfigurator = new RiskValuesConfigurator(scrolledRiskValueTab,
                configurationUpdateListener);
        riskValueConfigurator.setRiskConfiguration(currentRiskConfiguration);
        return scrolledRiskValueTab;
    }

    public ScrolledComposite createRiskImpact(Composite parent) {
        scrolledImpactTab = createScrollableComposite(parent);
        impactConfigurator = new ImpactConfigurator(scrolledImpactTab, configurationUpdateListener);
        impactConfigurator.setRiskConfiguration(currentRiskConfiguration);
        scrolledImpactTab.setContent(impactConfigurator);
        return scrolledImpactTab;
    }

    public ScrolledComposite createRiskFrequency(Composite parent) {
        scrolledFrequenciesTab = createScrollableComposite(parent);
        frequenciesConfigurator = new FrequencyConfigurator(scrolledFrequenciesTab,
                configurationUpdateListener);
        frequenciesConfigurator.setRiskConfiguration(currentRiskConfiguration);
        scrolledFrequenciesTab.setContent(frequenciesConfigurator);
        return scrolledFrequenciesTab;
    }

    public void updateConfiguration() {
        updateConfiguration(currentRiskConfiguration);
    }

    public static boolean checkRights() {
        Activator.inheritVeriniceContextState();
        RightsServiceClient service = (RightsServiceClient) VeriniceContext
                .get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(ActionRightIDs.EDITRISKCONFIGURATION);
    }

    private ScrolledComposite createScrollableComposite(Composite parent) {
        ScrolledComposite scrolledComposite = new ScrolledComposite(parent,
                SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        return scrolledComposite;
    }

    private void updateConfiguration(RiskConfiguration riskConfiguration) {
        currentRiskConfiguration = riskConfiguration;

        riskValueConfigurator.setRiskConfiguration(currentRiskConfiguration);
        frequenciesConfigurator.setRiskConfiguration(currentRiskConfiguration);
        impactConfigurator.setRiskConfiguration(currentRiskConfiguration);

        // update composite size to fit the scroll view
        riskMatrixConfigurator = new RiskMatrixConfigurator(scrolledMatrixTab,
                currentRiskConfiguration, configurationUpdateListener);
        scrolledMatrixTab.setContent(riskMatrixConfigurator);
        riskMatrixConfigurator.pack(true);
        scrolledMatrixTab.setMinSize(riskMatrixConfigurator.getClientArea().width,
                riskMatrixConfigurator.getClientArea().height);

        riskValueConfigurator.pack(true);
        scrolledRiskValueTab.setMinSize(riskValueConfigurator.getClientArea().width,
                riskValueConfigurator.getClientArea().height);
        frequenciesConfigurator.pack(true);
        scrolledFrequenciesTab.setMinSize(frequenciesConfigurator.getClientArea().width,
                frequenciesConfigurator.getClientArea().height);
        impactConfigurator.pack(true);
        scrolledImpactTab.setMinSize(impactConfigurator.getClientArea().width,
                impactConfigurator.getClientArea().height);
        riskChangeListener.riskConfigurationChanged();
    }

    private RiskService getRiskService() {
        if (riskService == null) {
            riskService = (RiskService) VeriniceContext.get(VeriniceContext.ITBP_RISK_SERVICE);
        }
        return riskService;
    }
}
