/*******************************************************************************
 * Copyright (c) 2020 Finn Westendorf
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
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.elasticsearch.common.base.Objects;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.IIdentifiableElement;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.NonNullUtils;

/**
 * This is the command for the MoGS consolidator.
 * <p>
 * This is supposed to be used by {@link ConsolidatorWizard}, the wizard stores
 * all collected info in a {@link ConsoliData} and sends this command to the
 * server to actually edit the properties.
 */
public class ConsolidatorCommand extends GenericCommand implements IAuthAwareCommand {

    private static final Logger logger = Logger.getLogger(ConsolidatorCommand.class);

    private static final long serialVersionUID = -5191684349649490022L;
    private ConsoliData data;

    private static String error;

    private transient IAuthService authService;

    private transient String userName;

    public ConsolidatorCommand(@NonNull ConsoliData data) {
        this.data = data;
    }

    @Override
    public void execute() {
        // Maybe evaluate and save everything from source, so I don't need to do
        // that again for every module.
        long start = System.currentTimeMillis();
        userName = authService.getUsername();
        IBaseDao<BpRequirementGroup, Serializable> dao = getDaoFactory()
                .getDAO(BpRequirementGroup.class);

        BpRequirementGroup sourceRequirementGroup = dao.findByUuid(
                data.getSourceRequirementGroupUuid(), RetrieveInfo.getPropertyInstance());

        if (sourceRequirementGroup == null) {
            logger.fatal("Source BpRequirementGroup is null, can't consolidate null. Uuid: "
                    + data.getSourceRequirementGroupUuid());
            return;
        }

        DetachedCriteria criteria = DetachedCriteria.forClass(BpRequirementGroup.class);
        criteria.add(Restrictions.in("uuid", data.getTargetRequirementGroupUuids()));
        @SuppressWarnings("unchecked")
        List<@NonNull BpRequirementGroup> targetRequirementGroups = dao.findByCriteria(criteria);

        targetRequirementGroups
                .forEach(x -> consolidateRequirementGroupProperties(sourceRequirementGroup, x));
        targetRequirementGroups.forEach(dao::saveOrUpdate);
        targetRequirementGroups.forEach(
                reqGroups -> reqGroups.getChildren().forEach(CnATreeElement::valuesChanged));

        if (logger.isDebugEnabled()) {
            logger.debug("Consolidator Runtime: " + (System.currentTimeMillis() - start) + " ms.");
        }
    }

    private void consolidateRequirementGroupProperties(@NonNull BpRequirementGroup source,
            @NonNull BpRequirementGroup target) {
        consolidateElement(source, target);
        List<BpRequirement> sourceChildren = source.getChildren().stream()
                .filter(x -> x instanceof BpRequirement).map(x -> (BpRequirement) x)
                .collect(Collectors.toList());
        List<BpRequirement> targetChildren = target.getChildren().stream()
                .filter(x -> x instanceof BpRequirement).map(x -> (BpRequirement) x)
                .collect(Collectors.toList());
        for (BpRequirement req : targetChildren) {
            for (BpRequirement possibleSource : sourceChildren) {
                if (possibleSource.getIdentifier().equals(req.getIdentifier())) {
                    consolidateElement(possibleSource, req);
                    consolidateLinks(possibleSource, req, BpThreat.TYPE_ID);
                    consolidateLinks(possibleSource, req, Safeguard.TYPE_ID);
                    break;
                }
            }
        }
    }

    private void consolidateLinks(@NonNull BpRequirement source, @NonNull BpRequirement target,
            @NonNull String typeId) {
        Map<String, @NonNull CnATreeElement> targetLinkedOfType = getLinkedIdElementMap(target,
                typeId);
        Map<String, @NonNull CnATreeElement> sourceLinkedOfType = getLinkedIdElementMap(source,
                typeId);
        Set<String> s = new HashSet<>(targetLinkedOfType.keySet());
        s.retainAll(sourceLinkedOfType.keySet());
        s.forEach(x -> consolidateElement(sourceLinkedOfType.get(x), targetLinkedOfType.get(x)));
    }

    private void consolidateElement(@NonNull CnATreeElement source,
            @NonNull CnATreeElement target) {
        if (!source.getTypeId().equals(target.getTypeId())) {
            logger.warn(String.format("Consolidator Type Mismatch. Source: %s Target: %s",
                    source.getTypeId(), target.getTypeId()));
            return;
        }

        HUITypeFactory typeFactory = HitroUtil.getInstance().getTypeFactory();
        EntityType type = typeFactory.getEntityType(source.getTypeId());
        boolean targetUpdated = false;
        if (data.getSelectedPropertyGroups().contains(source.getTypeId() + "_general")) {
            targetUpdated = type.getPropertyTypes().stream().filter(x -> x.getId() != null).map(
                    x -> consolidateProperty(NonNullUtils.toNonNull(x.getId()), source, target))
                    .collect(Collectors.toSet()).contains(Boolean.TRUE);
        }

        targetUpdated |= type.getPropertyGroups().stream()
                .filter(c -> data.getSelectedPropertyGroups().contains(c.getId()))
                .flatMap(
                        x -> x.getPropertyTypes().stream()
                                .map(y -> consolidateProperty(NonNullUtils.toNonNull(y.getId()),
                                        source, target)))
                .collect(Collectors.toSet()).contains(Boolean.TRUE);
        if (targetUpdated) {
            getDaoFactory().getDAO(CnATreeElement.class).checkRights(target);
            target.getEntity().trackChange(userName);
        }
    }

    private Map<String, @NonNull CnATreeElement> getLinkedIdElementMap(
            @NonNull BpRequirement source, @NonNull String typeId) {
        Supplier<Stream<IIdentifiableElement>> supplier = () -> source.getLinksDown().stream()
                .map(CnALink::getDependency).filter(x -> x.getTypeId().equals(typeId))
                .map(x -> x instanceof HibernateProxy
                        ? ((HibernateProxy) x).getHibernateLazyInitializer().getImplementation()
                        : x)
                .filter(x -> x instanceof IIdentifiableElement)
                .map(IIdentifiableElement.class::cast);

        // Detect & collect identifiers used multiple times
        Map<String, List<IIdentifiableElement>> collect = supplier.get()
                .collect(Collectors.groupingBy(IIdentifiableElement::getIdentifier));
        String doubles = collect.entrySet().stream().filter(x -> x.getValue().size() > 1)
                .map(Entry::getKey).collect(Collectors.joining(", "));
        if (!StringUtils.isEmpty(doubles)) {
            error = String.format(Messages.getString("duplicateIdentifierError"), doubles, typeId);
            throw new RuntimeCommandException(error);
        }

        return supplier.get().collect(Collectors.toMap(IIdentifiableElement::getIdentifier,
                x -> (CnATreeElement) NonNullUtils.toNonNull(x)));
    }

    private boolean consolidateProperty(@NonNull String property, @NonNull CnATreeElement source,
            @NonNull CnATreeElement target) {
        String oldValue = target.getEntity().getRawPropertyValue(property);
        String newValue = source.getEntity().getRawPropertyValue(property);
        if (!Objects.equal(oldValue, newValue)) {
            target.setSimpleProperty(property, newValue);
            return true;
        }
        return false;
    }

    public String getError() {
        return error;
    }

    @Override
    public void setAuthService(IAuthService authService) {
        this.authService = authService;

    }

    @Override
    public IAuthService getAuthService() {
        return authService;
    }
}