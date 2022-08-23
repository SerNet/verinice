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
import java.util.Optional;

import sernet.hui.common.connect.IAbbreviatedElement;
import sernet.hui.common.connect.ITaggableElement;
import sernet.hui.common.connect.ITargetObject;
import sernet.snutils.TagHelper;
import sernet.verinice.model.bp.BCMUtils;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.Proceeding;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpDocumentGroup;
import sernet.verinice.model.bp.groups.BpIncidentGroup;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bp.groups.BpRecordGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.BusinessProcessGroup;
import sernet.verinice.model.bp.groups.DeviceGroup;
import sernet.verinice.model.bp.groups.IcsSystemGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.NetworkGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.bp.risk.configuration.ConfigurationSerializer;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CnATypeMapper;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ItNetwork extends CnATreeElement
        implements IBpElement, IAbbreviatedElement, ITaggableElement, ITargetObject {

    private static final long serialVersionUID = 6531710922463646931L;

    public static final String TYPE_ID = "bp_itnetwork"; //$NON-NLS-1$
    public static final String PROP_NAME = "bp_itnetwork_name"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_itnetwork_tag"; //$NON-NLS-1$
    public static final String PROP_ABBR = "bp_itnetwork_abbr"; //$NON-NLS-1$
    public static final String PROP_QUALIFIER = "bp_itnetwork_qualifier"; //$NON-NLS-1$
    // These keys shall not be used for localization but only to identify which
    // ENUM value shall be used. Use the ENUMs getLabel() instead.
    private static final String PROP_QUALIFIER_BASIC = "bp_itnetwork_qualifier_basic"; //$NON-NLS-1$
    private static final String PROP_QUALIFIER_STANDARD = "bp_itnetwork_qualifier_standard"; //$NON-NLS-1$
    // the right hand side has to stay "_high" until a proper db-migration has
    // been added
    private static final String PROP_QUALIFIER_CORE = "bp_itnetwork_qualifier_high"; //$NON-NLS-1$

    private static final String PROP_RISK_CONFIGURATION = "bp_itnetwork_risk_configuration"; //$NON-NLS-1$

    public static final String PROP_UNTRAGBARKEITSNIVEAU = "bp_itnetwork_value_untragbarkeitsniveau";

    protected ItNetwork() {
    }

    public ItNetwork(CnATreeElement parent) {
        this(parent, false);
    }

    public ItNetwork(CnATreeElement parent, boolean createChildren) {
        super(parent);
        init();
        if (createChildren) {
            createNewCategories();
        }
    }

    public void createNewCategories() {
        addChild(new ApplicationGroup(this));
        addChild(new BpPersonGroup(this));
        addChild(new BusinessProcessGroup(this));
        addChild(new DeviceGroup(this));
        addChild(new IcsSystemGroup(this));
        addChild(new ItSystemGroup(this));
        addChild(new NetworkGroup(this));
        addChild(new RoomGroup(this));
        addChild(new BpDocumentGroup(this));
        addChild(new BpIncidentGroup(this));
        addChild(new BpRecordGroup(this));
    }

    @Override
    public boolean canContain(Object object) {
        return object instanceof ApplicationGroup || object instanceof BpPersonGroup
                || object instanceof BpRequirementGroup || object instanceof BpThreatGroup
                || object instanceof BusinessProcessGroup || object instanceof DeviceGroup
                || object instanceof IcsSystemGroup || object instanceof ItSystemGroup
                || object instanceof NetworkGroup || object instanceof RoomGroup
                || object instanceof SafeguardGroup || object instanceof BpDocumentGroup
                || object instanceof BpIncidentGroup || object instanceof BpRecordGroup;
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
    public String getTypeId() {
        return TYPE_ID;
    }

    /**
     * @return The Proceeding level represented by property PROP_QUALIFIER
     */
    public Proceeding getProceeding() {
        // Parsing the string as Proceeding should actually be done
        // in Proceeding. But every class has different
        // localization keys. If unique keys, e.g. "QUALIFIER_BASIC"
        // would be used everywhere this code can and should be moved to
        // Proceeding.ofLocalizationKey.
        String qualifier = getEntity().getRawPropertyValue(PROP_QUALIFIER);
        if (qualifier == null) {
            return null;
        }
        switch (qualifier) {
        case PROP_QUALIFIER_BASIC:
            return Proceeding.BASIC;
        case PROP_QUALIFIER_STANDARD:
            return Proceeding.STANDARD;
        case PROP_QUALIFIER_CORE:
            return Proceeding.CORE;
        case "":
            return null;
        default:
            throw new IllegalStateException("Failed to determine proceeding for bp_itnetwork id "
                    + getDbId() + ", invalid value '" + qualifier + "'");
        }
    }

    public static boolean isItNetwork(CnATreeElement element) {
        if (element == null) {
            return false;
        }
        return TYPE_ID.equals(element.getTypeId());
    }

    @Override
    public Collection<String> getTags() {
        return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
    }

    @Override
    public String getAbbreviation() {
        return getEntity().getPropertyValue(PROP_ABBR);
    }

    /**
     * Gets the explicitly configured risk configuration for this IT network,
     * will return {@code null} if there is no explicit configuration.
     *
     * @see #getRiskConfigurationOrDefault()
     */
    public RiskConfiguration getRiskConfiguration() {
        String rawPropertyValue = getEntity().getRawPropertyValue(PROP_RISK_CONFIGURATION);
        if (rawPropertyValue == null) {
            return null;
        }
        return ConfigurationSerializer.configurationFromString(rawPropertyValue);
    }

    /**
     * Gets the <em>effective</em> risk configuration for this IT network, be it
     * an explicitly configured or the default one.
     *
     * @see #getRiskConfiguration()
     * @see DefaultRiskConfiguration
     */
    public RiskConfiguration getRiskConfigurationOrDefault() {
        return Optional.ofNullable(getRiskConfiguration())
                .orElseGet(DefaultRiskConfiguration::getInstance);
    }

    public void setRiskConfiguration(RiskConfiguration riskConfiguration) {
        String rawPropertyValue = null;
        if (riskConfiguration != null) {
            rawPropertyValue = ConfigurationSerializer.configurationToString(riskConfiguration);
        }
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_RISK_CONFIGURATION),
                rawPropertyValue);
    }

    @Override
    public void valuesChanged() {
        super.valuesChanged();
        // Untragbarkeitsniveau might have changed -> update target objects.
        String damagePotentialValueRaw = getEntity().getRawPropertyValue(PROP_UNTRAGBARKEITSNIVEAU);
        if (damagePotentialValueRaw != null && !damagePotentialValueRaw.isEmpty()) {
            Integer damagePotentialValue = Integer.valueOf(damagePotentialValueRaw);
            getChildren().forEach(c -> updateMtpdValues(c, damagePotentialValue));
        }

    }

    private void updateMtpdValues(CnATreeElement child, Integer damagePotentialValue) {
        if (child instanceof ITargetObject && child instanceof IBpElement) {
            if (BCMUtils.isMtpdCalculationEnabled(child)) {
                BCMUtils.updateMtpd(child, damagePotentialValue);
                BCMUtils.updateMinMtpd(child);
            }
            if (BusinessProcess.TYPE_ID.equals(child.getTypeId())) {
                BCMUtils.updateProcessZeitkritisch(child, damagePotentialValue.toString());
            }
        } else if (child instanceof IBpGroup) {
            Class<Object> elementType = CnATypeMapper.getClassFromTypeId(
                    CnATypeMapper.getElementTypeIdFromGroupTypeId(child.getTypeId()));
            if (ITargetObject.class.isAssignableFrom(elementType)
                    && IBpElement.class.isAssignableFrom(elementType)) {
                child.getChildren().forEach(c -> updateMtpdValues(c, damagePotentialValue));
            }
        }
    }
}
