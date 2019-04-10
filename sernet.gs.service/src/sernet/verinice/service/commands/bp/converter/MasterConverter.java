/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
package sernet.verinice.service.commands.bp.converter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bp.groups.BusinessProcessGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.NetworkGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.bsi.Addition;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.service.commands.AttachmentFileCreationFactory;

/**
 * Converts a whole ITVerbund from the old ITBP to an ITNetwork from the
 * modernized ITBP
 */
public class MasterConverter {

    public static final String TAG_MOGS_ANWENDUNG = "MoGs:Anwendung";
    public static final String TAG_MOGS_PROZESS = "MoGs:Prozess";

    private static final Logger logger = Logger.getLogger(MasterConverter.class);

    private Map<CnATreeElement, CnATreeElement> convertedElements = new HashMap<>();

    private final IDAOFactory daoFactory;

    private final @NonNull IBaseDao<@NonNull CnATreeElement, Serializable> elementDao;

    public MasterConverter(IDAOFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.elementDao = daoFactory.getDAO(CnATreeElement.class);
    }

    public Collection<ItNetwork> convert(Collection<ITVerbund> itVerbunds) {
        BpModel bpModel = daoFactory.getDAO(BpModel.class).findAll().get(0);
        ITVerbundConverter itVerbundConverter = new ITVerbundConverter();
        Collection<ItNetwork> result = new ArrayList<>(itVerbunds.size());
        for (ITVerbund itVerbund : itVerbunds) {
            ItNetwork itNetwork = convertITVerbund(bpModel, itVerbundConverter, itVerbund);
            result.add(itNetwork);
        }
        convertAdditions();

        LinkConverter.convertLinks(convertedElements, daoFactory.getDAO(CnALink.class));

        return result;
    }

    private ItNetwork convertITVerbund(BpModel bpModel, ITVerbundConverter itVerbundConverter,
            ITVerbund itVerbund) {
        ItNetwork itNetwork = itVerbundConverter.convert(itVerbund);
        itNetwork.setPermissions(
                Permission.clonePermissionSet(itNetwork, itVerbund.getPermissions()));
        convertedElements.put(itVerbund, itNetwork);

        handleITSystems(itVerbund, itNetwork);

        handleRooms(itVerbund, itNetwork);

        Set<CnATreeElement> netzKomponenten = findChildrenInCategory(itVerbund,
                NKKategorie.TYPE_ID);
        convertIfNotEmpty(netzKomponenten, NetworkGroup::new, itNetwork, Optional.empty(),
                NetzKomponenteConverter::new);

        Set<CnATreeElement> personen = findChildrenInCategory(itVerbund, PersonenKategorie.TYPE_ID);

        convertIfNotEmpty(personen, BpPersonGroup::new, itNetwork, Optional.empty(),
                PersonConverter::new);

        Set<CnATreeElement> anwendungen = findChildrenInCategory(itVerbund,
                AnwendungenKategorie.TYPE_ID);

        Set<CnATreeElement> anwendungenApplication = new HashSet<>(anwendungen.size());
        Set<CnATreeElement> anwendungenBusinessProcess = new HashSet<>(anwendungen.size());

        for (CnATreeElement e : anwendungen) {
            Anwendung anwendung = (Anwendung) e;
            Collection<? extends String> tags = anwendung.getTags();
            if (tags.contains(TAG_MOGS_ANWENDUNG) && !tags.contains(TAG_MOGS_PROZESS)) {
                anwendungenApplication.add(anwendung);
            } else if (tags.contains(TAG_MOGS_PROZESS) && !tags.contains(TAG_MOGS_ANWENDUNG)) {
                anwendungenBusinessProcess.add(anwendung);
            } else {
                logger.warn("Not converting " + anwendung + " with tags " + tags
                        + ", expecting tags to contain either \"" + TAG_MOGS_ANWENDUNG + "\" or \""
                        + TAG_MOGS_PROZESS + "\".");
            }
        }

        convertIfNotEmpty(anwendungenApplication, ApplicationGroup::new, itNetwork,
                Optional.empty(), AnwendungApplicationConverter::new);

        convertIfNotEmpty(anwendungenBusinessProcess, BusinessProcessGroup::new, itNetwork,
                Optional.empty(), AnwendungBusinessProcessConverter::new);

        bpModel.addChild(itNetwork);
        itNetwork.setParent(bpModel);

        elementDao.saveOrUpdate(itNetwork);
        itNetwork.setScopeId(itNetwork.getDbId());
        setScopeIdRecursively(itNetwork, itNetwork.getScopeId());
        itNetwork = (ItNetwork) elementDao.merge(itNetwork);
        return itNetwork;
    }

    private void handleRooms(ITVerbund itVerbund, ItNetwork itNetwork) {
        CnATreeElement raeumeKategorie = itVerbund.getCategory(RaeumeKategorie.TYPE_ID);
        Set<CnATreeElement> raeume = nullSafeGetChildren(raeumeKategorie);

        CnATreeElement gebaeudeKategorie = itVerbund.getCategory(GebaeudeKategorie.TYPE_ID);
        Set<CnATreeElement> gebaeude = nullSafeGetChildren(gebaeudeKategorie);

        boolean hasRooms = !raeume.isEmpty() || !gebaeude.isEmpty();

        if (hasRooms) {
            RoomGroup roomGroup = new RoomGroup(itNetwork);
            Set<Permission> permissions = mergeReadPermissions(roomGroup, raeumeKategorie,
                    gebaeudeKategorie);
            roomGroup.setPermissions(permissions);
            itNetwork.addChild(roomGroup);

            convertIfNotEmpty(raeume, RoomGroup::new, roomGroup, Optional.of("Räume"),
                    RaumConverter::new);
            convertIfNotEmpty(gebaeude, RoomGroup::new, roomGroup, Optional.of("Gebäude"),
                    GebaeudeConverter::new);

        }
    }

    private void handleITSystems(ITVerbund itVerbund, ItNetwork itNetwork) {
        CnATreeElement clientsKategorie = itVerbund.getCategory(ClientsKategorie.TYPE_ID);
        Set<CnATreeElement> clients = nullSafeGetChildren(clientsKategorie);
        CnATreeElement serverKategorie = itVerbund.getCategory(ServerKategorie.TYPE_ID);
        Set<CnATreeElement> servers = nullSafeGetChildren(serverKategorie);
        CnATreeElement tkKomponentenKategorie = itVerbund.getCategory(TKKategorie.TYPE_ID);
        Set<CnATreeElement> tkKomponenten = nullSafeGetChildren(tkKomponentenKategorie);
        CnATreeElement sonstigeITKategorie = itVerbund.getCategory(SonstigeITKategorie.TYPE_ID);
        Set<CnATreeElement> sonstigeIt = nullSafeGetChildren(sonstigeITKategorie);

        boolean hasItSystem = !clients.isEmpty() || !servers.isEmpty() || !tkKomponenten.isEmpty()
                || !sonstigeIt.isEmpty();

        if (hasItSystem) {
            ItSystemGroup itSystemGroup = new ItSystemGroup(itNetwork);
            Set<Permission> permissions = mergeReadPermissions(itSystemGroup, clientsKategorie,
                    serverKategorie, tkKomponentenKategorie, sonstigeITKategorie);
            itSystemGroup.setPermissions(permissions);

            itNetwork.addChild(itSystemGroup);

            convertIfNotEmpty(clients, ItSystemGroup::new, itSystemGroup, Optional.of("Clients"),
                    ClientConverter::new);
            convertIfNotEmpty(servers, ItSystemGroup::new, itSystemGroup, Optional.of("Server"),
                    ServerConverter::new);
            convertIfNotEmpty(tkKomponenten, ItSystemGroup::new, itSystemGroup,
                    Optional.of("TK-Komponenten"), TelefonKomponenteConverter::new);
            convertIfNotEmpty(sonstigeIt, ItSystemGroup::new, itSystemGroup,
                    Optional.of("Netzkomponenten / sonstige"), SonstITConverter::new);
        }
    }

    private static Set<Permission> mergeReadPermissions(CnATreeElement targetElement,
            CnATreeElement... sourceElements) {
        return Stream.of(sourceElements).filter(Objects::nonNull)
                .flatMap(element -> Permission
                        .clonePermissionSet(targetElement, element.getPermissions()).stream())
                .filter(Permission::isReadAllowed).map(permission -> {
                    permission.setWriteAllowed(false);
                    return permission;
                }).collect(Collectors.toSet());
    }

    private static Set<CnATreeElement> findChildrenInCategory(ITVerbund itVerbund,
            String categoryTypeId) {
        return nullSafeGetChildren(itVerbund.getCategory(categoryTypeId));
    }

    private static Set<CnATreeElement> nullSafeGetChildren(CnATreeElement cnATreeElement) {
        return Optional.ofNullable(cnATreeElement).map(CnATreeElement::getChildren)
                .orElseGet(Collections::emptySet);
    }

    private <S extends CnATreeElement & IBSIStrukturElement, T extends CnATreeElement & IBpElement> void convertIfNotEmpty(
            Set<CnATreeElement> elements, Function<CnATreeElement, Group<T>> newGroupCreator,
            CnATreeElement newGroupParent, Optional<String> newGroupTitle,
            Supplier<ElementConverter<S, T>> converterSupplier) {
        if (!elements.isEmpty()) {
            CnATreeElement newGroup = newGroupCreator.apply(newGroupParent);
            newGroupTitle.ifPresent(newGroup::setTitel);
            newGroup.setPermissions(Permission.clonePermissionSet(newGroup,
                    elements.iterator().next().getParent().getPermissions()));
            newGroupParent.addChild(newGroup);
            ElementConverter<S, T> converter = converterSupplier.get();
            convertAndAddToContainer(elements, newGroup, converter);
        }
    }

    private static void setScopeIdRecursively(CnATreeElement element, Integer scopeId) {
        element.setScopeId(scopeId);
        element.getChildren().forEach(child -> setScopeIdRecursively(child, scopeId));
    }

    private void convertAndAddToContainer(Set<CnATreeElement> elements, CnATreeElement container,
            @SuppressWarnings("rawtypes") ElementConverter converter) {
        for (CnATreeElement element : elements) {
            @SuppressWarnings("unchecked")
            CnATreeElement converted = converter.convert(element);
            converted.setParent(container);
            converted.setPermissions(
                    Permission.clonePermissionSet(converted, element.getPermissions()));
            container.addChild(converted);
            convertedElements.put(element, converted);
        }
    }

    @SuppressWarnings("unchecked")
    private void convertAdditions() {
        for (Entry<CnATreeElement, CnATreeElement> entry : convertedElements.entrySet()) {
            CnATreeElement originalSource = entry.getKey();
            CnATreeElement newSource = entry.getValue();
            if (logger.isInfoEnabled()) {
                logger.info("Converting additions for " + originalSource);
            }
            List<Addition> additionsForSourceElement = elementDao
                    .findByCriteria(DetachedCriteria.forClass(Addition.class)
                            .add(Restrictions.eq("cnATreeElementId", originalSource.getDbId())));

            for (Addition addition : additionsForSourceElement) {
                try {
                    Addition newAddition = addition.getClass().newInstance();
                    newAddition.getEntity().copyEntity(addition.getEntity());
                    newAddition.setCnATreeElementId(newSource.getDbId());
                    if (addition instanceof Attachment) {
                        Attachment newAttachmeht = (Attachment) newAddition;
                        daoFactory.getAttachmentDao().saveOrUpdate(newAttachmeht);
                        IBaseDao<AttachmentFile, Serializable> attachmentFileDao = daoFactory
                                .getDAO(AttachmentFile.class);
                        AttachmentFile attachmentFile = attachmentFileDao
                                .findById(addition.getDbId());
                        AttachmentFileCreationFactory.createAttachmentFile(newAttachmeht,
                                attachmentFile.getFileData());

                    } else {
                        daoFactory.getDAO(addition.getTypeId()).saveOrUpdate(newAddition);
                    }
                } catch (InstantiationError | InstantiationException | IllegalAccessException
                        | IOException | CommandException e) {
                    throw new RuntimeException("Failed to copy " + addition, e);
                }
            }
        }
    }

}
