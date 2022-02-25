/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bp.elements;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import sernet.gs.service.StringUtil;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.IIdentifiableElement;
import sernet.hui.common.connect.ITaggableElement;
import sernet.snutils.TagHelper;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.bp.risk.RiskService;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 */
public class BpThreat extends CnATreeElement
        implements IBpElement, IIdentifiableElement, ITaggableElement {

    private static final long serialVersionUID = -7182966153863832177L;

    public static final String PROP_OBJECTBROWSER_DESC = "bp_threat_objectbrowser_content"; //$NON-NLS-1$
    public static final String PROP_NAME = "bp_threat_name"; //$NON-NLS-1$
    public static final String PROP_ID = "bp_threat_id"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_threat_tag"; //$NON-NLS-1$

    public static final String PROP_CONFIDENTIALITY = "bp_threat_value_method_confidentiality"; //$NON-NLS-1$
    public static final String PROP_INTEGRITY = "bp_threat_value_method_integrity"; //$NON-NLS-1$
    public static final String PROP_AVAILABILITY = "bp_threat_value_method_availability"; //$NON-NLS-1$
    public static final String PROP_FREQUENCY_WITHOUT_SAFEGUARDS = "bp_threat_risk_without_safeguards_frequency";//$NON-NLS-1$
    public static final String PROP_IMPACT_WITHOUT_SAFEGUARDS = "bp_threat_risk_without_safeguards_impact";//$NON-NLS-1$
    public static final String PROP_RISK_WITHOUT_SAFEGUARDS = "bp_threat_risk_without_safeguards_risk";//$NON-NLS-1$
    public static final String PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS = "bp_threat_risk_without_additional_safeguards_frequency";//$NON-NLS-1$
    public static final String PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS = "bp_threat_risk_without_additional_safeguards_impact";//$NON-NLS-1$
    public static final String PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS = "bp_threat_risk_without_additional_safeguards_risk";//$NON-NLS-1$
    public static final String PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS = "bp_threat_risk_with_additional_safeguards_frequency";//$NON-NLS-1$
    public static final String PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS = "bp_threat_risk_with_additional_safeguards_impact";//$NON-NLS-1$
    public static final String PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS = "bp_threat_risk_with_additional_safeguards_risk";//$NON-NLS-1$
    public static final String PROP_RISK_TREATMENT_OPTION = "bp_threat_risk_treatment_option";//$NON-NLS-1$
    public static final String PROP_RELEASE = "bp_threat_release"; //$NON-NLS-1$
    public static final String PROP_CHANGE_TYPE = "bp_threat_change_type"; //$NON-NLS-1$
    public static final String PROP_CHANGE_TYPE_REMOVED = "bp_threat_change_type_removed"; //$NON-NLS-1$
    public static final String PROP_CHANGE_DETAILS = "bp_threat_change_details"; //$NON-NLS-1$
    public static final String PROP_RISK_TREATMENT_OPTION_RISK_REDUCTION = "bp_threat_risk_treatment_option_risk_reduction"; //$NON-NLS-1$
    public static final String PROP_RISK_TREATMENT_OPTION_TRANSFER_OF_RISK = "bp_threat_risk_treatment_option_transfer_of_risk"; //$NON-NLS-1$
    public static final String PROP_RISK_TREATMENT_OPTION_RISK_AVOIDANCE = "bp_threat_risk_treatment_option_risk_avoidance"; //$NON-NLS-1$
    public static final String PROP_RISK_TREATMENT_OPTION_RISK_ACCEPTANCE = "bp_threat_risk_treatment_option_risk_acceptance"; //$NON-NLS-1$

    public static final String TYPE_ID = "bp_threat"; //$NON-NLS-1$

    public static final String REL_BP_THREAT_BP_APPLICATION = "rel_bp_threat_bp_application"; //$NON-NLS-1$
    public static final String REL_BP_THREAT_BP_BUSINESSPROCESS = "rel_bp_threat_bp_businessprocess"; //$NON-NLS-1$
    public static final String REL_BP_THREAT_BP_DEVICE = "rel_bp_threat_bp_device"; //$NON-NLS-1$
    public static final String REL_BP_THREAT_BP_ICSSYSTEM = "rel_bp_threat_bp_icssystem"; //$NON-NLS-1$
    public static final String REL_BP_THREAT_BP_ITNETWORK = "rel_bp_threat_bp_itnetwork"; //$NON-NLS-1$
    public static final String REL_BP_THREAT_BP_ITSYSTEM = "rel_bp_threat_bp_itsystem"; //$NON-NLS-1$
    public static final String REL_BP_THREAT_BP_NETWORK = "rel_bp_threat_bp_network"; //$NON-NLS-1$
    public static final String REL_BP_THREAT_BP_ROOM = "rel_bp_threat_bp_room"; //$NON-NLS-1$

    public static final String PROP_GRP_RISK_TREATMENT_OPTION_GROUP = "bp_threat_risk_treatment_option_group"; //$NON-NLS-1$
    public static final String PROP_GRP_RISK_WITH_ADDITIONAL_SAFEGUARDS = "bp_threat_risk_with_additional_safeguards"; //$NON-NLS-1$
    public static final String PROP_GRP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS = "bp_threat_risk_without_additional_safeguards"; //$NON-NLS-1$
    public static final String PROP_GRP_RISK_WITHOUT_SAFEGUARDS = "bp_threat_risk_without_safeguards"; //$NON-NLS-1$
    private static final Map<String, String> RELATION_TYPES_BY_TARGET_OBJECT_TYPE = new HashMap<>();
    static {
        RELATION_TYPES_BY_TARGET_OBJECT_TYPE.put(Application.TYPE_ID,
                BpThreat.REL_BP_THREAT_BP_APPLICATION);
        RELATION_TYPES_BY_TARGET_OBJECT_TYPE.put(BusinessProcess.TYPE_ID,
                BpThreat.REL_BP_THREAT_BP_BUSINESSPROCESS);
        RELATION_TYPES_BY_TARGET_OBJECT_TYPE.put(Device.TYPE_ID, BpThreat.REL_BP_THREAT_BP_DEVICE);
        RELATION_TYPES_BY_TARGET_OBJECT_TYPE.put(IcsSystem.TYPE_ID,
                BpThreat.REL_BP_THREAT_BP_ICSSYSTEM);
        RELATION_TYPES_BY_TARGET_OBJECT_TYPE.put(ItNetwork.TYPE_ID,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        RELATION_TYPES_BY_TARGET_OBJECT_TYPE.put(ItSystem.TYPE_ID,
                BpThreat.REL_BP_THREAT_BP_ITSYSTEM);
        RELATION_TYPES_BY_TARGET_OBJECT_TYPE.put(Network.TYPE_ID,
                BpThreat.REL_BP_THREAT_BP_NETWORK);
        RELATION_TYPES_BY_TARGET_OBJECT_TYPE.put(Room.TYPE_ID, BpThreat.REL_BP_THREAT_BP_ROOM);
    }

    protected BpThreat() {
    }

    public BpThreat(CnATreeElement parent) {
        super(parent);
        init();
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    public String getObjectBrowserDescription() {
        return getEntity().getPropertyValue(PROP_OBJECTBROWSER_DESC);
    }

    public void setObjectBrowserDescription(String description) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_OBJECTBROWSER_DESC),
                description);
    }

    @Override
    public String getTitle() {
        return getEntity().getPropertyValue(PROP_NAME);
    }

    @Override
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }

    @Override
    public String getIdentifier() {
        return getEntity().getPropertyValue(PROP_ID);
    }

    public void setIdentifier(String id) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ID), id);
    }

    public boolean isConfidentiality() {
        String value = getEntity().getPropertyValue(PROP_CONFIDENTIALITY);
        return Boolean.getBoolean(value);
    }

    public void setConfidentiality(boolean isConfidentiality) {
        int value = (isConfidentiality) ? 1 : 0;
        setNumericProperty(PROP_CONFIDENTIALITY, value);
    }

    public boolean isIntegrity() {
        String value = getEntity().getPropertyValue(PROP_INTEGRITY);
        return Boolean.getBoolean(value);
    }

    public void setIntegrity(boolean isIntegrity) {
        int value = (isIntegrity) ? 1 : 0;
        setNumericProperty(PROP_INTEGRITY, value);
    }

    public boolean isAvailability() {
        String value = getEntity().getPropertyValue(PROP_AVAILABILITY);
        return Boolean.getBoolean(value);
    }

    public void setAvailibility(boolean isAvailability) {
        int value = (isAvailability) ? 1 : 0;
        setNumericProperty(PROP_AVAILABILITY, value);
    }

    public static String getIdentifierOfThreat(CnATreeElement requirement) {
        return requirement.getEntity().getPropertyValue(PROP_ID);
    }

    public static String getLinkTypeToTargetObject(String objectType) {
        return RELATION_TYPES_BY_TARGET_OBJECT_TYPE.get(objectType);
    }

    @Override
    public String getFullTitle() {
        return joinPrefixAndTitle(getIdentifier(), getTitle());
    }

    @Override
    public Collection<String> getTags() {
        return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
    }

    public String getFrequencyWithoutSafeguards() {
        return StringUtil.replaceEmptyStringByNull(
                getEntity().getRawPropertyValue(PROP_FREQUENCY_WITHOUT_SAFEGUARDS));
    }

    public String getFrequencyWithAdditionalSafeguards() {
        return StringUtil.replaceEmptyStringByNull(
                getEntity().getRawPropertyValue(PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS));
    }

    public String getFrequencyWithoutAdditionalSafeguards() {
        return StringUtil.replaceEmptyStringByNull(
                getEntity().getRawPropertyValue(PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS));
    }

    public String getImpactWithoutSafeguards() {
        return StringUtil.replaceEmptyStringByNull(
                getEntity().getRawPropertyValue(PROP_IMPACT_WITHOUT_SAFEGUARDS));
    }

    public String getImpactWithAdditionalSafeguards() {
        return StringUtil.replaceEmptyStringByNull(
                getEntity().getRawPropertyValue(PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS));

    }

    public String getImpactWithoutAdditionalSafeguards() {
        return StringUtil.replaceEmptyStringByNull(
                getEntity().getRawPropertyValue(PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS));
    }

    public String getRiskWithoutSafeguards() {
        return StringUtil.replaceEmptyStringByNull(
                getEntity().getRawPropertyValue(PROP_RISK_WITHOUT_SAFEGUARDS));
    }

    public String getRiskWithoutAdditionalSafeguards() {
        return StringUtil.replaceEmptyStringByNull(
                getEntity().getRawPropertyValue(PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS));
    }

    public String getRiskWithAdditionalSafeguards() {
        return StringUtil.replaceEmptyStringByNull(
                getEntity().getRawPropertyValue(PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS));
    }

    public void setRiskWithoutSafeguards(String risk) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_RISK_WITHOUT_SAFEGUARDS),
                risk);
    }

    public void setFrequencyWithoutSafeguards(String frequency) {
        getEntity().setSimpleValue(
                getEntityType().getPropertyType(PROP_FREQUENCY_WITHOUT_SAFEGUARDS), frequency);
    }

    public void setImpactWithoutSafeguards(String impact) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_IMPACT_WITHOUT_SAFEGUARDS),
                impact);
    }

    public void setRiskWithoutAdditionalSafeguards(String risk) {
        getEntity().setSimpleValue(
                getEntityType().getPropertyType(PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS), risk);

    }

    public void setFrequencyWithoutAdditionalSafeguards(String frequency) {
        getEntity().setSimpleValue(
                getEntityType().getPropertyType(PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS),
                frequency);

    }

    public void setImpactWithoutAdditionalSafeguards(String impact) {
        getEntity().setSimpleValue(
                getEntityType().getPropertyType(PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS), impact);

    }

    public void setFrequencyWithAdditionalSafeguards(String frequency) {
        getEntity().setSimpleValue(
                getEntityType().getPropertyType(PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS),
                frequency);

    }

    public void setImpactWithAdditionalSafeguards(String impact) {
        getEntity().setSimpleValue(
                getEntityType().getPropertyType(PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS), impact);

    }

    public void setRiskWithAdditionalSafeguards(String risk) {
        getEntity().setSimpleValue(
                getEntityType().getPropertyType(PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS), risk);

    }

    public String getRiskLabel() {
        String riskId = Optional.ofNullable(getRiskWithAdditionalSafeguards())
                .orElseGet(this::getRiskWithoutAdditionalSafeguards);
        if (riskId != null) {
            return ((RiskService) VeriniceContext.get(VeriniceContext.ITBP_RISK_SERVICE))
                    .getRisk(riskId, getScopeId()).getLabel();
        }
        return null;
    }
}
