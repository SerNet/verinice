/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.HitroUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IPostProcessor;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Copies a list of elements with all children to a group. Element types in
 * BLACKLIST are ignored.
 *
 * CopyCommand uses command SaveElement to save element copies. SaveElement is a
 * IChangeLoggingCommand and logs all changes from CopyCommand
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyCommand extends GenericCommand {

    private static final long serialVersionUID = -269076325994387265L;
    private static final Logger logger = Logger.getLogger(CopyCommand.class);

    private static final int FLUSH_LEVEL = 50;

    private String uuidGroup;

    private transient CnATreeElement groupToPasteTo;

    private List<String> uuidList;

    private List<IPostProcessor> postProcessorList;

    private int number = 0;

    private transient IBaseDao<CnATreeElement, Serializable> dao;

    private List<String> newElements;

    private boolean copyAttachments = false;

    private boolean copyChildren = true;

    /**
     * @param uuidGroup
     *            Uuid of an group
     * @param uuidList
     *            Uuids of the elements to copy
     */
    public CopyCommand(final String uuidGroup, final List<String> uuidList) {
        this(uuidGroup, uuidList, Collections.emptyList());
    }

    /**
     * @param uuid
     * @param uuidList2
     * @param postProcessorList2
     */
    public CopyCommand(final String uuidGroup, final List<String> uuidList,
            final List<IPostProcessor> postProcessorList) {
        super();
        this.uuidGroup = uuidGroup;
        this.uuidList = uuidList;
        this.postProcessorList = postProcessorList;
    }

    /**
     * Copies the elements from uuidList to group with uuidGroup. Calls
     * recursive method copy to copy all children of an element.
     *
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            number = 0;
            List<CnATreeElement> allElements = getDao()
                    .findByCriteria(DetachedCriteria.forClass(CnATreeElement.class)
                            .add(Restrictions.in(CnATreeElement.UUID, uuidList)));

            List<CnATreeElement> rootElementsToCopy;
            if (allElements.size() == 1) {
                rootElementsToCopy = allElements;
            } else {
                rootElementsToCopy = filterRoots(allElements);
            }
            newElements = new ArrayList<>(rootElementsToCopy.size());
            groupToPasteTo = getDao().findByUuid(uuidGroup,
                    RetrieveInfo.getChildrenInstance().setParent(true).setProperties(true));
            boolean postProcessorsPresent = postProcessorList != null
                    && !postProcessorList.isEmpty();
            Optional<Map<String, String>> sourceDestMap = postProcessorsPresent
                    ? Optional.of(new HashMap<>())
                    : Optional.empty();
            for (final CnATreeElement copyElement : rootElementsToCopy) {
                final CnATreeElement newElement = copy(groupToPasteTo, copyElement, sourceDestMap);
                if (newElement != null && newElement.getUuid() != null) {
                    newElements.add(newElement.getUuid());
                }
            }
            if (postProcessorsPresent) {
                getDao().flush();
                getDao().clear();
                final List<String> copyElementUuidList = rootElementsToCopy.stream()
                        .map(CnATreeElement::getUuid).collect(Collectors.toList());
                for (final IPostProcessor postProcessor : postProcessorList) {
                    postProcessor.process(getCommandService(), copyElementUuidList,
                            sourceDestMap.get());
                }
            }
        } catch (final Exception e) {
            logger.error("Error while copying element", e); //$NON-NLS-1$
            throw new RuntimeCommandException("Error while copying element", e); //$NON-NLS-1$
        }
    }

    private CnATreeElement copy(final CnATreeElement groupToCopyTo,
            final CnATreeElement elementToCopy, final Optional<Map<String, String>> sourceDestMap)
            throws CommandException, IOException {
        CnATreeElement elementCopy = elementToCopy;
        if (elementToCopy != null && elementToCopy.getTypeId() != null
                && groupToCopyTo.canContain(elementToCopy)) {
            if (elementToCopy instanceof FinishedRiskAnalysis) {
                elementCopy = copyRiskAnalysis(groupToCopyTo, elementToCopy, sourceDestMap);
                afterCopy(elementToCopy, elementCopy, sourceDestMap);
            } else {
                elementCopy = saveCopy(groupToCopyTo, elementToCopy);
                number++;
                afterCopy(elementToCopy, elementCopy, sourceDestMap);
                if (copyChildren) {
                    copyChildrenIfExistant(elementToCopy, sourceDestMap, elementCopy);
                }
            }
        } else if (elementToCopy != null) {
            logger.warn("Can not copy element with pk: " + elementToCopy.getDbId() //$NON-NLS-1$
                    + " to group with pk: " + groupToPasteTo.getDbId()); //$NON-NLS-1$
        } else {
            logger.warn("Can not copy element. Element is null");
        }
        return elementCopy;
    }

    private CnATreeElement copyRiskAnalysis(CnATreeElement group,
            CnATreeElement finishedRiskAnalysis, Optional<Map<String, String>> sourceDestMap)
            throws CommandException, IOException {

        CnATreeElement copyOfFinishedRiskAnalysis = saveCopy(group, finishedRiskAnalysis);
        number++;
        copyFinishedRiskAnalysisLists((FinishedRiskAnalysis) finishedRiskAnalysis,
                (FinishedRiskAnalysis) copyOfFinishedRiskAnalysis, sourceDestMap);

        return copyOfFinishedRiskAnalysis;
    }

    private void copyFinishedRiskAnalysisLists(FinishedRiskAnalysis oldFinishedRiskAnalysis,
            FinishedRiskAnalysis copyOfFinishedRiskAnalysis,
            Optional<Map<String, String>> sourceDestMap) throws CommandException, IOException {

        FindRiskAnalysisListsByParentID command = new FindRiskAnalysisListsByParentID(
                oldFinishedRiskAnalysis.getDbId());
        command = getCommandService().executeCommand(command);
        FinishedRiskAnalysisLists listsToCopy = command.getFoundLists();

        FinishedRiskAnalysisLists newLists = new FinishedRiskAnalysisLists();
        newLists.setFinishedRiskAnalysisId(copyOfFinishedRiskAnalysis.getDbId());

        copyAssociatedGefaehrdungen(copyOfFinishedRiskAnalysis, sourceDestMap, listsToCopy,
                newLists);

        saveFinishedRiskAnalysisLists(newLists);

    }

    private void copyAssociatedGefaehrdungen(FinishedRiskAnalysis copyOfFinishedRiskAnalysis,
            Optional<Map<String, String>> sourceDestMap, FinishedRiskAnalysisLists listsToCopy,
            FinishedRiskAnalysisLists newLists) throws CommandException, IOException {

        for (GefaehrdungsUmsetzung gefaehrdung : listsToCopy.getAssociatedGefaehrdungen()) {

            GefaehrdungsUmsetzung newGefaehrdung = (GefaehrdungsUmsetzung) copy(
                    copyOfFinishedRiskAnalysis, gefaehrdung, sourceDestMap);
            newLists.getAssociatedGefaehrdungen().add(newGefaehrdung);
            addToRAWizardListsIfNeccessary(listsToCopy, newLists, gefaehrdung, newGefaehrdung);

            SaveElement<GefaehrdungsUmsetzung> saveCommand = new SaveElement<>(newGefaehrdung);
            getCommandService().executeCommand(saveCommand);
            number++;
        }
    }

    private void addToRAWizardListsIfNeccessary(FinishedRiskAnalysisLists listsToCopy,
            FinishedRiskAnalysisLists newLists, GefaehrdungsUmsetzung gefaehrdung,
            GefaehrdungsUmsetzung newGefaehrdung) {
        if (gefaehrdung.getParent() == null) {
            newGefaehrdung.setParent(null);
            return;
        }
        if (listsToCopy.getAllGefaehrdungsUmsetzungen().contains(gefaehrdung)) {
            newLists.getAllGefaehrdungsUmsetzungen().add(newGefaehrdung);
        }
        if (listsToCopy.getNotOKGefaehrdungsUmsetzungen().contains(gefaehrdung)) {
            newLists.getNotOKGefaehrdungsUmsetzungen().add(newGefaehrdung);
        }
    }

    private void saveFinishedRiskAnalysisLists(FinishedRiskAnalysisLists newLists)
            throws CommandException {
        SaveElement<FinishedRiskAnalysisLists> saveCommand = new SaveElement<>(newLists);
        getCommandService().executeCommand(saveCommand);
    }

    private void afterCopy(CnATreeElement original, CnATreeElement copy,
            Optional<Map<String, String>> sourceDestMap) {
        sourceDestMap.ifPresent(map -> map.put(original.getUuid(), copy.getUuid()));
        if (number > 0 && number % FLUSH_LEVEL == 0) {
            getDao().flush();
        }
    }

    private void copyChildrenIfExistant(CnATreeElement element,
            Optional<Map<String, String>> sourceDestMap, CnATreeElement elementCopy)
            throws CommandException, IOException {
        if (element.getChildren() != null) {
            for (CnATreeElement child : element.getChildren()) {
                copy(elementCopy, child, sourceDestMap);
            }
        }
    }

    private CnATreeElement saveCopy(CnATreeElement toGroup, CnATreeElement copyElement)
            throws CommandException, IOException {
        copyElement = getDao().initializeAndUnproxy(copyElement);
        CnATreeElement newElement = saveNew(toGroup, copyElement);
        if (newElement.getEntity() != null) {
            newElement.getEntity().copyEntity(copyElement.getEntity());
            if (copyElement.getIconPath() != null) {
                newElement.setIconPath(copyElement.getIconPath());
            }

            if (toGroup.getChildren() != null && !toGroup.getChildren().isEmpty()) {
                final String title = newElement.getTitle();
                String prospectiveTitle = title;
                if (newElement instanceof GefaehrdungsUmsetzung) {
                    prospectiveTitle = ((GefaehrdungsUmsetzung) newElement).getText();
                }
                Set<CnATreeElement> siblings = toGroup.getChildren();
                siblings.remove(newElement);
                siblings = removeDifferentTypes(siblings, newElement.getTypeId());
                newElement.setTitel(getUniqueTitle(title, prospectiveTitle, siblings, 0));
            }
        }
        SaveElement<CnATreeElement> saveCommand = new SaveElement<>(newElement);
        saveCommand = getCommandService().executeCommand(saveCommand);
        newElement = saveCommand.getElement();
        newElement.setParentAndScope(toGroup);
        if (copyAttachments) {
            LoadAttachments attachmentLoader = new LoadAttachments(copyElement.getDbId());
            copyAttachments(newElement,
                    getCommandService().executeCommand(attachmentLoader).getAttachmentList());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Copy created: " + newElement.getTitle()); //$NON-NLS-1$
        }
        newElement.setChildren(new HashSet<CnATreeElement>());
        return newElement;
    }

    private static Set<CnATreeElement> removeDifferentTypes(Set<CnATreeElement> set,
            String typeId) {
        return set.stream().filter(element -> typeId.equals(element.getTypeId()))
                .collect(Collectors.toSet());
    }

    private void copyAttachments(final CnATreeElement destinationElement,
            final Collection<Attachment> attachmentsToCopy) throws CommandException, IOException {
        IBaseDao<Attachment, Serializable> attachmentDao = getDaoFactory().getDAO(Attachment.class);

        for (final Attachment attachment : attachmentsToCopy) {
            handleSourceAttachment(destinationElement, attachmentDao, attachment);
        }
    }

    /**
     * creates a copy of a given @param attachment (including the
     * {@link AttachmentFile} and references it from the newly created
     * {@link CnATreeElement} @param destinationElement
     * 
     * @throws IOException
     */
    private void handleSourceAttachment(final CnATreeElement destinationElement,
            IBaseDao<Attachment, Serializable> dao, final Attachment attachment)
            throws CommandException, IOException {
        final Attachment newAttachmentEntity = createAttachmentCopy(destinationElement, attachment);
        dao.saveOrUpdate(newAttachmentEntity);
        LoadAttachmentFile attachmentFileLoader = new LoadAttachmentFile(attachment.getDbId());
        attachmentFileLoader = getCommandService().executeCommand(attachmentFileLoader);
        AttachmentFileCreationFactory.createAttachmentFile(newAttachmentEntity,
                attachmentFileLoader.getAttachmentFile().getFileData());
    }

    /**
     * sets (copies) the data of the to be created {@link Attachment} (excluding
     * {@link AttachmentFile} fileData)
     */
    private Attachment createAttachmentCopy(final CnATreeElement destinationElement,
            final Attachment sourceAttachment) {
        final Attachment newAttachmentEntity = new Attachment();
        newAttachmentEntity.getEntity().copyEntity(sourceAttachment.getEntity());
        newAttachmentEntity.setCnATreeElementId(destinationElement.getDbId());
        newAttachmentEntity.setCnAElementTitel(destinationElement.getTitle());
        return newAttachmentEntity;
    }

    @SuppressWarnings("unchecked")
    private CnATreeElement saveNew(final CnATreeElement container, final CnATreeElement element)
            throws CommandException {
        final String title = HitroUtil.getInstance().getTypeFactory()
                .getMessage(element.getTypeId());
        CreateElement<CnATreeElement> saveCommand = new CreateElement<>(container,
                (Class<CnATreeElement>) element.getClass(), title, true, false);
        saveCommand.setInheritAuditPermissions(true);
        saveCommand = getCommandService().executeCommand(saveCommand);
        final CnATreeElement child = saveCommand.getNewElement();
        container.addChild(child);
        child.setParentAndScope(container);
        return child;
    }

    /**
     * Returns a list with elements filtered out that are descendants of other
     * items of the list
     */
    protected List<CnATreeElement> filterRoots(final List<CnATreeElement> elements) {
        return elements.stream().filter(element -> {
            CnATreeElement currentAncestor = element.getParent();
            while (currentAncestor != null) {
                if (elements.contains(currentAncestor)) {
                    return false;
                }
                currentAncestor = currentAncestor.getParent();
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Returns a unique title compared to titles of all siblings
     * 
     * @param Title
     *            A title of an element
     * @param siblings
     *            Siblings of the element
     * @return A unique title
     */
    private String getUniqueTitle(final String title, final String copyTitle,
            final Set<CnATreeElement> siblings, final int n) {
        final String result = copyTitle;
        int nLocal = n;
        for (final CnATreeElement sibling : siblings) {
            if (sibling != null && sibling.getTitle() != null
                    && (sibling.getTitle().equals(copyTitle))) {
                nLocal++;
                return getUniqueTitle(title, getCopyTitle(title, nLocal), siblings, nLocal);
            }
        }
        return result;
    }

    private String getCopyTitle(final String title, final int n) {
        return Messages.getString("CopyCommand.0", title, n); //$NON-NLS-1$
    }

    public String getUuidGroup() {
        return uuidGroup;
    }

    public void setUuidGroup(final String uuidGroup) {
        this.uuidGroup = uuidGroup;
    }

    public List<String> getUuidList() {
        return uuidList;
    }

    public void setUuidList(final List<String> uuidList) {
        this.uuidList = uuidList;
    }

    public List<IPostProcessor> getPostProcessorList() {
        return postProcessorList;
    }

    public void addPostProcessor(final IPostProcessor task) {
        if (postProcessorList == null) {
            postProcessorList = new LinkedList<>();
        }
        postProcessorList.add(task);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if (dao == null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

    public List<String> getNewElements() {
        return newElements;
    }

    public boolean isCopyChildren() {
        return copyChildren;
    }

    public void setCopyChildren(boolean copyChildren) {
        this.copyChildren = copyChildren;
    }

    /**
     * @return the copyAttachments
     */
    public boolean isCopyAttachments() {
        return copyAttachments;
    }

    /**
     * @param copyAttachments
     *            the copyAttachments to set
     */
    public void setCopyAttachments(final boolean copyAttachments) {
        this.copyAttachments = copyAttachments;
    }

}
